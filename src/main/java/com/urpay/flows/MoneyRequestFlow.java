package com.urpay.flows;

import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.MoneyRequestPage;

import io.qameta.allure.Step;

/**
 * Send Money Request And Approve flow (two-phase, single-driver journey).
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/RequestMoney/Send Money Request And Approve Test
 *   Suite (SendMoneyRequest/* test cases, no DB setup).
 *
 * <p>Phase 1 (requester): navigate to the Money Request service, send a request to a mobile number,
 * verify the success screen, then check it appears in the Sent tab as "Requested" and log out.
 * <p>Phase 2 (approver): open the request list, approve the pending request with an OTP, and verify
 * the "Approved" status.
 *
 * <p>Composes page actions into reusable journeys — no Thread.sleep, no assertions.
 */
public class MoneyRequestFlow {

    private final MoneyRequestPage moneyRequestPage = new MoneyRequestPage();
    private final SettingsPage settingsPage = new SettingsPage();

    @Step("Navigate Dashboard → Money Request service")
    public MoneyRequestPage navigateToMoneyRequestService() {
        new DashboardPage().dismissPopups();
        moneyRequestPage.openMoneyRequestService();
        return moneyRequestPage;
    }

    @Step("Send a money request of {amount} to {recipientMobile}")
    public MoneyRequestPage sendMoneyRequest(String recipientMobile, String amount) {
        moneyRequestPage.sendMoneyRequest(recipientMobile, amount);
        return moneyRequestPage;
    }

    @Step("Open the Sent tab of the Money Request list")
    public MoneyRequestPage openSentList() {
        moneyRequestPage.openSentTab();
        return moneyRequestPage;
    }

    @Step("Approve the latest pending money request")
    public MoneyRequestPage approveLatestRequest(String otp) {
        moneyRequestPage.openRequestList();
        moneyRequestPage.approveLatestRequest();
        moneyRequestPage.enterVerificationCode(otp);
        return moneyRequestPage;
    }

    @Step("Reject the latest pending money request")
    public MoneyRequestPage rejectLatestRequest() {
        moneyRequestPage.openRequestList();
        moneyRequestPage.rejectLatestRequest();
        return moneyRequestPage;
    }

    @Step("Kid sends a money request to the linked parent")
    public MoneyRequestPage kidRequestFromParent(String amount) {
        moneyRequestPage.kidRequestFromParent(amount);
        return moneyRequestPage;
    }

    @Step("Open the Money Request list (More → Request List → Money Request)")
    public MoneyRequestPage openRequestList() {
        moneyRequestPage.openRequestList();
        return moneyRequestPage;
    }

    @Step("Logout: deep link to Settings → Logout → confirm")
    public void logout() {
        settingsPage.openViaDeepLink();
        settingsPage.tapLogout();
        settingsPage.confirmLogout();
    }
}
