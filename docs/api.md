# URL Shortener API Design

## Document status

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Scope:** API contract design only
- **Implementation authorized:** No
- **Related requirements:** FR-001 through FR-011, SEC-001 through SEC-010, REL-001 through REL-012

This contract reflects the simplest architecture proposed in `docs/architecture.md`. Values marked `TBD` and all proposed behavior require engineer approval before implementation.

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
- Expiration boundary inclusivity is pending engineer approval.
- Analytics time buckets and range bounds must be documented before implementation.

### Identifiers

- Proposed short code: exactly ten case-sensitive Base62 characters matching `[0-9A-Za-z]{10}`.
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
  "url": "https://example.com/articles/architecture?source=demo",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

| Field | Required | Proposed rules |
| --- | --- | --- |
| `url` | Yes | Absolute `http` or `https`; non-empty host; no credentials or control characters; maximum length `TBD`; preserved after approved structural validation |
| `expiresAt` | No | UTC timestamp later than creation and within an approved maximum; omit for no expiration if that policy is approved |

Unknown request fields are proposed to be rejected to catch client mistakes. That behavior remains pending approval.

### Success

Proposed status: `201 Created`

```json
{
  "code": "aZ3kP9mQ2x",
  "shortUrl": "https://sho.rt/aZ3kP9mQ2x",
  "url": "https://example.com/articles/architecture?source=demo",
  "createdAt": "2026-09-02T15:30:00Z",
  "expiresAt": "2026-12-31T23:59:59Z",
  "analyticsToken": "generated-base64url-bearer-token"
}
```

- `analyticsToken` is returned once if the proposed per-link token model is approved.
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
| `400` | `VALIDATION_ERROR` | URL or expiration violates the approved policy |
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

`Cache-Control: no-store` is the conservative proposed baseline because client caching could bypass analytics and expiration. If a different caching policy is approved, it must state its effect on analytics and lifecycle guarantees.

### Redirect outcomes

| Status | Outcome | Meaning |
| --- | --- | --- |
| `302` | `ACTIVE` | The active mapping resolved; analytics is attempted under the fail-open policy |
| `404` | `NOT_FOUND` | The code format is valid but no mapping exists |
| `410` | `EXPIRED` | The mapping exists but is expired; conditional on expiration approval |
| `429` | `RATE_LIMITED` | Optional redirect limit is exceeded if such a limit is approved |
| `503` | `DEPENDENCY_UNAVAILABLE` | The authoritative mapping result cannot be determined safely |

Malformed code handling may return `404` rather than a detailed `400` to reduce unnecessary distinction on the public redirect surface. The exact choice remains pending engineer approval.

### Method handling

- `GET` is supported.
- `HEAD` support is pending because preview clients and bots may affect click definition.
- Other methods are rejected using the framework's approved `405 Method Not Allowed` behavior.
- The service does not forward the original request method or body to the destination.

### Analytics behavior

- Proposed click boundary: an active mapping reaches analytics event capture immediately before the redirect response.
- Analytics insertion failure does not change the redirect response.
- The response does not prove that the browser reached the destination.
- The treatment of `HEAD`, browser prefetch, retries, and suspected bots remains subject to the approved analytics policy.

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
| `from` | No | Inclusive UTC start; approved default window `TBD` |
| `to` | No | Exclusive UTC end; defaults to request time if approved |
| `bucket` | No | Baseline supports `day`; total-only response remains an alternative |

The allowed range and maximum query window are `TBD` and must be bounded to prevent expensive requests.

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
    "suspectedBot": 7,
    "unknown": 35
  },
  "buckets": [
    {
      "start": "2026-09-01T00:00:00Z",
      "all": 18,
      "suspectedBot": 2,
      "unknown": 16
    }
  ],
  "asOf": "2026-09-02T15:30:00Z"
}
```

Semantics:

- `all` includes every stored event in the range.
- `suspectedBot` is heuristic and does not establish automated identity with certainty.
- `unknown` must not be labeled “human” or “unique visitor.”
- The API does not expose raw click events, IP addresses, user agents, referrers, or destinations.
- Counts are best-effort under the proposed analytics failure policy and are not exactly once.

### Errors

| Status | Error code | Condition |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` | Invalid time range, bucket, or code syntax under the approved policy |
| `401` | `AUTHENTICATION_REQUIRED` | Analytics token is missing |
| `403` | `ACCESS_DENIED` | Token is invalid for the link; use of `404` to reduce existence disclosure remains an alternative |
| `404` | `NOT_FOUND` | Mapping does not exist, subject to approved anti-enumeration behavior |
| `429` | `RATE_LIMITED` | Analytics query limit is exceeded |
| `503` | `DEPENDENCY_UNAVAILABLE` | Analytics cannot be queried safely |

Token comparison uses the stored hash and an approved constant-time comparison. Tokens never appear in query parameters.

## 4. Health endpoints

### Liveness

`GET /health/live`

- Proposed success: `200 OK` when the process can execute its event loop or request handler.
- Does not fail solely because PostgreSQL, Redis, or analytics is unavailable.
- Returns no configuration, dependency address, version inventory, or stack detail.

### Readiness

`GET /health/ready`

- Proposed success: `200 OK` when the instance can safely serve approved traffic.
- Proposed failure: `503 Service Unavailable` when required dependencies are unavailable.
- PostgreSQL is required in the no-cache baseline.
- Redis is not a required dependency unless an approved design makes its function mandatory.
- The exact analytics readiness effect follows the approved fail-open policy.

Response bodies are intentionally minimal and must not expose sensitive operational detail.

## 5. Rate-limit contract

Proposed rate-limit response:

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 30
Content-Type: application/json
```

The response uses the common error envelope with `RATE_LIMITED`. Numeric quotas, windows, burst values, client identity, and whether informational limit headers are exposed remain `TBD`.

Proposed scope:

- Creation: limited.
- Analytics retrieval: limited.
- Redirect: coarse edge protection; application limit only if an approved abuse or capacity need exists.

## 6. Caching contract

- Baseline: application responses and resolution do not depend on Redis.
- Redirect responses use conservative client cache control until analytics and expiration effects are approved.
- If Redis mapping cache is added, it is transparent to the API and cannot change active, unknown, expired, or dependency-failed semantics.
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

## 8. Open contract decisions

The API cannot be considered approved until the engineer decides:

1. Maximum request and URL lengths.
2. Whether optional expiration is supported and its bounds and exact boundary.
3. Whether `HEAD` redirects count, redirect, or return another outcome.
4. Final malformed-code and invalid-analytics-token disclosure behavior.
5. Creation and analytics quotas and client identity.
6. Whether per-link bearer tokens are the approved analytics authorization model.
7. Analytics default and maximum time range, bucket support, freshness, loss, and retention.
8. Whether `Cache-Control: no-store` is required or a bounded redirect cache is acceptable.
9. Whether creation requires explicit idempotency.
10. Exact public base URL and trusted proxy behavior per environment.

## Approval gate

This document is a proposed contract. It must be reviewed alongside `docs/architecture.md` and the proposed records in `DECISIONS.md`. Approval of individual decisions must be recorded before related implementation tasks become ready.
