package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.FamilyRegistrationApiHelper;
import com.urpay.helpers.FamilyRegistrationApiHelper.LinkedPair;
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
 * <p>Registration case: a FRESH parent + kid pair is provisioned and linked via the backend before
 * the test (generated accounts every run), then the kid logs in → opens the profile → scrolls to
 * the "Unlink from parent" button → taps it → verifies the unlink-request alert popup → sends the
 * delink request.
 */
@Epic("Wallet & VAS")
@Feature("Delink From Parent")
public class DelinkFromParentTest extends BaseTest {

    /** Kid credentials from the freshly provisioned + linked pair (generated each run). */
    private String kidMobile;
    private String kidId;

    @BeforeClass(alwaysRun = true)
    public void provisionFreshLinkedPair() {
        LinkedPair pair = FamilyRegistrationApiHelper.provisionLinkedParentAndKid();
        Assert.assertNotNull(pair,
                "Provisioning a fresh linked parent + kid pair failed — cannot run the delink flow "
                + "(requires the neoleap VPN for the SIT API / simulators / Oracle DB)");
        // The kid registers young (so the family link settles), then it is aged up to >15 in the DB
        // for the delink scenario. This DB update is deliberately OUTSIDE the registration flow.
        FamilyRegistrationApiHelper.updateKidDateOfBirth(pair.kidPartyId(), "2010-06-01", "1431-06-17");
        kidMobile = toLocalMobile(pair.kidMobile());
        kidId = pair.kidPoi();
        log.info("Delink: provisioned fresh linked pair — kid mobile {} / poi {} (consumerId {})",
                kidMobile, kidId, pair.kidConsumerId());
    }

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

    @Step("Login as the freshly provisioned kid")
    private DashboardPage loginAsKid() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                kidMobile,
                kidId,
                c.get("delink.kid.verificationCode", "1234"),
                c.get("delink.kid.passCode", "2233"));
    }

    /** Convert the generated {@code +966XXXXXXXXX} mobile to the local {@code 05XXXXXXXX} login form. */
    private static String toLocalMobile(String intlMobile) {
        if (intlMobile != null && intlMobile.startsWith("+966")) {
            return "0" + intlMobile.substring(4);
        }
        return intlMobile;
    }
}
