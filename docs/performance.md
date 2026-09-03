<!-- Author: Manoj reddy <amireddymanojreddy@gmail.com> -->
<!-- Since: 2026-09-03 -->

## Reliability performance budget

This document reflects the approved ARC-005 design, the REL-IMPL-001 implementation adjustment, and the ADR-015 process-local cache decision. Dependency work is bounded at the supported HikariCP minimum of 250 ms for pool acquisition, 150 ms for lookup, 500 ms for creation, 50 ms for analytics append, and 1 s for analytics queries, with no automatic retries. Redirect performance should measure the hot-cache hit path and the PostgreSQL miss path separately; any shared cache addition still requires new evidence and approval.
