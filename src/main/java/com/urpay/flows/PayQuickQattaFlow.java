package com.urpay.flows;

import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.QuickQattaPage;

import io.qameta.allure.Step;

/**
 * Pay Quick (single) Qatta flow (two-user journey).
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/QattaSuites/PayQuickQattaSuite
 *
 * Phase 1 (sender): create a single qatta to one contact, then logout.
 * Phase 2 (receiver): open the received tab and pay the qatta.
 */
public class PayQuickQattaFlow {

    private final QuickQattaPage quickPage = new QuickQattaPage();
    private final SettingsPage settingsPage = new SettingsPage();

    @Step("Create quick qatta '{qattaName}' to {recipientMobile}")
    public QuickQattaPage createQuickQatta(String qattaName, String recipientMobile,
                                           String recipientName) {
        quickPage.createQuickQatta(qattaName, recipientMobile, recipientName);
        return quickPage;
    }

    @Step("Pay the received quick qatta")
    public QuickQattaPage payReceivedQatta(String verificationCode) {
        quickPage.payLatestReceivedQatta();
        quickPage.enterVerificationCode(verificationCode);
        return quickPage;
    }

    @Step("Logout: return to dashboard → More → Settings → Logout → confirm")
    public void logout() {
        quickPage.returnToDashboard();
        DashboardPage dashboard = new DashboardPage();
        dashboard.isLoaded();
        dashboard.navigateToMore();
        settingsPage.openSettings();
        settingsPage.tapLogout();
        settingsPage.confirmLogout();
    }
}
