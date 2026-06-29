package com.urpay.flows;

import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.QuickQattaPage;

import io.qameta.allure.Step;

/**
 * Reject Quick (single) Qatta flow (two-user journey).
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/QattaSuites/RejectQuickQattaSuite
 *
 * Phase 1 (sender): create a single qatta to one contact, then logout.
 * Phase 2 (receiver): open the received qatta and reject it (no OTP — reject needs no verification).
 *
 * Reject (Katalon ToRejectQuickQattaInReceivedTab):
 *   receivedTab → latest qatta → Reject (testID-secondary-action-main)
 *   → confirm reject (testID-primary-action-main) → "Qatta request has been rejected." → back.
 */
public class RejectQuickQattaFlow {

    private final QuickQattaPage quickPage = new QuickQattaPage();
    private final SettingsPage settingsPage = new SettingsPage();

    @Step("Create quick qatta '{qattaName}' to {recipientMobile}")
    public QuickQattaPage createQuickQatta(String qattaName, String recipientMobile,
                                           String recipientName) {
        quickPage.createQuickQatta(qattaName, recipientMobile, recipientName);
        return quickPage;
    }

    @Step("Reject the received quick qatta")
    public QuickQattaPage rejectReceivedQatta() {
        quickPage.rejectLatestReceivedQatta();
        return quickPage;
    }

    @Step("Logout: deep link to Settings → Logout → confirm")
    public void logout() {
        settingsPage.openViaDeepLink();
        settingsPage.tapLogout();
        settingsPage.confirmLogout();
    }
}
