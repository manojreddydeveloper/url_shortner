# Engineering Plan

<!-- Author: Manoj reddy <amireddymanojreddy@gmail.com> -->
<!-- Since: 2026-09-03 -->

This document converts the approved requirements analysis into a normalized engineering plan. It does not approve unresolved product behavior, select an architecture, authorize dependencies, or authorize implementation. Any item marked `PENDING ENGINEER APPROVAL` remains undecided.

## 1. Problem Statement

Build a greenfield, production-oriented URL shortener prototype with core APIs, analytics, and reliability features.

The service must provide the essential behavior of accepting a destination URL, creating a short reference, and redirecting requests for that reference to the stored destination. It must also provide analytics and demonstrate defined behavior for invalid input, concurrency, dependency failures, abuse, and other expected failure conditions.

The project is also an AI-assisted software engineering assessment. In addition to a working service, it must demonstrate requirements understanding, normalization, task decomposition, codebase reasoning, implementation, test generation, debugging, refactoring, documentation, code review, validation, risk management, human oversight, and traceability of AI-generated, edited, accepted, and rejected output.

The assignment intentionally leaves important behaviors unspecified. Those behaviors are documented in Section 9 and must not be silently resolved during design or implementation.

## 2. Goals

- Deliver the smallest production-oriented prototype that satisfies the approved URL-shortening, redirect, analytics, and reliability requirements.
- Define stable and testable behavior for successful requests, invalid input, missing links, dependency failures, and concurrent requests.
- Protect the service and collected data using an engineer-approved security and privacy policy.
- Establish measurable performance, scale, and reliability targets before those qualities are used to select an architecture.
- Create automated tests and reproducible validation evidence for every approved requirement.
- Keep architecture, dependency, security, schema, and deployment decisions under explicit engineer control.
- Maintain traceability from requirements and decisions through tasks, implementation, tests, review, and final disposition.
- Demonstrate AI assistance without transferring engineering accountability to the AI.

## 3. Functional Requirements

The following requirements normalize the assignment's request for core APIs, analytics, and reliability features. Where behavior depends on an ambiguity in Section 9, the final details remain pending engineer approval.

- **FR-001 — Create a shortened URL:** The service shall accept a destination URL that satisfies the approved validation policy and create a short-code mapping.
- **FR-002 — Return a usable short reference:** A successful creation response shall return a short URL, or the approved components needed to construct one, using the documented response contract.
- **FR-003 — Preserve mapping uniqueness:** A short code shall identify no more than one destination mapping at a time, and creation shall not silently overwrite an existing mapping.
- **FR-004 — Resolve a short code:** The service shall accept a short-code request, resolve an active mapping, and issue a redirect using the approved redirect semantics.
- **FR-005 — Handle unsuccessful resolution:** Malformed, unknown, inactive, and expired short codes shall produce the approved deterministic outcomes without issuing an incorrect redirect. Disabled and expired states apply only if those lifecycle capabilities are approved.
- **FR-006 — Apply lifecycle rules:** Creation and resolution shall apply the approved expiration, mutability, disabling, and deletion rules. Any capability not approved is out of scope.
- **FR-007 — Record analytics:** The service shall record analytics according to the approved click definition, bot policy, privacy policy, and failure policy.
- **FR-008 — Provide analytics access:** The service shall provide the approved API or reporting mechanism for retrieving the required analytics.
- **FR-009 — Enforce request policies:** Relevant operations shall enforce the approved validation, request-size, authentication, authorization, and rate-limit policies.
- **FR-010 — Handle concurrent operations:** Concurrent creation, resolution, and analytics operations shall preserve the approved uniqueness, consistency, and idempotency behavior.
- **FR-011 — Return documented errors:** APIs shall return stable, machine-readable error responses for approved client, rate-limit, dependency, and internal failure classes.
- **FR-012 — Preserve AI-work traceability:** Each AI-assisted implementation change shall be traceable to its requirement, prompt or instruction, affected artifacts, engineer edits, validation evidence, review result, and acceptance or rejection.

### Functional scope disposition

On 2026-09-02, the engineer approved FR-001 through FR-012 as written for REQ-001. This approval establishes capability scope only. Terms that depend on an “approved” policy remain unresolved until their corresponding ambiguity and downstream requirement tasks are explicitly decided.

The approved capability scope involves these logical actors:

- **Creation client:** Submits a destination and receives the approved short reference.
- **Redirect client:** Requests a short code and receives the approved resolution outcome.
- **Analytics consumer:** Retrieves approved analytics through an access model that remains pending REQ-003.
- **Engineer/operator:** Reviews AI-assisted changes, validates evidence, and controls approvals; operational interfaces remain subject to later requirements.

| Requirement | Disposition | Rationale |
| --- | --- | --- |
| FR-001 | APPROVED AS WRITTEN | URL creation is a core assignment capability while its validation policy remains separately decidable. |
| FR-002 | APPROVED AS WRITTEN | A successful creation must produce a usable reference without prematurely fixing the response representation. |
| FR-003 | APPROVED AS WRITTEN | Mapping uniqueness is required for correct and safe resolution. |
| FR-004 | APPROVED AS WRITTEN | Redirect resolution is a core assignment capability while redirect semantics remain separately decidable. |
| FR-005 | APPROVED AS WRITTEN | Unsuccessful lookups require deterministic behavior without assuming optional lifecycle features. |
| FR-006 | APPROVED AS WRITTEN | Lifecycle behavior must follow later policy decisions and unapproved lifecycle capabilities remain excluded. |
| FR-007 | APPROVED AS WRITTEN | Analytics is required by the assignment, subject to later click, bot, privacy, and failure decisions. |
| FR-008 | APPROVED AS WRITTEN | Required analytics must have a retrieval mechanism whose exact contract remains separately decidable. |
| FR-009 | APPROVED AS WRITTEN | Input, access, size, and abuse controls must follow explicit policies rather than implicit defaults. |
| FR-010 | APPROVED AS WRITTEN | Concurrency must preserve the uniqueness and consistency semantics selected by later tasks. |
| FR-011 | APPROVED AS WRITTEN | Stable error behavior is required without prematurely selecting every failure mapping. |
| FR-012 | APPROVED AS WRITTEN | Traceability is a required assessment capability and preserves human oversight of AI-assisted work. |

## 4. Non-Functional Requirements

- **NFR-001 — Documentation:** Approved API, configuration, operational, failure, security, privacy, and validation behavior shall be documented.
- **NFR-002 — Testability:** URL validation, short-code generation, persistence, resolution, expiration, analytics, and failure policies shall be independently testable at appropriate boundaries.
- **NFR-003 — Configuration:** Environment-specific configuration and secrets shall not require source-code changes.
- **NFR-004 — Error stability:** Client-facing errors shall use documented schemas and shall not expose internal implementation or dependency details.
- **NFR-005 — Deterministic retry behavior:** State-changing requests shall have an approved and documented idempotency or retry policy.
- **NFR-006 — Maintainability:** Components shall have clear responsibilities and avoid abstractions or infrastructure not justified by approved requirements.
- **NFR-007 — Reproducibility:** Setup, build, test, and validation procedures shall run from documented commands in a clean approved environment.
- **NFR-008 — Data lifecycle:** Retention and deletion behavior for mappings, analytics, logs, and backups shall be documented.
- **NFR-009 — Human governance:** High-impact architecture, dependency, schema, security, privacy, and deployment changes shall require explicit engineer approval.
- **NFR-010 — AI traceability:** AI-generated, AI-edited, engineer-edited, accepted, and rejected output shall remain distinguishable in project records.
- **NFR-011 — Change isolation:** Implementation work shall be decomposed into small, independently reviewable tasks with explicit acceptance criteria.
- **NFR-012 — Compatibility:** API and stored-data compatibility expectations shall be defined before a change that can affect existing clients or records is approved.

## 5. Security Requirements

- **SEC-001 — Destination validation:** Destination URLs shall be validated against an explicitly approved scheme, structure, encoding, credential, host, and length policy.
- **SEC-002 — Injection prevention:** Stored and supplied destinations and short codes shall not permit response-header injection, log injection, command injection, or other unsafe interpretation.
- **SEC-003 — Short-code input controls:** Short-code input shall be constrained to the approved character set and maximum length before lookup.
- **SEC-004 — Collision and enumeration controls:** Code uniqueness shall be enforced atomically, and the approved generation design shall address practical collision and enumeration risk.
- **SEC-005 — Request and abuse controls:** Request-size limits and approved rate limits shall protect selected operations from resource exhaustion and automated abuse.
- **SEC-006 — Sensitive-data minimization:** The service shall not collect, persist, return, log, or emit sensitive or personal data beyond the approved operational and analytics need.
- **SEC-007 — Secret management:** Credentials and secrets shall not be stored in source control, committed configuration, client responses, or logs.
- **SEC-008 — Transport security:** Non-local deployments shall use an approved TLS termination and trusted-proxy configuration.
- **SEC-009 — Access control:** Analytics and management operations shall enforce the approved authentication and authorization policy.
- **SEC-010 — Safe error disclosure:** Client responses shall not disclose database details, stack traces, secrets, or private-resource information.
- **SEC-011 — Abuse response:** The service's phishing, spam, malware-link, and takedown limitations and controls shall be documented.
- **SEC-012 — Conditional server-side fetching:** If destination fetching is later approved for previews, validation, or scanning, server-side request forgery defenses shall be separately designed and approved before implementation.
- **SEC-013 — Dependency review:** Added dependencies shall be engineer-approved and evaluated using the project's approved security-validation process.

## 6. Reliability Requirements

- **REL-001 — Durable creation:** A successful creation response shall not be returned until the mapping satisfies the approved durability guarantee.
- **REL-002 — Collision safety:** A short-code collision shall never overwrite or corrupt an existing mapping.
- **REL-003 — Failure classification:** Dependency failure shall remain distinguishable from a valid not-found result and shall produce the approved client outcome.
- **REL-004 — Bounded dependency calls:** Database, cache, analytics, and other dependency operations shall use approved bounded timeout and retry policies.
- **REL-005 — Safe retries:** Retries shall be limited to operations for which repetition is safe under the approved idempotency and duplication policies.
- **REL-006 — Analytics degradation:** Analytics failure shall follow the approved redirect-availability, buffering, retry, duplication, and loss policy.
- **REL-007 — Cache correctness:** If caching is approved, cached data shall not keep an expired or inactive mapping active beyond its authoritative lifecycle.
- **REL-008 — Cache degradation:** If caching is approved, cache failure and stale data shall produce the approved fallback behavior without concealing authoritative datastore failure.
- **REL-009 — Concurrent correctness:** Concurrent operations shall preserve uniqueness and the approved data-consistency guarantees.
- **REL-010 — Service lifecycle:** Startup, readiness, liveness, and graceful shutdown shall follow documented behavior for requests and buffered work.
- **REL-011 — Recovery:** Backup, restore, recovery-time, and recovery-point behavior shall be defined and validated if those capabilities are included in the approved prototype scope.
- **REL-012 — Time consistency:** Expiration and time-based analytics shall use an approved clock source, time zone, and boundary convention.

## 7. Performance Requirements

The REQ-004 prototype targets below are approved by RDR-004. They are validation objectives for the documented reference environment, not contractual production service-level commitments.

- **PERF-001 — Redirect latency:** Redirect processing shall meet the approved latency percentile targets at the approved load and data cardinality.
- **PERF-002 — Creation latency:** URL creation shall meet the approved latency percentile targets at the approved sustained and peak request rates.
- **PERF-003 — Analytics latency:** Analytics capture shall stay within the approved redirect-path overhead, and analytics retrieval shall meet its approved freshness and query-latency targets.
- **PERF-004 — Throughput:** The service shall sustain approved creation, redirect, and analytics volumes without exceeding approved error-rate or resource limits.
- **PERF-005 — Data scale:** Mapping and analytics behavior shall meet approved objectives at the expected record count and retention period.
- **PERF-006 — Overload behavior:** When capacity is exceeded, the service shall apply approved rate limits, backpressure, or degradation behavior instead of failing unpredictably.
- **PERF-007 — Evidence:** Performance claims shall be supported by a reproducible workload, documented environment, percentile results, error rates, and known limitations.
- **PERF-008 — Infrastructure justification:** A cache, queue, additional datastore, or distributed component shall not be required unless approved targets and evidence justify it.

## 8. Observability Requirements

- **OBS-001 — Request metrics:** Record request counts, latency, and error counts by bounded operation and outcome dimensions.
- **OBS-002 — Redirect metrics:** Distinguish successful, malformed, unknown, expired, disabled, rate-limited, and dependency-failed redirect outcomes where those states apply.
- **OBS-003 — Creation metrics:** Record successful and failed creations, validation rejections, idempotency outcomes, and collision retries using bounded-cardinality dimensions.
- **OBS-004 — Dependency metrics:** Record approved database, cache, and analytics latency, timeout, saturation, and error signals.
- **OBS-005 — Analytics pipeline metrics:** Record accepted, processed, delayed, retried, dropped, and failed analytics events as applicable to the approved design.
- **OBS-006 — Structured logs:** Emit structured operational logs with timestamp, severity, operation, outcome, and correlation information.
- **OBS-007 — Privacy-safe telemetry:** Logs, metrics, and traces shall follow the approved redaction and data-minimization policy and shall avoid unbounded identifier dimensions.
- **OBS-008 — Health signals:** Expose or emit distinct liveness and readiness signals according to the approved service-lifecycle policy.
- **OBS-009 — Alerting:** Define alerts for approved service-objective violations and material dependency, analytics, collision, cache-load, and abuse failures.
- **OBS-010 — Diagnostic correlation:** Failures shall be diagnosable across relevant components using approved correlation or trace context without exposing that context unsafely to clients.
- **OBS-011 — Product analytics separation:** Operational telemetry shall remain conceptually and access-control-wise distinct from product click analytics.

## 9. Ambiguities

Each ambiguity retains its original context and alternatives. Its status identifies whether the engineer has approved a controlling requirements decision; recommendations without an approved status remain non-binding.

### AMB-001 — URL validation

- **Ambiguity:** The assignment does not define allowed schemes, host requirements, URL length, credential syntax, fragments, internationalized domains, localhost or private-address destinations, normalization, or reachability checks.
- **Why it matters:** The decision affects security, compatibility, deduplication, stored value integrity, and test cases.
- **Possible interpretations:** Accept any parseable URI; accept only absolute HTTP/HTTPS URLs; apply additional host and destination restrictions; normalize accepted URLs; preserve accepted URLs exactly.
- **Recommended interpretation:** Accept absolute ASCII URI syntax using `http` or `https`, with a DNS name, IPv4 address, bracketed IPv6 address, `localhost`, or IDNA A-label host and an optional port from 1 through 65535. Limit the complete destination string to 4,096 characters. Allow paths, queries, fragments, private addresses, and localhost. Reject credentials, whitespace, literal or percent-encoded control characters, malformed percent encoding, missing hosts, and other schemes. Perform no DNS lookup, fetch, crawl, or reachability check. Preserve every accepted destination character-for-character.
- **Status:** APPROVED — RDR-002, 2026-09-02

### AMB-002 — Short-code generation

- **Ambiguity:** The assignment does not specify random, sequential, encoded-identifier, or hash-derived codes, nor alphabet, length, case sensitivity, entropy, or collision retry limits.
- **Why it matters:** The choice affects collision probability, predictability, enumeration resistance, storage coordination, and scalability.
- **Possible interpretations:** Sequential IDs; encoded database IDs; destination hashes; fixed-length random codes; variable-length random codes.
- **Recommended interpretation:** Generate exactly ten case-sensitive characters from `0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz` using a cryptographically secure random source without modulo bias. This supplies approximately 59.5 bits of code-space entropy and avoids sequential disclosure; correctness comes from an atomic datastore uniqueness constraint. After an initial collision, allow at most five new candidates, then fail creation without changing an existing mapping. RDR-004 confirms that this code length is adequate for the approved one-million-mapping prototype envelope; a materially larger scale requires reassessment.
- **Status:** APPROVED — RDR-002, 2026-09-02

### AMB-003 — Duplicate destination URLs

- **Ambiguity:** It is unclear whether repeated submissions of the same destination reuse a code or create separate mappings.
- **Why it matters:** The policy affects privacy, idempotency, ownership, expiration, analytics attribution, and storage growth.
- **Possible interpretations:** Always return an existing mapping; deduplicate only within an owner or tenant; always create a new mapping; reuse only when an idempotency key matches.
- **Recommended interpretation:** Create a new mapping for every ordinary creation request, including a destination identical to an existing mapping. Do not perform global or destination-based deduplication.
- **Status:** APPROVED — RDR-002, 2026-09-02

### AMB-004 — Idempotent creation

- **Ambiguity:** The assignment does not state whether clients must be able to retry creation without producing another mapping.
- **Why it matters:** Network retries can create duplicates, while idempotency storage and expiration add behavioral and persistence complexity.
- **Possible interpretations:** Creation is non-idempotent; destination-based deduplication provides idempotency; client-supplied idempotency keys provide bounded replay behavior.
- **Recommended interpretation:** Keep baseline creation non-idempotent and do not accept an idempotency key. Clients must not automatically retry when they cannot determine whether creation succeeded. Any later idempotency feature requires separately approved client identity, key scope, retention, conflict, and replay semantics; it must not be implemented through destination deduplication.
- **Status:** APPROVED — RDR-002, 2026-09-02

### AMB-005 — URL expiration

- **Ambiguity:** Expiration support, optionality, default, maximum lifetime, time boundary, renewal, storage cleanup, and expired response are unspecified.
- **Why it matters:** Expiration affects API shape, persistence, caching, analytics retention, response behavior, and time-dependent tests.
- **Possible interpretations:** Links never expire; every link has a fixed lifetime; clients choose an optional lifetime; operators configure a default and maximum; expired links return not found or gone.
- **Recommended interpretation:** Exclude expiration from the initial prototype. Links do not expire, creation does not accept an expiration value, and no expired state, cleanup policy, cache-expiration behavior, or `410 Gone` response is included. Adding expiration later requires a new approved requirement and compatibility review.
- **Status:** APPROVED — RDR-002, 2026-09-02

### AMB-006 — Redirect status code

- **Ambiguity:** The assignment does not select `301`, `302`, `307`, or `308`.
- **Why it matters:** Permanent redirects can be cached by clients and intermediaries, bypassing future analytics, expiration, or management changes; status codes also differ in method-preservation semantics.
- **Possible interpretations:** Permanent `301` or `308`; temporary `302` or `307`; configurable status by link.
- **Recommended interpretation:** Support `GET /{code}` and return `302 Found` with the accepted destination preserved exactly in `Location` and `Cache-Control: no-store`. Malformed and unknown codes return `404 Not Found` without a `Location` header. Other methods are unsupported; their exact error representation remains an ARC-002 API-contract concern.
- **Status:** APPROVED — RDR-002, 2026-09-02

### REQ-002 behavior matrix

This matrix is normative for the requirements baseline. Exact JSON schemas and error codes remain ARC-002 concerns unless stated here.

| Area | Example or condition | Required result | Related requirements |
| --- | --- | --- | --- |
| Valid URL | `https://example.com/a?x=1#part` | Accept and preserve the complete string exactly. | FR-001, SEC-001, AC-001 |
| Valid URL | `HTTP://localhost:8080/path` | Accept; scheme matching is case-insensitive and the original representation is preserved. | FR-001, SEC-001, AC-001 |
| Valid URL | `https://xn--bcher-kva.example/%E2%9C%93` | Accept the IDNA A-label and percent-encoded path without decoding or normalization. | FR-001, SEC-001, AC-001 |
| URL boundary | An otherwise valid destination containing exactly 4,096 characters | Accept. | FR-001, FR-009, AC-001 |
| URL boundary | An otherwise valid destination containing 4,097 characters | Reject and create no mapping. | FR-001, FR-009, AC-002 |
| Invalid URL | Relative path, missing host, unsupported scheme, malformed escape, port outside 1–65535, raw Unicode host, or credential-bearing authority | Reject and create no mapping. | FR-001, SEC-001, AC-002 |
| Injection input | Destination containing whitespace, a literal C0/DEL control, or `%00`–`%1F`/`%7F` in any letter case | Reject and create no mapping. | FR-009, SEC-002, AC-002 |
| Private destination | `http://127.0.0.1/resource`, private IP space, or `http://localhost/` | Accept structurally; perform no network access during validation or creation. | FR-001, SEC-012, AC-001 |
| Preservation | Accepted destination contains case, an explicit default port, dot segments, query ordering, or percent-encoding choices | Store and return the accepted string without canonicalization, decoding, or reordering. | FR-001, FR-004, AC-001, AC-006 |
| Short code | Generated candidate | Exactly ten case-sensitive Base62 characters from the approved alphabet. | FR-002, FR-003, SEC-003, AC-003 |
| Collision | Initial candidate already exists | Leave the existing mapping unchanged and try up to five new independently generated candidates. | FR-003, FR-010, REL-002, AC-004 |
| Collision exhaustion | Initial candidate and all five retries collide | Fail creation, create no new mapping, and preserve all existing mappings; exact external error mapping is deferred to ARC-002. | FR-003, FR-010, REL-002, AC-004 |
| Duplicate destination | Two ordinary requests contain the same accepted destination | Create two independent mappings with distinct successfully persisted codes. | FR-001, FR-010, AC-005 |
| Ambiguous retry | Client loses a creation response and repeats the request | Treat the repeat as a new ordinary request; no replay guarantee is provided. | FR-010, REL-005, AC-005 |
| Expiration input | Creation request supplies an expiration field | Reject it under the eventual strict request contract; links otherwise have no expiration state. | FR-005, FR-006, AC-007 |
| Successful redirect | GET contains a well-formed code mapped to a link | Return `302 Found`, exact stored destination in `Location`, and `Cache-Control: no-store`. | FR-004, AC-006 |
| Failed redirect | GET contains malformed Base62/length input or an unknown code | Return `404 Not Found` without `Location`. | FR-005, SEC-003, AC-007 |
| Unsupported method | Method other than GET targets `/{code}` | Do not resolve or redirect; exact response contract is deferred to ARC-002. | FR-004, FR-009, AC-007 |

### AMB-007 — Analytics scope

- **Ambiguity:** Required metrics, dimensions, query interface, access control, aggregation, consistency, accuracy, and retention are unspecified.
- **Why it matters:** Analytics can dominate the data model, infrastructure, privacy risk, operating cost, and redirect-path design.
- **Possible interpretations:** Total count only; counts by time bucket; raw events; bot-aware counts; referrer, user-agent, or geographic breakdown; public or protected reporting.
- **Recommended interpretation:** Store one minimal event for each eligible redirect when the analytics append succeeds. Expose only total and daily UTC counts for `all`, `suspectedAutomated`, and `unclassified`, plus an `asOf` time. Use an inclusive `from`, exclusive `to`, a default preceding 30-day range, and a maximum 90-day range. Query committed events directly without exposing raw events or claiming exactly-once, unique-human, destination-arrival, or read-your-click semantics.
- **Status:** APPROVED — RDR-003, 2026-09-02

### AMB-008 — Click definition

- **Ambiguity:** A click could mean receipt of a request, successful mapping resolution, emitted redirect response, or confirmed destination arrival.
- **Why it matters:** Different definitions yield different counts, retry behavior, and reliability guarantees. The service cannot directly prove destination arrival using only a redirect response.
- **Possible interpretations:** Count every short-code request; count every known mapping; count only active mappings that reach the redirect response path; attempt client-side confirmation.
- **Recommended interpretation:** Define an eligible click as each supported GET that resolves a known mapping and reaches the `302 Found` response path. Each separate retry or refresh is independently eligible. Malformed and unknown codes, unsupported methods, mapping dependency failures, and rate-limited requests are not clicks. Reaching the response path does not prove that the client received the response or reached the destination and does not identify a unique person.
- **Status:** APPROVED — RDR-003, 2026-09-02

### AMB-009 — Bot, crawler, and prefetch traffic

- **Ambiguity:** The assignment does not state whether automated traffic is included, excluded, or separately reported.
- **Why it matters:** Messaging clients, browsers, crawlers, and security scanners can inflate counts, while bot classification is imperfect and changes over time.
- **Possible interpretations:** Count all traffic; discard identified bots; publish raw and classified counts; maintain an allowlist or blocklist.
- **Recommended interpretation:** Include every eligible click in `all`. Inspect only transient user-agent and purpose-header signals to classify the event as `suspected_automated` or `unclassified`; classification is heuristic and spoofable. Report suspected automation separately, never discard it, and never describe `unclassified` as human. HEAD remains unsupported and creates no click event.
- **Status:** APPROVED — RDR-003, 2026-09-02

### AMB-010 — Analytics privacy

- **Ambiguity:** Collection, transformation, access, retention, and deletion rules for IP address, user agent, referrer, geography, query parameters, and identifiers are unspecified.
- **Why it matters:** These values can be personal or sensitive data and create security, compliance, access-control, and breach impact.
- **Possible interpretations:** Store raw events; pseudonymize selected fields; aggregate without retaining raw values; collect only a counter.
- **Recommended interpretation:** Persist only link identifier, UTC event time, and coarse traffic classification for analytics. Never persist raw or hashed IP addresses, user agents, referrers, destinations or destination queries, cookies, correlation IDs, geographic data, fingerprints, or analytics credentials in click events. User-agent and purpose headers may exist only transiently for classification. Retain click events for 90 days, exclude older events from queries at the boundary, and physically delete them within 24 hours. RDR-004 defers backup and restore policy, including backup retention, beyond the prototype baseline.
- **Status:** APPROVED — RDR-003, 2026-09-02

### AMB-011 — Rate limiting

- **Ambiguity:** Protected operations, client identity, quotas, burst capacity, distributed enforcement, recovery, and response headers are unspecified.
- **Why it matters:** Rate limiting affects abuse resistance, legitimate users, proxy handling, reliability, state, and testability.
- **Possible interpretations:** No prototype limits; limits only on creation and analytics; separate limits for every operation; per-IP, per-account, per-key, or combined identity.
- **Recommended interpretation:** Apply a per-client creation token bucket with capacity 20 and refill 10 per minute, and a per-analytics-token query bucket with capacity 60 and refill 60 per minute. Do not apply an application redirect quota in the single-instance baseline. Identify clients from the direct peer or an explicitly trusted proxy chain, use an in-memory keyed pseudonymous identity rather than retaining raw IP, expire idle limit state after 15 minutes, accept state reset on restart, and return `429` with bounded `Retry-After`. Distributed enforcement requires a new decision if replicas are introduced.
- **Status:** APPROVED — RDR-004, 2026-09-02

### AMB-012 — Cache behavior

- **Ambiguity:** It is unclear whether a cache is required and, if so, how positive and negative caching, TTL, invalidation, expiration, stale reads, and outage fallback work.
- **Why it matters:** Caching can reduce latency and datastore load but can also serve expired data, conceal failures, and complicate correctness.
- **Possible interpretations:** No cache; process-local cache; shared cache; positive entries only; negative entries; stale-on-error behavior.
- **Recommended interpretation:** Use a bounded process-local positive cache for resolved mappings, write through after durable creation, and do not add Redis. Cache hits may satisfy redirects without a datastore round trip, cache misses still fall back to the datastore, and redirect responses remain `Cache-Control: no-store`. Do not negative-cache unknown codes. Reconsider a shared cache only if measured evidence or topology requires it; any later shared cache requires separately approved key, TTL, negative, invalidation, stale, fallback, and load-protection semantics.
- **Status:** APPROVED — ADR-015, 2026-09-03

### AMB-013 — Database failure

- **Ambiguity:** Creation and redirect behavior during datastore timeouts, unavailability, or partial failure is unspecified.
- **Why it matters:** Returning not found during an outage is incorrect, returning success before durability can lose mappings, and unbounded retries can amplify an outage.
- **Possible interpretations:** Fail all operations; serve approved cached redirects; allow stale reads; queue creation; retry synchronously.
- **Recommended interpretation:** Require a durable authoritative commit before creation success and never translate datastore failure into not found. Use budgets of 250 ms for connection acquisition (the supported HikariCP minimum), 150 ms for mapping lookup, 500 ms for creation transaction, 50 ms for analytics append, and 1 second for analytics aggregate query. Perform no application-level dependency retry and do not queue creation. Return a retryable `503` outcome for authoritative creation, redirect, or analytics-query failure; exact wire details remain ARC-002. Analytics append alone follows the approved RDR-003 fail-open loss policy.
- **Status:** APPROVED — RDR-004, 2026-09-02

### AMB-014 — Analytics failure

- **Ambiguity:** It is unclear whether an analytics failure should fail a redirect, drop an event, buffer it, retry it, or persist it through another mechanism.
- **Why it matters:** The decision trades redirect availability and latency against analytics completeness, infrastructure complexity, and possible duplication.
- **Possible interpretations:** Synchronous fail-closed analytics; best-effort fail-open analytics; in-memory buffer; durable asynchronous delivery.
- **Recommended interpretation:** For an eligible click, make one bounded event-append attempt before returning the redirect. Do not retry, buffer, or queue the event. Append failure or an ambiguous timeout may lose the event and must emit privacy-safe operational failure evidence, but it must not change the valid `302 Found` response. Analytics may lose every event during an outage; redirect availability is preferred to completeness and no exactly-once guarantee is made.
- **Status:** APPROVED — RDR-003, 2026-09-02

### AMB-015 — Expected scale and service objectives

- **Ambiguity:** Stored records, traffic rates, latency percentiles, availability, retention, regions, recovery objectives, and cost limits are unspecified.
- **Why it matters:** These values determine whether caches, queues, partitioning, replication, and multi-region deployment are justified and whether production quality is testable.
- **Possible interpretations:** Local assessment prototype; modest single-region service; internet-scale public service; multi-region high-availability service.
- **Recommended interpretation:** Validate a single-region, single-application-instance prototype with one authoritative datastore at one million mappings and ten million retained click events. Meet the approved 30-minute mixed workload, five-minute isolated peaks, percentile latency, error-rate, lifecycle, and operational objectives documented below. Treat these as reference-environment validation targets rather than production SLOs. Defer contractual availability, production backup/restore, RTO, RPO, and cloud-cost commitments until deployment ownership and production data exist.
- **Status:** APPROVED — RDR-004, 2026-09-02

### AMB-016 — API access and tenancy

- **Ambiguity:** Anonymous versus authenticated creation, link ownership, tenant boundaries, analytics authorization, and management capabilities are unspecified.
- **Why it matters:** This determines the API contract, data model, authorization, rate-limit identity, duplicate behavior, and privacy exposure.
- **Possible interpretations:** Fully anonymous public service; authenticated single-user service; multi-tenant service; anonymous redirect with protected creation and analytics.
- **Recommended interpretation:** Keep creation and redirect anonymous. Return a cryptographically random 256-bit per-link analytics bearer token once at creation, store only its SHA-256 hash with the mapping, and require the plaintext token in the `Authorization` header for analytics retrieval. A missing credential produces `401 Unauthorized`; an unknown code or invalid token produces the same `404 Not Found` outcome. Do not add accounts, tenants, token recovery, rotation, or management APIs. Possession or sharing of the bearer token grants analytics access; possession of the short code alone does not.
- **Status:** APPROVED — RDR-003, 2026-09-02

### REQ-003 analytics event-decision matrix

This matrix defines analytics eligibility separately from whether the best-effort append succeeds.

| Request or outcome | Eligible click | Classification | Stored-event result |
| --- | --- | --- | --- |
| Known mapping reaches the GET `302 Found` path with no automation signal | Yes | `unclassified` | Attempt one append; include a successful append in `all` and `unclassified`. |
| Known mapping reaches the GET `302 Found` path with a user-agent or purpose-header automation signal | Yes | `suspected_automated` | Attempt one append; include a successful append in `all` and `suspectedAutomated`. |
| Browser refresh or client retry reaches the GET `302 Found` path | Yes, for each request | Classify each request independently | Attempt one append for each request; no cross-request deduplication. |
| Prefetch signal reaches the GET `302 Found` path | Yes | `suspected_automated` | Attempt one append and retain it in both `all` and the suspected-automation count. |
| User-agent or purpose header is missing or unrecognized | Yes, if the redirect is otherwise eligible | `unclassified` | Attempt one append; never infer human identity. |
| HEAD or another unsupported method | No | Not applicable | Do not create an event. |
| Malformed or unknown short code | No | Not applicable | Do not create an event. |
| Mapping dependency fails before resolution | No | Not applicable | Do not create an event. |
| Request is rejected by an approved redirect rate limit | No | Not applicable | Do not create an event. |
| Eligible click's event append succeeds | Yes | As classified | One committed event becomes queryable under datastore snapshot semantics. |
| Eligible click's append fails or has an ambiguous timeout | Yes by definition, absent from or uncertain in reports | As classified transiently | Return the valid redirect, perform no application retry, emit privacy-safe failure evidence, and accept event loss or uncertain commit. |
| Client does not receive the emitted redirect or never reaches the destination | Still eligible at the service boundary | As classified | No correction is attempted because destination arrival is not observable. |

### REQ-003 analytics data-classification matrix

| Data | Handling | Retention or exposure |
| --- | --- | --- |
| Link identifier | Persist in each successful click event | Retain for 90 days; expose only indirectly through authorized aggregates. |
| Event time | Persist as an approved UTC instant | Retain for 90 days; use for inclusive-start, exclusive-end daily UTC aggregation. |
| Traffic classification | Persist only as `suspected_automated` or `unclassified` | Retain for 90 days; expose only as aggregate counts. |
| Raw click event | Internal persisted record only | Never return through the analytics API; exclude from queries at 90 days and physically delete within 24 hours. |
| Analytics aggregates | Derive from committed retained events at query time | Do not persist in the baseline; return only to an authorized token holder with `asOf`. |
| Plaintext analytics token | Generate and return once during creation; use transiently for verification | Never persist, log, place in a URL, or return again. |
| Analytics token hash | Persist SHA-256 hash with its link mapping | Retain for the mapping lifetime; RDR-004 bounds prototype mappings to the environment lifetime and defers production mapping and backup retention. |
| User agent and purpose headers | Inspect transiently only for coarse classification | Never persist or return; do not log raw values. |
| Raw or hashed IP address | Prohibited from analytics processing and events | Never persist or expose in analytics; RDR-004 permits only transient pseudonymous in-memory rate-limit identity with a 15-minute idle expiry. |
| Referrer, destination, destination query, cookies, correlation ID, geographic data, and fingerprint | Prohibited from analytics processing and events | Never persist or expose in analytics. |

### REQ-003 analytics query semantics

- Return total and daily UTC buckets for `all`, `suspectedAutomated`, and `unclassified`, plus `asOf`.
- Treat `from` as inclusive and `to` as exclusive. Default to the 30 days preceding query time and reject ranges longer than 90 days.
- Exclude events at or older than the 90-day retention boundary even if physical deletion is delayed.
- Read committed retained events visible to the query's datastore snapshot. Do not promise that a preceding redirect event is present.
- Return zero counts for an authorized range with no retained events. Do not expose raw events or prohibited fields.
- Require the per-link bearer token in the `Authorization` header. Never accept it in a query string.

### REQ-004 prototype operating envelope

These targets apply at the service boundary in a documented single-region reference environment with a separate load generator. Performance evidence must record application and datastore CPU, memory, runtime, configuration, dataset, warm-up, workload, saturation, percentiles, errors, and limitations.

| Dimension | Approved baseline |
| --- | --- |
| Deployment topology | One application instance and one authoritative datastore in one region; multi-region and contractual production SLOs are excluded. |
| Validation cardinality | 1,000,000 mappings and 10,000,000 retained click events. |
| Mapping retention | Retain for the lifetime of the prototype environment, capped at the validated one-million-mapping envelope; production retention and deletion require a new decision. |
| Analytics retention | 90 days, with query exclusion at the boundary and physical deletion within 24 hours as approved by RDR-003. |
| Operational-data retention | Target 7 days for structured logs and 30 days for operational metrics when an external backend is configured; the application itself persists neither. |
| Mixed sustained workload | For 30 minutes: 150 redirects/second, 10 creations/second, and 2 analytics queries/second concurrently. |
| Isolated peak workload | For 5 minutes: 500 redirects/second, 50 creations/second, or 10 analytics queries/second; peaks need not occur simultaneously. |
| Healthy-dependency error objective | Unexpected server errors below 0.5%; expected validation, authorization, not-found, and rate-limit responses are excluded. |
| Redirect latency | p95 at or below 100 ms and p99 at or below 250 ms. |
| Creation latency | p95 at or below 200 ms and p99 at or below 500 ms. |
| Analytics query latency | p95 at or below 500 ms and p99 at or below 1 second for an allowed query at validated scale. |
| Analytics append overhead | p95 added time at or below 25 ms and a hard 50 ms attempt budget before fail-open behavior. |
| Availability | External monthly availability SLO explicitly deferred; no production-availability claim is permitted. |
| Recovery | Production backup, restore, RTO, and RPO explicitly deferred; production data is prohibited until these are approved. |
| Service lifecycle | Recover readiness within 30 seconds after the required datastore becomes healthy; allow up to 30 seconds to drain active requests during graceful shutdown. |

At one million active mappings, ten-character Base62 occupancy is approximately `1.2 × 10^-12` of the `62^10` code space. This satisfies the approved prototype envelope; a higher cardinality requires renewed entropy and enumeration analysis.

### REQ-004 control and failure policy

| Concern | Approved behavior |
| --- | --- |
| Cache | Bounded process-local positive cache for resolved mappings; no Redis, negative cache, or stale serving. The datastore remains authoritative on cache miss. |
| Event infrastructure | No Kafka, durable queue, separate analytics service, or analytics buffer. RDR-003 single-attempt fail-open semantics apply. |
| Creation limit | Per derived client identity, capacity 20 and refill 10 per minute. |
| Analytics-query limit | Per analytics bearer token, capacity 60 and refill 60 per minute. |
| Redirect limit | No application quota in the single-instance baseline; coarse connection and request protection belongs at the trusted edge. |
| Limit identity | Direct peer unless the peer is an explicitly trusted proxy; ignore forwarding headers otherwise. Use a keyed in-memory pseudonymous derivation, never persist or log raw IP, and expire idle entries after 15 minutes. |
| Limit reset and scaling | State reset on process restart is accepted. Multiple application instances require a new shared-enforcement decision. |
| Rate rejection | Return `429 Too Many Requests` with bounded `Retry-After`; exact schema belongs to ARC-002. |
| Connection acquisition | Fail after 250 ms, the supported HikariCP minimum, without an application retry. |
| Mapping lookup | Fail after 150 ms without an application retry; authoritative uncertainty produces `503`, never `404`. |
| Creation transaction | Fail after 500 ms without dependency retry or queued creation; never return success without durable commit. Collision retries are governed separately by RDR-002. |
| Analytics append | Stop waiting after 50 ms, do not retry or buffer, emit privacy-safe loss evidence, and preserve a valid redirect. |
| Analytics query | Fail after 1 second without application retry; return the approved dependency-unavailable outcome. |
| Overload | Bound request queues, workers, and dependency pools. Reject quota excess with `429` and service-capacity exhaustion with `503`; never create an unbounded queue. Exact resource sizes remain architecture configuration. |
| Startup and health | Invalid required configuration fails startup. The process may run while the datastore is unavailable, but readiness is false; liveness reflects process execution and does not fail solely because a dependency is down. |
| Shutdown | Stop admitting new traffic and drain active requests for at most 30 seconds. There is no analytics buffer to drain. |

### REQ-004 operational signals and alert objectives

Metric dimensions must use normalized operations, outcomes, status classes, and other bounded allowlisted values. Destination URLs, short codes, analytics tokens, raw or hashed IP addresses, raw user agents or referrers, and request or correlation IDs are prohibited metric labels.

| Signal or alert | Approved objective |
| --- | --- |
| Request metrics | Count and measure latency/errors for normalized creation, redirect, analytics, and health operations by bounded outcome. |
| Creation metrics | Distinguish success, validation rejection, collision retry, collision exhaustion, rate limiting, datastore failure, and internal failure. |
| Redirect metrics | Distinguish success, malformed, unknown, rate limited if later added, datastore failure, and internal failure; expired/disabled outcomes are absent from this baseline. |
| Analytics metrics | Distinguish append attempted, committed, failed, ambiguous/lost, and query outcomes; no retry or backlog state exists. |
| Dependency metrics | Record acquisition/query duration, timeout, failure, and bounded pool-saturation signals without addresses or SQL values. |
| Lifecycle metrics | Expose distinct readiness, liveness, shutdown, rate-limit, and analytics-deletion-lag signals. |
| Error-rate alert | Unexpected 5xx responses exceed 1% for 5 minutes. |
| Latency alert | An operation's p95 exceeds its approved target for 10 minutes. |
| Readiness alert | The instance remains unready for 2 minutes. |
| Datastore alert | Datastore operations exceed 1% failure for 5 minutes. |
| Analytics-loss alert | Warning on any sustained loss; alert when failed or ambiguous appends exceed 1% for 5 minutes. |
| Rate-limit alert | `429` responses exceed 10% of an operation for 10 minutes. |
| Collision alert | Collision retries exceed 0.1% for 5 minutes, or any collision exhaustion occurs. |
| Saturation alert | An approved worker or dependency pool exceeds 80% utilization for 10 minutes. |
| Retention alert | Analytics physical-deletion lag exceeds 24 hours. |

Alert routing, escalation recipient, and external telemetry product remain deployment-specific ARC-005 decisions. Alert rules must be testable with synthetic input before operational acceptance.

### Requirements-to-acceptance baseline coverage

| Requirement group | Acceptance criteria or approved rationale |
| --- | --- |
| FR-001–FR-003 | AC-001–AC-005 cover creation, preservation, code format, uniqueness, collision, and concurrency. |
| FR-004–FR-006 | AC-006–AC-009 cover redirect, unsuccessful resolution, excluded expiration, and datastore failure. |
| FR-007–FR-008 | AC-010–AC-013 cover event eligibility, automation, failure, aggregate access, freshness, and retention. |
| FR-009–FR-011 | AC-002, AC-007, AC-009, and AC-013–AC-016 cover boundaries, access, rates, dependency failures, and safe errors. |
| FR-012 | AC-020–AC-022 cover task structure, AI traceability, and human approval. |
| NFR-001–NFR-004 | AC-015, AC-018, and AC-019 cover documentation, testability, configuration validation, and safe errors. |
| NFR-005–NFR-008 | AC-005, AC-009, AC-012, AC-013, AC-016, AC-018, and AC-019 cover retries, maintainability evidence, reproducibility, and lifecycle. |
| NFR-009–NFR-012 | AC-019–AC-022 cover governance, traceability, isolated tasks, and compatibility review. |
| SEC-001–SEC-004 | AC-002–AC-004, AC-007, and AC-015 cover URL/code controls, injection, collision, and enumeration evidence. |
| SEC-005–SEC-010 | AC-013–AC-016 and AC-018 cover rate limits, minimization, secrets, transport, access, errors, and security tests. |
| SEC-011 | AC-014 and AC-019 cover approved abuse controls and documentation; full malware moderation remains an approved non-goal. |
| SEC-012–SEC-013 | AC-015, AC-018, and AC-022 cover the approved no-fetch boundary and dependency approval/security review. |
| REL-001–REL-005 | AC-004, AC-005, AC-009, and AC-016 cover durability, collision, failure classification, timeouts, and safe retry. |
| REL-006–REL-012 | AC-008, AC-009, AC-012, AC-013, AC-016, and AC-018 cover degradation, cache disposition, concurrency, lifecycle, deferred recovery, and UTC analytics. |
| PERF-001–PERF-006 | AC-017 defines measurable workload, percentile, throughput, cardinality, and overload evidence. |
| PERF-007–PERF-008 | AC-017–AC-019 and AC-022 require reproducible evidence and approval before infrastructure expansion. |
| OBS-001–OBS-005 | AC-009–AC-014, AC-016, and AC-017 require observable operation, dependency, and analytics outcomes. |
| OBS-006–OBS-011 | AC-015, AC-016, and AC-019 require structured privacy-safe telemetry, health, correlation, alert evidence, and analytics separation. |

REQ-004 approves the requirement baseline and validation envelope, not their implementation. A requirement remains unverified until its mapped task and tests produce reviewed evidence.

## 10. Assumptions

The following remain planning assumptions unless an individual entry identifies an approved controlling decision. Approval does not itself authorize implementation.

- **ASM-001 — API-only prototype:** No browser or mobile user interface is required.
- **ASM-002 — No destination fetching — APPROVED BY RDR-002:** Core shortening and redirect behavior does not fetch, preview, crawl, or verify destination content.
- **ASM-003 — Authoritative persistence — APPROVED BY RDR-004 and ADR-015:** One approved durable datastore is the authoritative source for mappings, and a bounded process-local cache accelerates hot redirect resolution without changing datastore authority.
- **ASM-004 — Single-region starting point — APPROVED BY RDR-004:** The prototype begins with a single-region, single-application-instance validation envelope.
- **ASM-005 — Environment base URL:** The short-link base URL is supplied through approved environment configuration rather than inferred from untrusted request headers.
- **ASM-006 — UTC time semantics:** Stored and compared timestamps use an approved UTC representation. Baseline links do not expire under RDR-002; a future expiration capability would require a separately approved boundary.
- **ASM-007 — No exactly-once claim — APPROVED BY RDR-003:** Analytics will not claim exactly-once delivery unless a separately approved requirement and validation method establish it.
- **ASM-008 — No unique-human claim — APPROVED BY RDR-003:** Analytics will not equate a request, click event, or bot-filtered event with a verified unique person.
- **ASM-009 — Engineer-controlled dependencies:** No framework, datastore, cache, queue, or monitoring dependency is selected until the engineer approves its need and tradeoffs.
- **ASM-010 — Explicit implementation authorization:** Approval of this plan does not by itself authorize source-code implementation.

## 11. Non-Goals

Unless separately approved as new requirements, the following are outside the initial assignment scope:

The engineer approved this non-goal list as written on 2026-09-02 for REQ-001. This disposition does not decide the separate ambiguities or architecture proposals elsewhere in this plan.

- Browser or mobile user interfaces
- User accounts, billing, subscriptions, or payment processing
- Custom aliases, branded domains, and multiple short-link domains
- QR-code generation
- Social previews and destination crawling
- Malware scanning or a full phishing and abuse-moderation platform
- Link editing or destination mutation
- Password-protected links
- Bulk creation and bulk export
- Advertising and third-party tracking integrations
- Guaranteed proof that a user reached the destination
- Guaranteed unique-human analytics
- Exactly-once analytics
- Multi-region active-active deployment
- Unlimited mapping, analytics, log, or backup retention
- Service objectives or disaster-recovery commitments that have not been explicitly approved

## 12. Acceptance Criteria

These criteria define the approved baseline. They are not implementation evidence and remain unverified until their mapped tasks and tests are reviewed.

- **AC-001:** Given a destination satisfying the approved validation policy, creation returns the approved success status and response schema and stores a retrievable mapping.
- **AC-002:** Given a prohibited or malformed destination, creation returns the approved client error and stores no mapping.
- **AC-003:** The returned short code conforms to the approved format and identifies only its stored mapping.
- **AC-004:** A forced code collision never overwrites an existing mapping and follows the approved retry and exhaustion policy.
- **AC-005:** Concurrent creation requests preserve the approved uniqueness, duplicate, and idempotency behavior.
- **AC-006:** Given a known active code, resolution returns the approved redirect status and approved exact `Location` representation.
- **AC-007:** Malformed, unknown, expired, and disabled codes produce their approved outcomes without an incorrect redirect.
- **AC-008:** Baseline creation rejects expiration inputs, links have no expiration state, and the cache does not introduce unapproved lifecycle behavior.
- **AC-009:** Database failure never creates false success or a false not-found result and follows the approved client and cache-fallback policy.
- **AC-010:** Analytics records exactly the request classes included in the approved click definition.
- **AC-011:** Retry, refresh, prefetch, and suspected-bot events follow the approved analytics policy.
- **AC-012:** Analytics failure affects redirect availability and event delivery exactly as approved, and any loss or delay is observable.
- **AC-013:** Analytics retrieval conforms to the approved schema, authorization, aggregation, freshness, and retention behavior.
- **AC-014:** Rate-limited requests follow the approved client identity, quota, `429` response, and recovery behavior.
- **AC-015:** Prohibited personal or sensitive values do not appear in source control, client errors, logs, metrics, traces, or unauthorized analytics output.
- **AC-016:** Startup, readiness, liveness, dependency timeout, retry, and graceful shutdown behavior match the approved reliability policy.
- **AC-017:** Approved creation, redirect, and analytics throughput and latency targets are demonstrated with a reproducible workload.
- **AC-018:** Unit, integration, contract, concurrency, failure, security, privacy, load, and recovery tests pass where required by the approved scope.
- **AC-019:** API, architecture, security, privacy, testing, performance, and operating behavior are documented and agree with validated behavior.
- **AC-020:** Every implementation task has explicit scope, dependencies, acceptance criteria, tests, risks, affected components, and a human approval checkpoint.
- **AC-021:** Every AI-assisted change records the AI contribution, engineer changes, validation, review findings, and accepted or rejected disposition.
- **AC-022:** No high-impact architecture, dependency, schema, security, privacy, or deployment decision is accepted without explicit engineer approval.

## 13. Risks

| ID | Risk | Impact | Proposed mitigation |
| --- | --- | --- | --- |
| RISK-001 | Architecture selected before scale and service targets | Unnecessary complexity or failure to meet actual needs | Approve measurable targets before architecture selection. |
| RISK-002 | Short-code space is too small, predictable, or poorly coordinated | Collisions, enumeration, or corrupted mappings | Perform entropy and capacity analysis and enforce authoritative uniqueness. |
| RISK-003 | Permanent redirect caching bypasses the service | Missing analytics and unenforceable expiration or future controls | Approve redirect and cache semantics explicitly. |
| RISK-004 | Duplicate and idempotency concepts are conflated | Privacy leakage, unexpected reuse, or duplicate mappings | Decide and test the policies separately. |
| RISK-005 | Analytics is coupled synchronously to redirect availability | Elevated redirect latency or outage propagation | Approve loss tolerance and failure behavior before analytics design. |
| RISK-006 | Cache state diverges from authoritative state | Redirects to expired, disabled, or incorrect destinations | Bound cache lifetime and define invalidation and fallback behavior. |
| RISK-007 | Database outage is reported as an unknown link | Incorrect client behavior and concealed operational failure | Preserve distinct failure classification and test dependency faults. |
| RISK-008 | Analytics or telemetry collects excessive personal data | Privacy, compliance, and breach exposure | Minimize data and approve access, retention, and deletion rules. |
| RISK-009 | Anonymous creation is abused | Spam, phishing, resource exhaustion, and reputational harm | Approve access, rate-limit, monitoring, and takedown policies. |
| RISK-010 | Bots and prefetchers inflate analytics | Misleading product reports | Document the click definition and classification limitations. |
| RISK-011 | Telemetry has unbounded cardinality | Monitoring cost, degraded monitoring, or outage | Use bounded dimensions and keep unique identifiers out of metric labels. |
| RISK-012 | Requirements expand during implementation | Schedule delay and poorly validated behavior | Enforce non-goals, change review, and task-level scope boundaries. |
| RISK-013 | AI-generated output is trusted without sufficient review | Correctness, security, maintainability, or compliance defects | Require engineer review, independent validation, and explicit disposition. |
| RISK-014 | URL normalization changes destination meaning | Incorrect redirects or unsafe deduplication | Preserve accepted input unless a tested normalization policy is approved. |
| RISK-015 | Performance claims lack representative evidence | Production-oriented quality cannot be evaluated | Define workloads, environments, percentiles, and reproducible validation. |

## 14. Definition of Done

The project is done only when all of the following are true:

1. The engineer has approved the final functional, non-functional, security, reliability, performance, and observability requirements.
2. Every ambiguity affecting implemented behavior has an approved decision in `DECISIONS.md`; deferred ambiguities are explicitly outside scope.
3. Approved assumptions and non-goals are recorded without contradiction.
4. Every implementation task has satisfied its explicit acceptance criteria and test requirements.
5. All required automated tests and documented validation commands pass in a clean approved environment.
6. Approved performance and reliability targets have reproducible evidence, or any accepted limitation has a documented engineer disposition.
7. Security, privacy, failure, concurrency, and recovery scenarios required by scope have been reviewed and validated.
8. API, architecture, security, testing, performance, operating, and project documentation agrees with observed behavior.
9. Requirements, decisions, tasks, code, tests, documentation, prompts, review findings, and validation evidence are traceable.
10. AI-generated, AI-edited, engineer-edited, accepted, and rejected output is distinguishable.
11. No unresolved blocking or high-severity review finding remains.
12. The engineer completes final review and explicitly accepts the result.

## 15. AI-Assisted Engineering Approach

### Responsibility model

**AI proposes and assists. The engineer reviews, modifies or rejects, validates, and approves.**

The AI may assist with:

- Requirements normalization and ambiguity discovery
- Task decomposition and dependency identification
- Architecture options and tradeoff analysis
- Draft implementation after explicit authorization
- Test-case generation
- Debugging hypotheses and evidence organization
- Refactoring proposals
- Documentation drafts
- Code-review findings
- Validation checklists and risk analysis

The AI does not:

- Make final product, architecture, security, privacy, dependency, schema, or deployment decisions
- Treat recommendations or assumptions as approved decisions
- Begin implementation without an approved task and explicit acceptance criteria
- Approve its own code or validation results
- Replace independent engineer review and testing
- Conceal rejected, substantially modified, or unvalidated output

### Required workflow

1. Link each proposed task to requirement, ambiguity, risk, and decision IDs as applicable.
2. Define task scope, non-scope, dependencies, acceptance criteria, test requirements, security considerations, failure scenarios, affected components, and human approval requirements before implementation.
3. Obtain engineer approval for high-impact decisions and any new dependency.
4. Generate or edit only the artifacts authorized by the approved task.
5. Run the approved automated and manual validation steps.
6. Present implementation, assumptions, limitations, validation evidence, and residual risk for engineer review.
7. Record engineer modifications, accepted portions, rejected portions, and rationale.
8. Do not mark work complete until the engineer approves it.

### Traceability records

- `ENGINEERING_PLAN.md` records normalized requirements, ambiguities, assumptions, acceptance criteria, risks, and governance.
- `DECISIONS.md` records engineer-approved decisions and rejected alternatives.
- `TASKS.md` records scoped work, dependencies, acceptance criteria, validation, and status.
- `PROMPT_LOG.md` records material AI instructions and outputs without secrets.
- `TRACEABILITY.md` maps requirements and decisions to tasks, implementation, tests, documentation, and evidence.
- `AI_REVIEW.md` records AI review findings and the engineer's accepted, modified, deferred, or rejected disposition.

Approval of this document authorizes planning and review only. It does not authorize application-code implementation.

## 16. Version-2 migration roadmap

This branch now implements the version-2 runtime overlay in Docker Compose: two application instances, one Redis service, and one public reverse-proxy entrypoint, while PostgreSQL remains authoritative and Flyway still runs on application startup. The overlay preserves the approved baseline and does not by itself approve a deeper microservice split.

### 16.1 Version-2 objectives

- Run two application instances on the local development stack and in the target deployment shape.
- Use Redis as the shared cache for hot redirect lookup across instances.
- Keep Flyway-managed PostgreSQL migrations as the source of truth for schema changes.
- Preserve the existing observability, validation, and error boundaries while the runtime topology changes.
- Keep a later microservice split as an explicit follow-on decision rather than an assumed outcome.

### 16.2 Implemented workstreams

| Workstream | Implemented outcome |
| --- | --- |
| Topology | Approved and implemented a two-instance runtime with Redis and a public reverse proxy. |
| Runtime | `docker-compose.yml` now starts PostgreSQL, Redis, two application containers, and the `edge` proxy together. |
| Schema | Flyway still runs on application startup and PostgreSQL remains the schema authority. |
| Cache | Redis-backed shared-cache startup and shared redirect lookup are implemented for the version-2 runtime. |
| Smoke validation | A repo-local compose smoke test verifies Redis readiness and proxy readiness through the compose network. |

### 16.3 Version-2 guardrails

- PostgreSQL remains authoritative for mappings and analytics until a separate decision changes that role.
- Redis is a shared cache, not a source of truth.
- Compose changes must not break the existing migration path or require manual schema setup.
- The implemented version-2 overlay does not imply that all future architecture changes are approved; a deeper microservice split still needs an explicit decision.
