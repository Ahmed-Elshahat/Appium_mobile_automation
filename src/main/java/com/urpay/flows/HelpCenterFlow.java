package com.urpay.flows;

import com.urpay.pages.settings.HelpCenterPage;

import io.qameta.allure.Step;

/** Composes the Help Center support-ticket journey. */
public class HelpCenterFlow {

    private final HelpCenterPage helpCenterPage;

    public HelpCenterFlow() {
        this.helpCenterPage = new HelpCenterPage();
    }

    @Step("Navigate to Wallet Management in Help Center")
    public HelpCenterPage openWalletManagement() {
        helpCenterPage.openWalletManagement();
        return helpCenterPage;
    }

    @Step("Open a Wallet Management support ticket")
    public HelpCenterPage openWalletManagementTicket(String description) {
        helpCenterPage.openWalletManagement();
        helpCenterPage.selectGeneralWalletIssue();
        helpCenterPage.submitTicket(description);
        return helpCenterPage;
    }
}