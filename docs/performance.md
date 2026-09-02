## Reliability performance budget

This document reflects the ARC-005 proposal and remains pending engineer approval. Dependency work is bounded at 100 ms pool acquisition, 150 ms lookup, 500 ms creation, 50 ms analytics append, and 1 s analytics query, with no automatic retries. The baseline has no application cache; performance validation must measure direct PostgreSQL behavior and analytics overhead before infrastructure additions.
