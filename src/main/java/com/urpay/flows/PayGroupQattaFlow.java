package com.urpay.flows;

import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.GroupQattaPage;

import io.qameta.allure.Step;

/**
 * Pay Group Qatta flow (two-user journey).
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/QattaSuites/PayGroupQattaSuite
 *
 * Phase 1 (admin): open the latest group, add two qattas, validate.
 * Phase 2 (member): open the latest group, pay both qattas.
 * Logout between phases switches users.
 */
public class PayGroupQattaFlow {

    private final GroupQattaPage groupPage = new GroupQattaPage();
    private final SettingsPage settingsPage = new SettingsPage();

    @Step("Add qatta '{qattaName}' to the latest group")
    public GroupQattaPage addQattaToLatestGroup(String qattaName, boolean openGroupFirst) {
        if (openGroupFirst) {
            groupPage.openLatestGroup();
        }
        groupPage.addQatta(qattaName);
        return groupPage;
    }

    @Step("Pay both qattas in the latest group")
    public GroupQattaPage payBothQattas(String verificationCode) {
        groupPage.openLatestGroup();
        groupPage.tapReceivedTab();
        groupPage.tapPayMultiple();
        groupPage.selectBothQattas();
        groupPage.tapNextMulti();
        groupPage.tapConfirmPay();
        groupPage.enterVerificationCode(verificationCode);
        return groupPage;
    }

    @Step("Logout: return to dashboard → More → Settings → Logout → confirm")
    public void logout() {
        groupPage.returnToDashboard();
        DashboardPage dashboard = new DashboardPage();
        dashboard.isLoaded();
        dashboard.navigateToMore();
        settingsPage.openSettings();
        settingsPage.tapLogout();
        settingsPage.confirmLogout();
    }
}
