# URL Shortener Engineering Tasks

This backlog is derived from `ENGINEERING_PLAN.md`. It describes future work but does not authorize application-code implementation or dependency installation.

## Task classification

- **AI-friendly:** AI can efficiently draft, analyze, generate tests, or review the work. All output still requires engineer validation.
- **Engineer-heavy:** Product judgment, risk ownership, environment knowledge, or final decision-making is central to the task.
- **High-impact:** The task can materially affect API behavior, architecture, dependencies, persisted data, security, privacy, or production reliability. Engineer approval is required before work starts.

For every task, “Human Approval Required” means the engineer must review and accept the result. High-impact tasks additionally require explicit approval of the proposed approach before implementation begins.

## PHASE 0 - Requirements

### REQ-001 — Disposition functional scope and non-goals

- **Task ID:** REQ-001
- **Title:** Disposition functional scope and non-goals
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Establish which proposed capabilities are required for the prototype.
- **Description:** Review FR-001 through FR-012, the goals, and the non-goals. Approve, modify, reject, or defer each capability without selecting implementation technology.
- **Dependencies:** None
- **Acceptance Criteria:**
  1. Every functional requirement is approved, modified, rejected, or deferred with rationale.
  2. Required actors and core creation, redirect, analytics, and lifecycle capabilities are explicit.
  3. Non-goals are explicitly approved.
  4. Outcomes are recorded in `ENGINEERING_PLAN.md` and `DECISIONS.md`.
- **Test Requirements:** Perform a document consistency review confirming that goals, requirements, non-goals, and acceptance criteria do not conflict.
- **Security Considerations:** Do not approve public creation, analytics access, or management behavior without considering ownership and abuse exposure.
- **Failure Scenarios:** An omitted actor or capability causes later API or authorization redesign; a recommendation is accidentally recorded as a decision.
- **Impacted Components:** `ENGINEERING_PLAN.md`, `DECISIONS.md`, future API scope
- **Human Approval Required:** Yes — the engineer must approve the product scope before subsequent requirement tasks are accepted.

### REQ-002 — Resolve URL and short-link behavior

- **Task ID:** REQ-002
- **Title:** Resolve URL and short-link behavior
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Convert URL creation and redirect ambiguities into testable decisions.
- **Description:** Resolve AMB-001 through AMB-006, including URL validation, short-code generation, duplicate URLs, idempotency, expiration, and redirect status.
- **Dependencies:** REQ-001
- **Acceptance Criteria:**
  1. Allowed URL forms, size limit, preservation or normalization behavior, and prohibited input are specified.
  2. Short-code alphabet, length policy, case sensitivity, entropy objective, uniqueness, and collision behavior are specified.
  3. Duplicate and idempotency policies are decided separately.
  4. Expiration and redirect semantics are specified or explicitly excluded.
  5. Decisions are recorded with affected FR, SEC, REL, and AC identifiers.
- **Test Requirements:** Produce a reviewable behavior matrix containing valid, invalid, boundary, collision, duplicate, expiration, and redirect examples.
- **Security Considerations:** Address unsafe schemes, encoded control characters, credential-bearing URLs, enumeration, and header injection.
- **Failure Scenarios:** Ambiguous normalization changes a destination; collision policy permits overwrite; permanent caching bypasses lifecycle rules.
- **Impacted Components:** `ENGINEERING_PLAN.md`, `DECISIONS.md`, future validation, code-generation, creation, and redirect components
- **Human Approval Required:** Yes — all behavior decisions require engineer approval before API or data design.

### REQ-003 — Resolve analytics, privacy, access, and failure semantics

- **Task ID:** REQ-003
- **Title:** Resolve analytics, privacy, access, and failure semantics
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Define what analytics means and what data the system may process.
- **Description:** Resolve AMB-007 through AMB-010, AMB-014, and AMB-016: analytics scope, click definition, bot treatment, privacy, failure behavior, access, and tenancy.
- **Dependencies:** REQ-001
- **Acceptance Criteria:**
  1. The click boundary and treatment of retries, refreshes, prefetch, and bots are testable.
  2. Analytics output, aggregation, freshness, consistency, retention, and access rules are specified.
  3. Collected, transformed, prohibited, and deleted fields are listed.
  4. Accepted event loss and duplication and redirect behavior during analytics failure are explicit.
  5. Decisions reference FR-007, FR-008, SEC-006, SEC-009, REL-006, and relevant acceptance criteria.
- **Test Requirements:** Review an event-decision table and data-classification table against every analytics-related requirement.
- **Security Considerations:** Minimize personal data and prevent public analytics from exposing private link usage or ownership information.
- **Failure Scenarios:** Counts are misleading; bot traffic is silently discarded; analytics failure causes redirect outage; personal data is retained without approval.
- **Impacted Components:** `ENGINEERING_PLAN.md`, `DECISIONS.md`, future analytics, authorization, storage, and telemetry components
- **Human Approval Required:** Yes — the engineer must approve the analytics and privacy contract.

### REQ-004 — Approve scale, reliability, observability, and requirements baseline

- **Task ID:** REQ-004
- **Title:** Approve non-functional targets and baseline requirements
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Make production-oriented quality measurable before architecture selection.
- **Description:** Resolve AMB-011 through AMB-015 as applicable, assign numeric or explicitly prototype-limited targets, reconcile NFR, SEC, REL, PERF, OBS, assumptions, risks, and acceptance criteria, then baseline the plan.
- **Dependencies:** REQ-002, REQ-003
- **Acceptance Criteria:**
  1. Expected data volume, sustained and peak traffic, latency percentiles, availability, retention, and recovery targets are approved or explicitly deferred.
  2. Database, cache, rate-limit, overload, timeout, retry, and degradation policies are decided.
  3. Required operational signals and alert objectives are identified.
  4. Every normative requirement maps to acceptance criteria or an approved non-testable rationale.
  5. No unresolved ambiguity is silently embedded in the baselined requirements.
- **Test Requirements:** Run a requirements traceability and contradiction review; verify all IDs are unique and referenced decisions exist.
- **Security Considerations:** Scale and failure targets must account for abuse, trusted-proxy identity, privacy-safe telemetry, and recovery-data protection.
- **Failure Scenarios:** Architecture is optimized for an invented scale; datastore outage appears as not found; telemetry creates a privacy or cardinality incident.
- **Impacted Components:** `ENGINEERING_PLAN.md`, `DECISIONS.md`, `TRACEABILITY.md`, all future technical components
- **Human Approval Required:** Yes — the engineer must approve the requirements baseline before architecture work.

## PHASE 1 - Architecture

### ARC-001 — Select system boundaries and technical stack

- **Task ID:** ARC-001
- **Title:** Select system boundaries and technical stack
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Choose the smallest architecture and dependency set justified by approved requirements.
- **Description:** Compare credible runtime, framework, datastore, deployment, and component-boundary options. Include a no-cache and no-queue baseline unless requirements rule them out.
- **Dependencies:** REQ-004
- **Acceptance Criteria:**
  1. Selected and rejected alternatives are compared against approved functional and quality targets.
  2. Every proposed dependency has a purpose, risk, maintenance assessment, and simpler-alternative analysis.
  3. Data and request flows and trust boundaries are documented.
  4. No dependency is installed during this task.
  5. The selected architecture and dependency set are recorded in `DECISIONS.md`.
- **Test Requirements:** Conduct an architecture review using representative success, scale, security, and dependency-failure scenarios.
- **Security Considerations:** Define external trust boundaries, secret locations, transport termination, and dependency supply-chain exposure.
- **Failure Scenarios:** Unnecessary infrastructure increases failure modes; selected storage cannot enforce uniqueness; runtime or dependency is unsupported.
- **Impacted Components:** `docs/architecture.md`, `DECISIONS.md`, future project structure and deployment
- **Human Approval Required:** Yes — explicit approval is required before scaffolding or installing dependencies.

### ARC-002 — Define the API contract

- **Task ID:** ARC-002
- **Title:** Define the API contract
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Establish stable, testable creation, redirect, analytics, and error interfaces.
- **Description:** Specify paths or equivalent interfaces, methods, request and response schemas, status codes, authentication, authorization, idempotency, rate limits, and examples.
- **Dependencies:** ARC-001
- **Acceptance Criteria:**
  1. Every approved functional capability has a success contract.
  2. Invalid input, unknown or inactive code, rate limit, dependency failure, and internal error behavior are distinct where required.
  3. Redirect status and `Location` preservation conform to approved decisions.
  4. Analytics access and privacy rules are represented.
  5. The contract maps to FR and AC identifiers and is approved.
- **Test Requirements:** Draft contract-test cases for every documented response and validate examples against their schemas.
- **Security Considerations:** Prevent sensitive error disclosure; specify access control, request limits, and safe base-URL construction.
- **Failure Scenarios:** Contract omits retry behavior; host headers influence unsafe short URLs; analytics is accessible without approved authorization.
- **Impacted Components:** `docs/api.md`, `DECISIONS.md`, future API handlers and contract tests
- **Human Approval Required:** Yes — the engineer must approve the external contract before implementation.

### ARC-003 — Define mapping and lifecycle data model

- **Task ID:** ARC-003
- **Title:** Define mapping and lifecycle data model
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Specify authoritative mapping data and consistency rules.
- **Description:** Define mapping fields, identifiers, timestamps, constraints, indexes or equivalent lookup mechanism, expiration representation, idempotency data if approved, transaction boundaries, retention, and schema evolution.
- **Dependencies:** ARC-001, ARC-002
- **Acceptance Criteria:**
  1. Every field has purpose, type, nullability, source, and retention classification.
  2. Authoritative uniqueness and collision behavior can be enforced atomically.
  3. Duplicate, idempotency, expiration, and inactive-state decisions can be represented without ambiguity.
  4. Creation and lookup consistency boundaries are documented.
  5. Migration and rollback expectations are documented and approved.
- **Test Requirements:** Define persistence contract tests for uniqueness, concurrency, transactions, expiration boundaries, and schema migration.
- **Security Considerations:** Minimize stored destinations and metadata, protect identifiers, and document access and backup exposure.
- **Failure Scenarios:** Partial creation leaves inconsistent state; migration loses mappings; nullable lifecycle data produces ambiguous resolution.
- **Impacted Components:** `docs/architecture.md`, `DECISIONS.md`, future schema, repository, and migration components
- **Human Approval Required:** Yes — the engineer must approve the model before persistence implementation.

### ARC-004 — Define analytics data and processing architecture

- **Task ID:** ARC-004
- **Title:** Define analytics data and processing architecture
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Translate approved analytics semantics into a minimal processing and query design.
- **Description:** Define event boundaries, fields, identity, capture, delivery, aggregation, bot classification, retention, query path, and failure behavior. Justify any separate queue, service, or datastore.
- **Dependencies:** ARC-001, ARC-002, ARC-003
- **Acceptance Criteria:**
  1. The event is created at exactly the approved click boundary.
  2. Duplication, loss, consistency, freshness, and retention guarantees are explicit.
  3. Privacy transformations occur at documented boundaries.
  4. Redirect-path overhead satisfies the approved design target.
  5. Additional infrastructure, if any, has explicit engineer approval.
- **Test Requirements:** Define analytics contract, privacy, aggregation, duplication, loss, backlog, and dependency-failure test cases.
- **Security Considerations:** Minimize event fields, restrict query access, define retention deletion, and avoid sensitive or high-cardinality telemetry.
- **Failure Scenarios:** Events block redirects; retries double-count; buffered data is lost silently; raw personal data leaks through reports.
- **Impacted Components:** `docs/architecture.md`, `docs/api.md`, `DECISIONS.md`, future event, analytics persistence, and query components
- **Human Approval Required:** Yes — architecture and any infrastructure additions require approval before implementation.

### ARC-005 — Define reliability and observability architecture

- **Task ID:** ARC-005
- **Title:** Define reliability and observability architecture
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Specify failure containment, optional caching, service lifecycle, and operational evidence.
- **Description:** Produce failure-mode behavior for dependencies; define timeouts, retries, cache policy if approved, readiness, liveness, shutdown, recovery, metrics, logs, traces, dashboards, and alerts.
- **Dependencies:** ARC-001, ARC-003, ARC-004
- **Acceptance Criteria:**
  1. Each material failure maps to client behavior and an operational signal.
  2. Retryable operations and bounds are explicit.
  3. Any cache has documented TTL, invalidation, negative, stale, and fallback semantics.
  4. Startup, health, shutdown, and buffered-work behavior are specified.
  5. Metrics have bounded dimensions and telemetry follows privacy policy.
- **Test Requirements:** Define fault-injection, lifecycle, cache-consistency, recovery, metric, log-redaction, and alert test scenarios.
- **Security Considerations:** Health endpoints must not expose internals; telemetry must not leak sensitive values; stale data must not bypass lifecycle controls.
- **Failure Scenarios:** Retry storms amplify outage; cache serves expired links; readiness admits unsafe traffic; alerts fail to detect analytics loss.
- **Impacted Components:** `docs/architecture.md`, `docs/security.md`, `docs/performance.md`, future dependency clients, cache, health, and telemetry components
- **Human Approval Required:** Yes — the engineer must approve failure policies and any added infrastructure.

## PHASE 2 - Project Foundation

### FND-001 — Scaffold the approved project

- **Task ID:** FND-001
- **Title:** Scaffold the approved project
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: Yes
- **Intent:** Create only the minimum buildable and testable structure required by the approved architecture.
- **Description:** After explicit implementation authorization, add the approved source, test, configuration, build, and ignore structure and only approved dependencies.
- **Dependencies:** ARC-002, ARC-003, ARC-004, ARC-005
- **Acceptance Criteria:**
  1. The project builds or starts with the approved toolchain.
  2. Only approved dependencies are present with reproducibility metadata.
  3. A minimal automated test passes.
  4. Generated artifacts and secrets are excluded from version control.
  5. Setup and validation commands work in a clean approved environment.
- **Test Requirements:** Run clean dependency resolution, build or compile, minimal test, and repository hygiene checks.
- **Security Considerations:** Pin or lock dependencies as approved; exclude secrets and local artifacts; use safe default binding and debug settings.
- **Failure Scenarios:** Unapproved transitive dependency is added; environment-specific files are committed; scaffold embeds architectural behavior not yet reviewed.
- **Impacted Components:** Repository root, build configuration, source and test directories, ignore rules, `README.md`
- **Human Approval Required:** Yes — approve dependency changes before work and review the scaffold before feature implementation.

### FND-002 — Implement cross-cutting service foundations

- **Task ID:** FND-002
- **Title:** Implement configuration, errors, request context, and baseline logging
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Provide consistent foundations without adding product behavior.
- **Description:** Implement approved configuration loading and validation, safe error mapping, correlation context, and structured baseline logging.
- **Dependencies:** FND-001
- **Acceptance Criteria:**
  1. Required invalid configuration fails early with actionable non-secret diagnostics.
  2. Client error envelopes conform to the approved contract.
  3. Requests receive or propagate approved correlation context.
  4. Logs conform to the approved schema and redaction rules.
  5. No creation, redirect, or analytics behavior is added.
- **Test Requirements:** Add unit and integration tests for valid and invalid configuration, error mapping, correlation propagation, and log redaction.
- **Security Considerations:** Never print secret values; do not trust client correlation data without validation; avoid stack traces in client responses.
- **Failure Scenarios:** Missing configuration is ignored; errors leak internals; malformed correlation values cause log injection.
- **Impacted Components:** Future configuration, middleware, error, request-context, and logging components
- **Human Approval Required:** Yes — engineer review and acceptance are required.

## PHASE 3 - URL Creation

### CRT-001 — Implement destination URL validation

- **Task ID:** CRT-001
- **Title:** Implement destination URL validation
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Enforce the approved URL input and preservation policy independently of transport and storage.
- **Description:** Implement structural parsing, scheme and host rules, length limits, credential and encoding policy, and preservation or normalization behavior.
- **Dependencies:** FND-002, REQ-002
- **Acceptance Criteria:**
  1. Every approved valid URL class is accepted.
  2. Every prohibited class returns the designated validation result.
  3. Accepted input is not altered outside the approved policy.
  4. Validation performs no destination fetch unless separately approved.
- **Test Requirements:** Add table-driven and property tests for valid cases, boundaries, malformed values, schemes, credentials, Unicode, encoding, controls, and maximum size.
- **Security Considerations:** Address unsafe schemes, response-header injection, log injection, Unicode ambiguity, and oversized input.
- **Failure Scenarios:** Parser accepts a prohibited scheme; normalization changes meaning; encoded control characters reach storage.
- **Impacted Components:** Future URL-validation domain component and its unit tests
- **Human Approval Required:** Yes — engineer review of the behavior matrix and implementation is required.

### CRT-002 — Implement mapping persistence

- **Task ID:** CRT-002
- **Title:** Implement authoritative mapping persistence
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: Yes
- **Intent:** Persist and retrieve URL mappings with approved consistency and lifecycle guarantees.
- **Description:** Implement the approved schema, migration, repository operations, uniqueness constraints, transactions, and error classification.
- **Dependencies:** FND-001, ARC-003
- **Acceptance Criteria:**
  1. A mapping can be atomically created and retrieved.
  2. Authoritative short-code uniqueness is enforced under concurrent writes.
  3. Persistence failure is distinct from not found.
  4. Lifecycle and idempotency fields behave according to approved decisions.
  5. Migration and rollback behavior is reproducible.
- **Test Requirements:** Use the approved real datastore for integration tests covering create, lookup, uniqueness, concurrency, rollback, lifecycle data, and dependency errors.
- **Security Considerations:** Use parameterized datastore operations, minimum data access, safe migration handling, and no destination values in error logs.
- **Failure Scenarios:** Partial transaction creates orphaned state; collision overwrites data; datastore outage is returned as not found.
- **Impacted Components:** Future database schema, migrations, mapping model, and persistence component
- **Human Approval Required:** Yes — approve schema changes before work and accept migration evidence afterward.

### CRT-003 — Implement short-code generation

- **Task ID:** CRT-003
- **Title:** Implement approved short-code generation and collision handling
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Generate codes that meet approved format, entropy, and collision-safety requirements.
- **Description:** Implement the approved generator, code validation, bounded collision retry, and exhaustion behavior integrated with authoritative uniqueness.
- **Dependencies:** CRT-002, REQ-002
- **Acceptance Criteria:**
  1. Generated codes always match the approved alphabet and length policy.
  2. The approved randomness or sequencing source is used.
  3. A forced collision never overwrites an existing mapping.
  4. Retries stop at the approved bound and return the approved internal failure.
- **Test Requirements:** Add deterministic generator tests, high-volume property tests, forced-collision tests, and exhaustion-bound tests using injectable approved test seams.
- **Security Considerations:** Avoid predictable output when unpredictability is required; do not log unused candidate codes or sensitive generator state.
- **Failure Scenarios:** Weak randomness increases enumeration; retry loop is unbounded; collision check is not atomic.
- **Impacted Components:** Future short-code generator, mapping persistence integration, and unit/property tests
- **Human Approval Required:** Yes — engineer review of code-space evidence and implementation is required.

### CRT-004 — Implement URL creation API

- **Task ID:** CRT-004
- **Title:** Implement URL creation API
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Expose approved URL creation behavior through the API contract.
- **Description:** Orchestrate request validation, idempotency or duplicate behavior, code generation, durable persistence, response construction, and error mapping.
- **Dependencies:** CRT-001, CRT-002, CRT-003, ARC-002
- **Acceptance Criteria:**
  1. Valid requests return the approved status and schema with a durably stored mapping.
  2. Invalid requests store nothing and return the approved client error.
  3. Duplicate and retry requests follow approved policy.
  4. Collision exhaustion and datastore failure cannot produce false success.
  5. The short base URL comes only from approved configuration.
- **Test Requirements:** Add API contract, integration, concurrency, idempotency or duplicate, request-size, collision, and datastore-failure tests.
- **Security Considerations:** Enforce body limits and access policy; do not trust host headers for public URLs; redact destinations from unsafe logs.
- **Failure Scenarios:** Response is returned before durability; client retry creates unexpected duplicates; untrusted headers poison returned short URLs.
- **Impacted Components:** Future creation handler, service orchestration, API models, configuration, and persistence
- **Human Approval Required:** Yes — engineer review against FR-001 through FR-003 and AC-001 through AC-005 is required.

## PHASE 4 - Redirect

### RED-001 — Implement resolution and lifecycle evaluation

- **Task ID:** RED-001
- **Title:** Implement short resolution and lifecycle evaluation
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Resolve codes and classify active, unknown, inactive, expired, and dependency-failed outcomes correctly.
- **Description:** Implement short-code input validation, authoritative lookup, approved expiration boundary, and internal outcome classification independent of HTTP redirect formatting.
- **Dependencies:** CRT-002, REQ-002
- **Acceptance Criteria:**
  1. Active mappings resolve to the approved stored destination.
  2. Malformed, unknown, inactive, expired, and dependency failure outcomes remain distinct as approved.
  3. Expiration is correct immediately before, at, and after the approved boundary.
  4. Datastore failure is never converted to unknown.
- **Test Requirements:** Add unit and integration tests for code format, every outcome, clock boundaries, concurrent lifecycle changes if supported, and datastore failure.
- **Security Considerations:** Bound short-code length before lookup; use an approved controllable clock; avoid disclosing private state through errors.
- **Failure Scenarios:** Expired mapping remains active; clock mismatch changes boundary behavior; outage returns false not found.
- **Impacted Components:** Future resolver, lifecycle policy, clock abstraction, and mapping persistence
- **Human Approval Required:** Yes — engineer review of lifecycle behavior and boundary tests is required.

### RED-002 — Implement redirect API

- **Task ID:** RED-002
- **Title:** Implement redirect API and response semantics
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Return the approved redirect or error response without changing the stored destination.
- **Description:** Map resolver outcomes to the approved status codes, `Location`, cache-control behavior, headers, and error schema.
- **Dependencies:** RED-001, ARC-002
- **Acceptance Criteria:**
  1. Active codes return the approved redirect status and exact approved destination representation.
  2. Other outcomes return their approved statuses without `Location` pointing to an unintended destination.
  3. Response caching follows approved redirect and lifecycle policy.
  4. Only approved request methods are accepted.
- **Test Requirements:** Add contract tests for status, method handling, `Location`, cache headers, malformed codes, unknown and expired outcomes, and dependency failure.
- **Security Considerations:** Prevent response splitting, unsafe method forwarding, host confusion, and leakage through error responses.
- **Failure Scenarios:** Permanent cache bypasses analytics; method semantics differ from the contract; encoded destination injects a header.
- **Impacted Components:** Future redirect handler, API routing, resolver, and response middleware
- **Human Approval Required:** Yes — engineer review against FR-004 through FR-006 and AC-006 through AC-009 is required.

## PHASE 5 - Analytics

### ANL-001 — Implement analytics event capture

- **Task ID:** ANL-001
- **Title:** Implement approved click-event capture
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Create an analytics event only at the approved click boundary.
- **Description:** Integrate event creation with redirect processing, including approved request classification, bot or prefetch indicators, field minimization, and correlation behavior.
- **Dependencies:** RED-002, ARC-004
- **Acceptance Criteria:**
  1. Exactly the approved request outcomes create events.
  2. Refresh, retry, prefetch, and suspected-bot inputs are represented as approved.
  3. Prohibited fields are never included.
  4. Event capture does not alter redirect semantics beyond approved overhead and failure policy.
- **Test Requirements:** Add unit and integration tests for every event-producing and non-event-producing redirect outcome and privacy field rules.
- **Security Considerations:** Avoid raw IP or sensitive URL/referrer data unless explicitly approved; validate untrusted headers before classification.
- **Failure Scenarios:** Unknown links count as clicks; bot signals are treated as certain; personal data enters the event payload.
- **Impacted Components:** Future redirect integration, analytics event model, classification, and privacy transformation
- **Human Approval Required:** Yes — engineer review against the approved click and privacy decisions is required.

### ANL-002 — Implement analytics processing and storage

- **Task ID:** ANL-002
- **Title:** Implement analytics processing, aggregation, and retention
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: Yes
- **Intent:** Process and retain events within approved consistency, loss, duplication, and privacy guarantees.
- **Description:** Implement the approved delivery, persistence, aggregation, retry, failure, and deletion behavior without silently strengthening delivery claims.
- **Dependencies:** ANL-001, ARC-004
- **Acceptance Criteria:**
  1. Stored or aggregated analytics matches the approved data model and time boundaries.
  2. Duplication and loss remain within approved tolerances and are observable.
  3. Retention and deletion behavior is enforceable.
  4. Analytics failure affects redirects exactly as approved.
  5. Buffered work follows approved startup and shutdown semantics.
- **Test Requirements:** Add persistence, aggregation, duplicate, retry, event-loss, backlog, retention, deletion, shutdown, and dependency-failure tests.
- **Security Considerations:** Restrict analytics storage access, protect retained data, and ensure retries or dead-letter behavior do not retain prohibited fields.
- **Failure Scenarios:** Retry double-counts; buffer loss is silent; retention cleanup deletes incorrect data; analytics outage propagates to redirects.
- **Impacted Components:** Future analytics processor, analytics schema or aggregation store, retention jobs, and redirect failure boundary
- **Human Approval Required:** Yes — approve data and infrastructure changes before work and accept reliability evidence afterward.

### ANL-003 — Implement analytics retrieval API

- **Task ID:** ANL-003
- **Title:** Implement analytics retrieval API
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Expose only the approved analytics with approved authorization and accuracy semantics.
- **Description:** Implement analytics queries, aggregation windows, freshness representation, bot breakdown, access checks, response schemas, and errors.
- **Dependencies:** ANL-002, ARC-002
- **Acceptance Criteria:**
  1. Results match approved aggregation and time-boundary rules.
  2. Authorization prevents access outside approved link ownership or public behavior.
  3. Empty, unknown, unauthorized, and expired cases return approved outcomes.
  4. Responses do not claim unique-human or exactly-once accuracy unless approved.
- **Test Requirements:** Add contract, authorization, aggregation, time-boundary, freshness, empty-state, and privacy tests.
- **Security Considerations:** Prevent identifier enumeration, cross-owner data access, and exposure of raw events or prohibited dimensions.
- **Failure Scenarios:** Analytics for another owner is exposed; stale data is presented as current; aggregation crosses the wrong time boundary.
- **Impacted Components:** Future analytics query service, API handler, authorization, and response models
- **Human Approval Required:** Yes — engineer review against FR-008 and AC-013 is required.

## PHASE 6 - Reliability

### REL-IMPL-001 — Implement dependency timeout, retry, and error policies

- **Task ID:** REL-IMPL-001
- **Title:** Implement bounded dependency behavior
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Contain datastore and analytics failures without incorrect client outcomes or retry amplification.
- **Description:** Apply approved timeouts, safe retry bounds, error classification, and fallback behavior to runtime dependency clients.
- **Dependencies:** CRT-004, RED-002, ANL-002, ARC-005
- **Acceptance Criteria:**
  1. Each dependency operation uses its approved timeout.
  2. Only approved safe operations retry and retries are bounded.
  3. Create, redirect, and analytics failures map to approved behavior.
  4. Dependency failure remains observable and distinct from client errors.
- **Test Requirements:** Add deterministic timeout, transient error, permanent error, retry-exhaustion, and outage tests.
- **Security Considerations:** Do not expose dependency addresses or credentials; prevent attacker-controlled requests from triggering excessive retry work.
- **Failure Scenarios:** Retry storm worsens an outage; partial write is retried unsafely; database timeout becomes not found.
- **Impacted Components:** Future datastore and analytics clients, service orchestration, error mapping, and telemetry
- **Human Approval Required:** Yes — engineer review against REL-003 through REL-006 is required.

### REL-IMPL-002 — Implement cache policy if approved

- **Task ID:** REL-IMPL-002
- **Title:** Implement approved redirect cache behavior
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: Yes
- **Intent:** Meet approved performance or availability needs without weakening authoritative lifecycle correctness.
- **Description:** If caching was approved and justified, implement keys, values, positive or negative behavior, TTL, invalidation, stale policy, outage fallback, and load protection. Otherwise close this task as not applicable.
- **Dependencies:** RED-001, ARC-005, approved performance justification
- **Acceptance Criteria:**
  1. Hit and miss behavior is equivalent to approved authoritative resolution.
  2. Cache lifetime never extends link validity beyond expiration or inactive state.
  3. Negative, stale, invalidation, and fallback behavior matches the decision record.
  4. Cache failure does not create false not-found results or unsafe datastore load.
- **Test Requirements:** Add hit, miss, expiration, invalidation, negative, outage, stale, concurrency, and load-protection tests.
- **Security Considerations:** Do not cache prohibited personal data; protect shared cache access; prevent cache-key injection or tenant collision.
- **Failure Scenarios:** Expired link remains cached; cache outage overloads datastore; negative cache conceals newly created mapping.
- **Impacted Components:** Future cache client, resolver, configuration, lifecycle policy, and telemetry
- **Human Approval Required:** Yes — explicit approval is required before adding cache infrastructure or a dependency.

### REL-IMPL-003 — Implement service lifecycle and recovery behavior

- **Task ID:** REL-IMPL-003
- **Title:** Implement health, startup, shutdown, and approved recovery behavior
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Make the service safe to operate and terminate under approved dependency and buffered-work policies.
- **Description:** Implement distinct readiness and liveness, startup checks, and a 30-second graceful request drain. The approved baseline has no analytics buffer and defers backup or restore behavior; adding either requires a new decision.
- **Dependencies:** REL-IMPL-001, REL-IMPL-002 if applicable, ARC-005
- **Acceptance Criteria:**
  1. Liveness and readiness reflect their approved meanings.
  2. Startup cannot accept unsafe traffic before required dependencies are ready.
  3. Shutdown stops new traffic and gives active requests at most 30 seconds to drain; no analytics-buffer behavior is introduced.
  4. Readiness recovers within 30 seconds after the required datastore becomes healthy in the approved reference environment.
- **Test Requirements:** Add process-lifecycle, dependency-readiness and recovery, graceful-shutdown, forced-termination, and no-buffer conformance tests.
- **Security Considerations:** Health output must not reveal internals; backup and restore material must use approved protection and access control.
- **Failure Scenarios:** Instance remains ready while unsafe; shutdown loses events outside tolerance; health endpoint discloses configuration.
- **Impacted Components:** Future health handlers, process lifecycle, deployment configuration, and lifecycle tests
- **Human Approval Required:** Yes — engineer review against REL-010 and REL-011 is required.

### OBS-IMPL-001 — Implement operational metrics and alert definitions

- **Task ID:** OBS-IMPL-001
- **Title:** Implement operational metrics and alert definitions
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Make approved service outcomes, dependency health, lifecycle state, and material failures measurable without leaking sensitive or high-cardinality data.
- **Description:** Implement bounded request, creation, redirect, analytics, dependency, rate-limit, saturation, and lifecycle metrics using the observability stack approved by ARC-005. Add alert-rule artifacts for the thresholds approved by RDR-004 and document dashboard, routing, ownership, and runbook gaps without inventing operational infrastructure.
- **Dependencies:** ARC-005, CRT-004, RED-002, ANL-002, REL-IMPL-001, REL-IMPL-003
- **Acceptance Criteria:**
  1. Request counts, latency distributions, and errors use normalized operation and outcome dimensions.
  2. Creation, redirect, analytics, dependency, rate-limit, saturation, and lifecycle outcomes required by OBS-001 through OBS-005 and OBS-008 are emitted.
  3. Metric labels use a reviewed allowlist and exclude sensitive or unbounded identifiers.
  4. Alert definitions match the thresholds and evaluation windows approved by RDR-004 and pass synthetic rule evaluation.
  5. Dashboard coverage, alert routing, ownership, and any missing runbook work are documented factually.
- **Test Requirements:** Add tests for metric emission and outcome classification, label allowlisting and cardinality, privacy exclusions, lifecycle transitions, and alert threshold, duration, firing, and recovery behavior.
- **Security Considerations:** Metric labels and alert payloads must not contain destination URLs, short codes, credentials, IP addresses, user agents, referrers, correlation IDs, or other sensitive or unbounded values.
- **Failure Scenarios:** An operation omits or misclassifies its outcome; an attacker creates unbounded label cardinality; sensitive data reaches metrics or alerts; an alert fires too early, fails to fire, or does not recover; observability-backend failure changes application behavior.
- **Impacted Components:** Future request and service instrumentation, metrics configuration, alert rules, observability documentation, and tests
- **Human Approval Required:** Yes — engineer review of metric semantics, label bounds, privacy controls, and alert thresholds is required.

## PHASE 7 - Security

### SEC-IMPL-001 — Create threat model and harden exposed inputs

- **Task ID:** SEC-IMPL-001
- **Title:** Threat-model and harden URL-shortener inputs and outputs
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Verify that approved controls address realistic trust-boundary threats.
- **Description:** Model assets, actors, entry points, data flows, threats, controls, and residual risk; then implement or verify input, output, transport, and safe-error controls within scope.
- **Dependencies:** CRT-004, RED-002, ANL-003, ARC-005
- **Acceptance Criteria:**
  1. Threat model covers creation, redirect, analytics, storage, telemetry, and operational interfaces.
  2. SEC-001 through SEC-004, SEC-008, SEC-010, and SEC-012 as applicable have verified controls.
  3. Residual threats have an owner and disposition.
  4. No destination fetch exists unless SSRF controls were separately approved.
- **Test Requirements:** Add malicious URL, encoding, control-character, header-injection, malformed-code, response-disclosure, and transport-boundary tests.
- **Security Considerations:** This task owns the explicit threat analysis; rejected controls and accepted residual risks must remain traceable.
- **Failure Scenarios:** Unsafe scheme bypasses validation; response splitting occurs; an optional preview feature introduces SSRF without review.
- **Impacted Components:** `docs/security.md`, validation, API handlers, error responses, transport and proxy configuration
- **Human Approval Required:** Yes — the engineer must approve the threat model, controls, and residual risks.

### SEC-IMPL-002 — Implement access control, rate limiting, and abuse controls

- **Task ID:** SEC-IMPL-002
- **Title:** Implement approved identity, authorization, rate-limit, and abuse policies
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Prevent unauthorized analytics or management access and bound abusive use.
- **Description:** Implement only the approved authentication, authorization, trusted-client identity, quota, burst, `429`, recovery, and abuse-monitoring behavior.
- **Dependencies:** ANL-003, REQ-004, SEC-IMPL-001
- **Acceptance Criteria:**
  1. Every protected operation enforces the approved identity and authorization policy.
  2. Cross-owner or cross-tenant access is denied if ownership exists.
  3. Rate-limit boundaries and recovery match approved policy.
  4. Rejections use the approved response and retry metadata.
  5. Rate-limit state and telemetry have approved bounds.
- **Test Requirements:** Add authentication, authorization, privilege-boundary, rate boundary, concurrency, spoofed-proxy, bypass, and recovery tests.
- **Security Considerations:** Validate trusted proxy configuration, avoid raw credential logging, and resist memory or storage exhaustion in limit state.
- **Failure Scenarios:** Client spoofs identity through forwarding headers; analytics leaks across owners; distributed limit permits unacceptable bursts.
- **Impacted Components:** Future authentication and authorization middleware, rate limiter, API handlers, proxy configuration, and telemetry
- **Human Approval Required:** Yes — approve identity and enforcement design before implementation and residual limitations afterward.

### SEC-IMPL-003 — Validate secrets, dependencies, privacy, and data lifecycle

- **Task ID:** SEC-IMPL-003
- **Title:** Complete security and privacy hardening review
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Confirm the complete system does not exceed approved data or dependency risk.
- **Description:** Review secrets, dependency provenance, logs, metrics, traces, analytics, retention, deletion, backup, and deployment defaults; fix only issues within approved task scope and track others separately.
- **Dependencies:** SEC-IMPL-001, SEC-IMPL-002, REL-IMPL-003
- **Acceptance Criteria:**
  1. No secret or prohibited personal data appears in source, configuration examples, responses, telemetry, or unauthorized analytics.
  2. Approved retention and deletion behavior is verified across primary, buffered, cached, and backup data.
  3. Dependency checks have no unresolved blocking finding.
  4. Deployment defaults do not enable debug leakage or unsafe exposure.
  5. Residual risks are documented and engineer-dispositioned.
- **Test Requirements:** Run approved secret, dependency, static, privacy-field, retention, deletion, log, metric, trace, and configuration checks.
- **Security Considerations:** Covers SEC-005 through SEC-013 and validates defense consistency across components.
- **Failure Scenarios:** Secret enters logs; deleted analytics remains in a queue; dependency vulnerability is ignored; debug mode is enabled in deployment.
- **Impacted Components:** Entire repository, dependencies, configuration, telemetry, analytics storage, caches, backups, `docs/security.md`
- **Human Approval Required:** Yes — the engineer must review findings and explicitly accept residual risk.

## PHASE 8 - Testing

### TST-001 — Complete unit and property testing

- **Task ID:** TST-001
- **Title:** Complete unit and property-test coverage
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Validate pure rules and invariants independently and reproducibly.
- **Description:** Complete unit and property tests for validation, short-code generation, lifecycle, classification, aggregation, configuration, and error behavior.
- **Dependencies:** All applicable feature implementations through Phase 7
- **Acceptance Criteria:**
  1. Each approved pure-behavior requirement has direct test coverage.
  2. Boundary and property tests cover documented invariants.
  3. Tests are deterministic and independent of external network access.
  4. Approved coverage goals are met or gaps are dispositioned.
- **Test Requirements:** Execute the clean unit/property test command repeatedly and verify consistent results and useful failure diagnostics.
- **Security Considerations:** Include adversarial inputs without embedding live secrets, personal data, or dangerous external targets.
- **Failure Scenarios:** Flaky generators hide collisions; tests duplicate implementation rather than behavior; boundary cases remain untested.
- **Impacted Components:** All unit-test suites and test fixtures
- **Human Approval Required:** Yes — engineer review of requirement coverage and exclusions is required.

### TST-002 — Complete contract, integration, and concurrency testing

- **Task ID:** TST-002
- **Title:** Complete API, datastore, analytics, and concurrency validation
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Validate component interactions and external behavior using approved real integrations.
- **Description:** Complete API contract, migration, persistence, analytics, cache-if-applicable, authorization, and concurrency tests.
- **Dependencies:** TST-001
- **Acceptance Criteria:**
  1. Every documented API outcome has an automated contract test.
  2. Approved real datastore and infrastructure integrations are exercised.
  3. Concurrent creation cannot violate uniqueness or approved idempotency behavior.
  4. Time boundaries, retention, invalidation, and authorization are validated.
  5. Tests pass from documented clean setup commands.
- **Test Requirements:** Run the complete integration suite with isolated fixtures, migration setup and teardown, concurrency workers, and contract-schema checks.
- **Security Considerations:** Use synthetic data, isolated credentials, least-privileged test services, and no outbound requests to unapproved destinations.
- **Failure Scenarios:** Tests pass only against mocks; shared state causes order dependence; concurrent defect remains hidden.
- **Impacted Components:** API, persistence, analytics, optional cache, authorization, migration, and integration-test suites
- **Human Approval Required:** Yes — engineer review of integration fidelity and results is required.

### TST-003 — Complete fault, recovery, and security testing

- **Task ID:** TST-003
- **Title:** Complete failure-mode, recovery, security, and privacy validation
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: No
- **Intent:** Demonstrate that the system fails according to the approved policies and retains no hidden blocking risk.
- **Description:** Inject dependency failures, delays, termination, malformed and malicious input, access violations, privacy checks, and recovery operations in scope.
- **Dependencies:** TST-002, SEC-IMPL-003, REL-IMPL-003
- **Acceptance Criteria:**
  1. Database, analytics, cache, timeout, retry, shutdown, and recovery scenarios behave as approved.
  2. No false success, silent overwrite, false not-found, or unobserved loss outside tolerance occurs.
  3. Security and privacy controls withstand the approved abuse cases.
  4. Each failure emits the expected safe response and operational evidence.
  5. Residual limitations are documented and approved.
- **Test Requirements:** Execute the approved fault-injection and security suite and preserve reproducible commands, environment, results, and diagnostics.
- **Security Considerations:** Keep destructive fault tests isolated; do not expose test credentials or send malicious payloads to external systems.
- **Failure Scenarios:** Fault injection damages shared data; recovery appears successful but loses mappings; logs expose attack payloads or secrets.
- **Impacted Components:** Full service, test environment, dependency clients, telemetry, recovery procedures, and security test suites
- **Human Approval Required:** Yes — the engineer must review evidence and residual limitations.

## PHASE 9 - Brownfield Change

### BWF-001 — Characterize and approve a post-baseline change

- **Task ID:** BWF-001
- **Title:** Analyze a genuine brownfield change request
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Depends on selected change
- **Intent:** Demonstrate safe reasoning about an existing implementation without manufacturing evidence.
- **Description:** After a working baseline exists, select a real assessment change or defect, inspect current behavior and tests, identify affected requirements and components, and propose the smallest safe change. Do not implement it in this task.
- **Dependencies:** TST-002 and a genuine engineer-supplied or discovered change request
- **Acceptance Criteria:**
  1. Existing behavior is reproduced and documented.
  2. Desired behavior, scope, non-scope, compatibility impact, and acceptance criteria are explicit.
  3. Impact analysis identifies code, data, API, test, security, and operational consequences.
  4. Alternatives and rollback are documented.
  5. The engineer approves or rejects the change plan.
- **Test Requirements:** Add or propose a characterization test that fails only for the approved desired change, without altering production behavior in this task.
- **Security Considerations:** Reassess trust boundaries, authorization, privacy, dependencies, and migrations affected by the change.
- **Failure Scenarios:** Existing behavior is misunderstood; change breaks clients or stored data; assessment evidence is artificially invented.
- **Impacted Components:** To be identified from the selected change; `TASKS.md`, `DECISIONS.md`, `TRACEABILITY.md`
- **Human Approval Required:** Yes — required before any brownfield edit; high-impact approval applies if the selected change meets that definition.

### BWF-002 — Implement and validate the approved brownfield change

- **Task ID:** BWF-002
- **Title:** Execute the approved brownfield change
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: Depends on BWF-001
- **Intent:** Make the smallest approved modification while preserving unrelated behavior.
- **Description:** Implement only the BWF-001 scope, retain characterization coverage, add focused regression tests, and update affected documentation and traceability.
- **Dependencies:** BWF-001 approved
- **Acceptance Criteria:**
  1. The approved new behavior passes its focused tests.
  2. Characterized and unrelated behavior remains unchanged.
  3. Compatibility and migration behavior matches the approved plan.
  4. Documentation and traceability identify AI-generated, edited, accepted, and rejected output.
  5. Rollback remains possible as approved.
- **Test Requirements:** Run focused characterization and regression tests plus the full applicable suite; perform migration or rollback validation if required.
- **Security Considerations:** Verify that the change introduces no new unauthorized input path, data exposure, dependency, or weakened control.
- **Failure Scenarios:** Scope creep alters unrelated behavior; regression suite misses compatibility break; rollback cannot restore prior state.
- **Impacted Components:** Components identified and approved in BWF-001
- **Human Approval Required:** Yes — approve approach before work when high-impact and accept the final diff and evidence afterward.

## PHASE 10 - Ambiguous Requirement

### AMB-TASK-001 — Analyze a newly introduced ambiguous requirement

- **Task ID:** AMB-TASK-001
- **Title:** Normalize and resolve a new ambiguous requirement
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Depends on ambiguity
- **Intent:** Demonstrate that ambiguity is surfaced and decided rather than silently implemented.
- **Description:** For a genuine new or changed requirement, document ambiguity, context, possible interpretations, effects, recommendation, questions, risks, and testable outcomes. Do not implement it in this task.
- **Dependencies:** BWF-002 or an engineer-approved reason to run independently; a genuine ambiguous requirement
- **Acceptance Criteria:**
  1. Facts, assumptions, recommendations, and decisions are clearly separated.
  2. At least two credible interpretations and their impacts are documented.
  3. No source change encodes an unapproved interpretation.
  4. The engineer approves, modifies, rejects, or defers the recommended interpretation.
  5. Affected requirements, decisions, tasks, risks, and acceptance criteria are updated.
- **Test Requirements:** Draft discriminating examples or tests showing how interpretations produce different observable outcomes.
- **Security Considerations:** Identify whether any interpretation expands data collection, access, attack surface, dependency use, or failure impact.
- **Failure Scenarios:** Recommendation is mistaken for approval; ambiguity is resolved inside code review; chosen interpretation conflicts with existing API or data.
- **Impacted Components:** `ENGINEERING_PLAN.md`, `DECISIONS.md`, `TASKS.md`, `TRACEABILITY.md`, components identified by analysis
- **Human Approval Required:** Yes — the engineer alone selects or rejects the interpretation.

### AMB-TASK-002 — Implement the approved interpretation

- **Task ID:** AMB-TASK-002
- **Title:** Implement and validate the resolved ambiguous requirement
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: Depends on AMB-TASK-001
- **Intent:** Implement only the interpretation explicitly selected by the engineer.
- **Description:** Make a separately reviewed change using the approved acceptance criteria and document rejected interpretations and resulting behavior.
- **Dependencies:** AMB-TASK-001 approved
- **Acceptance Criteria:**
  1. Implementation matches only the approved interpretation.
  2. Discriminating tests prove the selected behavior and reject incompatible interpretations.
  3. Existing contract and data compatibility is preserved or changed only as approved.
  4. Documentation and traceability record the decision and AI/engineer disposition.
- **Test Requirements:** Run focused interpretation tests, all impacted suites, and any required compatibility, migration, security, or fault tests.
- **Security Considerations:** Implement and validate the security controls identified in AMB-TASK-001 before exposure.
- **Failure Scenarios:** Code implements a hybrid interpretation; old behavior regresses; rejected AI output is not traceable.
- **Impacted Components:** Components approved in AMB-TASK-001
- **Human Approval Required:** Yes — pre-implementation approval is required if high-impact; final engineer acceptance is always required.

## PHASE 11 - Performance Validation

### PERF-VAL-001 — Define and implement the performance workload

- **Task ID:** PERF-VAL-001
- **Title:** Create a reproducible performance-validation workload
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: No
- **Intent:** Measure approved service objectives using a representative, reviewable method.
- **Description:** Define data cardinality, creation/redirect/analytics mix, concurrency, warm-up, duration, environment, percentiles, error limits, cache state if applicable, and reproducible workload tooling.
- **Dependencies:** TST-003, approved PERF requirements
- **Acceptance Criteria:**
  1. Workload maps to approved sustained, peak, data-size, and latency targets.
  2. Environment and measurement boundaries are documented.
  3. Results capture throughput, percentiles, errors, saturation, and dependency behavior.
  4. Test data is synthetic and repeatable.
  5. The engineer approves the methodology before results are treated as evidence.
- **Test Requirements:** Dry-run the workload at low volume, validate measurements against operational metrics, and prove cleanup or isolation behavior.
- **Security Considerations:** Use isolated targets and synthetic URLs; do not stress shared or external systems; protect performance-environment credentials.
- **Failure Scenarios:** Workload is unrepresentative; coordinated omission hides latency; test traffic affects external services; cleanup destroys unrelated data.
- **Impacted Components:** Performance-test tooling, test environment, `docs/performance.md`, telemetry
- **Human Approval Required:** Yes — the engineer must approve workload safety and representativeness.

### PERF-VAL-002 — Execute performance validation and disposition findings

- **Task ID:** PERF-VAL-002
- **Title:** Execute load tests and review bottlenecks
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes for any resulting architecture change
- **Intent:** Determine whether the system meets approved targets and whether further optimization is justified.
- **Description:** Run the approved workload, analyze bottlenecks and saturation, compare results with PERF-001 through PERF-008, and propose separate remediation tasks without silently changing architecture.
- **Dependencies:** PERF-VAL-001
- **Acceptance Criteria:**
  1. Results include environment, version, workload, throughput, percentiles, error rate, and resource observations.
  2. Each approved performance criterion is marked met or unmet with evidence.
  3. Cache or infrastructure value is supported or rejected by evidence.
  4. Every unmet target has an approved remediation, accepted limitation, or scope change.
  5. Repeated runs show an understood level of variation.
- **Test Requirements:** Execute repeated sustained and peak runs plus approved degradation scenarios; preserve commands and summarized results.
- **Security Considerations:** Maintain environment isolation and credential hygiene; ensure performance logging does not capture sensitive payloads.
- **Failure Scenarios:** One favorable run is cherry-picked; optimization weakens correctness; load test causes service or datastore damage.
- **Impacted Components:** Performance environment, telemetry, `docs/performance.md`, possible follow-up task backlog
- **Human Approval Required:** Yes — the engineer must approve findings and any optimization or infrastructure follow-up before implementation.

## PHASE 12 - Final Review

### FIN-001 — Complete documentation and traceability

- **Task ID:** FIN-001
- **Title:** Reconcile documentation and end-to-end traceability
- **Classification:** AI-friendly: Yes; Engineer-heavy: No; High-impact: No
- **Intent:** Ensure the delivered evidence matches the approved and observed system.
- **Description:** Finalize API, architecture, security, testing, performance, operating, decision, prompt, task, and traceability records.
- **Dependencies:** PERF-VAL-002, BWF-002, AMB-TASK-002, or approved not-applicable dispositions
- **Acceptance Criteria:**
  1. Every approved requirement maps to decisions, tasks, implementation, tests, and evidence.
  2. Every ambiguity is decided, deferred, or excluded without contradiction.
  3. Documentation examples and commands match validated behavior.
  4. AI-generated, edited, accepted, and rejected work is distinguishable.
  5. No secret or prohibited personal data exists in documentation or trace records.
- **Test Requirements:** Run link, identifier, command, schema-example, and documentation consistency checks; manually sample trace chains end to end.
- **Security Considerations:** Redact secrets, private endpoints, credentials, personal data, and dangerous copy-paste examples.
- **Failure Scenarios:** Documentation claims unimplemented behavior; rejected AI output appears approved; trace links are incomplete.
- **Impacted Components:** `README.md`, `ENGINEERING_PLAN.md`, `DECISIONS.md`, `TASKS.md`, `PROMPT_LOG.md`, `TRACEABILITY.md`, `AI_REVIEW.md`, `docs/`
- **Human Approval Required:** Yes — engineer review of documentation accuracy and traceability is required.

### FIN-002 — Perform final AI-assisted code and risk review

- **Task ID:** FIN-002
- **Title:** Perform final code, test, security, reliability, and risk review
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: No
- **Intent:** Identify remaining defects or unsupported claims before final validation.
- **Description:** Review implementation and evidence for correctness, concurrency, security, privacy, reliability, performance, maintainability, tests, documentation, and requirement conformance. The engineer independently dispositions findings.
- **Dependencies:** FIN-001
- **Acceptance Criteria:**
  1. Findings include severity, evidence, affected requirement or risk, and proposed action.
  2. The engineer accepts, modifies, defers, or rejects every material AI finding.
  3. Blocking findings are resolved and revalidated or explicitly prevent completion.
  4. Review disposition is recorded in `AI_REVIEW.md` and traceability records.
- **Test Requirements:** Re-run focused tests for corrected findings and all impacted suites; verify rejected findings require no change.
- **Security Considerations:** Give explicit attention to input handling, access control, privacy, secrets, dependency risk, failure modes, and unsafe defaults.
- **Failure Scenarios:** AI review is treated as approval; false positives cause risky churn; material finding is deferred without risk ownership.
- **Impacted Components:** Entire codebase, tests, documentation, `AI_REVIEW.md`, `TRACEABILITY.md`
- **Human Approval Required:** Yes — the engineer owns finding disposition and review acceptance.

### FIN-003 — Execute final validation and engineer acceptance

- **Task ID:** FIN-003
- **Title:** Execute clean validation and record final decision
- **Classification:** AI-friendly: Yes; Engineer-heavy: Yes; High-impact: Yes
- **Intent:** Determine whether the project satisfies its Definition of Done.
- **Description:** Validate from a clean approved environment, review remaining limitations and risks, and record an accepted, conditionally accepted, or rejected outcome.
- **Dependencies:** FIN-002 and resolution of all blocking findings
- **Acceptance Criteria:**
  1. All required build, test, security, fault, and performance commands pass from a clean environment.
  2. Every approved requirement and Definition of Done item has evidence or an explicit blocking disposition.
  3. Deferred work, known limitations, and residual risks are documented with owners.
  4. The engineer records the final outcome and any conditions.
  5. No AI statement is treated as final approval.
- **Test Requirements:** Execute the complete documented validation suite, archive or summarize results, and independently spot-check critical creation, redirect, analytics, security, and failure paths.
- **Security Considerations:** Confirm no validation step changes or exposes production data, secrets, or external systems; reassess accepted residual risk.
- **Failure Scenarios:** Dirty environment hides missing setup; flaky tests are ignored; documentation and implementation versions differ; conditional acceptance lacks tracked conditions.
- **Impacted Components:** Entire repository, validation environment, all engineering and traceability documents
- **Human Approval Required:** Yes — only the engineer may make and record the final acceptance decision.

## Current execution gate

The next proposed task is **REQ-001**. No architecture or implementation task is ready until its documented dependencies and approval gates are satisfied.
