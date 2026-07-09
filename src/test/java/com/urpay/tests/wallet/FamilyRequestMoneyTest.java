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
 * Family Wallet Request Money.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/RequestMoney/Family Wallet Request Money Test Suite
 *   ({@code Wallet_VAS/MoneyRequest/KidValidationTCs/*} + {@code ParentValidationTCs/*}).
 *
 * <p>Three driver-shared phases:
 * <ul>
 *   <li>Phase 1 (kid): request money from the linked parent, verify it shows as "Requested", log out.</li>
 *   <li>Phase 2 (parent): approve the kid's request with an OTP, verify "Approved", log out.</li>
 *   <li>Phase 3 (kid): log back in and confirm the request status is "Approved".</li>
 * </ul>
 *
 * <p>Requires a LINKED parent/kid pair. Runs in the sequential {@code family-wallet.xml} suite (not
 * the parallel aggregate) because it shares scarce family accounts.
 */
@Epic("Wallet & VAS")
@Feature("Family Request Money")
public class FamilyRequestMoneyTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  1) KID: REQUEST FROM PARENT + VERIFY "Requested"
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request", "family"}, priority = 1)
    @Story("Kid requests money from the parent")
    @Description("Login as kid, request money from the linked parent, verify the request shows as "
            + "'Requested', then log out")
    @Severity(SeverityLevel.CRITICAL)
    public void testKidRequestsMoneyFromParent() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsKid();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after kid login");

        MoneyRequestFlow flow = new MoneyRequestFlow();
        flow.kidRequestFromParent(c.get("familyRequestMoney.amount", "5"));

        MoneyRequestPage page = flow.openSentRequestList();
        Assert.assertEquals(page.getLatestStatus(),
                c.get("familyRequestMoney.expectedStatusBefore", "Requested"),
                "Kid's request status should be 'Requested' before parent approval");

        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  2) PARENT: APPROVE + VERIFY "Approved"
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request", "family"}, priority = 2,
            dependsOnMethods = "testKidRequestsMoneyFromParent")
    @Story("Parent approves the kid's request")
    @Description("Login as the parent, approve the kid's pending request with an OTP, verify the "
            + "status becomes 'Approved', then log out")
    @Severity(SeverityLevel.CRITICAL)
    public void testParentApprovesKidRequest() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsParent();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent login");

        MoneyRequestFlow flow = new MoneyRequestFlow();
        MoneyRequestPage page = flow.approveLatestRequest(c.get("familyRequestMoney.otp", "1234"));

        Assert.assertEquals(page.getLatestStatus(),
                c.get("familyRequestMoney.expectedStatusAfter", "Approved"),
                "Request status should be 'Approved' after parent approval");

        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  3) KID: VERIFY "Approved"
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "money-request", "family"}, priority = 3,
            dependsOnMethods = "testParentApprovesKidRequest")
    @Story("Kid confirms the approved status")
    @Description("Log back in as the kid and confirm the request status is 'Approved'")
    @Severity(SeverityLevel.NORMAL)
    public void testKidSeesApprovedStatus() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsKid();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after kid re-login");

        MoneyRequestFlow flow = new MoneyRequestFlow();
        MoneyRequestPage page = flow.openSentRequestList();

        Assert.assertEquals(page.getLatestStatus(),
                c.get("familyRequestMoney.expectedStatusAfter", "Approved"),
                "Kid's request status should be 'Approved' after parent approval");
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login as the requesting kid")
    private DashboardPage loginAsKid() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("familyRequestMoney.kid.mobileNumber"),
                c.get("familyRequestMoney.kid.id"),
                c.get("familyRequestMoney.kid.verificationCode", "1234"),
                c.get("familyRequestMoney.kid.passCode", "2233"));
    }

    @Step("Login as the approving parent")
    private DashboardPage loginAsParent() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("familyRequestMoney.parent.mobileNumber"),
                c.get("familyRequestMoney.parent.id"),
                c.get("familyRequestMoney.parent.verificationCode", "1234"),
                c.get("familyRequestMoney.parent.passCode", "2233"));
    }
}
