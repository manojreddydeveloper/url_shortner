<!-- Author: Manoj reddy <amireddymanojreddy@gmail.com> -->
<!-- Since: 2026-09-03 -->

## Threat model and hardened inputs/outputs

### Assets
- Link mappings (destination URLs, short codes, analytics token hashes, creation timestamps)
- Click events (link ID, UTC time, traffic classification)
- Per-link 256-bit analytics bearer tokens (stored only as SHA-256 hashes)
- Operational metrics and structured logs
- Database schema and connection configuration

### Actors
- **Creation client**: Submits a destination URL, receives a short reference and one-time analytics token
- **Redirect client**: Requests a short code, receives the approved resolution outcome (redirect or error)
- **Analytics consumer**: Retrieves aggregate click metrics using a per-link bearer token
- **Engineer/operator**: Reviews and approves changes, manages deployment, observes telemetry

### Entry points (trust boundaries)
- `POST /api/v1/links` (creation) — untrusted request body, URL, and headers
- `GET /{code}` (redirect) — untrusted short-code path parameter
- `GET /api/v1/links/{code}/analytics` (analytics) — untrusted `Authorization` header
- `/health/live` and `/health/ready` (liveness/readiness) — untrusted health probes

### Data flows
- **Creation**: request → `DestinationUrlValidator` → code generation → `LinkEntity` persist → `Result` with one-time token returned over HTTPS
- **Redirect**: request → `LinkResolver.lookup()` → outcome classification → `302 Found` with `Location` + `Cache-Control: no-store` → optional `AnalyticsCapture.capture()` (best-effort, fail-open)
- **Analytics retrieval**: request → bearer token validation → `AnalyticsQueryService.query()` → aggregate counts only, no raw events

### Threats & verified controls
| Threat | Control | Status |
|---|---|---|
| Unsafe scheme (non-http/https) | `DestinationUrlValidator.isHttp()` rejects non-http schemes | Verified |
| Credential-bearing authority (`user:pass@host`) | `DestinationUrlValidator` rejects `uri.getUserInfo() != null` | Verified |
| Response-header injection (`\r\n` or control chars) | `DestinationUrlValidator.rejectUnsafeCharacters()` blocks whitespace and `%00`-`%1F`/`%7F` | Verified |
| Log-injection via destination | Same unsafe-character rejection; logger does not embed unvalidated user input | Verified |
| Short-code enumeration | Ten-character Base62 from CSPRNG (~59.5 bits); uniqueness enforced by DB constraint | Verified |
| Collision overwrite | Database `UNIQUE` constraint; application retries up to 5 candidates without overwriting existing mapping | Verified |
| SSRF via destination fetch | No server-side destination fetch in the approved baseline (explicitly out of scope) | Verified |
| Unsafe error disclosure (stack traces, DB details) | `GlobalExceptionHandler` wraps all errors in `ApiErrorResponse`; `ApiException` never exposes raw exceptions | Verified |
| Bearer-token leakage | Token returned only once at creation, over HTTPS; only its SHA-256 hash stored with mapping | Verified |
| Raw IP/personal data in analytics | Analytics events persist only link ID, UTC time, and coarse traffic class; IP, user agent, referrer prohibited | Verified |
| Rate-limit bypass via forged headers | Client identity derived from direct peer or explicitly trusted proxy; forwarding headers ignored for limit derivation | Approved per RDR-004 |
| Redirect cache serving expired data | `Cache-Control: no-store` on all redirect responses; no application or shared mapping cache in baseline | Verified |

### Residual threats (owner: engineer; disposition: accepted)
- **Bearer-token sharing**: Possession of the per-link analytics token grants analytics access; sharing the token with others inadvertently grants them access. Owner/operator must treat the token like a password. Disposition: accepted — client-side concern, not enforceable in baseline.
- **Local rate-limit reset on process restart**: In-memory token buckets reset on restart; no distributed enforcement in the single-instance baseline. Disposition: accepted — operator must plan restarts with awareness; distributed limits require a new decision.
- **Pseudonymous client identity not globally unique**: Rate-limit identity is keyed in-memory pseudonymously; different application instances may derive different identities for the same peer. Disposition: accepted — single-instance baseline; distributed deployment requires Redis or edge gateway.
- **No exactly-once analytics guarantee**: Analytics append is best-effort single attempt; events may be lost on failure without retry or buffer. Disposition: accepted — redirect availability preferred over completeness per RDR-003.

### Approved no-fetch boundary
- Per RDR-002 and SEC-012: No server-side fetch, crawl, or reachability check of destination URLs. Validation is purely structural/syntactic. This decision is final for the prototype baseline; any future preview/Scanning feature requires a separate ADR with SSRF defenses.

### Summary of verified SEC controls
- **SEC-001**: Destination validation — structurally validate and exactly preserve bounded HTTP/HTTPS destinations without fetching them.
- **SEC-002**: Injection prevention — reject unsafe characters, control characters, and percent-encoded control values in destinations and short codes.
- **SEC-003**: Short-code input constraints — constrain lookup to ten-character case-sensitive Base62 via the compiled regex before datastore access.
- **SEC-004**: Collision and enumeration controls — atomic uniqueness via database constraint; bounded collision retries; codes designed to be unpredictable.
- **SEC-008**: Transport security — TLS termination and trusted-proxy configuration required for non-local deployments; `UrlShortenerProperties` validates scheme, no credentials, no query/fragment.
- **SEC-010**: Safe error disclosure — stable JSON error envelope; no database details, stack traces, secrets, or private-resource information in client responses.
- **SEC-012**: Conditional server-side fetching — no destination fetch in the approved baseline; SSRF defenses would require a separately approved ADR.
