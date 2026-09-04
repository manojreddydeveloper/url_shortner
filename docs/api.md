# URL Shortener API Design

<!-- Author: Manoj reddy <amireddymanojreddy@gmail.com> -->
<!-- Since: 2026-09-03 -->

## Document status

- **Status:** APPROVED
- **Scope:** API contract baseline and implementation constraints
- **Implementation authorized:** Yes, within the approved baseline
- **Related requirements:** FR-001 through FR-011, SEC-001 through SEC-010, REL-001 through REL-012

This contract reflects the approved ARC-001 architecture baseline and the approved RDR-001 through RDR-004 requirements. The wire-level choices in this document are approved at the contract level; remaining implementation details are recorded as documented open choices where applicable.

## Conventions

### Base URLs

- Management API: `{PUBLIC_BASE_URL}/api/v1`
- Redirect: `{PUBLIC_BASE_URL}/{code}`
- Health: `{PUBLIC_BASE_URL}/health/...`

`PUBLIC_BASE_URL` is trusted configuration. It is not constructed from an untrusted `Host` or forwarding header.

### Media type

Management API requests and responses use `application/json`. Redirect responses contain no JSON body requirement.

### Time

- Timestamps use ISO 8601 UTC with a `Z` suffix.
- Baseline links do not expire; expiration fields and expired responses are not part of this contract.
- Analytics uses UTC daily buckets, an inclusive `from`, exclusive `to`, a default 30-day range, and a maximum 90-day range.

### Identifiers

- Short code: exactly ten case-sensitive Base62 characters matching `[0-9A-Za-z]{10}`.
- Codes must not be decoded into internal database identifiers.
- Request correlation IDs are returned through `X-Request-ID` and in safe API error envelopes.

### Common error envelope

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The request contains invalid fields.",
    "requestId": "01JEXAMPLE0000000000000000",
    "details": [
      {
        "field": "url",
        "reason": "INVALID_URL"
      }
    ]
  }
}
```

Rules:

- `error.code` is stable and intended for client logic.
- `message` and `details` contain no destination value, secret, database information, stack trace, or dependency address.
- `details` is optional and limited to safe validation information.
- Unexpected errors use a generic client message and retain details only in privacy-safe operational telemetry.

## 1. Create a short link

### Request

`POST /api/v1/links`

```json
{
  "url": "https://example.com/articles/architecture?source=demo"
}
```

| Field | Required | Proposed rules |
| --- | --- | --- |
| `url` | Yes | Absolute `http` or `https`; non-empty approved host form; no credentials, whitespace, malformed escapes, or control characters; maximum 4,096 characters; preserved exactly after structural validation |

Unknown request fields are rejected with `400 VALIDATION_ERROR` so clients cannot assume unsupported lifecycle or management behavior.

### Success

Proposed status: `201 Created`

```json
{
  "code": "aZ3kP9mQ2x",
  "shortUrl": "https://sho.rt/aZ3kP9mQ2x",
  "url": "https://example.com/articles/architecture?source=demo",
  "createdAt": "2026-09-02T15:30:00Z",
  "analyticsToken": "generated-base64url-bearer-token"
}
```

- `analyticsToken` is returned once under the approved per-link token model.
- The plaintext token must not be stored, logged, placed in a URL, or returned by another endpoint.
- `Location` may identify a future link-resource endpoint only if such an endpoint is approved; it is not defined by this design.

### Duplicate and retry semantics

- Proposed ordinary duplicate behavior: each request creates a new code even if `url` matches an existing link.
- Proposed baseline: the operation is non-idempotent and does not accept an idempotency key.
- Clients must not automatically retry an ambiguous failed response until an idempotency policy is approved.
- If idempotency is added, it requires a separate contract defining key scope, identity, retention, conflicts, replay response, and privacy.

### Errors

| Status | Error code | Condition |
| --- | --- | --- |
| `400` | `INVALID_JSON` | Body is not valid JSON |
| `400` | `VALIDATION_ERROR` | URL, unsupported field, or baseline-prohibited expiration field violates the approved policy |
| `413` | `PAYLOAD_TOO_LARGE` | Request exceeds the approved limit |
| `415` | `UNSUPPORTED_MEDIA_TYPE` | Content type is not supported |
| `429` | `RATE_LIMITED` | Approved creation limit is exceeded |
| `500` | `INTERNAL_ERROR` | An internal creation failure occurs, including exhausted code attempts; the specific cause is not disclosed to the client |
| `503` | `DEPENDENCY_UNAVAILABLE` | Mapping cannot meet its durability requirement |

The service must not return a success response until the mapping is durably committed according to the approved guarantee.

## 2. Redirect a short link

### Request

`GET /{code}`

The route accepts only the approved fixed-length code format. Static `/api` and `/health` routes take precedence.

### Success

Proposed status: `302 Found`

```http
HTTP/1.1 302 Found
Location: https://example.com/articles/architecture?source=demo
Cache-Control: no-store
X-Request-ID: 01JEXAMPLE0000000000000000
```

`Cache-Control: no-store` is required by RDR-002 so redirects remain under service control and do not bypass analytics or future lifecycle controls.

### Redirect outcomes

| Status | Outcome | Meaning |
| --- | --- | --- |
| `302` | `ACTIVE` | The active mapping resolved; analytics is attempted under the fail-open policy |
| `404` | `NOT_FOUND` | The code format is valid but no mapping exists |
| `429` | `RATE_LIMITED` | Not emitted by the application baseline; coarse edge protection remains possible |
| `503` | `DEPENDENCY_UNAVAILABLE` | The authoritative mapping result cannot be determined safely |

Malformed and unknown codes both return `404 Not Found` without `Location` to avoid unnecessary public distinction.

### Method handling

- `GET` is supported.
- `HEAD` is unsupported and returns the framework's `405 Method Not Allowed`; it creates no analytics event.
- Other methods are rejected using the framework's approved `405 Method Not Allowed` behavior.
- The service does not forward the original request method or body to the destination.

### Analytics behavior

- Proposed click boundary: an active mapping reaches analytics event capture immediately before the redirect response.
- Analytics insertion failure does not change the redirect response.
- The response does not prove that the browser reached the destination.
- Browser prefetch, retries, refreshes, and suspected bots follow the approved analytics event-decision matrix; each eligible GET is counted independently.

## 3. Retrieve link analytics

### Request

`GET /api/v1/links/{code}/analytics?from={timestamp}&to={timestamp}&bucket={day}`

Proposed authorization:

```http
Authorization: Bearer generated-base64url-bearer-token
```

| Parameter | Required | Proposed rules |
| --- | --- | --- |
| `code` | Yes | Approved fixed-length case-sensitive code |
| `from` | No | Inclusive UTC start; defaults to 30 days before query time |
| `to` | No | Exclusive UTC end; defaults to query time |
| `bucket` | No | Must be `day`; daily UTC buckets are returned with totals |

The range must be non-negative and no longer than 90 days. Invalid timestamps, ranges, or bucket values return `400 VALIDATION_ERROR`.

### Success

Proposed status: `200 OK`

```json
{
  "code": "aZ3kP9mQ2x",
  "from": "2026-09-01T00:00:00Z",
  "to": "2026-09-03T00:00:00Z",
  "bucket": "day",
  "totals": {
    "all": 42,
    "suspectedAutomated": 7,
    "unclassified": 35
  },
  "buckets": [
    {
      "start": "2026-09-01T00:00:00Z",
      "all": 18,
      "suspectedAutomated": 2,
      "unclassified": 16
    }
  ],
  "asOf": "2026-09-02T15:30:00Z"
}
```

Semantics:

- `all` includes every stored event in the range.
- `suspectedAutomated` is heuristic and does not establish automated identity with certainty.
- `unclassified` must not be labeled “human” or “unique visitor.”
- `buckets` contains event-bearing UTC days only; an authorized range with no events returns zero totals and an empty `buckets` array.
- The API does not expose raw click events, IP addresses, user agents, referrers, or destinations.
- Counts are best-effort under the approved analytics failure policy and are not exactly once.

### Errors

| Status | Error code | Condition |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` | Invalid time range, bucket, or code syntax under the approved policy |
| `401` | `AUTHENTICATION_REQUIRED` | Analytics token is missing |
| `404` | `NOT_FOUND` | Mapping does not exist or the bearer token is invalid for the link |
| `429` | `RATE_LIMITED` | Analytics query limit is exceeded |
| `503` | `DEPENDENCY_UNAVAILABLE` | Analytics cannot be queried safely |

Token comparison uses the stored hash and a constant-time comparison. Tokens never appear in query parameters.

## 4. Health endpoints

### Liveness

`GET /health/live`

- Success: `200 OK` when the process can execute its event loop or request handler.
- Does not fail solely because PostgreSQL or analytics is unavailable.
- Returns no configuration, dependency address, version inventory, or stack detail.

### Readiness

`GET /health/ready`

- Success: `200 OK` when the instance can safely serve approved traffic.
- Failure: `503 Service Unavailable` when PostgreSQL is unavailable.
- PostgreSQL is required in the no-cache baseline.
- Analytics append failure does not make readiness false because redirects fail open for analytics.

Response bodies are intentionally minimal and must not expose sensitive operational detail.

## 5. Rate-limit contract

Proposed rate-limit response:

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 30
Content-Type: application/json
```

The response uses the common error envelope with `RATE_LIMITED` and a bounded `Retry-After` value. Creation uses capacity 20/refill 10 per minute per derived client identity; analytics retrieval uses capacity 60/refill 60 per minute per bearer token. Rate-limit state remains per application instance in memory and resets on restart; the current version-2 runtime does not yet distribute rate-limit state across replicas.

Proposed scope:

- Creation: limited.
- Analytics retrieval: limited.
- Redirect: no application quota in the current runtime; coarse edge protection remains possible at the shared edge proxy.

## 6. Caching contract

- Version-2 runtime: application responses and resolution use a shared Redis cache for hot mappings, but Redis remains transparent to the API contract.
- Redirect responses use `Cache-Control: no-store`.
- Redis mapping cache cannot change active, unknown, expired, or dependency-failed semantics.
- Unknown-code negative caching is not part of the baseline.
- Database failure must not be returned as `404` because a cache lookup missed.

## 7. API security requirements

- Accept requests only through approved TLS boundaries outside local development.
- Trust forwarding headers only from configured edge addresses.
- Enforce body, URL, code, time-range, and header-size limits.
- Never fetch a destination as part of creation or redirect.
- Never place analytics credentials in URLs.
- Never log raw destinations, tokens, IP addresses, user agents, or referrers by default.
- Use parameterized persistence operations.
- Apply approved authentication and authorization before analytics data access.
- Return safe errors without stack, SQL, dependency, or private-resource details.

## 8. Contract-test matrix

The following cases are the contract-test baseline. They are documentation evidence for ARC-002; executable tests belong to later implementation tasks.

| Case | Request | Expected result | Coverage |
| --- | --- | --- | --- |
| Create valid URL | `POST /api/v1/links` with an accepted 4,096-character-or-shorter URL | `201`, exact URL preserved, ten-character code, one-time analytics token | FR-001/FR-002, AC-001/AC-003 |
| Reject invalid URL | Unsupported scheme, credentials, control/whitespace, malformed escape, missing host, or length 4,097 | `400 VALIDATION_ERROR`, no mapping | FR-001/FR-009, SEC-001/SEC-002, AC-002 |
| Reject prohibited field | Creation body contains `expiresAt` or another unknown field | `400 VALIDATION_ERROR` | FR-006, AC-007 |
| Create duplicate destination | Two ordinary requests contain the same accepted URL | Two independent `201` responses and distinct persisted codes | FR-010, AC-005 |
| Redirect active code | `GET /{code}` for a known mapping | `302`, exact `Location`, `Cache-Control: no-store`, analytics attempted | FR-004, AC-006/AC-010 |
| Redirect malformed/unknown | Invalid code shape or valid-shape code without mapping | `404`, no `Location`, no analytics event | FR-005, AC-007 |
| Redirect datastore failure | Known-shape code while lookup cannot complete | `503 DEPENDENCY_UNAVAILABLE`, no false `404` | REL-003, AC-009 |
| Redirect unsupported method | `HEAD` or another method on `/{code}` | `405`, no redirect, no analytics event | FR-004/FR-009, AC-007 |
| Analytics authorized | Valid bearer token and range at or below 90 days | `200`, totals and daily UTC buckets with `asOf` | FR-008, SEC-009, AC-013 |
| Analytics unauthorized | Missing token, invalid token, or unknown code | `401` only for missing token; otherwise `404 NOT_FOUND` | SEC-009, AC-013 |
| Analytics invalid range | `to < from`, range over 90 days, invalid timestamp, or bucket | `400 VALIDATION_ERROR` | FR-008, AC-013 |
| Rate limit exceeded | Creation or analytics request above approved token bucket | `429 RATE_LIMITED` with bounded `Retry-After` | FR-009, SEC-005, AC-014 |
| Safe dependency error | Any dependency/internal failure | Stable envelope without secrets, SQL, stack, URLs, tokens, or addresses | FR-011, SEC-010, AC-015/AC-016 |

## 9. Open contract decisions

The following wire-level details remain open for engineer review; they do not change the approved requirements:

1. Exact JSON property naming and whether `details` is omitted or returned as an empty list.
2. Whether creation includes an HTTP `Location` header for a future link-resource endpoint; no such endpoint is part of the baseline.
3. The exact `405 Method Not Allowed` response envelope and `Allow` header.
4. The exact `Retry-After` value calculation and whether informational rate-limit headers are exposed.
5. Whether the framework's default content negotiation errors are normalized into the common envelope.
6. Environment-specific public base URL and trusted-proxy allowlist values.

## Approval gate

This document is the approved contract baseline. It must be reviewed alongside `docs/architecture.md` and the ARC-002 record in `DECISIONS.md`. Any later wire-level changes must be recorded before creation, redirect, or analytics implementation tasks change.
