# Engineering Plan

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

Numeric targets are pending engineer approval and must be defined before architecture selection or performance acceptance.

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

Every ambiguity in this section remains unresolved. A recommendation is not an approved decision.

### AMB-001 — URL validation

- **Ambiguity:** The assignment does not define allowed schemes, host requirements, URL length, credential syntax, fragments, internationalized domains, localhost or private-address destinations, normalization, or reachability checks.
- **Why it matters:** The decision affects security, compatibility, deduplication, stored value integrity, and test cases.
- **Possible interpretations:** Accept any parseable URI; accept only absolute HTTP/HTTPS URLs; apply additional host and destination restrictions; normalize accepted URLs; preserve accepted URLs exactly.
- **Recommended interpretation:** Accept structurally valid absolute `http` and `https` URLs, reject control characters and credential-bearing forms, apply an approved length limit, perform no network fetch, and preserve the accepted destination unless explicit normalization is approved.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-002 — Short-code generation

- **Ambiguity:** The assignment does not specify random, sequential, encoded-identifier, or hash-derived codes, nor alphabet, length, case sensitivity, entropy, or collision retry limits.
- **Why it matters:** The choice affects collision probability, predictability, enumeration resistance, storage coordination, and scalability.
- **Possible interpretations:** Sequential IDs; encoded database IDs; destination hashes; fixed-length random codes; variable-length random codes.
- **Recommended interpretation:** Use fixed-length, URL-safe random codes from an approved entropy source, with an authoritative uniqueness constraint and bounded collision retries. Select length only after the scale target and entropy calculation are approved.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-003 — Duplicate destination URLs

- **Ambiguity:** It is unclear whether repeated submissions of the same destination reuse a code or create separate mappings.
- **Why it matters:** The policy affects privacy, idempotency, ownership, expiration, analytics attribution, and storage growth.
- **Possible interpretations:** Always return an existing mapping; deduplicate only within an owner or tenant; always create a new mapping; reuse only when an idempotency key matches.
- **Recommended interpretation:** Create a new mapping for an ordinary request and handle retried requests through a separate approved idempotency policy.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-004 — Idempotent creation

- **Ambiguity:** The assignment does not state whether clients must be able to retry creation without producing another mapping.
- **Why it matters:** Network retries can create duplicates, while idempotency storage and expiration add behavioral and persistence complexity.
- **Possible interpretations:** Creation is non-idempotent; destination-based deduplication provides idempotency; client-supplied idempotency keys provide bounded replay behavior.
- **Recommended interpretation:** Keep destination deduplication separate from retry safety and add idempotency keys only if the approved client and reliability requirements need them.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-005 — URL expiration

- **Ambiguity:** Expiration support, optionality, default, maximum lifetime, time boundary, renewal, storage cleanup, and expired response are unspecified.
- **Why it matters:** Expiration affects API shape, persistence, caching, analytics retention, response behavior, and time-dependent tests.
- **Possible interpretations:** Links never expire; every link has a fixed lifetime; clients choose an optional lifetime; operators configure a default and maximum; expired links return not found or gone.
- **Recommended interpretation:** Support an optional explicit expiration only if approved, use a consistently defined UTC boundary, never let cache extend the lifetime, and return a separately approved expired outcome.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-006 — Redirect status code

- **Ambiguity:** The assignment does not select `301`, `302`, `307`, or `308`.
- **Why it matters:** Permanent redirects can be cached by clients and intermediaries, bypassing future analytics, expiration, or management changes; status codes also differ in method-preservation semantics.
- **Possible interpretations:** Permanent `301` or `308`; temporary `302` or `307`; configurable status by link.
- **Recommended interpretation:** Use `302 Found` for the initial GET-oriented prototype because its non-permanent semantics better preserve control of analytics and lifecycle behavior.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-007 — Analytics scope

- **Ambiguity:** Required metrics, dimensions, query interface, access control, aggregation, consistency, accuracy, and retention are unspecified.
- **Why it matters:** Analytics can dominate the data model, infrastructure, privacy risk, operating cost, and redirect-path design.
- **Possible interpretations:** Total count only; counts by time bucket; raw events; bot-aware counts; referrer, user-agent, or geographic breakdown; public or protected reporting.
- **Recommended interpretation:** Begin with the smallest approved aggregate needed by the assessment, expose its accuracy and freshness limitations, and add dimensions only when explicitly approved.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-008 — Click definition

- **Ambiguity:** A click could mean receipt of a request, successful mapping resolution, emitted redirect response, or confirmed destination arrival.
- **Why it matters:** Different definitions yield different counts, retry behavior, and reliability guarantees. The service cannot directly prove destination arrival using only a redirect response.
- **Possible interpretations:** Count every short-code request; count every known mapping; count only active mappings that reach the redirect response path; attempt client-side confirmation.
- **Recommended interpretation:** Count a valid, active mapping resolution that reaches the redirect response path, and document that this does not prove destination arrival.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-009 — Bot, crawler, and prefetch traffic

- **Ambiguity:** The assignment does not state whether automated traffic is included, excluded, or separately reported.
- **Why it matters:** Messaging clients, browsers, crawlers, and security scanners can inflate counts, while bot classification is imperfect and changes over time.
- **Possible interpretations:** Count all traffic; discard identified bots; publish raw and classified counts; maintain an allowlist or blocklist.
- **Recommended interpretation:** Preserve a raw count and report suspected automated traffic separately when classification is approved; do not claim that filtered counts represent unique humans.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-010 — Analytics privacy

- **Ambiguity:** Collection, transformation, access, retention, and deletion rules for IP address, user agent, referrer, geography, query parameters, and identifiers are unspecified.
- **Why it matters:** These values can be personal or sensitive data and create security, compliance, access-control, and breach impact.
- **Possible interpretations:** Store raw events; pseudonymize selected fields; aggregate without retaining raw values; collect only a counter.
- **Recommended interpretation:** Minimize collection, do not store raw IP addresses by default, avoid retaining complete destination or referrer query strings, and document approved retention and access.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-011 — Rate limiting

- **Ambiguity:** Protected operations, client identity, quotas, burst capacity, distributed enforcement, recovery, and response headers are unspecified.
- **Why it matters:** Rate limiting affects abuse resistance, legitimate users, proxy handling, reliability, state, and testability.
- **Possible interpretations:** No prototype limits; limits only on creation and analytics; separate limits for every operation; per-IP, per-account, per-key, or combined identity.
- **Recommended interpretation:** Apply approved limits to resource-intensive or abuse-prone operations first, select client identity only after tenancy and trusted-proxy decisions, and return documented `429` behavior.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-012 — Cache behavior

- **Ambiguity:** It is unclear whether a cache is required and, if so, how positive and negative caching, TTL, invalidation, expiration, stale reads, and outage fallback work.
- **Why it matters:** Caching can reduce latency and datastore load but can also serve expired data, conceal failures, and complicate correctness.
- **Possible interpretations:** No cache; process-local cache; shared cache; positive entries only; negative entries; stale-on-error behavior.
- **Recommended interpretation:** Do not require a cache until approved scale targets justify it. If approved, treat the datastore as authoritative and ensure cache lifetime cannot exceed mapping expiration.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-013 — Database failure

- **Ambiguity:** Creation and redirect behavior during datastore timeouts, unavailability, or partial failure is unspecified.
- **Why it matters:** Returning not found during an outage is incorrect, returning success before durability can lose mappings, and unbounded retries can amplify an outage.
- **Possible interpretations:** Fail all operations; serve approved cached redirects; allow stale reads; queue creation; retry synchronously.
- **Recommended interpretation:** Never return creation success without the approved durable write, never convert datastore failure into not found, use bounded retries only when safe, and serve cached redirects only under an explicit approved policy.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-014 — Analytics failure

- **Ambiguity:** It is unclear whether an analytics failure should fail a redirect, drop an event, buffer it, retry it, or persist it through another mechanism.
- **Why it matters:** The decision trades redirect availability and latency against analytics completeness, infrastructure complexity, and possible duplication.
- **Possible interpretations:** Synchronous fail-closed analytics; best-effort fail-open analytics; in-memory buffer; durable asynchronous delivery.
- **Recommended interpretation:** Keep a valid redirect available, make analytics loss or delay observable, and choose buffering or durable delivery only from an approved loss tolerance and scale target.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-015 — Expected scale and service objectives

- **Ambiguity:** Stored records, traffic rates, latency percentiles, availability, retention, regions, recovery objectives, and cost limits are unspecified.
- **Why it matters:** These values determine whether caches, queues, partitioning, replication, and multi-region deployment are justified and whether production quality is testable.
- **Possible interpretations:** Local assessment prototype; modest single-region service; internet-scale public service; multi-region high-availability service.
- **Recommended interpretation:** Establish explicit prototype targets before architecture selection and start with a single-region design unless approved targets require more.
- **Status:** PENDING ENGINEER APPROVAL

### AMB-016 — API access and tenancy

- **Ambiguity:** Anonymous versus authenticated creation, link ownership, tenant boundaries, analytics authorization, and management capabilities are unspecified.
- **Why it matters:** This determines the API contract, data model, authorization, rate-limit identity, duplicate behavior, and privacy exposure.
- **Possible interpretations:** Fully anonymous public service; authenticated single-user service; multi-tenant service; anonymous redirect with protected creation and analytics.
- **Recommended interpretation:** Approve the intended actors and access boundaries before designing the API or data model; do not infer accounts or tenancy solely from the word analytics.
- **Status:** PENDING ENGINEER APPROVAL

## 10. Assumptions

The following are planning assumptions only. None authorizes implementation, and each remains `PENDING ENGINEER APPROVAL`.

- **ASM-001 — API-only prototype:** No browser or mobile user interface is required.
- **ASM-002 — No destination fetching:** Core shortening and redirect behavior does not fetch, preview, crawl, or verify destination content.
- **ASM-003 — Authoritative persistence:** One approved durable datastore is the authoritative source for mappings; caching, if later approved, is not authoritative.
- **ASM-004 — Single-region starting point:** The prototype begins with a single-region operating assumption unless approved service objectives require otherwise.
- **ASM-005 — Environment base URL:** The short-link base URL is supplied through approved environment configuration rather than inferred from untrusted request headers.
- **ASM-006 — UTC time semantics:** Stored and compared timestamps use an approved UTC representation, while exact expiration inclusivity remains to be decided.
- **ASM-007 — No exactly-once claim:** Analytics will not claim exactly-once delivery unless a separately approved requirement and validation method establish it.
- **ASM-008 — No unique-human claim:** Analytics will not equate a request, click event, or bot-filtered event with a verified unique person.
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

Policy-dependent criteria are not executable until the related ambiguity is resolved.

- **AC-001:** Given a destination satisfying the approved validation policy, creation returns the approved success status and response schema and stores a retrievable mapping.
- **AC-002:** Given a prohibited or malformed destination, creation returns the approved client error and stores no mapping.
- **AC-003:** The returned short code conforms to the approved format and identifies only its stored mapping.
- **AC-004:** A forced code collision never overwrites an existing mapping and follows the approved retry and exhaustion policy.
- **AC-005:** Concurrent creation requests preserve the approved uniqueness, duplicate, and idempotency behavior.
- **AC-006:** Given a known active code, resolution returns the approved redirect status and approved exact `Location` representation.
- **AC-007:** Malformed, unknown, expired, and disabled codes produce their approved outcomes without an incorrect redirect.
- **AC-008:** Expiration, if approved, is correct immediately before, at, and after the approved time boundary and cannot be extended by cached state.
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
