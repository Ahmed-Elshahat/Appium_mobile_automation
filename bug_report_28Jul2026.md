# URPay Mobile Automation - Test Execution Bug Report

**Date:** 28 July 2026  
**Platform:** Android (Galaxy S24 Ultra - Android 14)  
**Environment:** SIT  
**Total Test Cases:** 477  
**Passed:** 231 | **Failed:** 49 | **Broken:** 3 | **Skipped:** 194  
**Pass Rate:** 48.4% (excluding skipped: 81.6%)

---

## Executive Summary

| Category | Count | Action Required |
|----------|-------|-----------------|
| Backend/Service Issues | 5 unique (10 runs) | Escalate to backend team |
| Functional Bugs | 8 | Report to dev team |
| UI Element Not Found (Locator Issues) | 20 | Investigate - possible UI changes |
| App Crash | 1 | Critical - report immediately |
| Infrastructure/Connectivity | 1 | DevOps/Infra team |
| Test Data Issue | 1 | Update test data |

---

## Critical Bugs (P1)

| # | Test Name | Module/Suite | Error Description | Root Cause |
|---|-----------|-------------|-------------------|------------|
| 1 | testSearchWalletBeneficiary | Wallet Transfer | APP CRASHED: app process not running, App State: NOT_RUNNING | App crash when searching wallet beneficiary |

---

## Backend/Service Defects (P2) - Escalate to Backend Team

| # | Test Name | Module/Suite | Error Description | Occurrences |
|---|-----------|-------------|-------------------|-------------|
| 1 | testEnterValidDobNavigatesToNewPasscode | Forgot Passcode (Multiple Tiers) | Backend rejected forgot-passcode DOB submit - "Service is currently unavailable" | 5 |
| 2 | testWesternUnionCashPickupTransfer | Western Union International Transfer | Service is currently unavailable - SIT backend/provider outage on international transfer route | 1 |
| 3 | testMoneyGramBankDepositToIndia | MoneyGram Bank Deposit | Service is currently unavailable - SIT backend/provider outage on international transfer route | 1 |
| 4 | testTahweelBankDepositTransfer | H2H Tahweel Bank Deposit | Service is currently unavailable - SIT backend/provider outage on international transfer route | 1 |
| 5 | testTahweelCashPickupTransfer | H2H Tahweel Cash Pickup | Service is currently unavailable - SIT backend/provider outage on international transfer route | 1 |
| 6 | testReenterPasscodeShowsSuccess | Forgot Passcode Default Tier | Expected "Passcode successfully updated" but got "Service is currently unavailable" | 1 |

---

## Functional Bugs (P2) - Report to Development Team

| # | Test Name | Module/Suite | Expected Behavior | Actual Behavior |
|---|-----------|-------------|-------------------|-----------------|
| 1 | testApplyValidPromoShowsSuccess | DMP - Product Specific | Promo code shows "Congrats! Code has been successfully applied." | Shows "Sorry, this promo code has expired" or "promo code entered is incorrect" | 6 occurrences |
| 2 | testNavigateToChangePasscodeScreen | Change Passcode (Multiple Tiers) | Dashboard should be visible after login | Dashboard not visible (login failure) | 4 occurrences |
| 3 | testOrderFromNewArrival | DMP - Order From New Arrival | Order should be placed successfully (Order Placed! screen shown) | Order placement failed |
| 4 | testAccountStatementFullFlow | Account Statement | Should show PDF statement or 'no data' message | Neither shown after tapping View Account Statement |
| 5 | testValidateCardBenefits | Platinum Card | Expected specific card benefit descriptions | Card benefits text has changed (content update not reflected in test expectations) |
| 6 | testZainRecharge | Telecom - Zain | Zain recharge must show success (Done button) | Done button not shown |
| 7 | testInviteFriendsShareOptions | Invite Friends | Header tab should show "Invite Friends" | Shows "Share your code" instead |

---

## UI Element Not Found / Locator Timeout Issues (P3)

| # | Test Name | Module/Suite | Missing Element | Occurrences |
|---|-----------|-------------|-----------------|-------------|
| 1 | testAutoTopUpSetAmountLimit | Top-up Bank Card | `testID-input-container-undefined` | 1 |
| 2 | testAddNewPrepaidBill | SADAD Bills | `testID-primary-onConfirmSave-main` / `testID-primary-bSv-main` | 1 |
| 3 | testFullCycleMarketPlaceOrder | DMP - Full Cycle | "Add to Cart" / "Buy now" button | 1 |
| 4 | testEditGroupQattaName | Edit Group Qatta Details | Group Qatta ReactText element | 1 |
| 5 | testValidateCancelCardSteps | Signature Card | "Cancel" TextView button | 2 |
| 6 | testRIACashPickupTransfer | RIA Cash Pickup | `testID-primary--main` | 1 |
| 7 | testRIABankDepositTransfer | RIA Bank Deposit | `testID-primary--main` | 1 |
| 8 | testChangeCardPin | Signature Card | "Next" button / passcode field | 1 |
| 9 | testValidateCardInfo | Platinum Card | `testID-secondary-action-0` | 2 |
| 10 | testInvitationCodeMatchesReferralCode | Registration Using Invitation Code | Tab element `testID-Tabs.c013129a...` | 1 |
| 11 | testMobilyReOrder | Telecom - Mobily | `testID-right-icon-0` | 1 |
| 12 | testOverpaidBillPaymentAndVerifyBalance | SADAD Payment | `testID-search-item-1` | 1 |
| 13 | testPostpaidBillPaymentAndVerifyBalance | SADAD Payment | `testID-primary-onConfirmSave-main` | 1 |
| 14 | testPrepaidBillPaymentAndVerifyBalance | SADAD Payment | `testID-primary-onConfirmSave-main` | 1 |
| 15 | testNavigateToCard | AlAhli Card / Mada Card | `testID-input-direct-mobile` / "Request Card" button | 2 |
| 16 | loginAndSetup | Notifications | `testID-input-direct-mobile` (login field) | 1 |
| 17 | testMemberPaysQattas | Pay Group Qatta | `testID-input-direct-mobile` | 1 |
| 18 | testOrderFromSuggest | DMP - Order From Suggest | `testID-tags-menu-2` / "Suggested" tab | 1 |
| 19 | testReorderFromHistory | DMP - Reorder from History | "View History" / `testID-secondary-action-main` | 1 |
| 20 | testPlaceOrderWithSelectedProduct | DMP - Place Order With Selected Product | Login/onboarding screen elements | 1 |
| 21 | testValidateResetPasscode | Change Phone Number Full Tier | Avatar element for user selection | 1 |

---

## Infrastructure Issues (P3)

| # | Test Name | Module/Suite | Error Description |
|---|-----------|-------------|-------------------|
| 1 | testSTCQuickNetRecharge | Telecom - STC | Network connectivity lost: "dial tcp 10.100.48.189:5289: connect: no route to host" |

---

## Test Data Issues (P4)

| # | Test Name | Module/Suite | Error Description |
|---|-----------|-------------|-------------------|
| 1 | testPlaceOrderWithPhysicalProduct | DMP - Place Order With Physical Product | Physical product SKU 'MYDR3AH/A' is out of stock or deep link not resolving |

---

## Recommended Actions

1. **Immediate (P1):** Investigate `testSearchWalletBeneficiary` app crash - potential null pointer or unhandled exception in wallet beneficiary search
2. **Backend Team:** All international transfer services (Western Union, MoneyGram, Tahweel, RIA) are returning "Service Unavailable" - likely SIT environment outage
3. **Dev Team:** Promo code validation logic needs review - valid promo codes returning expired/incorrect messages (6 failures across tiers)
4. **Dev Team:** Login flow appears broken for multiple tiers (Dashboard not visible after login) - possible session/auth issue
5. **Dev Team:** "Invite Friends" header changed to "Share your code" - verify if this is intentional UI copy change
6. **Dev Team:** Card benefits text updated for Platinum Card - verify if business content was updated
7. **UI/Locator Review:** Multiple `testID-primary-onConfirmSave-main` failures in SADAD flows suggest the confirm/save button testID may have changed
8. **DevOps:** STC network connectivity issue (no route to host) - check device farm network configuration
9. **Test Data:** Update physical product SKU in test data - current SKU is out of stock
