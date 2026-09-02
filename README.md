# URL Shortener

Production-oriented URL shortener prototype. The project is being implemented as a sequence of explicitly approved engineering tasks.

## Current scope

FND-001 provides only the buildable project foundation:

- Java 21
- Gradle 9.7.1 wrapper
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA with Boot-managed Hibernate
- PostgreSQL JDBC driver at runtime
- Spring Boot test support

No URL creation, redirect, persistence mapping, analytics, rate limiting, or other product behavior is implemented yet.

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

The application is packaged by the build. Runtime database configuration and startup policy belong to a later approved task and are intentionally not defined by FND-001.
