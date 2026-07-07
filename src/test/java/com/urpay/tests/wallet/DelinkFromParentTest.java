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
        ConfigManager c = ConfigManager.getInstance();
        boolean provisionFresh = Boolean.parseBoolean(c.get("delink.provisionFresh", "false"));
        if (provisionFresh) {
            // Registration case (kept in the code): provision + link a FRESH parent/kid pair every
            // run, then age the kid up to >15 in the DB (deliberately OUTSIDE the registration flow).
            LinkedPair pair = FamilyRegistrationApiHelper.provisionLinkedParentAndKid();
            Assert.assertNotNull(pair,
                    "Provisioning a fresh linked parent + kid pair failed — cannot run the delink flow "
                    + "(requires the neoleap VPN for the SIT API / simulators / Oracle DB)");
            FamilyRegistrationApiHelper.updateKidDateOfBirth(pair.kidPartyId(), "2010-06-01", "1431-06-17");
            kidMobile = toLocalMobile(pair.kidMobile());
            kidId = pair.kidPoi();
            log.info("Delink: provisioned fresh linked pair — kid mobile {} / poi {} (consumerId {})",
                    kidMobile, kidId, pair.kidConsumerId());
        } else {
            // Use a pre-provisioned, already-linked kid from config (for UI iteration; no VPN needed).
            kidMobile = c.get("delink.kid.mobileNumber");
            kidId = c.get("delink.kid.id");
            log.info("Delink: using configured kid creds {} / {} (fresh provisioning skipped)", kidMobile, kidId);
        }
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
        Assert.assertTrue(openUnlinkAlert(page),
                "The unlink-request alert popup should be displayed after tapping 'Unlink from parent'");

        page.tapSendRequest();
    }

    /**
     * Self-heal the profile → unlink → alert navigation: tapping the RN text can miss (or dismiss the
     * profile sheet back to the dashboard) without opening the confirmation popup. Re-open the
     * profile, scroll to the button and tap it until the alert renders, up to a few attempts.
     */
    @Step("Open the unlink-request alert (self-healing)")
    private boolean openUnlinkAlert(DelinkFromParentPage page) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            page.openProfile();
            if (!page.scrollToUnlink()) {
                log.warn("'Unlink from parent' button not reachable (attempt {}/3) — retrying", attempt);
                continue;
            }
            page.tapUnlink();
            if (page.isUnlinkAlertShown()) {
                return true;
            }
            log.warn("Unlink alert not shown after tap (attempt {}/3) — re-navigating", attempt);
        }
        return false;
    }

    @Step("Login as the delinking kid")
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
