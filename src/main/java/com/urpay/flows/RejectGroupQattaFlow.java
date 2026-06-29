package com.urpay.flows;

import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.GroupQattaPage;

import io.qameta.allure.Step;

/**
 * Reject Group Qatta flow (two-user journey).
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/QattaSuites/RejectQroupQattaSuite
 *
 * Phase 1 (admin): open the latest group, add two qattas (same as the pay-group setup).
 * Phase 2 (member): open the latest group, reject each unpaid qatta from its detail screen.
 * Logout between phases switches users.
 *
 * Reject (Katalon ToRejectQuickQattaInReceivedTab / QattaGroup reject):
 *   open qatta detail → Reject (testID-secondary-action-main)
 *   → confirm reject (testID-primary-action-main) → "Qatta request has been rejected." → back.
 */
public class RejectGroupQattaFlow {

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

    @Step("Open the latest group and switch to the unpaid (received) qattas")
    public GroupQattaPage openGroupUnpaidQattas() {
        groupPage.openLatestGroup();
        groupPage.tapReceivedTab();
        return groupPage;
    }

    @Step("Reject the topmost unpaid qatta in the group")
    public GroupQattaPage rejectTopUnpaidQatta() {
        groupPage.openFirstUnpaidQatta();
        groupPage.tapReject();
        groupPage.tapConfirmReject();
        return groupPage;
    }

    @Step("Logout: deep link to Settings → Logout → confirm")
    public void logout() {
        settingsPage.openViaDeepLink();
        settingsPage.tapLogout();
        settingsPage.confirmLogout();
    }
}
