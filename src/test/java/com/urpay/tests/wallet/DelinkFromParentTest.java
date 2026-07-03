package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.DelinkFromParentPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Delink From Parent — kid sends an unlink request.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/DelinkFromParentBySendRequest/DelinkFromParentByKid
 *   ({@code Wallet_VAS/DelinkFromParentBySendRequest/*}).
 *
 * <p>Kid logs in → opens the profile → scrolls to the "Unlink from parent" button → taps it →
 * verifies the unlink-request alert popup appears → sends the delink request.
 */
@Epic("Wallet & VAS")
@Feature("Delink From Parent")
public class DelinkFromParentTest extends BaseTest {

    @Test(groups = {"wallet", "delink", "family"}, priority = 1)
    @Story("Kid sends an unlink-from-parent request")
    @Description("Kid opens the profile, taps 'Unlink from parent', verifies the request alert popup, "
            + "and sends the delink request")
    @Severity(SeverityLevel.CRITICAL)
    public void testKidSendsUnlinkRequest() {
        DashboardPage dashboard = loginAsKid();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after kid login");

        DelinkFromParentPage page = new DelinkFromParentPage();
        page.openProfile();
        Assert.assertTrue(page.scrollToUnlink(),
                "The 'Unlink from parent' button should be reachable in the profile footer");

        page.tapUnlink();
        Assert.assertTrue(page.isUnlinkAlertShown(),
                "The unlink-request alert popup should be displayed");

        page.tapSendRequest();
    }

    @Step("Login as the delinking kid")
    private DashboardPage loginAsKid() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("delink.kid.mobileNumber"),
                c.get("delink.kid.id"),
                c.get("delink.kid.verificationCode", "1234"),
                c.get("delink.kid.passCode", "2233"));
    }
}
