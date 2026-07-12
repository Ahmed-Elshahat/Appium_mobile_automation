package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.ChangePasscodeFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.PasscodeResetApiHelper;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.settings.ChangePasscodePage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Change Passcode — shared linear tier flow.
 *
 * Migrated from Katalon suites:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/&lt;Tier&gt;Tier/ChangePasscode&lt;Tier&gt;Tier
 *
 * Each concrete tier (Default / Family / Full / Visitor) only differs by the login user, so
 * the whole journey lives here and the subclass supplies the config-key prefix via
 * {@link #keyPrefix()}. The methods are chained via priority + dependsOnMethods so they run
 * in the exact Katalon order on a single shared driver session:
 *   navigate → Current → New → Confirm → OTP → Thank You → re-login → restore → re-login.
 *
 * Test data (Katalon ChangePasscodeFor&lt;Tier&gt;User/setUpTestData):
 *   current passcode 2233, new passcode 3344, OTP 1234.
 */
public abstract class AbstractChangePasscodeTierTest extends BaseTest {

    /** Config-key prefix for this tier, e.g. {@code "changePasscode"} or {@code "changePasscodeFamily"}. */
    protected abstract String keyPrefix();

    private ConfigManager cfg() {
        return ConfigManager.getInstance();
    }

    private String currentPasscode() {
        return cfg().get(keyPrefix() + ".currentPasscode", "2233");
    }

    private String newPasscode() {
        return cfg().get(keyPrefix() + ".newPasscode", "3344");
    }

    private String otp() {
        return cfg().get(keyPrefix() + ".validOtp", "1234");
    }

    // ══════════════════════════════════════════════════
    //  PRE-SUITE: RESET PASSCODE TO KNOWN STATE (BEST-EFFORT)
    // ══════════════════════════════════════════════════

    /**
     * Reset the tier account's passcode to its known starting value via the backend API before
     * the UI journey runs (mirrors Katalon's setUpTestData). Best-effort: if the API chain fails
     * (e.g. network/endpoint unavailable), it is logged and skipped so the UI flow still runs.
     */
    @BeforeClass(alwaysRun = true)
    public void resetPasscodeViaApi() {
        boolean reset = PasscodeResetApiHelper.resetToDefaultPasscode(
                cfg().get(keyPrefix() + ".mobileNumber"),
                cfg().get(keyPrefix() + ".id"),
                cfg().get(keyPrefix() + ".poiType", "NAT"),
                otp(),
                cfg().get(keyPrefix() + ".dob", ""));
        if (!reset) {
            log.warn("Pre-suite passcode reset skipped/failed for {} — continuing with UI flow", keyPrefix());
        }
    }

    // ══════════════════════════════════════════════════
    //  1) LOGIN + NAVIGATE TO CHANGE PASSCODE
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "smoke"}, priority = 1)
    @Story("Navigate to Change Passcode")
    @Description("Login, open More → Settings → Change Passcode and verify the header")
    @Severity(SeverityLevel.CRITICAL)
    public void testNavigateToChangePasscodeScreen() {
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        ChangePasscodePage page = new ChangePasscodeFlow().navigateToChangePasscode();

        Assert.assertEquals(page.getHeaderText(), "Change Passcode",
                "Change Passcode header should be displayed");
    }

    // ══════════════════════════════════════════════════
    //  2) VERIFY CURRENT PASSCODE SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 2,
            dependsOnMethods = "testNavigateToChangePasscodeScreen")
    @Story("Current Passcode Screen")
    @Description("Verify the title and subtitle on the Current passcode screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyCurrentPasscodeScreenTexts() {
        ChangePasscodePage page = new ChangePasscodePage();

        Assert.assertEquals(page.getCurrentPasscodeSubtitle(), "Enter your current passcode",
                "Current passcode subtitle should match");
        Assert.assertEquals(page.getCurrentPasscodeTitle(), "Current passcode",
                "Current passcode title should match");
    }

    // ══════════════════════════════════════════════════
    //  3) ENTER CURRENT PASSCODE → NEW PASSCODE SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 3,
            dependsOnMethods = "testVerifyCurrentPasscodeScreenTexts")
    @Story("Enter Current Passcode")
    @Description("Enter the current passcode and verify navigation to the New passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterCurrentPasscodeNavigatesToNew() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(currentPasscode());

        Assert.assertTrue(page.isNewPasscodeScreenDisplayed(),
                "New passcode screen should be shown after entering current passcode");
    }

    // ══════════════════════════════════════════════════
    //  4) VERIFY NEW PASSCODE SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 4,
            dependsOnMethods = "testEnterCurrentPasscodeNavigatesToNew")
    @Story("New Passcode Screen")
    @Description("Verify the title and subtitle on the New passcode screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyNewPasscodeScreenTexts() {
        ChangePasscodePage page = new ChangePasscodePage();

        Assert.assertTrue(page.getNewPasscodeSubtitle().contains("Setup a new passcode for your account"),
                "New passcode subtitle should match");
        Assert.assertEquals(page.getNewPasscodeTitle(), "New Passcode",
                "New passcode title should match");
    }

    // ══════════════════════════════════════════════════
    //  5) ENTER NEW PASSCODE → CONFIRM SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 5,
            dependsOnMethods = "testVerifyNewPasscodeScreenTexts")
    @Story("Enter New Passcode")
    @Description("Enter the new passcode and verify navigation to the Confirm passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterNewPasscodeNavigatesToConfirm() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(newPasscode());

        Assert.assertTrue(page.isConfirmPasscodeScreenDisplayed(),
                "Confirm passcode screen should be shown after entering new passcode");
    }

    // ══════════════════════════════════════════════════
    //  6) VERIFY CONFIRM PASSCODE SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 6,
            dependsOnMethods = "testEnterNewPasscodeNavigatesToConfirm")
    @Story("Confirm Passcode Screen")
    @Description("Verify the title and subtitle on the Confirm new passcode screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyConfirmPasscodeScreenTexts() {
        ChangePasscodePage page = new ChangePasscodePage();

        Assert.assertEquals(page.getConfirmPasscodeTitle(), "Confirm new passcode",
                "Confirm passcode title should match");
        Assert.assertEquals(page.getConfirmPasscodeSubtitle(), "Re-enter your new passcode",
                "Confirm passcode subtitle should match");
    }

    // ══════════════════════════════════════════════════
    //  7) CONFIRM NEW PASSCODE → OTP SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 7,
            dependsOnMethods = "testVerifyConfirmPasscodeScreenTexts")
    @Story("Confirm New Passcode")
    @Description("Re-enter the new passcode to confirm and verify navigation to the OTP screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterConfirmPasscodeNavigatesToOtp() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(newPasscode());

        Assert.assertTrue(page.isOtpScreenDisplayed(),
                "OTP screen should be shown after confirming the new passcode");
    }

    // ══════════════════════════════════════════════════
    //  8) VERIFY OTP SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 8,
            dependsOnMethods = "testEnterConfirmPasscodeNavigatesToOtp")
    @Story("Verification Code Screen")
    @Description("Verify the 'Didn't get the code?' text on the OTP screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyOtpScreenTexts() {
        ChangePasscodePage page = new ChangePasscodePage();

        // The helper text uses a typographic apostrophe (’), so match on the stable substring
        // rather than an exact ASCII string.
        Assert.assertTrue(page.getDidntGetCodeText().contains("get the code"),
                "OTP screen helper text should match");
    }

    // ══════════════════════════════════════════════════
    //  9) ENTER VALID OTP → THANK YOU (+ verify texts)
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 9,
            dependsOnMethods = "testVerifyOtpScreenTexts")
    @Story("Enter Valid OTP")
    @Description("Enter the valid OTP, verify the Thank You screen and its texts before it auto-dismisses")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterValidOtpNavigatesToThankYou() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterOtp(otp());

        // The Thank You screen is transient: the app commits the change and logs the user out
        // within a couple of seconds. isThankYouScreenDisplayed() grabs a static XML snapshot
        // the instant the title appears, so assert its texts against that snapshot atomically
        // (per-element reads race against the dismiss → StaleElementReferenceException).
        Assert.assertTrue(page.isThankYouScreenDisplayed(),
                "Thank You screen should appear after entering the valid OTP");
        String snapshot = page.getThankYouSnapshot();
        Assert.assertTrue(snapshot.contains("Thank You"),
                "Thank You title should be present on the screen");
        Assert.assertTrue(snapshot.contains("passcode has been changed successfully"),
                "Thank You message should confirm the passcode change");
        Assert.assertTrue(snapshot.contains("Done"),
                "Done button should be present on the screen");
    }

    // ══════════════════════════════════════════════════
    //  10) COMPLETE CHANGE → RE-LOGIN → DASHBOARD
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 10,
            dependsOnMethods = "testEnterValidOtpNavigatesToThankYou")
    @Story("Complete Change Passcode")
    @Description("After the change the app logs out; re-login with the new passcode and verify the Dashboard")
    @Severity(SeverityLevel.CRITICAL)
    public void testCompleteChangeReturnsToDashboard() {
        // The change fully logs the user out (to the passcode re-login or the landing screen).
        // A full login that lands on the new passcode is the robust way back to the dashboard.
        DashboardPage dashboard = loginWithPasscode(newPasscode());

        Assert.assertTrue(dashboard.isLoaded(),
                "Dashboard should be visible after re-login with the new passcode");
    }

    // ══════════════════════════════════════════════════
    //  11) RESTORE DEFAULT PASSCODE → RE-LOGIN → DASHBOARD
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode"}, priority = 11,
            dependsOnMethods = "testCompleteChangeReturnsToDashboard")
    @Story("Restore Default Passcode")
    @Description("Change the passcode back to its original value, then re-login to restore a clean state")
    @Severity(SeverityLevel.CRITICAL)
    public void testRestoreDefaultPasscode() {
        ChangePasscodePage page = new ChangePasscodeFlow()
                .restoreDefaultPasscode(newPasscode(), currentPasscode(), otp());

        Assert.assertTrue(page.isThankYouScreenDisplayed(),
                "Thank You screen should appear after restoring the original passcode");

        // The app logs out again — re-login with the original passcode to leave a clean state.
        DashboardPage dashboard = loginWithPasscode(currentPasscode());
        Assert.assertTrue(dashboard.isLoaded(),
                "Dashboard should be visible after restoring and re-login with the original passcode");
    }

    // ══════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════

    @Step("Login with Change Passcode tier user")
    private DashboardPage login() {
        return loginWithPasscode(cfg().get(keyPrefix() + ".passCode", "2233"));
    }

    /** Full login (handles landing / passcode / dashboard states) ending on the given passcode. */
    @Step("Login with Change Passcode tier user (passcode {passcode})")
    private DashboardPage loginWithPasscode(String passcode) {
        ConfigManager config = cfg();
        return new LoginFlow().loginWith(
                config.get(keyPrefix() + ".mobileNumber"),
                config.get(keyPrefix() + ".id"),
                config.get(keyPrefix() + ".verificationCode", "1234"),
                passcode);
    }
}
