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
 * Change Passcode — shared linear NEGATIVE tier flow.
 *
 * Migrated from Katalon suites:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/&lt;Tier&gt;Tier/ChangePasscode&lt;Tier&gt;Tier-NegativeCases
 *
 * IMPORTANT — all four negative-case suites (.ts) are byte-identical: every one references the
 * same {@code ChangePasscodeForDefaultUser/setUpTestData -NegativeCases} setup and the same shared
 * negative test cases, so they all log in as the Default negative user (0520403447 / 4111538080)
 * and run the exact same 10-step flow. The concrete tier subclasses therefore differ only by their
 * config-key prefix (all populated with the same Default negative creds) so each named suite stays
 * independently runnable, mirroring {@link AbstractChangePasscodeTierTest}.
 *
 * Linked Katalon test cases (in order):
 *   1. setUpTestData -NegativeCases ............ backend passcode reset to 2233 (here: API best-effort)
 *   2. ToValidateLoginForRemoteDevicesOnly ..... login
 *   3. toNavigateToPasscodeScreenAndVerifyTheHeader ... More → Settings → Change Passcode
 *   4. enterInvalidPasscodeInCurrentPasscodeScreenAndverifyAlertMessage
 *   5. enterCurrentPasscodeAndNavigateToNewPassCodeScreen
 *   6. enterConsecutiveNumbersInNewPasscodeScreenAndVerifyAlertMessage
 *   7. enterCurrentPasscodeAndNavigateToConfirmPassCodeScreen
 *   8. enterConsecutiveNumbersInConfirmNewPasscodeScreenAndVerifyAlertMessage
 *   9. enterCurrentPasscodeInConfirmPassCodeScreenAndNavigateToOTPScreen
 *  10. enterInvalidOTPAndVerifyAlertMessage
 *
 * The negative journey never commits a passcode change (it ends on an invalid OTP), so the
 * account is left at its original passcode (2233). Tests share one driver session and run in the
 * exact Katalon order via priority + dependsOnMethods.
 *
 * Test data (Katalon ChangePasscodeForDefaultUser/setUpTestData -NegativeCases):
 *   current passcode 2233, invalid passcode 2365, consecutive digits 1111, invalid OTP 1122.
 */
public abstract class AbstractChangePasscodeTierNegativeTest extends BaseTest {

    /** Config-key prefix for this tier, e.g. {@code "changePasscodeNegative"}. */
    protected abstract String keyPrefix();

    // Katalon SmartUIValidator.verifyEqual expected popup messages.
    private static final String INVALID_CURRENT_PASSCODE_MSG =
            "The entered passcode is incorrect. Please try again";
    private static final String CONSECUTIVE_PASSCODE_MSG =
            "The passcode should not be in sequence or consecutive";
    private static final String PASSCODE_NOT_MATCHED_MSG =
            "The passcode is not matched";

    private ConfigManager cfg() {
        return ConfigManager.getInstance();
    }

    private String currentPasscode() {
        return cfg().get(keyPrefix() + ".currentPasscode", "2233");
    }

    private String invalidPasscode() {
        return cfg().get(keyPrefix() + ".invalidPasscode", "2365");
    }

    private String consecutiveNumber() {
        return cfg().get(keyPrefix() + ".consecutiveNumber", "1111");
    }

    private String invalidOtp() {
        return cfg().get(keyPrefix() + ".invalidOtp", "1122");
    }

    // ══════════════════════════════════════════════════
    //  1) PRE-SUITE: RESET PASSCODE TO KNOWN STATE (BEST-EFFORT)
    // ══════════════════════════════════════════════════

    @BeforeClass(alwaysRun = true)
    public void resetPasscodeViaApi() {
        boolean reset = PasscodeResetApiHelper.resetToDefaultPasscode(
                cfg().get(keyPrefix() + ".mobileNumber"),
                cfg().get(keyPrefix() + ".id"),
                cfg().get(keyPrefix() + ".poiType", "NAT"),
                cfg().get(keyPrefix() + ".verificationCode", "1234"),
                cfg().get(keyPrefix() + ".dob", ""));
        if (!reset) {
            log.warn("Pre-suite passcode reset skipped/failed for {} — continuing with UI flow", keyPrefix());
        }
    }

    // ══════════════════════════════════════════════════
    //  2-3) LOGIN + NAVIGATE TO CHANGE PASSCODE
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 1)
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
    //  4) INVALID CURRENT PASSCODE → ERROR
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 2,
            dependsOnMethods = "testNavigateToChangePasscodeScreen")
    @Story("Invalid Current Passcode")
    @Description("Enter an invalid current passcode and verify the 'incorrect passcode' alert message")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidCurrentPasscodeShowsError() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(invalidPasscode());

        Assert.assertEquals(page.getNotificationMessage(15), INVALID_CURRENT_PASSCODE_MSG,
                "Invalid current passcode alert message should match");
    }

    // ══════════════════════════════════════════════════
    //  5) VALID CURRENT PASSCODE → NEW PASSCODE SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 3,
            dependsOnMethods = "testInvalidCurrentPasscodeShowsError")
    @Story("Enter Current Passcode")
    @Description("Enter the valid current passcode and verify navigation to the New passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testCurrentPasscodeNavigatesToNew() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(currentPasscode());

        Assert.assertTrue(page.isNewPasscodeScreenDisplayed(),
                "New passcode screen should be shown after entering the current passcode");
        Assert.assertEquals(page.getNewPasscodeTitle(), "New Passcode",
                "New passcode title should match");
    }

    // ══════════════════════════════════════════════════
    //  6) CONSECUTIVE DIGITS ON NEW SCREEN → ERROR
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 4,
            dependsOnMethods = "testCurrentPasscodeNavigatesToNew")
    @Story("Consecutive New Passcode")
    @Description("Enter consecutive digits on the New passcode screen and verify the alert message")
    @Severity(SeverityLevel.NORMAL)
    public void testConsecutiveNewPasscodeShowsError() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(consecutiveNumber());

        Assert.assertEquals(page.getNotificationMessage(15), CONSECUTIVE_PASSCODE_MSG,
                "Consecutive new passcode alert message should match");
    }

    // ══════════════════════════════════════════════════
    //  7) ENTER NEW PASSCODE → CONFIRM SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 5,
            dependsOnMethods = "testConsecutiveNewPasscodeShowsError")
    @Story("Enter New Passcode")
    @Description("Enter a valid new passcode and verify navigation to the Confirm passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testNewPasscodeNavigatesToConfirm() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(currentPasscode());

        Assert.assertTrue(page.isConfirmPasscodeScreenDisplayed(),
                "Confirm passcode screen should be shown after entering the new passcode");
    }

    // ══════════════════════════════════════════════════
    //  8) CONSECUTIVE DIGITS ON CONFIRM SCREEN → ERROR
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 6,
            dependsOnMethods = "testNewPasscodeNavigatesToConfirm")
    @Story("Mismatched Confirm Passcode")
    @Description("Enter consecutive (mismatched) digits on the Confirm screen and verify the alert message")
    @Severity(SeverityLevel.NORMAL)
    public void testConsecutiveConfirmPasscodeShowsError() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(consecutiveNumber());

        Assert.assertEquals(page.getNotificationMessage(15), PASSCODE_NOT_MATCHED_MSG,
                "Mismatched confirm passcode alert message should match");
    }

    // ══════════════════════════════════════════════════
    //  9) MATCHING CONFIRM PASSCODE → OTP SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 7,
            dependsOnMethods = "testConsecutiveConfirmPasscodeShowsError")
    @Story("Confirm New Passcode")
    @Description("Re-enter the matching new passcode and verify navigation to the OTP screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfirmPasscodeNavigatesToOtp() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterPasscode(currentPasscode());

        Assert.assertTrue(page.isOtpScreenDisplayed(),
                "OTP screen should be shown after confirming the new passcode");
    }

    // ══════════════════════════════════════════════════
    //  10) INVALID OTP → CHANGE NOT COMMITTED
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "passcode", "negative"}, priority = 8,
            dependsOnMethods = "testConfirmPasscodeNavigatesToOtp")
    @Story("Invalid OTP")
    @Description("Enter an invalid OTP and verify the passcode change is not committed (stays on the OTP screen)")
    @Severity(SeverityLevel.CRITICAL)
    public void testInvalidOtpDoesNotCommitChange() {
        ChangePasscodePage page = new ChangePasscodePage();
        page.enterOtp(invalidOtp());

        // An invalid OTP must not commit the change — the Thank You screen must not appear and
        // the user remains on the OTP screen (Katalon's message verify was disabled, so we assert
        // the observable outcome instead).
        Assert.assertFalse(page.isThankYouScreenDisplayed(),
                "Thank You screen should NOT appear after an invalid OTP");
        Assert.assertTrue(page.isOtpScreenDisplayed(),
                "User should remain on the OTP screen after an invalid OTP");
    }

    // ══════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════

    @Step("Login with Change Passcode negative-tier user")
    private DashboardPage login() {
        ConfigManager config = cfg();
        return new LoginFlow().loginWith(
                config.get(keyPrefix() + ".mobileNumber"),
                config.get(keyPrefix() + ".id"),
                config.get(keyPrefix() + ".verificationCode", "1234"),
                config.get(keyPrefix() + ".passCode", "2233"));
    }
}
