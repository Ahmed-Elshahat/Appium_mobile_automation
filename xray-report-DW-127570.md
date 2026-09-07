# Xray Merged Test Report — Epic DW-127570

**Epic:** DW-127570 — IPS Routing for Local Transfers  
**Status:** Implementing  
**Report Date:** 2026-08-10  

---

## Executive Summary

| Metric | Count |
|--------|-------|
| Total Stories | 12 |
| Stories with Test Plans | 10 |
| Stories without Tests | 2 (DW-135047 Ops requirements, DW-135230 Cancelled) |
| Total Test Plans | 10 |
| Total Test Executions | 10 |
| Total Test Runs | 146 |
| **PASS** | **67** |
| **FAIL** | **5** |
| **ABORTED** | **16** |
| **TODO** | **58** |
| **Pass Rate (executed)** | **76.1%** (67/88 executed) |
| **Overall Completion** | **60.3%** (88/146 executed) |

---

## Results by Test Execution

| Test Execution | Story | Platform | Total | Pass | Fail | Aborted | TODO |
|---|---|---|---|---|---|---|---|
| DW-138325 | DW-130633 — IPS Feature Flag & Fees Configuration in Staff Portal | — | 20 | 17 | 0 | 3 | 0 |
| DW-138347 | DW-130635 — IPS Eligibility Check on urpay side for Local Transfers | — | 20 | 14 | 0 | 3 | 3 |
| DW-138268 | DW-132951 — Capture user consent for IPS Terms & Conditions | AOS | 12 | 7 | 0 | 5 | 0 |
| DW-138269 | DW-132951 — Capture user consent for IPS Terms & Conditions | iOS | 12 | 8 | 0 | 4 | 0 |
| DW-138302 | DW-133029 — Execute IPS Payment | — | 14 | 8 | 0 | 1 | 5 |
| DW-138248 | DW-135030 — Display transaction and share details | AOS | 18 | 13 | 5 | 0 | 0 |
| DW-138270 | DW-135030 — Display transaction and share details | iOS | 18 | 0 | 0 | 0 | 18 |
| DW-138228 | DW-137209 — Online PSR Handling for IPS transactions | — | 10 | 0 | 0 | 0 | 10 |
| DW-138203 | DW-137210 — EOD File Processing for IPS Transactions | — | 12 | 0 | 0 | 0 | 12 |
| DW-138215 | DW-137211 — IPS Transaction Status Inquiry | — | 10 | 0 | 0 | 0 | 10 |

---

## Test Plans Without Executions

| Test Plan | Story | Tests Planned |
|---|---|---|
| DW-138376 | DW-132442 — IPS Transaction Validation on ARB | 25 |
| DW-138285 | DW-135029 — Transaction Notifications for IPS | 10 |

**Note:** These 35 tests have no execution runs created yet.

---

## Failed Tests (5)

All from **DW-138248** (AOS — Display transaction and share details):

| Test Key | Summary |
|---|---|
| DW-138236 | Verify Share as Image from Success Screen |
| DW-138237 | Verify Share as Image from Transaction Details |
| DW-138239 | Verify local transfer filter includes IPS transactions |
| DW-138245 | Verify user can complete transfer after Retransfer from IPS transaction |
| DW-138246 | Verify Retransfer negative case with insufficient balance |

**Related Defects:**
- DW-138583 — local - Share Image option is missing
- DW-138662 — Keys are displayed in Shared Text
- DW-138664 — Incorrect Details are displayed in Transaction Details
- DW-138665 — Existing Local Transfer Filter isn't working for IPS transfers
- DW-138794 — Error Displayed in Re-transfer flow for IPS Transactions

---

## Aborted Tests (16)

| Test Key | Summary | Execution |
|---|---|---|
| DW-138307 | Verify IPS feature flag toggle action is recorded in the audit log | DW-138325 |
| DW-138312 | Verify updating the non-IPS fee reflects on the IPS fee (shared configuration phase) | DW-138325 |
| DW-138323 | IPS feature flag OFF, Check Banks List | DW-138325 |
| DW-138337 | Beneficiary (existing) bank not in IPS bank table — routed to non-IPS | DW-138347 |
| DW-138341 | Incoming IPS transfer to urpay account — received and credited correctly | DW-138347 |
| DW-138342 | Incoming IPS transfer — transaction history entry is correct | DW-138347 |
| DW-138259 | Field variation coverage for mixed transaction attributes | DW-138268 |
| DW-138260 | Consent behavior after filtering beneficiary or transaction list before transfer | DW-138268 |
| DW-138261 | Consent displayed during retransfer flow from transaction history/details | DW-138268 |
| DW-138263 | Negative: disclaimer content service/configuration unavailable | DW-138268 |
| DW-138265 | Edge: transaction data changes after consent screen display | DW-138268 |
| DW-138259 | Field variation coverage for mixed transaction attributes | DW-138269 |
| DW-138260 | Consent behavior after filtering beneficiary or transaction list before transfer | DW-138269 |
| DW-138261 | Consent displayed during retransfer flow from transaction history/details | DW-138269 |
| DW-138263 | Negative: disclaimer content service/configuration unavailable | DW-138269 |
| DW-138300 | Timeout during execution | DW-138302 |

---

## Blocking Defects

| Defect | Summary | Blocks |
|---|---|---|
| DW-138807 | PSR Is Not Received & Transfers Remain in Accepted Status | DW-133029, DW-135029, DW-137209, DW-137210, DW-137211 |
| DW-138646 | Sarie limit validation is performed before determining transfer type | DW-130635 |
| DW-138667 | OTP message displays currency placeholder instead of currency code | DW-135029 |
| DW-138752 | "The entered ID isn't correct" Error When Adding/Editing Local Beneficiary | DW-130635 |

---

## Unplanned Test Coverage

| Story | Summary | Test Coverage |
|---|---|---|
| DW-135047 | Ops requirements for IPS | No test plan |
| DW-135230 | Ops requirements to include UTI number on wallet transactions | Cancelled |

---

## Detailed Test Inventory by Test Plan

### DW-138376 — IPS Transaction Validation on ARB (25 tests, no execution)

| # | Test Key | Summary |
|---|---|---|
| 1 | DW-138351 | Trigger ARB validation for IPS-eligible transaction after urpay checks pass |
| 2 | DW-138352 | Do not trigger ARB validation when urpay eligibility checks fail |
| 3 | DW-138353 | Route to IPS when ARB response is IPSEnabled Y and Status 01 Available |
| 4 | DW-138354 | Block transfer when ARB response is Status 02 Unavailable |
| 5 | DW-138355 | Route to Sarie when ARB response is Status 03 Unsubscribed |
| 6 | DW-138356 | Block transfer when ARB response is Status 04 IPS Unavailable |
| 7 | DW-138357 | Route to Sarie when IPSEnabled is N |
| 8 | DW-138358 | Reroute to Sarie when ARB validation fails |
| 9 | DW-138359 | Reroute to Sarie when ARB validation times out |
| 10 | DW-138360 | Reroute to Sarie on ARB technical error |
| 11 | DW-138361 | Ignore sarieEnabled flag from ARB validation response |
| 12 | DW-138362 | Verify behavior when existing Beneficiary with IPS only supported bank attempts to transfer when IPS Feature flag is OFF |
| 13 | DW-138363 | Use Neoleap Staff Portal fee override instead of ARB default fees |
| 14 | DW-138364 | Use ARB default transaction fees when no internal override exists |
| 15 | DW-138365 | Auto-select previous Purpose of Transfer for same beneficiary |
| 16 | DW-138366 | Require user to select Purpose of Transfer when no previous POT exists |
| 17 | DW-138367 | Validate POT mapping 001 to IPS key 1 |
| 18 | DW-138368 | Validate POT mapping 002 to IPS key 9 |
| 19 | DW-138369 | Validate POT mapping 004 to IPS key E |
| 20 | DW-138370 | Validate POT mapping 005 to IPS key F |
| 21 | DW-138371 | Display IPS confirmation screen with instant receipt message and mandatory disclaimer |
| 22 | DW-138372 | Use production bank code for bank not available in IPS list |
| 23 | DW-138373 | Use IPS bank ID for bank not available in Sarie but available in IPS |
| 24 | DW-138374 | Handle merged bank mapping when production and IPS names/codes differ |
| 25 | DW-138375 | Validate transfer amount passed to ARB matches user-entered amount |

### DW-138285 — Transaction Notifications for IPS (10 tests, no execution)

| # | Test Key | Summary |
|---|---|---|
| 1 | DW-138275 | IPS transfer to ARB bank during working hours |
| 2 | DW-138276 | IPS transfer to ARB bank outside working hours |
| 3 | DW-138277 | IPS transfer to IPS-supported non-ARB bank on weekend (COUT_IPS) |
| 4 | DW-138278 | IPS transfer to IPS-supported non-ARB bank on holiday (COUT_IPS) |
| 5 | DW-138279 | Non-IPS transfer to ARB bank (COUT_BANK) |
| 6 | DW-138280 | Non-IPS transfer to non-ARB bank during working hours (COUT_SARIE) |
| 7 | DW-138281 | Validate beneficiary name and amount placeholders in PN |
| 8 | DW-138282 | Boundary time scenario at configured start/end |
| 9 | DW-138283 | Arabic / English SMS content |
| 10 | DW-138284 | Arabic / English PN content |

---

## Recommendations

1. **Critical:** DW-138807 (PSR Not Received) blocks 5 stories — must be resolved before execution can proceed on DW-137209, DW-137210, DW-137211.
2. **High:** Create test executions for DW-138376 (ARB Validation, 25 tests) and DW-138285 (Notifications, 10 tests).
3. **High:** Execute iOS test run DW-138270 (Display transaction & share details) — currently all 18 tests in TODO.
4. **Medium:** Investigate and re-run 16 aborted tests, particularly consent-related tests on both AOS/iOS.
5. **Medium:** Fix 5 failing tests related to Share Image and Re-transfer flows (linked defects already raised).

---

*Generated from Jira Xray data via REST API on 2026-08-10*
