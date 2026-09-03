# Testing

## Test layers and scope

| Layer | Current evidence | Remaining evidence |
| --- | --- | --- |
| Unit/property | URL acceptance/rejection and 4,096 boundary; Base62 format, rejection sampling and retry bound; creation orchestration; resolution outcomes; click classification; UTC query ranges/aggregation; retention; configuration; errors; lifecycle; bounded metrics and alert evaluation | Security-specific rules remain with SEC-IMPL-001 through SEC-IMPL-003 and TST-003 |
| API contract | Controller tests cover creation, redirect, analytics, health, authentication, ranges, safe errors, and cache headers | TST-002 completes the documented outcome matrix and method/content-type behavior |
| PostgreSQL integration | Migration/schema text and repository contracts are checked without a live database | TST-002 requires real PostgreSQL migration, constraint, query, transaction, retention, and concurrency evidence |
| Fault/security/performance | Deterministic component failure tests exist | TST-003, security tasks, and PERF-VAL tasks own full evidence |

No numeric line or branch coverage threshold was approved. TST-001 therefore uses direct requirement/invariant coverage and explicit gap review rather than inventing a percentage target. Tests use synthetic `example.com` values and do not make outbound network calls.

## Commands

Run deterministic unit and component coverage:

```shell
./gradlew test
```

Run it from a clean build when validating reproducibility:

```shell
./gradlew clean test --rerun-tasks --no-daemon
```

TST-001 requires two consecutive successful executions. TST-002 will document the isolated PostgreSQL command and the Docker Compose environment for the approved runtime baseline when that dependency is exercised.
