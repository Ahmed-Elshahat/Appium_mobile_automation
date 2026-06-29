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
 * Send Money Request And Approve Suite.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/RequestMoney/Send Money Request And Approve Test
 *   Suite (SendMoneyRequest/* test cases + ToValidateLoginForRemoteDevicesOnly, no DB setup).
 *
 * <p>Consolidated into two driver-shared phases:
 * <ul>
 *   <li>Phase 1 (requester): send a request, verify the "Thank You!" success screen, then verify it
 *       appears in the Sent tab as "Requested", and log out (Katalon MoneyRequestSetupData →
 *       login → toValidateMoveToDashboardServices → scrollTillRequestMoneyAndTap →
 *       SentMoneyRequest → VerifyMoneyRequestSentSuccessfulScreenDetails →
 *       navigateToSentMoneyRequestTab → VerifyMoneyRequestSentToUserFromSentMoneyRequestTab →
 *       logoutFromAnyScreen).</li>
 *   <li>Phase 2 (approver = recipient): approve the pending request with an OTP and verify the
 *       "Approved" status (Katalon MoneyRequestSetupData2 → login → navigateToReceivedMoneyTab →
 *       verifyUserApproveRequest → VerifyRequestStatusAfterApproveTheRequest).</li>
 * </ul>
 */
@Epic("Wallet & VAS")
@Feature("Send Money Request And Approve")
public class SendMoneyRequestApproveTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  1) REQUESTER: SEND REQUEST + VERIFY "Requested"
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request", "smoke"}, priority = 1)
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
                c.get("moneyRequest.recipientMobile"),
                c.get("moneyRequest.amount", "5.00"));

        Assert.assertTrue(page.isSuccessScreenShown(), "Success screen should be displayed");
        Assert.assertEquals(page.getThankYouText(),
                c.get("moneyRequest.thankYouText", "Thank You!"),
                "Success-screen title should be 'Thank You!'");
        Assert.assertEquals(page.getSuccessSubtitle(), c.get("moneyRequest.successSubtitle"),
                "Success-screen subtitle should match the expected message");
        page.tapDone();

        flow.openSentList();
        Assert.assertEquals(page.getSentAmount().replace(" ", ""),
                c.get("moneyRequest.expectedSentAmount", "5"),
                "Sent amount should match the requested amount");
        Assert.assertEquals(page.getLatestStatus(),
                c.get("moneyRequest.expectedStatusBefore", "Requested"),
                "Request status should be 'Requested' before approval");

        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  2) APPROVER (= recipient): APPROVE + VERIFY "Approved"
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request"}, priority = 2,
            dependsOnMethods = "testRequesterSendsMoneyRequest")
    @Story("Approver approves the money request")
    @Description("Login as the recipient, approve the pending request with an OTP, verify the success "
            + "message and that the status becomes 'Approved'")
    @Severity(SeverityLevel.CRITICAL)
    public void testApproverApprovesMoneyRequest() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsApprover();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after approver login");

        MoneyRequestFlow flow = new MoneyRequestFlow();
        MoneyRequestPage page = flow.approveLatestRequest(c.get("moneyRequest.otp", "1234"));

        Assert.assertEquals(page.getNotificationMessage(20),
                c.get("moneyRequest.approveSuccessMessage", "Request has been approved successfully."),
                "Approval success message should be displayed");
        Assert.assertEquals(page.getLatestStatus(),
                c.get("moneyRequest.expectedStatusAfter", "Approved"),
                "Request status should be 'Approved' after approval");
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login as the money-request requester")
    private DashboardPage loginAsRequester() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("moneyRequest.requester.mobileNumber"),
                c.get("moneyRequest.requester.id"),
                c.get("moneyRequest.requester.verificationCode", "1234"),
                c.get("moneyRequest.requester.passCode", "2233"));
    }

    @Step("Login as the money-request approver (recipient)")
    private DashboardPage loginAsApprover() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("moneyRequest.approver.mobileNumber"),
                c.get("moneyRequest.approver.id"),
                c.get("moneyRequest.approver.verificationCode", "1234"),
                c.get("moneyRequest.approver.passCode", "2233"));
    }
}
