package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.InvitationCodeFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.auth.RegistrationPage;
import com.urpay.pages.dashboard.DashboardPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Registration Using Invitation Code — verifies the referral/invitation code flow:
 *   Login → Navigate to Invite Friends → Get Referral Code →
 *   Verify Invitation Code Matches → Register New User with Invitation Code.
 *
 * Katalon source: Test Suites/WMVSuites/RegisterationUsingInvitationCode
 */
@Epic("Wallet & VAS")
@Feature("Invitation Code")
public class RegistrationUsingInvitationCodeTest extends BaseTest {

    private String referralCode;
    private String invitationCode;

    // ══════════════════════════════════════════════════
    //  TEST 1: NAVIGATE TO INVITE FRIENDS AND GET REFERRAL CODE
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "referral", "smoke"}, priority = 1)
    @Story("Navigate to Invite Friends")
    @Description("Login and navigate to Invite Friends page, verify referral code is present")
    @Severity(SeverityLevel.CRITICAL)
    public void testNavigateToInviteFriendsAndGetReferralCode() {
        ConfigManager config = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                config.get("referral.mobileNumber"),
                config.get("referral.id"),
                config.get("referral.verificationCode", "1234"),
                config.get("referral.passCode", "2233")
        );
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        InvitationCodeFlow flow = new InvitationCodeFlow();
        referralCode = flow.navigateAndGetReferralCode();

        Assert.assertNotNull(referralCode, "Referral code should not be null");
        Assert.assertFalse(referralCode.isEmpty(), "Referral code should not be empty");
    }

    // ══════════════════════════════════════════════════
    //  TEST 2: VERIFY INVITATION CODE MATCHES REFERRAL CODE
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "referral"}, priority = 2,
            dependsOnMethods = "testNavigateToInviteFriendsAndGetReferralCode")
    @Story("Verify Invitation Code Matches Referral Code")
    @Description("Switch to Invite Friends tab and verify invitation code equals referral code")
    @Severity(SeverityLevel.CRITICAL)
    public void testInvitationCodeMatchesReferralCode() {
        InvitationCodeFlow flow = new InvitationCodeFlow();
        invitationCode = flow.getInvitationCode();

        Assert.assertNotNull(invitationCode, "Invitation code should not be null");
        Assert.assertEquals(invitationCode, referralCode,
                "Invitation code should match referral code");
    }

    // ══════════════════════════════════════════════════
    //  TEST 3: REGISTER NEW USER WITH INVITATION CODE
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "referral"}, priority = 3,
            dependsOnMethods = "testInvitationCodeMatchesReferralCode")
    @Story("Register with Invitation Code")
    @Description("Logout, navigate to registration page, and enter invitation code")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegisterNewUserWithInvitationCode() {
        InvitationCodeFlow flow = new InvitationCodeFlow();
        RegistrationPage regPage = flow.registerWithInvitationCode(invitationCode);

        Assert.assertTrue(regPage.isInvitationCodeInputVisible(),
                "Invitation code input should be visible after entering code");
    }
}
