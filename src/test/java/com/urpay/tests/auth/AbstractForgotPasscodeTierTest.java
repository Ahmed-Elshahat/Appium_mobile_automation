package com.urpay.tests.auth;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.ForgotPasscodeFlow;
import com.urpay.helpers.PasscodeResetApiHelper;
import com.urpay.pages.auth.ForgotPasscodePage;
import com.urpay.pages.dashboard.DashboardPage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Forgot Passcode — shared linear tier flow.
 *
 * Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/&lt;Tier&gt;Tier/ForgotPasscode&lt;Tier&gt;Tier
 *
 * Each concrete tier (Default / Family / Full / Visitor) only differs by the login user, so the
 * whole journey lives here and the subclass supplies the config-key prefix via {@link #keyPrefix()}.
 * The methods are chained via priority + dependsOnMethods so they run in the exact Katalon order on
 * a single shared driver session:
 *   login → passcode screen texts → Forgot Passcode → User Verification texts → enter DOB →
 *   new passcode texts → enter new passcode → confirm screen texts → re-enter → success → dashboard.
 *
 * Test data (Katalon ForgotPasscodeFor&lt;Tier&gt;User/setUpTestData):
 *   DOB 30/05/1994, new passcode 3344, OTP 1234. The login NEVER enters the passcode (it takes the
 *   Forgot-Passcode path), so the account's live passcode does not block the journey.
 */
public abstract class AbstractForgotPasscodeTierTest extends BaseTest {

    /** Config-key prefix for this tier, e.g. {@code "forgotPasscode"} or {@code "forgotPasscodeFull"}. */
    protected abstract String keyPrefix();

    private ConfigManager cfg() {
        return ConfigManager.getInstance();
    }

    private String newPasscode() {
        return cfg().get(keyPrefix() + ".newPasscode", "3344");
    }

    private String otp() {
        return cfg().get(keyPrefix() + ".validOtp", "1234");
    }

    private String dobMonth() {
        return cfg().get(keyPrefix() + ".dob.month", "May");
    }

    private String dobDay() {
        return cfg().get(keyPrefix() + ".dob.day", "30");
    }

    private String dobYear() {
        return cfg().get(keyPrefix() + ".dob.year", "1994");
    }

    // ══════════════════════════════════════════════════
    //  PRE-SUITE: RESET PASSCODE TO KNOWN STATE (BEST-EFFORT)
    // ══════════════════════════════════════════════════

    /**
     * Reset the tier account's passcode to its known starting value via the backend API before the
     * UI journey runs (mirrors Katalon's setUpTestData). Best-effort: if the API chain fails (e.g.
     * network/endpoint unavailable) it is logged and skipped. The Forgot-Passcode journey does not
     * depend on this because the login never enters the passcode.
     */
    @BeforeClass(alwaysRun = true)
    public void resetPasscodeViaApi() {
        boolean reset = PasscodeResetApiHelper.resetToDefaultPasscode(
                cfg().get(keyPrefix() + ".mobileNumber"),
                cfg().get(keyPrefix() + ".id"),
                cfg().get(keyPrefix() + ".poiType", "NAT"),
                otp());
        if (!reset) {
            log.warn("Pre-suite passcode reset skipped/failed for {} — continuing with UI flow", keyPrefix());
        }
    }

    // ══════════════════════════════════════════════════
    //  1) LOGIN UNTIL PASSCODE SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode", "smoke"}, priority = 1)
    @Story("Login Till Passcode Screen")
    @Description("Login with credentials + OTP and stop on the passcode screen (no passcode entry)")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginUntilPasscodeScreen() {
        ForgotPasscodePage page = login();

        Assert.assertTrue(page.isPasscodeScreenDisplayed(),
                "Passcode screen should be displayed after login");
    }

    // ══════════════════════════════════════════════════
    //  2) VERIFY PASSCODE SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 2,
            dependsOnMethods = "testLoginUntilPasscodeScreen")
    @Story("Passcode Screen")
    @Description("Verify the 'Language' text on the passcode screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyLanguageText() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getLanguageText(), "Language",
                "Language text should be displayed on the passcode screen");
    }

    @Test(groups = {"auth", "passcode"}, priority = 3,
            dependsOnMethods = "testVerifyLanguageText")
    @Story("Passcode Screen")
    @Description("Verify the 'Passcode' text on the passcode screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyPasscodeText() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getPasscodeText(), "Passcode",
                "Passcode text should be displayed on the passcode screen");
    }

    // ══════════════════════════════════════════════════
    //  3) TAP FORGOT PASSCODE → USER VERIFICATION SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 4,
            dependsOnMethods = "testVerifyPasscodeText")
    @Story("Forgot Passcode")
    @Description("Tap 'Forgot your passcode?' and verify the User Verification screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testTapForgotPasscodeShowsUserVerification() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.tapForgotPasscode();

        Assert.assertTrue(page.isUserVerificationScreenDisplayed(),
                "User Verification screen should be displayed");
        Assert.assertEquals(page.getUserVerificationTitle(), "User Verification",
                "User Verification title should match");
    }

    // ══════════════════════════════════════════════════
    //  4) VERIFY USER VERIFICATION SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 5,
            dependsOnMethods = "testTapForgotPasscodeShowsUserVerification")
    @Story("User Verification Screen")
    @Description("Verify the prompt text below the User Verification header")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyTextBelowUserVerification() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getUserVerificationSubtitle(),
                "Please answer the following questions to verify you",
                "User Verification subtitle should match");
    }

    @Test(groups = {"auth", "passcode"}, priority = 6,
            dependsOnMethods = "testVerifyTextBelowUserVerification")
    @Story("User Verification Screen")
    @Description("Verify the 'Switch to Hijri' toggle text")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyToggleButtonText() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getToggleButtonText(), "Switch to Hijri",
                "Toggle button text should match");
    }

    @Test(groups = {"auth", "passcode"}, priority = 7,
            dependsOnMethods = "testVerifyToggleButtonText")
    @Story("User Verification Screen")
    @Description("Verify the 'Next' button text")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyNextButtonText() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getNextButtonText(), "Next",
                "Next button text should match");
    }

    // ══════════════════════════════════════════════════
    //  5) ENTER VALID DOB → ENTER NEW PASSCODE SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 8,
            dependsOnMethods = "testVerifyNextButtonText")
    @Story("Enter Date Of Birth")
    @Description("Enter a valid date of birth and verify navigation to the Enter New Passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterValidDobNavigatesToNewPasscode() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterDateOfBirth(dobMonth(), dobDay(), dobYear());

        Assert.assertTrue(page.isEnterNewPasscodeScreenDisplayed(),
                "Enter New Passcode screen should be shown after entering a valid date of birth");
    }

    // ══════════════════════════════════════════════════
    //  6) VERIFY ENTER NEW PASSCODE SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 9,
            dependsOnMethods = "testEnterValidDobNavigatesToNewPasscode")
    @Story("Enter New Passcode Screen")
    @Description("Verify the prompt text below the Enter New Passcode header")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyTextBelowEnterNewPasscode() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getTextBelowHeader(), "Enter your wallet passcode.",
                "Enter New Passcode subtitle should match");
    }

    // ══════════════════════════════════════════════════
    //  7) ENTER NEW PASSCODE → CONFIRM SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 10,
            dependsOnMethods = "testVerifyTextBelowEnterNewPasscode")
    @Story("Enter New Passcode")
    @Description("Enter the new passcode and verify navigation to the Confirm passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterNewPasscodeNavigatesToConfirm() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterPasscodeOnKeypad(newPasscode());

        Assert.assertTrue(page.isConfirmPasscodeScreenDisplayed(),
                "Confirm passcode screen should be shown after entering the new passcode");
    }

    // ══════════════════════════════════════════════════
    //  8) VERIFY CONFIRM PASSCODE SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 11,
            dependsOnMethods = "testEnterNewPasscodeNavigatesToConfirm")
    @Story("Confirm Passcode Screen")
    @Description("Verify the header text on the Confirm new passcode screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyConfirmHeaderText() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getEnterNewPasscodeHeader(), "Confirm New Passcode",
                "Confirm passcode header should match");
    }

    @Test(groups = {"auth", "passcode"}, priority = 12,
            dependsOnMethods = "testVerifyConfirmHeaderText")
    @Story("Confirm Passcode Screen")
    @Description("Verify the prompt text below the header on the Confirm new passcode screen")
    @Severity(SeverityLevel.NORMAL)
    public void testVerifyConfirmTextBelowHeader() {
        ForgotPasscodePage page = new ForgotPasscodePage();

        Assert.assertEquals(page.getTextBelowHeader(), "please enter the value",
                "Confirm passcode subtitle should match");
    }

    // ══════════════════════════════════════════════════
    //  9) RE-ENTER NEW PASSCODE → SUCCESS TOAST → DASHBOARD
    // ══════════════════════════════════════════════════

    @Test(groups = {"auth", "passcode"}, priority = 13,
            dependsOnMethods = "testVerifyConfirmTextBelowHeader")
    @Story("Confirm New Passcode")
    @Description("Re-enter the new passcode, verify the success toast and navigation to the Dashboard")
    @Severity(SeverityLevel.CRITICAL)
    public void testReenterPasscodeShowsSuccessAndDashboard() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterPasscodeOnKeypad(newPasscode());

        // The success toast is transient — read it immediately after re-entering the passcode.
        String message = page.getNotificationMessage(20);
        Assert.assertEquals(message, "Passcode successfully updated",
                "Success toast should confirm the passcode was updated");

        DashboardPage dashboard = new DashboardPage();
        Assert.assertTrue(dashboard.isLoaded(),
                "Dashboard should be displayed after the passcode reset");
    }

    // ══════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════

    @Step("Login until the passcode screen with the Forgot Passcode tier user")
    private ForgotPasscodePage login() {
        ConfigManager config = cfg();
        return new ForgotPasscodeFlow().loginUntilPasscodeScreen(
                config.get(keyPrefix() + ".mobileNumber"),
                config.get(keyPrefix() + ".id"),
                config.get(keyPrefix() + ".verificationCode", "1234"));
    }
}
