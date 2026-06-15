---
name: jiraplanning
description: Transform Jira tickets into detailed QA test strategies. Use this skill whenever a Jira issue key, ticket JSON, or ticket description is provided and the goal is to extract testable requirements, write test cases, or plan a test strategy. Triggers on Story, Bug, Epic, and Sub-task types. Always apply this skill before producing any QA output from Jira data.
---

# jiraplanning

Senior QA Engineer skill that extracts every testable signal from a Jira ticket and produces a specific, detailed test strategy. Never produces generic output.

## Usage

Use this skill when:
- A Jira ticket (Story, Bug, Epic, Sub-task) needs to be analyzed for QA
- Test cases, acceptance criteria, or a test strategy need to be written from a Jira ticket
- A Jira API response or raw ticket JSON is provided and test planning is required

## Steps

### Step 1 — Strip Garbage Fields

Before processing anything, permanently discard these fields — never reference or mention them in output:

- Any `customfield_*` key regardless of value
- `avatarUrls`, `iconUrl`, `self`, `thumbnail`, base64 strings, HTML style attributes
- `assignee`, `reporter`, `creator` — unless the ticket is about role-based access control
- `sprint`, `fixVersion`, `timetracking`, `worklog`, `aggregatetimespent`
- Status transition history — exception: include ONLY if it explains a Bug's reopen cycle
- Generic "As a user I want to…" preamble — extract the testable behavior only

### Step 2 — Identify Issue Type and Route to Instructions

Check `issuetype.name` and follow the matching format:

| Type | Format |
|------|--------|
| Story | Format A |
| Bug | Format B |
| Epic | Format C |
| Sub-task | Fetch parent Story first → use Format A scoped to sub-task behavior only |

### Step 3 — Extract Testable Signals

Apply a QA lens to every field:

| Field | What to Extract |
|-------|----------------|
| **Description** | Scan ALL prose — every sentence describing a behavior, condition, or constraint is a potential test case. Do not only look for a labeled AC section. |
| **Acceptance Criteria** | Extract verbatim, restate in testable Given/When/Then. If missing, infer and mark `[INFERRED]`. If vague, mark `[CLARIFICATION NEEDED: reason]`. |
| **Attachments** | Mockups → UI/state test cases. JSON/Logs → API contract tests. DB schema → data integrity tests. |
| **Comments** | Look for: deferred scope, dev workarounds, confirmed edge cases, "won't do" decisions. These are hidden requirements. |
| **Issue Links** | Identify regression risk. If linked to a prior Bug, add a retest for that fix. |
| **Sub-tasks** | Always fetch the parent Story before analyzing. Never analyze a sub-task in isolation. |
| **Epics** | List child stories. Identify shared APIs, UI components, or data flows as regression hotspots. |

### Step 4 — Apply Output Rules

- Never produce generic test cases — every TC must be traceable to a specific line in the ticket
- Never output stripped fields, even as a note or reference
- Omit any section that has no data — do not show empty tables or blank bullets
- Use `[INFERRED]` when AC is missing and you are deriving it from description prose
- Use `[CLARIFICATION NEEDED: reason]` when a requirement is too vague to write a TC for
- Do not narrate the extraction process — output the formatted result directly
