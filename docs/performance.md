<!-- Author: Manoj reddy <amireddymanojreddy@gmail.com> -->
<!-- Since: 2026-09-03 -->

## Reliability performance budget

This document reflects the approved ARC-005 design and the REL-IMPL-001 implementation adjustment. Dependency work is bounded at the supported HikariCP minimum of 250 ms for pool acquisition, 150 ms for lookup, 500 ms for creation, 50 ms for analytics append, and 1 s for analytics queries, with no automatic retries. The baseline has no application cache; performance validation must measure direct PostgreSQL behavior and analytics overhead before infrastructure additions.
