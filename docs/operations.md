# Operations

<!-- Author: Manoj reddy <amireddymanojreddy@gmail.com> -->
<!-- Since: 2026-09-03 -->

## Metrics

The service exposes Micrometer measurements through the Spring Boot metrics endpoint at `/actuator/metrics`. Custom `url_shortener.*` meters use only fixed `operation`, `outcome`, `status_class`, `action`, `state`, and `pool` tags. HikariCP supplies its bounded connection-pool measurements. Short codes, URLs, tokens, IP addresses, user agents, referrers, correlation IDs, SQL, and dependency addresses are prohibited as metric tags.

Request instrumentation normalizes creation, redirect, analytics, liveness, readiness, and unmatched traffic without retaining path parameters. Domain instrumentation distinguishes creation validation, success, database failure and collision behavior; malformed/unknown/active redirect behavior; analytics query and append outcomes; and readiness transitions. Rate-limit and explicit pool-utilization emitters use fixed enums/allowlists and are ready for the controls that SEC-IMPL-002 owns.

## Alerts and operational gaps

[`ops/alerts.yaml`](../ops/alerts.yaml) is a vendor-neutral, synthetically tested definition of the approved RDR-004 thresholds. Deployment automation must translate its signals to the selected telemetry backend and preserve the comparisons and evaluation windows.

The repository does not select a dashboard product, notification destination, escalation recipient, or on-call owner. Dashboard panels should cover request rate/error/latency, domain outcomes, dependency and HikariCP saturation, analytics loss, readiness, and retention lag. Routing, ownership, runbook URLs, and the unspecified duration for the “any sustained analytics loss” warning require deployment-owner decisions; this task does not invent them. The defined greater-than-1%-for-5-minutes analytics-loss alert is unaffected.
