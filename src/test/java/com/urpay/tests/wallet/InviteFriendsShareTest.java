package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.InviteFriendsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Invite Friends — share invitation flow.
 *
 * <p>Migrated from Katalon suites (both verify the same Invite Friends → Share → options bottom
 * sheet, so they are consolidated into one representative test):
 * <ul>
 *   <li>Test Suites/.../WMVSuites/Invite Friends/verifyAnyUserCanInviteUsingReferralLink</li>
 *   <li>Test Suites/.../WMVSuites/Invite Friends/VerifyUserCanInviteNonPayUser</li>
 * </ul>
 *
 * <p>Login → More → Invite Friends → verify the screen title/tab/Share label → tap Share →
 * verify the "Invite Friends Options" title, subtitle and "Share Invitation" label → tap Share
 * Invitation (opens the system share sheet — the end of the Katalon flow).
 */
@Epic("Wallet & VAS")
@Feature("Invite Friends")
public class InviteFriendsShareTest extends BaseTest {

    @Test(groups = {"wallet", "invite-friends"}, priority = 1)
    @Story("User can invite friends via the Share Invitation options")
    @Description("Open Invite Friends, verify the screen and Share button, then verify the Invite "
            + "Friends Options bottom sheet and Share Invitation button")
    @Severity(SeverityLevel.NORMAL)
    public void testInviteFriendsShareOptions() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        InviteFriendsPage page = new InviteFriendsPage();
        page.navigateViaMore();

        Assert.assertEquals(page.getInviteFriendsTitle(), c.get("inviteFriends.title", "Invite Friends"),
                "Invite Friends screen title should be shown");
        Assert.assertEquals(page.getInviteFriendsTab(), c.get("inviteFriends.tab", "Invite Friends"),
                "Invite Friends header tab should be shown");
        Assert.assertEquals(page.getShareButtonText(), c.get("inviteFriends.shareBtn", "Share"),
                "Share button should be shown");

        page.tapShare();

        Assert.assertEquals(page.getOptionsTitle(), c.get("inviteFriends.optionsTitle"),
                "Invite Friends Options title should be shown");
        Assert.assertEquals(page.getOptionsSubtitle(), c.get("inviteFriends.optionsSubtitle"),
                "Invite Friends Options subtitle should match");
        Assert.assertEquals(page.getShareInvitationText(), c.get("inviteFriends.shareInvitationBtn"),
                "Share Invitation button should be shown");

        page.tapShareInvitation();
    }

    @Step("Login as the invite-friends user")
    private DashboardPage login() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("inviteFriends.mobileNumber"),
                c.get("inviteFriends.id"),
                c.get("inviteFriends.verificationCode", "1234"),
                c.get("inviteFriends.passCode", "2233"));
    }
}
