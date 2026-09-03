# URL Shortener

Production-oriented URL shortener prototype. The project is being implemented as a sequence of explicitly approved engineering tasks.

## Current scope

FND-001 provides the buildable project foundation:

- Java 21
- Gradle 9.7.1 wrapper
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA with Boot-managed Hibernate
- PostgreSQL JDBC driver at runtime
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

The application is packaged by the build. Runtime database configuration belongs to a later approved task and is intentionally not defined yet.
