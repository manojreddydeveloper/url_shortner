# AI Engineering Activity Log

This document records material AI-assisted engineering activity for the project. It provides traceability from a prompt and task to generated output, engineer review, edits, rejection or acceptance, validation, and final approval.

> **AI-generated output is treated as untrusted until reviewed and validated by the engineer.**

An AI-generated artifact, recommendation, test, review finding, or validation summary must not be treated as correct or approved merely because it appears in this log.

## Status model

Each record has exactly one current status. Status changes must preserve the review and validation history in the record.

| Status | Meaning | Who may confirm it |
| --- | --- | --- |
| `PROPOSED` | A prompt or planned AI activity has been recorded, but no AI output has been produced. | Engineer or AI assistant |
| `GENERATED` | AI output exists but has not completed engineer review and validation. | AI assistant may record generation |
| `REVIEWED` | The engineer has reviewed the AI output and recorded findings; validation or final disposition may still be pending. | Engineer |
| `EDITED` | The generated output was materially modified after review. The edited result requires validation and approval. | Engineer, or AI assistant acting on explicit engineer direction |
| `REJECTED` | The engineer rejected all or a material portion of the output and recorded the reason and disposition. | Engineer |
| `VALIDATED` | The reviewed or edited output has completed the required validation, with evidence recorded. Validation does not itself grant final approval. | Engineer |
| `APPROVED` | The engineer reviewed the final output, considered its validation evidence, and explicitly accepted it. | Engineer only |

Typical successful progression:

`PROPOSED` → `GENERATED` → `REVIEWED` → optionally `EDITED` → `VALIDATED` → `APPROVED`

Rejection may occur after generation, review, editing, or validation. If only part of an output is rejected, record the accepted, edited, and rejected portions separately. A material edit after validation returns the record to `EDITED` until the affected validation is repeated.

## Recording rules

1. Create one record for each material prompt or logically independent AI activity.
2. Use a stable, unique Prompt ID in the form `PROMPT-NNN`.
3. Link the record to an approved Task ID from `TASKS.md`. If no task exists, use `UNASSIGNED` and explain why; do not invent a completed task retroactively.
4. Use an ISO 8601 date. Include time and time-zone offset when needed to order multiple activities on the same day.
5. Summarize context without copying secrets, credentials, private data, or unnecessarily large source material.
6. Record constraints and acceptance criteria that governed the output. Do not silently add criteria after generation.
7. List only files actually changed. Use `None` for read-only analysis.
8. Distinguish accepted, edited, and rejected portions. Do not rewrite history by deleting rejected output or rationale.
9. Record actual validation commands, manual checks, results, and limitations. Never report tests that were not run.
10. `Test Results: Not applicable` requires a reason.
11. AI may summarize evidence but may not provide engineer approval on the engineer's behalf.
12. Do not set a record to `APPROVED` until the engineer explicitly approves the reviewed and validated result.

## Standard record format

Copy this template for every new material AI activity.

```markdown
### PROMPT-NNN

- **Prompt ID:** PROMPT-NNN
- **Task ID:** TASK-ID or UNASSIGNED
- **Date:** YYYY-MM-DD or YYYY-MM-DDThh:mm:ss±hh:mm
- **Status:** PROPOSED | GENERATED | REVIEWED | EDITED | REJECTED | VALIDATED | APPROVED
- **Purpose:** Why AI assistance was requested.
- **Context Provided:** Requirements, decisions, files, code, errors, or other inputs supplied to the AI.
- **Constraints:** Explicit limits, prohibited actions, approval gates, and safety requirements.
- **Acceptance Criteria:** Conditions the output must satisfy.
- **AI Output Summary:** Concise description of proposals, generated artifacts, edits, or findings.
- **Files Changed:** Exact changed file paths, or None.
- **Engineer Review:** PENDING, or the engineer's findings and review date.
- **Accepted Output:** PENDING, None, or the portions the engineer accepted.
- **Edited Output:** PENDING, None, or a description of engineer/AI edits and who made them.
- **Rejected Output:** PENDING, None, or the portions the engineer rejected.
- **Rejection Reason:** PENDING, Not applicable, or the engineer's rationale.
- **Validation:** PENDING, or exact commands/manual checks, results, environment, and limitations.
- **Test Results:** PENDING, Not applicable with reason, or test commands and results.
- **Engineer Approval:** PENDING, REJECTED with date, or APPROVED with date and engineer identity/reference.
```

## Activity records

### PROMPT-001

- **Prompt ID:** PROMPT-001
- **Task ID:** UNASSIGNED — project governance activity performed before implementation-task execution
- **Date:** 2026-09-02
- **Status:** GENERATED
- **Purpose:** Create the project's standard AI engineering activity log and review lifecycle.
- **Context Provided:** The engineer supplied the required record fields, allowed statuses, mandatory untrusted-output principle, and prohibition on application-code implementation. Existing `ENGINEERING_PLAN.md`, `TASKS.md`, and `TRACEABILITY.md` establish the project's governance and traceability context.
- **Constraints:** Modify only `PROMPT_LOG.md`; do not implement application code; do not claim unperformed engineer review, validation, tests, or approval; use the requested statuses and fields.
- **Acceptance Criteria:** The log defines Prompt ID, Task ID, Date, Purpose, Context Provided, Constraints, Acceptance Criteria, AI Output Summary, Files Changed, Engineer Review, Accepted Output, Edited Output, Rejected Output, Rejection Reason, Validation, Test Results, and Engineer Approval; defines all requested statuses; includes the required untrusted-output principle.
- **AI Output Summary:** Created a Markdown activity-log standard with status definitions, transition rules, recording rules, a reusable record template, and this initial generation record.
- **Files Changed:** `PROMPT_LOG.md`
- **Engineer Review:** PENDING
- **Accepted Output:** PENDING
- **Edited Output:** PENDING
- **Rejected Output:** PENDING
- **Rejection Reason:** PENDING
- **Validation:** AI structural check passed: Markdown whitespace validation reported no errors; all seven requested statuses are defined; the reusable template and this record each contain all 17 requested fields. Engineer validation remains PENDING.
- **Test Results:** Not applicable — this activity changes documentation only and introduces no executable behavior.
- **Engineer Approval:** PENDING

### PROMPT-002

- **Prompt ID:** PROMPT-002
- **Task ID:** REQ-001
- **Date:** 2026-09-02
- **Status:** APPROVED
- **Purpose:** Execute the earliest dependency-eligible task by recording the engineer's disposition of the initial functional scope and non-goals.
- **Context Provided:** `ENGINEERING_PLAN.md`, `TASKS.md`, `DECISIONS.md`, `TRACEABILITY.md`, the repository state, and the engineer's explicit “yes” response approving FR-001 through FR-012 and the current non-goals as written.
- **Constraints:** Execute only REQ-001; do not resolve ambiguities, approve assumptions or ADRs, add dependencies, implement source code, mark the task `APPROVED`, or continue to another task; update traceability and this log with factual results only.
- **Acceptance Criteria:** Every FR-001 through FR-012 has a recorded disposition and rationale; required actors and creation, redirect, analytics, lifecycle, failure, concurrency, request-policy, and traceability capabilities remain explicit; the current non-goals are explicitly approved; outcomes are recorded in `ENGINEERING_PLAN.md` and `DECISIONS.md`; goals, requirements, non-goals, ambiguities, and acceptance criteria remain consistent.
- **AI Output Summary:** Added a bounded functional-scope disposition, logical actor list, and rationales; recorded RDR-001; and updated traceability to distinguish approved capability scope from pending policy decisions and not-yet-started implementation evidence.
- **Files Changed:** `ENGINEERING_PLAN.md`, `DECISIONS.md`, `TRACEABILITY.md`, `PROMPT_LOG.md`
- **Engineer Review:** The engineer reviewed the REQ-001 changes and explicitly approved them on 2026-09-02.
- **Accepted Output:** FR-001 through FR-012, the Section 11 non-goals, the functional-scope disposition and rationales, the logical actor descriptions, RDR-001, and the associated traceability updates.
- **Edited Output:** None — the engineer approved the generated changes without requesting further edits.
- **Rejected Output:** None recorded.
- **Rejection Reason:** Not applicable.
- **Validation:** `git diff --check` passed. Identifier checks found 12 functional requirement definitions, 12 disposition rows, and 12 traceability rows with no set difference. Checks also confirmed that all 16 ambiguity statuses remain `PENDING ENGINEER APPROVAL` and all 14 ADR summary statuses remain `PROPOSED`.
- **Test Results:** `./gradlew --gradle-user-home /private/tmp/url-shortener-gradle.5lRGYH/project-home test --rerun-tasks --no-daemon` completed successfully in 10 seconds with 4 tasks executed. The generated JUnit reports contain 9 tests, 0 failures, 0 errors, and 0 skipped tests. These application tests are regression evidence; REQ-001 itself is a documentation and decision-record task.
- **Engineer Approval:** APPROVED on 2026-09-02 by the engineer through the project conversation.
