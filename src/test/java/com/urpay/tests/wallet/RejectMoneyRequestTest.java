package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.MoneyRequestFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.MoneyRequestPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Reject Money Request Suite.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/RequestMoney/Reject Money Request Test Suite
 *   (MoneyRequestSetupData → verifySendMoneyRequestByUser → VerifyMoneyRequestStatusBefore →
 *   MoneyRequestSetupData2 → verifyRejectMoneyRequest → verifyStatusAfterReject).
 *
 * <p>Three driver-shared phases:
 * <ul>
 *   <li>Phase 1 (requester): send a request, verify the "Thank You!" success screen, confirm it
 *       shows in the Sent tab as "Requested", then log out.</li>
 *   <li>Phase 2 (receiver): open the request list, reject the pending request, and verify the status
 *       becomes "Rejected", then log out.</li>
 *   <li>Phase 3 (requester): log back in, open the Sent tab and confirm the status is "Rejected".</li>
 * </ul>
 *
 * <p>Uses dedicated accounts (Katalon {@code RejectMoneySetuData}/{@code RejectMoneySetupData1}) so
 * the suite is parallel-safe against {@link SendMoneyRequestApproveTest}.
 */
@Epic("Wallet & VAS")
@Feature("Reject Money Request")
public class RejectMoneyRequestTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  1) REQUESTER: SEND REQUEST + VERIFY "Requested"
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request"}, priority = 1)
    @Story("Requester sends a money request")
    @Description("Login as requester, send a money request, verify the 'Thank You!' success screen, "
            + "confirm it shows in the Sent tab as 'Requested', then log out")
    @Severity(SeverityLevel.CRITICAL)
    public void testRequesterSendsMoneyRequest() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsRequester();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after requester login");

        MoneyRequestFlow flow = new MoneyRequestFlow();
        flow.navigateToMoneyRequestService();

        MoneyRequestPage page = flow.sendMoneyRequest(
                c.get("moneyRequestReject.recipientMobile"),
                c.get("moneyRequestReject.amount", "5.00"));

        Assert.assertTrue(page.isSuccessScreenShown(), "Success screen should be displayed");
        Assert.assertEquals(page.getThankYouText(),
                c.get("moneyRequestReject.thankYouText", "Thank You!"),
                "Success-screen title should be 'Thank You!'");
        Assert.assertEquals(page.getSuccessSubtitle(), c.get("moneyRequestReject.successSubtitle"),
                "Success-screen subtitle should match the expected message");
        page.tapDone();

        flow.openSentList();
        Assert.assertEquals(page.getSentAmount().replace(" ", ""),
                c.get("moneyRequestReject.expectedSentAmount", "5"),
                "Sent amount should match the requested amount");
        Assert.assertEquals(page.getLatestStatus(),
                c.get("moneyRequestReject.expectedStatusBefore", "Requested"),
                "Request status should be 'Requested' before the receiver responds");

        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  2) RECEIVER: REJECT + VERIFY "Rejected"
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request"}, priority = 2,
            dependsOnMethods = "testRequesterSendsMoneyRequest")
    @Story("Receiver rejects the money request")
    @Description("Login as the receiver, reject the pending request, verify the status becomes "
            + "'Rejected', then log out")
    @Severity(SeverityLevel.CRITICAL)
    public void testReceiverRejectsMoneyRequest() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsReceiver();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after receiver login");

        MoneyRequestFlow flow = new MoneyRequestFlow();
        MoneyRequestPage page = flow.rejectLatestRequest();

        Assert.assertEquals(page.getLatestStatus(),
                c.get("moneyRequestReject.expectedStatusAfterReject", "Rejected"),
                "Request status should be 'Rejected' after the receiver rejects it");

        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  3) REQUESTER: VERIFY "Rejected" IN THE SENT TAB
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request"}, priority = 3,
            dependsOnMethods = "testReceiverRejectsMoneyRequest")
    @Story("Requester confirms the rejected status")
    @Description("Log back in as the requester, open the Sent tab and confirm the status is 'Rejected'")
    @Severity(SeverityLevel.NORMAL)
    public void testRequesterSeesRejectedStatus() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsRequester();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after requester re-login");

        MoneyRequestFlow flow = new MoneyRequestFlow();
        MoneyRequestPage page = flow.openSentList();

        Assert.assertEquals(page.getLatestStatus(),
                c.get("moneyRequestReject.expectedStatusAfterReject", "Rejected"),
                "Sent-tab status should be 'Rejected' after the receiver rejects the request");
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login as the reject-flow requester")
    private DashboardPage loginAsRequester() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("moneyRequestReject.requester.mobileNumber"),
                c.get("moneyRequestReject.requester.id"),
                c.get("moneyRequestReject.requester.verificationCode", "1234"),
                c.get("moneyRequestReject.requester.passCode", "2233"));
    }

    @Step("Login as the reject-flow receiver")
    private DashboardPage loginAsReceiver() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("moneyRequestReject.receiver.mobileNumber"),
                c.get("moneyRequestReject.receiver.id"),
                c.get("moneyRequestReject.receiver.verificationCode", "1234"),
                c.get("moneyRequestReject.receiver.passCode", "2233"));
    }
}
