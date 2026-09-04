#!/usr/bin/env sh
set -eu

compose() {
  docker compose "$@"
}

echo "Checking Redis readiness..."
compose exec -T redis redis-cli ping | grep -qx PONG

echo "Checking edge proxy readiness through the compose network..."
compose exec -T edge sh -lc 'wget -qO- http://127.0.0.1:8080/health/ready' | grep -q '"status":"UP"'

echo "Smoke test passed."
