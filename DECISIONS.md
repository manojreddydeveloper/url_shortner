# Architecture Decision Log

## Governance

This log records architecture proposals derived from `ENGINEERING_PLAN.md`, `TASKS.md`, and `TRACEABILITY.md`.

- **No decision in this document is approved unless its status is `APPROVED` by the engineer.**
- The current entries are `PROPOSED — PENDING ENGINEER APPROVAL`.
- AI proposes and analyzes alternatives. The engineer approves, modifies, defers, or rejects each decision.
- A rejected alternative may be reconsidered if requirements, scale, evidence, or constraints change.
- Approval of architecture does not automatically authorize source-code implementation or dependency installation.

## Status values

| Status | Meaning |
| --- | --- |
| `PROPOSED` | Recommended outcome awaiting engineer review |
| `APPROVED` | Engineer accepted the outcome and consequences |
| `REJECTED` | Engineer rejected the proposal and recorded why |
| `SUPERSEDED` | A later approved decision replaces this entry |
| `DEFERRED` | No decision is needed until a documented trigger occurs |

## Requirements scope decisions

### RDR-001 — Approve initial functional scope and non-goals

- **Status:** APPROVED
- **Task:** REQ-001
- **Date:** 2026-09-02
- **Decision:** Approve FR-001 through FR-012 and the Section 11 non-goals in `ENGINEERING_PLAN.md` as written.
- **Rationale:** The functional requirements retain creation, redirect, analytics, failure handling, concurrency, request-policy, and AI-traceability capabilities required by the assignment. The non-goals constrain unrelated product and infrastructure expansion.
- **Boundary:** This decision approves capability scope only. It does not resolve AMB-001 through AMB-016, approve assumptions, establish non-functional targets, approve any ADR below, authorize dependencies, or approve implementation output.
- **Engineer disposition:** APPROVED — the engineer reviewed and approved the REQ-001 documentation changes on 2026-09-02.

### RDR-002 — Approve URL and short-link behavior

- **Status:** APPROVED
- **Task:** REQ-002
- **Date:** 2026-09-02
- **Requirements:** FR-001 through FR-006, FR-009, FR-010, NFR-005, SEC-001 through SEC-004, SEC-012, REL-002, REL-005, REL-007, REL-009, REL-012, AC-001 through AC-008
- **Decision:** Adopt the AMB-001 through AMB-006 interpretations and normative behavior matrix in `ENGINEERING_PLAN.md`: structurally validate and exactly preserve bounded HTTP/HTTPS destinations without fetching them; use ten-character case-sensitive random Base62 codes with datastore-enforced uniqueness and five collision retries after the initial candidate; create a new mapping for duplicate ordinary requests; provide no baseline idempotency; exclude expiration; and use non-cacheable `302 Found` redirects for successful GET resolution.
- **Rationale:** These policies provide deterministic and testable creation and redirect behavior, avoid normalization and deduplication privacy risks, bound hostile inputs, prevent collision overwrite, retain control of redirect traffic, and omit lifecycle functionality not required by the assignment.
- **Alternatives rejected for this baseline:** Other URL schemes, credential-bearing or control-containing destinations, destination canonicalization, destination deduplication, implicit retry safety, expiration, sequential or destination-derived codes, permanent redirects, and cacheable redirect responses.
- **Boundary:** RDR-004 later confirmed the ten-character length for the one-million-mapping prototype envelope; a materially larger scale requires reassessment. Exact request/response schemas and unspecified method errors remain ARC-002 work. The approved expiration exclusion controls over optional-expiration text in the still-proposed ADR-007 and proposed API/architecture documents; those proposals must be reconciled before approval. No ADR, dependency, or source implementation is approved by this decision.
- **Engineer disposition:** APPROVED — the engineer reviewed and approved the REQ-002 documentation changes on 2026-09-02.

### RDR-003 — Approve analytics, privacy, access, and failure semantics

- **Status:** APPROVED
- **Task:** REQ-003
- **Date:** 2026-09-02
- **Requirements:** FR-007, FR-008, FR-009, NFR-008, SEC-006, SEC-007, SEC-009, SEC-010, REL-006, REL-012, PERF-003, OBS-005, OBS-007, OBS-011, AC-010 through AC-013, AC-015
- **Decision:** Adopt the AMB-007 through AMB-010, AMB-014, and AMB-016 interpretations and normative matrices in `ENGINEERING_PLAN.md`. Count each successful GET redirect-path request; preserve suspected automation as a separate heuristic class; store only link ID, UTC time, and coarse traffic class; expose protected total and daily aggregates; retain events for 90 days; use a one-time 256-bit per-link bearer token whose SHA-256 hash is stored; and fail open without retry or buffering when analytics append fails.
- **Rationale:** The policy supplies testable analytics while minimizing personal data, preventing public analytics disclosure, keeping redirect availability independent of analytics, and avoiding accounts, queues, and exactly-once claims not required by the assignment.
- **Alternatives rejected for this baseline:** Public analytics, accounts or tenancy, raw-event access, unique-human claims, dropping suspected bots, storing request metadata, destination-arrival claims, fail-closed redirects, event retries, in-memory buffering, durable queues, and exactly-once delivery.
- **Boundary:** RDR-004 later approved analytics append/query targets, rate limits, expected event volume, and cleanup alerting while explicitly deferring production availability and backup retention. Exact wire schemas remain ARC-002 work, and datastore/process topology remains architecture work. The approved policy controls over conflicting placeholders in the proposed API and architecture documents, which must be reconciled before approval. No ADR, dependency, or source implementation is approved by this decision.
- **Engineer disposition:** APPROVED — the engineer reviewed and approved the REQ-003 documentation changes on 2026-09-02.

### RDR-004 — Approve non-functional targets and requirements baseline

- **Status:** APPROVED
- **Task:** REQ-004
- **Date:** 2026-09-02
- **Requirements:** NFR-001 through NFR-012, SEC-005, SEC-008, SEC-011, SEC-013, REL-001, REL-003, REL-004, REL-007, REL-008, REL-010, REL-011, PERF-001 through PERF-008, OBS-001 through OBS-011, AC-009, AC-014, AC-016 through AC-022; confirms RDR-001 through RDR-003
- **Decision:** Adopt the REQ-004 operating envelope, control/failure policy, operational objectives, alert thresholds, and requirements-to-acceptance coverage in `ENGINEERING_PLAN.md`. Validate a single-region, single-instance prototype at one million mappings and ten million retained events; require the documented mixed and peak throughput, percentile latency, and error objectives; use no cache or dependency retry; apply the approved creation and analytics rate limits; distinguish datastore failure as unavailable; and use bounded dependency, lifecycle, overload, and observability behavior.
- **Explicit deferrals:** Contractual monthly availability, production backup/restore, RTO, RPO, cloud cost, multi-region operation, and production-data acceptance. These require new engineer decisions before production deployment or production data.
- **Infrastructure boundary:** No cache, Redis, Kafka, queue, microservice, separate analytics service, or distributed rate-limit state is required by this baseline. Architecture may select one application framework and one authoritative datastore only after comparing approved alternatives. Additional infrastructure requires measured failure against an approved target and a new decision.
- **Rationale:** The selected envelope is large enough to exercise concurrency, data cardinality, failures, privacy, and performance while remaining a bounded assessment prototype. Explicit deferrals prevent prototype evidence from being represented as a production commitment.
- **Task-gap disposition:** Add OBS-IMPL-001 to own bounded request/creation/redirect metrics and alert definitions previously identified as gaps for OBS-001 through OBS-003 and OBS-009.
- **Engineer disposition:** APPROVED — the engineer reviewed and approved the generated REQ-004 documentation changes on 2026-09-02.

### ARC-001 — Propose system boundaries and technical stack

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Task:** ARC-001
- **Date:** 2026-09-02
- **Requirements:** NFR-002, NFR-006, NFR-007, NFR-009, PERF-004, PERF-008, REL-001, REL-003, REL-004, SEC-005, SEC-008, SEC-013, OBS-001 through OBS-011
- **Decision:** Use one Java/Spring Boot application built with Gradle, organized as a modular monolith with explicit HTTP, configuration, URL-policy, creation, code-generation, mapping-repository, resolver, redirect, analytics, access-control, rate-limit, lifecycle, and observability boundaries. Use Hibernate/JPA with one PostgreSQL datastore as the authoritative source for mappings and minimal analytics events. Validate the prototype as one application instance in one region. Do not add Redis, Kafka, a queue, microservices, a separate analytics service, a separate ID-generation service, or distributed locking in the baseline.
- **Alternatives evaluated:** A single unstructured application; microservices; SQLite or a document database; a separate analytics service; Kafka or an outbox; Redis cache/shared limiter; a separate ID service; and distributed locking. These alternatives add operational, network, coordination, or consistency cost without an approved requirement or measured failure of the baseline envelope.
- **Dependency rationale:** Java, Gradle, Spring Boot, Hibernate/JPA, and PostgreSQL each have a direct role in the approved prototype. Redis, Kafka, queues, and additional services have no baseline role and remain conditional evolution options only.
- **Trust and data boundary:** Clients and forwarding metadata are untrusted until validated; PostgreSQL is authoritative but failure must remain distinguishable from not-found; analytics and operational telemetry remain separate and privacy-safe; configured public base URL and explicitly trusted proxy addresses are the only trusted deployment inputs.
- **Revisit triggers:** Add infrastructure or split a service only after measured failure against an approved target, a new deployment/ownership requirement, or a new engineer-approved durability, replay, global-rate-limit, or multi-region requirement.
- **Engineer disposition:** APPROVED — the engineer reviewed and approved the ARC-001 architecture proposal on 2026-09-02. This approval authorizes downstream architecture and foundation tasks to proceed within the documented boundaries; it does not approve unlisted dependencies or source changes.

### ARC-002 — Propose the versioned API contract

- **Status:** APPROVED
- **Task:** ARC-002
- **Date:** 2026-09-02
- **Requirements:** FR-001 through FR-011, NFR-001, NFR-004, NFR-005, SEC-001 through SEC-010, REL-001 through REL-006, REL-010, REL-012, PERF-001 through PERF-003, OBS-006, OBS-008, AC-001 through AC-016
- **Decision:** Use `POST /api/v1/links` for anonymous creation, `GET /{code}` for anonymous redirect, `GET /api/v1/links/{code}/analytics` for token-protected aggregate analytics, and `/health/live` plus `/health/ready` for operational health. Use the common stable JSON error envelope, `201` creation, `302` redirect with exact `Location` and `Cache-Control: no-store`, `404` for malformed/unknown redirects and invalid analytics tokens, `401` for missing analytics credentials, `429` for approved creation/analytics quotas, and `503` for authoritative datastore or analytics-query unavailability.
- **Contract policies translated:** Creation accepts only `url`, preserves accepted HTTP/HTTPS destinations up to 4,096 characters, rejects baseline-prohibited expiration fields, returns the short code and one-time analytics token, and remains non-idempotent. Analytics accepts UTC `from`/`to` with inclusive/exclusive bounds, a default 30-day range, a maximum 90-day range, and `day` buckets; it returns only aggregate counts and `asOf`.
- **Alternatives evaluated:** Unversioned paths; public analytics; analytics keyed by short code alone; returning `403` for invalid tokens; permanent or method-preserving redirects; accepting expiration in the baseline; destination-based idempotency; and framework-default error bodies. These alternatives either conflict with approved privacy/lifecycle policy, increase disclosure, weaken service control, or reduce client stability.
- **Open wire details:** Exact JSON property naming, optional `Location` on creation, the `405` envelope/`Allow` header, `Retry-After` calculation, informational rate-limit headers, content-negotiation normalization, and environment-specific base URL/proxy values remain proposed in `docs/api.md` and require engineer review.
- **Engineer disposition:** APPROVED — the engineer reviewed and approved the ARC-002 API contract on 2026-09-02. The remaining wire details are accepted as documented proposals for implementation review; changes that materially alter the contract require a new decision.

### ARC-003 — Propose mapping and lifecycle data model

- **Status:** APPROVED
- **Task:** ARC-003
- **Date:** 2026-09-02
- **Requirements:** FR-003, FR-004, FR-006, FR-007, FR-010, NFR-002, NFR-008, NFR-012, SEC-003, SEC-004, SEC-006, SEC-007, REL-001, REL-002, REL-009, REL-012, PERF-005, OBS-005, OBS-011, AC-001, AC-003 through AC-005, AC-008, AC-010 through AC-013, AC-015, AC-019, AC-022
- **Decision:** Use PostgreSQL tables `links` and `click_events` with database-generated `BIGINT` internal keys. Store exact validated destinations, ten-character case-sensitive short codes, SHA-256 analytics-token hashes, UTC creation/event timestamps, link foreign keys, and coarse traffic classes. Enforce code uniqueness, Base62 shape, URL length, hash length, foreign-key integrity, and `(link_id, occurred_at)` analytics lookup indexing in the database. Do not store expiration, disabled/status, owner, tenant, idempotency, or mutable-destination state in the baseline.
- **Consistency and transaction policy:** Creation writes one mapping transaction and returns only after durable commit. Analytics append uses a separate bounded transaction and remains fail-open for redirects. Queries read committed retained events from one snapshot. The database unique constraint, not a pre-check or distributed lock, arbitrates collisions.
- **Schema-evolution policy:** Use reviewed versioned migration artifacts with explicit preconditions; do not permit Hibernate destructive auto-update. Prefer runtime schema validation and tested roll-forward or reversible changes. Production backup/restore rollback claims remain deferred by RDR-004.
- **Alternatives evaluated:** A document schema; destination-URL deduplication; storing expiration/status/idempotency fields speculatively; counter-only analytics; separate analytics storage; and application-only uniqueness. These either conflict with approved semantics or weaken transactional correctness and traceability.
- **Open model details:** Exact migration tool, PostgreSQL collation declaration, deletion policy if mappings later become mutable, and database-versus-application authoritative clock implementation remain implementation-level choices requiring review before persistence work.
- **Engineer disposition:** APPROVED — the engineer reviewed and approved the ARC-003 mapping and lifecycle data model on 2026-09-02. The documented implementation-level details remain subject to review within downstream persistence work.

### ARC-004 — Define analytics data and processing architecture

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Task:** ARC-004
- **Date:** 2026-09-02
- **Requirements:** FR-007, FR-008, FR-010, NFR-002, NFR-008, SEC-006, SEC-009, REL-006, REL-012, PERF-003, OBS-005, OBS-007, OBS-011, AC-010 through AC-013, AC-015, AC-022
- **Decision:** Keep analytics as an internal module in the modular monolith. For each successful GET resolving an active mapping, attempt one minimal append-only PostgreSQL event immediately before the redirect. Persist only link ID, UTC event time, and coarse traffic class. Bound the attempt, fail open on analytics failure, expose only authorized aggregate queries, and retain events for 90 days.
- **Guarantees and limitations:** The design is best-effort and at-least-attempted, not exactly once. It makes no destination-arrival or unique-human claim, provides no retry, queue, buffer, replay, or read-your-click guarantee, and treats timeout outcomes as operationally ambiguous. Product analytics and operational telemetry remain separate.
- **Alternatives evaluated:** Kafka, a PostgreSQL outbox, an in-process queue, a separate analytics service, counter-only analytics, raw-event storage, public analytics, and fail-closed redirects. These add infrastructure or weaken approved availability/privacy semantics without a demonstrated baseline failure.
- **Infrastructure decision:** Do not add Kafka, Redis, a queue, a second datastore, or a separate service for ARC-004. Reconsider only after measured redirect-overhead failure, a durability/replay requirement, independent consumers, or deployment ownership requires it and the engineer approves a new decision.
- **Engineer disposition:** PENDING — the engineer must review and approve the event boundary, loss tolerance, privacy transformation, and infrastructure boundary before analytics implementation.

## Decision summary

| ID | Topic | Proposed outcome | Status |
| --- | --- | --- | --- |
| ADR-001 | Deployment architecture | Modular monolith, not microservices | PROPOSED |
| ADR-002 | Authoritative datastore | PostgreSQL | PROPOSED |
| ADR-003 | Short-code generation | Ten-character random Base62 in application | PROPOSED |
| ADR-004 | Coordination | Database uniqueness; no distributed lock or ID service | PROPOSED |
| ADR-005 | Redirect semantics | `302 Found` with conservative client cache control | PROPOSED |
| ADR-006 | Duplicate and retry semantics | New mapping per ordinary request; no baseline idempotency | PROPOSED |
| ADR-007 | Expiration | Optional nullable expiration with UTC boundary | PROPOSED |
| ADR-008 | Analytics architecture | Internal module, PostgreSQL events, best-effort fail-open capture | PROPOSED |
| ADR-009 | Event streaming | Do not use Kafka in the baseline | PROPOSED |
| ADR-010 | Redis | Do not require Redis in the baseline | PROPOSED |
| ADR-011 | Rate limiting | In-memory for one instance; Redis or edge only when distributed limits are required | PROPOSED |
| ADR-012 | Analytics access and privacy | Per-link management token and minimal event data | PROPOSED |
| ADR-013 | Error and observability boundaries | Stable error envelope; privacy-safe operational telemetry separate from analytics | PROPOSED |
| ADR-014 | Testing architecture | Layered tests with real PostgreSQL integration and controlled clock/random seams | PROPOSED |

## ADR-001 — Use a modular monolith

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** NFR-002, NFR-006, NFR-007, PERF-004, PERF-008, REL-003
- **Context:** The prototype needs creation, redirect, analytics, reliability, security, and observability behavior. There is no approved team-boundary, independent-deployment, or per-domain scaling requirement.
- **Proposed decision:** Build one deployable application containing internal API, creation, resolution, analytics, access-control, rate-limit, and observability modules.
- **Alternatives evaluated:**
  - Microservices for creation, redirect, analytics, or code generation.
  - A single unstructured application with no module boundaries.
- **Advantages:** One deployment, one runtime failure domain, direct in-process calls, simple local development and testing, and no internal network authentication or versioning.
- **Trade-offs:** Modules cannot be independently deployed or scaled. Poorly enforced boundaries could become tangled.
- **Why not microservices:** Microservices add network failures, authentication, deployment orchestration, contract versioning, distributed tracing, and operational cost without an approved need. Logical module boundaries preserve a later extraction path.
- **Revisit when:** A module requires independently measured scaling, release ownership, isolation, or a distinct availability target.
- **Engineer disposition:** PENDING

## ADR-002 — Use PostgreSQL as the authoritative datastore

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-003, FR-010, REL-001, REL-002, REL-003, REL-009, PERF-005
- **Context:** Mappings require durable writes, atomic uniqueness, case-sensitive lookup, concurrency correctness, expiration data, and analytics range queries.
- **Proposed decision:** Use PostgreSQL for link mappings and minimal analytics events.
- **Alternatives evaluated:**
  - SQLite: simpler local packaging but materially different concurrent-write and deployment behavior.
  - Document database: flexible shape but no requirement benefits from schema flexibility; consistency and relational event linkage remain necessary.
  - Multiple specialized datastores: increases operations and cross-store consistency complexity.
- **Advantages:** Transactions, authoritative unique constraints, mature indexes, relational integrity, time-range queries, and a credible production path.
- **Trade-offs:** Requires operating a database service and managing schema migrations and connections.
- **Revisit when:** Approved scale, regional distribution, or retention cannot be met after measured PostgreSQL tuning and appropriate schema changes.
- **Engineer disposition:** PENDING

## ADR-003 — Generate random Base62 short codes in the application

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-003, SEC-003, SEC-004, REL-002, PERF-004
- **Context:** Codes should be compact, URL-safe, non-sequential, and independently generatable by application instances.
- **Proposed decision:** Generate ten case-sensitive Base62 characters from a cryptographically secure source. Insert directly and retry the specific unique-code conflict up to five times. The database remains the uniqueness authority.
- **Alternatives evaluated:**
  - Encoded sequential ID: collision-free and compact, but predictable, enumerable, and coupled to database identity.
  - Destination hash: encourages implicit deduplication and requires canonicalization and collision policy.
  - UUID: simple and safe but produces unnecessarily long short URLs.
  - Separate ID-generation service: evaluated in ADR-004.
- **Advantages:** About 59.5 bits of code space, no central allocation, easy horizontal application scaling, and reduced volume disclosure.
- **Trade-offs:** Collisions remain possible and must be handled; case-sensitive routing and collation must be preserved; length and retry values need approval against scale.
- **Revisit when:** Approved code length, alphabet, enumeration posture, or cardinality changes.
- **Engineer disposition:** PENDING

## ADR-004 — Use datastore constraints instead of an ID service or distributed locking

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-003, FR-010, SEC-004, REL-002, REL-009
- **Context:** Concurrent application instances can generate the same candidate code. The system needs correctness but does not require coordinated sequential identifiers.
- **Proposed decision:** Let each application instance generate candidates. Use PostgreSQL's unique constraint as the atomic arbiter. Do not use distributed locks and do not deploy a separate ID-generation service.
- **Alternatives evaluated:**
  - Separate ID service: globally allocates values but becomes a critical network dependency with its own persistence, scaling, and recovery requirements.
  - Distributed lock: serializes a problem that a unique constraint solves, adding lease expiry, deadlock, split-brain, latency, and difficult recovery behavior.
  - Pre-insert existence query: races under concurrency and adds a database round trip.
- **Advantages:** Small correctness surface, atomic database enforcement, no new runtime service, and safe multi-instance generation.
- **Trade-offs:** Application must identify the precise uniqueness violation and retry it without retrying unrelated failures.
- **Revisit when:** An approved identifier requirement cannot be satisfied by independent random generation and a transactional uniqueness constraint.
- **Engineer disposition:** PENDING

## ADR-005 — Use temporary redirect semantics

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-004, FR-005, FR-006, REL-012, PERF-001
- **Context:** Permanent redirects can be cached outside the service, bypassing analytics, expiration, and future lifecycle controls.
- **Proposed decision:** Use `302 Found` for `GET /{code}` and initially return conservative `Cache-Control: no-store` behavior.
- **Alternatives evaluated:**
  - `301 Moved Permanently`: broad client caching but makes analytics and lifecycle enforcement unreliable.
  - `307 Temporary Redirect`: preserves methods, which is unnecessary when only GET is approved.
  - `308 Permanent Redirect`: combines permanence with method preservation and has the same external-cache problem.
- **Advantages:** Familiar browser behavior and continued service control over each redirect.
- **Trade-offs:** Every redirect normally reaches the service; latency and infrastructure load are higher than permanent client caching.
- **Revisit when:** Analytics and lifecycle needs are removed or a bounded caching contract is explicitly approved.
- **Engineer disposition:** PENDING

## ADR-006 — Separate duplicate behavior from idempotency

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-001, FR-010, NFR-005, REL-005
- **Context:** Repeated destinations may belong to different users, expiration windows, or analytics contexts. A retry may also repeat an earlier successful request whose response was lost.
- **Proposed decision:** Create a new link for each ordinary request. Do not add an idempotency key in the baseline until client identity, key scope, retention, and replay semantics are approved.
- **Alternatives evaluated:**
  - Global destination deduplication: reduces rows but leaks destination existence and couples unrelated analytics and lifecycle.
  - Per-owner deduplication: needs approved accounts or tenancy.
  - Idempotency key: safely handles retries but requires scoped state and expiration.
- **Advantages:** Simple semantics, no normalization-dependent deduplication, and independent link analytics.
- **Trade-offs:** Ambiguous client retries can create multiple mappings; clients must avoid automatic retry until idempotency is added.
- **Revisit when:** Retry-safe creation is an approved requirement or authenticated ownership is introduced.
- **Engineer disposition:** PENDING

## ADR-007 — Represent optional expiration directly on links

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-005, FR-006, REL-007, REL-012
- **Context:** The requirements discuss expiration but do not yet approve defaults, maximums, or boundary behavior.
- **Proposed decision:** Include nullable `expires_at` design capacity. If expiration is approved, validate it on creation, compare it using UTC, return `410 Gone` after the approved boundary, and never let cache lifetime exceed it.
- **Alternatives evaluated:**
  - No expiration: smallest implementation but omits the evaluated lifecycle behavior.
  - Mandatory fixed expiry: simple operations but may conflict with product expectations.
  - Scheduled deletion only: loses the ability to distinguish expired from unknown and has cleanup lag.
- **Advantages:** Supports no-expiry and explicit-expiry links without a scheduler on the redirect correctness path.
- **Trade-offs:** Retains expired rows until a cleanup policy exists; exact boundary and retention still need approval.
- **Revisit when:** Expiration is rejected, mandatory retention is approved, or deletion and renewal enter scope.
- **Engineer disposition:** PENDING

## ADR-008 — Keep analytics in-process and use minimal PostgreSQL events

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-007, FR-008, SEC-006, REL-006, PERF-003, OBS-005, OBS-011
- **Context:** Analytics scope is modest and no event volume or separate availability target justifies an independent service. Redirects should remain available when analytics fails.
- **Proposed decision:** Capture a minimal event inside the application and attempt a bounded append-only insert into PostgreSQL. Store link ID, UTC time, and coarse traffic class only. Query approved aggregates from PostgreSQL. Analytics insert failure is observable but does not fail a valid redirect.
- **Alternatives evaluated:**
  - Synchronous counter update: smallest storage but creates hot rows and loses time detail.
  - In-process queue: reduces redirect write latency but can lose buffered events on crash and adds shutdown/backpressure behavior.
  - PostgreSQL outbox: durable handoff but adds polling, cleanup, and duplicate processing.
  - Separate analytics service: adds network, deployment, authentication, and versioning without eliminating delivery semantics.
- **Advantages:** One deployment and datastore, minimal personal data, straightforward querying, and explicit fail-open behavior.
- **Trade-offs:** One write per active redirect, best-effort counts, and shared database capacity. It does not provide exactly-once analytics.
- **Why no separate analytics service:** An internal module meets current isolation and testability needs. A service boundary is justified only by independent scale, ownership, release, or availability needs.
- **Revisit when:** Measured write overhead, retention volume, consumers, or service objectives cannot be met by the baseline.
- **Engineer disposition:** PENDING

## ADR-009 — Do not use Kafka in the baseline

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** NFR-006, NFR-007, PERF-003, PERF-004, PERF-008, REL-006
- **Context:** There is no approved event throughput, replay period, multi-consumer requirement, or organizational Kafka platform.
- **Proposed decision:** Do not add Kafka.
- **Alternatives evaluated:**
  - Kafka event stream between redirect and analytics.
  - PostgreSQL event insert, in-process queue, or PostgreSQL outbox.
- **Advantages of Kafka:** High sustained throughput, durable replay, consumer independence, and partitioned processing.
- **Costs and risks:** Broker operations, partitioning, schemas, delivery semantics, consumer lag, retention, security, local development, monitoring, and additional failure modes.
- **Why it is unnecessary now:** The proposed PostgreSQL event path satisfies the current functional shape with much lower complexity. Kafka would solve unapproved scale and replay requirements.
- **Revisit when:** Approved sustained volume, independent consumers, replay, retention, or existing platform standards justify it with evidence.
- **Engineer disposition:** PENDING

## ADR-010 — Defer Redis until evidence requires it

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** REL-007, REL-008, PERF-001, PERF-006, PERF-008
- **Context:** Redis could cache mappings or coordinate rate limits, but no numeric target or replica topology currently requires it.
- **Proposed decision:** Baseline requests use PostgreSQL directly and single-instance rate-limit state in memory. Do not add Redis initially.
- **Alternatives evaluated:**
  - Baseline Redis cache-aside for every redirect.
  - Redis as primary mapping store.
  - Redis as analytics queue or distributed lock manager.
- **Advantages of deferral:** One fewer stateful dependency, no stale-cache semantics, simpler failures, local setup, security, and tests.
- **Trade-offs:** Database absorbs every lookup; in-memory limits are not globally strict and reset on restart.
- **Conditional use:** Add Redis cache-aside if measured redirect latency or database load requires it; add atomic Redis token buckets if multiple instances require strict shared limits.
- **Revisit when:** PERF evidence or deployment topology reaches a recorded trigger.
- **Engineer disposition:** PENDING

## ADR-011 — Use the smallest rate limiter for the deployed topology

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-009, SEC-005, PERF-006
- **Context:** Creation and analytics queries are abuse-sensitive, but the deployment replica count and exact quotas are undecided.
- **Proposed decision:** For a single instance, use a bounded in-memory token bucket keyed by an approved pseudonymous client identity. For multiple instances requiring strict global limits, use Redis or an approved edge gateway. Do not use PostgreSQL for per-request rate state.
- **Alternatives evaluated:** Fixed windows, token buckets, database counters, Redis scripts, and edge-managed limits.
- **Advantages:** Token buckets support controlled bursts; the baseline has no extra network dependency.
- **Trade-offs:** Local state resets and is per-instance. IP-derived identity can be inaccurate behind shared networks and raises privacy concerns.
- **Revisit when:** Authentication, proxy topology, replica count, quota, and distributed-consistency requirements are approved.
- **Engineer disposition:** PENDING

## ADR-012 — Use a per-link analytics management token and minimal analytics data

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-008, SEC-006, SEC-009, OBS-011
- **Context:** Analytics should not be public merely because someone sees a short link, but full user accounts are outside the initial scope.
- **Proposed decision:** Return a random 256-bit bearer token once during creation, store only its SHA-256 hash, and require it for analytics. Store no raw IP, user agent, referrer, or destination in click events; store only link ID, UTC time, and coarse traffic class.
- **Alternatives evaluated:**
  - Public analytics by code: smallest but exposes usage to link recipients.
  - Global API key: simple for one operator but does not provide per-link ownership.
  - Full accounts and tenants: stronger management but materially expands scope.
- **Advantages:** Protects per-link analytics without an identity service and sharply limits privacy exposure.
- **Trade-offs:** Bearer-token sharing grants access; lost tokens are unrecoverable; rotation requires a future capability; token possession is not a durable identity.
- **Revisit when:** Accounts, tenant ownership, token rotation, deletion, or public analytics is approved.
- **Engineer disposition:** PENDING

## ADR-013 — Standardize safe errors and separate telemetry from analytics

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** FR-011, NFR-004, SEC-006, SEC-010, OBS-001 through OBS-011
- **Context:** Clients need stable errors while operators need enough detail to diagnose failures. Operational signals must not become a second ungoverned analytics dataset.
- **Proposed decision:** Use a stable JSON error envelope with safe code, message, optional field details, and request ID. Emit structured logs and bounded metrics without URLs, codes, tokens, IPs, user agents, or referrers. Keep product analytics separate in model and access control.
- **Alternatives evaluated:** Framework-default errors, unstructured logs, identifiers as metric labels, and raw request logging.
- **Advantages:** Predictable clients, lower disclosure risk, bounded monitoring cost, and clear data governance.
- **Trade-offs:** Redaction and bounded labels reduce some ad hoc diagnostic detail; correlation practices must be consistent.
- **Gap:** `TRACEABILITY.md` identifies missing dedicated implementation tasks for request metrics and alert rules. These must be added before Phase 2.
- **Engineer disposition:** PENDING

## ADR-014 — Use layered tests with real dependency integration

- **Status:** PROPOSED — PENDING ENGINEER APPROVAL
- **Requirements:** NFR-002, NFR-007, REL-009, PERF-007, SEC-013
- **Context:** Pure rules need fast deterministic tests, while database constraints, migrations, concurrency, and failure behavior cannot be proven by mocks alone.
- **Proposed decision:** Use unit/property, component, PostgreSQL integration, API contract, concurrency, fault, security/privacy, and performance layers. Provide controlled clock, random source, repository, optional cache, and telemetry boundaries only where tests need control.
- **Alternatives evaluated:** Unit tests only, end-to-end tests only, mocked database tests, and production-network validation.
- **Advantages:** Fast local feedback plus evidence against real datastore semantics and critical failure paths.
- **Trade-offs:** Integration and fault suites require isolated dependency setup and take longer than unit tests.
- **Safety:** Tests use synthetic destinations and isolated dependencies and never send traffic to unapproved external systems.
- **Engineer disposition:** PENDING

## Approval checklist

For each ADR, the engineer should record:

- `APPROVED`, `REJECTED`, `DEFERRED`, or a modified replacement;
- review date and engineer identity or reference;
- rationale for material changes;
- resulting task and acceptance-criteria updates;
- whether source implementation is separately authorized.
