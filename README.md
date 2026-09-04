# URL Shortener

<!-- Author: Manoj reddy <amireddymanojreddy@gmail.com> -->
<!-- Since: 2026-09-03 -->

Production-oriented URL shortener prototype. The project is being implemented as a sequence of explicitly approved engineering tasks.

## Project overview

This repository contains a Spring Boot-based URL shortener with:

- short-link creation for approved destination URLs
- redirect resolution for stored short codes
- analytics capture and aggregate retrieval
- health, observability, and reliability support
- documentation-driven task tracking and traceability

The codebase is intentionally governed by the engineering plan, task backlog, decision log, and traceability records in this repository. Those documents describe what is approved, what is still pending, and how implementation evidence is tracked.

## Current scope

FND-001 provides the buildable project foundation:

- Java 21
- Gradle 9.7.1 wrapper
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA with Boot-managed Hibernate
- PostgreSQL JDBC driver at runtime
- PostgreSQL runtime datastore, exercised locally through Docker Compose
- Spring Boot test support

FND-002 adds foundational configuration validation, safe API error envelopes, request correlation, and structured JSON logging. No URL creation, redirect, persistence mapping, analytics, rate limiting, or other product behavior is implemented yet.

## Foundational configuration

Application startup requires a public base URL supplied by trusted deployment configuration. Spring Boot maps the environment variable below to `url-shortener.public-base-url`:

```shell
export URL_SHORTENER_PUBLIC_BASE_URL=https://sho.rt
```

The value must be an absolute HTTP or HTTPS origin without credentials, a non-root path, a query, or a fragment. Invalid or missing values stop startup with a diagnostic that does not include the configured value.

Application logs use structured JSON with service identity and operation, outcome, and correlation fields where applicable. `APP_VERSION` supplies the deployed service version and defaults to `development` locally. Requests accept a safe `X-Request-ID` containing 1–64 ASCII letters, digits, periods, underscores, or hyphens; otherwise the application generates a new identifier. The identifier is returned in the response and included in request-scoped logging context.

## Service lifecycle

`GET /health/live` reports process liveness without consulting PostgreSQL. `GET /health/ready` reports `200` only when PostgreSQL accepts a connection validation and otherwise reports `503`; both responses contain only a minimal status value. Deployments must use readiness, rather than liveness, for traffic admission.

Shutdown is graceful: the embedded server stops accepting new requests and gives active requests up to 30 seconds to finish before termination. Analytics is attempted synchronously and has no queue or buffer to drain.

## Quick start

1. Read [ENGINEERING_PLAN.md](ENGINEERING_PLAN.md) for the approved requirements baseline.
2. Read [TASKS.md](TASKS.md) for the current task order and dependency chain.
3. Read [TRACEABILITY.md](TRACEABILITY.md) for the current implementation status.
4. Use the documented Gradle commands below to build and test the project.

For local development, set the required public base URL before starting the application:

```shell
export URL_SHORTENER_PUBLIC_BASE_URL=https://sho.rt
```

Then run the build or test commands from the sections below.

## Local run

Use the Gradle wrapper to start the application locally:

```shell
./gradlew bootRun
```

Environment variables used by the current codebase:

- `URL_SHORTENER_PUBLIC_BASE_URL` is required and maps to the trusted public base URL used in short-link responses.
- `APP_VERSION` is optional and defaults to `development` when not set.

Example:

```shell
export URL_SHORTENER_PUBLIC_BASE_URL=https://sho.rt
export APP_VERSION=development
./gradlew bootRun
```

The approved local runtime uses PostgreSQL through Docker Compose. The version-2 compose layout also starts Redis, a second application instance, and an Nginx edge proxy so the migration path can be exercised locally through one public entrypoint. Start the stack with:

```shell
docker compose up -d --build --remove-orphans
```

The Compose file provides the runtime database connection settings for both application services and publishes the proxy on `http://localhost:8080`. Flyway runs automatically on application startup against PostgreSQL, so no separate migration step is required for the compose stack. After the stack is up, run the repository smoke check to confirm Redis and the public edge proxy are reachable through the compose network:

```shell
./scripts/compose-smoke.sh
```

To exercise the live API flow end-to-end, including creation, redirect, and analytics retrieval through the edge proxy:

```shell
./scripts/compose-api-smoke.sh
```

If you run the application outside Docker Compose, set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` for a PostgreSQL instance that matches the approved runtime contract.

## Prerequisites

- A Java 21 JDK available on `PATH`
- Network access to Maven Central for the first dependency resolution

A system Gradle installation is not required; use the checked-in wrapper.

The wrapper distribution is checksum-pinned in `gradle/wrapper/gradle-wrapper.properties`. Resolved dependency versions are recorded in `gradle.lockfile`; dependency changes and lock regeneration require engineer review.

## Build and validate

On macOS or Linux:

```shell
./gradlew clean build
```

On Windows:

```powershell
.\gradlew.bat clean build
```

Run the minimal automated test independently:

```shell
./gradlew test
```

The application is packaged by the build. Runtime database configuration is defined for the approved PostgreSQL baseline and is provided locally through Docker Compose.

## Current implementation status

The traceability matrix currently records the following broad status:

- implemented and verified: project foundation, configuration and logging foundations, URL validation, short-code generation, creation orchestration, redirect handling, analytics capture and retrieval, reliability controls, observability, and integration validation
- explicitly out of scope for the baseline: expiration, cache-based redirect handling, recovery/backup guarantees, and other deferred production-only commitments
- still pending: remaining final review tasks, broader performance validation tasks, and any future requirement changes that depend on new approvals

See [TRACEABILITY.md](TRACEABILITY.md) for the detailed status of each requirement and task.

## Final engineering summary

This repository now contains the core deliverables expected by the assignment:

- a runnable URL shortener prototype with creation, redirect, analytics, health, rate limiting, and observability
- a documented architecture baseline and API contract
- implementation traceability across requirements, decisions, tasks, prompts, and review records
- a validation record showing the build and test suite pass

Plan and rationale:

- use a modular Spring Boot service instead of a microservice split to keep the prototype reviewable and bounded
- make PostgreSQL the authoritative store so uniqueness, persistence, and analytics queries remain simple and testable
- keep analytics fail-open so redirect availability does not depend on append success
- preserve destination URLs exactly after structural validation to avoid hidden normalization changes

Artifacts produced:

- application code under `src/main/java`
- tests under `src/test/java`
- migrations under `src/main/resources/db/migration`
- architecture and API documentation in `docs/`
- task, decision, review, and traceability records in the repository root

Risks and trade-offs:

- analytics is best-effort, not exactly-once
- duplicate destination URLs create separate mappings by design
- the prototype does not claim production recovery, backup, or multi-region guarantees
- some higher-order review items remain documented as future work in `TASKS.md`

Assumptions:

- the supplied `URL_SHORTENER_PUBLIC_BASE_URL` is trusted configuration
- PostgreSQL is available for the approved runtime contract
- validation is performed within the approved repository and compose environment

Limitations:

- no idempotency key model is implemented
- no expiration lifecycle is implemented in the baseline
- no public analytics access is exposed without a token
- performance validation beyond the documented unit and smoke checks is still a separate task

Validation:

- `./gradlew test` completed successfully in this pass
- repository smoke and API smoke scripts are documented for the compose runtime
- the traceability matrix and AI review log record the implementation and review trail

## Three scenarios

### Greenfield

Objective:

- build the URL shortener from an empty baseline into a reviewable prototype.

Decomposition:

1. establish the approved stack and repository scaffold
2. define URL validation, short-code generation, and durable mapping creation
3. add redirect, analytics, health, and operational surfaces
4. wire tests, documentation, and traceability

Execution:

- the codebase implements the main user-facing flow in a single Spring Boot application
- the persistence layer uses PostgreSQL migrations and JPA repositories
- the API layer returns stable, safe error envelopes and bounded responses

Validation:

- `./gradlew test` passes
- the documented contract and traceability documents align with the implementation

### Brownfield

Objective:

- evolve an existing codebase without breaking established contracts.

Decomposition:

1. identify affected modules: configuration, web, redirect, analytics, persistence, and observability
2. preserve stable behaviors such as redirect status, error envelopes, and analytics semantics
3. update tests and documentation alongside implementation changes

Execution:

- module boundaries remain explicit so future changes can be localized
- existing validation and error-handling behavior is kept stable where already approved
- traceability documents capture where implementation evidence came from

Validation:

- build and test output shows the current behavior is still coherent after the documentation pass
- the repository structure makes impacted modules easy to audit for regressions

### Ambiguous

Objective:

- normalize a requirement that could be interpreted multiple ways.

Example ambiguity:

- how analytics should treat redirect clicks, retries, and automated traffic

Decomposition:

1. enumerate the plausible interpretations
2. choose a testable policy that minimizes privacy risk and operational complexity
3. document the rejected alternatives and the resulting contract

Execution:

- the approved baseline counts eligible redirect-path GET requests
- suspected automation is reported separately
- analytics failure does not block redirect success

Validation:

- the decision is reflected in [docs/api.md](docs/api.md), [docs/architecture.md](docs/architecture.md), and [TRACEABILITY.md](TRACEABILITY.md)
- the behavior is deterministic enough to support unit and integration tests

## Markdown guide

### Root Markdown files

| File | What it contains | When to read it |
| --- | --- | --- |
| [ENGINEERING_PLAN.md](ENGINEERING_PLAN.md) | The normalized requirements baseline, including goals, functional requirements, non-functional requirements, ambiguities, and approved requirement decisions. | Before changing scope, requirements, or policy decisions. |
| [TASKS.md](TASKS.md) | The ordered engineering backlog with task IDs, dependencies, acceptance criteria, test requirements, and approval gates. | Before starting implementation work or validating task order. |
| [TRACEABILITY.md](TRACEABILITY.md) | The live requirements-to-task-and-evidence traceability matrix plus implementation status summaries. | When you need to confirm what is implemented, verified, or still pending. |
| [DECISIONS.md](DECISIONS.md) | The architecture and requirements decision log, including approved and proposed decisions. | Before making design or dependency changes that depend on approved decisions. |
| [PROMPT_LOG.md](PROMPT_LOG.md) | The AI engineering activity log with prompt records, context, edits, validation, and approval history. | When you need the work history, validation record, or approval trail. |
| [AI_REVIEW.md](AI_REVIEW.md) | The AI-assisted work review record and review findings. | When you need the final review notes for AI-assisted changes. |
| [README.md](README.md) | This project summary, setup guidance, and documentation map. | First stop for a quick repository overview. |

### `docs/` Markdown files

| File | What it contains | When to read it |
| --- | --- | --- |
| [docs/architecture.md](docs/architecture.md) | The approved system architecture baseline, component boundaries, request flow, data flow, and database design. | Before reviewing service boundaries, flows, or persistence design. |
| [docs/api.md](docs/api.md) | The approved HTTP API contract, request and response shapes, status codes, and example payloads. | Before implementing or testing HTTP endpoints. |
| [docs/security.md](docs/security.md) | The threat model, hardened input/output boundaries, and verified security controls. | Before reviewing risks, input validation, or exposed data. |
| [docs/operations.md](docs/operations.md) | Operational metrics, alerts, and operational gaps that still require deployment-owner decisions. | Before wiring metrics, alerts, or operational runbooks. |
| [docs/performance.md](docs/performance.md) | The reliability and performance budget, including approved dependency timing bounds and the no-cache baseline. | Before changing timeout, caching, or throughput-sensitive behavior. |
| [docs/testing.md](docs/testing.md) | The test strategy, coverage layers, and validation commands for the project. | Before running or extending the validation suite. |
