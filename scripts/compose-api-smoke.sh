#!/usr/bin/env sh
set -eu

compose() {
  docker compose "$@"
}

edge_request() {
  compose exec -T edge sh -lc "$1"
}

extract_field() {
  key=$1
  printf '%s\n' "$2" | sed -n "s/.*\"$key\":\"\\([^\"]*\\)\".*/\\1/p" | head -n 1
}

assert_http_status() {
  expected=$1
  label=$2
  payload=$3
  printf '%s\n' "$payload" | grep -q "^  HTTP/1.1 $expected " || {
    printf '%s\n' "$label did not return HTTP $expected" >&2
    printf '%s\n' "$payload" >&2
    exit 1
  }
}

assert_contains() {
  needle=$1
  label=$2
  payload=$3
  printf '%s\n' "$payload" | grep -q "$needle" || {
    printf '%s\n' "$label did not contain $needle" >&2
    printf '%s\n' "$payload" >&2
    exit 1
  }
}

echo "Checking Redis readiness..."
compose exec -T redis redis-cli ping | grep -qx PONG

echo "Checking edge proxy readiness..."
edge_request 'wget -qO- http://127.0.0.1:8080/health/ready' | grep -q '"status":"UP"'

echo "Creating a short link through the edge proxy..."
create_resp=$(
  edge_request 'wget -S -qO- --header="Content-Type: application/json" --post-data="{\"url\":\"https://example.com/live-test\"}" http://127.0.0.1:8080/api/v1/links 2>&1'
)
assert_http_status 201 "POST /api/v1/links" "$create_resp"

code=$(extract_field code "$create_resp")
token=$(extract_field analyticsToken "$create_resp")
short_url=$(extract_field shortUrl "$create_resp")

[ -n "$code" ] || {
  printf '%s\n' "Creation response did not include code" >&2
  printf '%s\n' "$create_resp" >&2
  exit 1
}
[ -n "$token" ] || {
  printf '%s\n' "Creation response did not include analyticsToken" >&2
  printf '%s\n' "$create_resp" >&2
  exit 1
}
[ -n "$short_url" ] || {
  printf '%s\n' "Creation response did not include shortUrl" >&2
  printf '%s\n' "$create_resp" >&2
  exit 1
}

echo "Created code: $code"
echo "Verifying redirect..."
redirect_resp=$(edge_request "wget -S -qO- http://127.0.0.1:8080/$code 2>&1" || true)
assert_http_status 302 "GET /$code" "$redirect_resp"
assert_contains "Location: https://example.com/live-test" "GET /$code" "$redirect_resp"

sleep 2

echo "Reading analytics..."
analytics_resp=""
analytics_ok=0

attempt=1
while [ "$attempt" -le 10 ]; do
  analytics_resp=$(
    edge_request "wget -S -qO- --header=\"Authorization: Bearer $token\" \"http://127.0.0.1:8080/api/v1/links/$code/analytics?bucket=day\" 2>&1"
  )
  assert_http_status 200 "GET /api/v1/links/$code/analytics" "$analytics_resp"
  body=$(printf '%s\n' "$analytics_resp" | sed -n '/^{/,$p')
  if printf '%s\n' "$body" | grep -q '"totals":{"all":1'; then
    analytics_ok=1
    break
  fi
  attempt=$((attempt + 1))
  sleep 1
done

[ "$analytics_ok" -eq 1 ] || {
  printf '%s\n' "Analytics did not reflect the redirect within the polling window" >&2
  printf '%s\n' "$analytics_resp" >&2
  exit 1
}

printf '%s\n' "$body" | grep -q '"bucket":"day"'
printf '%s\n' "$body" | grep -q '"buckets":\['

echo "API smoke test passed."
