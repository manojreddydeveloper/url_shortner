## Reliability and observability security boundaries

This document reflects the ARC-005 proposal and remains pending engineer approval. Health responses expose only liveness/readiness state, not database details, credentials, stack traces, or configuration. Logs and metrics use bounded dimensions and exclude destinations, tokens, raw IP addresses, user agents, referrers, SQL values, and secrets. Any future cache or tracing component requires explicit privacy and stale-data review.
