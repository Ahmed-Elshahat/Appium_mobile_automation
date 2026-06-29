package com.urpay.tests.auth;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.ForgotPasscodeFlow;
import com.urpay.helpers.PasscodeResetApiHelper;
import com.urpay.pages.auth.ForgotPasscodePage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Forgot Passcode — Date-of-Birth negative cases (shared base).
 *
 * Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/&lt;Tier&gt;Tier/ForgotPasscode&lt;Tier&gt;Tier-NegativeCases
 *
 * Covers the User-Verification date validation: login → passcode screen → Forgot Passcode →
 * open the date spinner and confirm a date that does NOT match the account's DOB → the app
 * rejects each attempt with the "date does not match" alert → drag down to go back.
 *
 * The Full tier's negative suite is exactly these cases. The Family/Visitor negative suites
 * additionally exercise the new-passcode negatives — see {@link AbstractForgotPasscodeNegativeJourneyTest}.
 *
 * The alert string is asserted case-insensitively: the Katalon scripts expect a capital-T variant
 * for some attempts and a lowercase-t variant for others, while the app returns a single value.
 */
public abstract class AbstractForgotPasscodeDobNegativeTest extends BaseTest {

    protected static final String DOB_MISMATCH_ALERT =
            "The entered date does not match, please enter a valid date";

    /** Config-key prefix for this tier, e.g. {@code "forgotPasscodeFullNeg"}. */
    protected abstract String keyPrefix();

    protected ConfigManager cfg() {
        return ConfigManager.getInstance();
    }

    protected String otp() {
        return cfg().get(keyPrefix() + ".validOtp", "1234");
    }

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

    // ── 1) LOGIN UNTIL PASSCODE SCREEN ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 1)
    @Story("Login Till Passcode Screen")
    @Description("Login with credentials + OTP and stop on the passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginUntilPasscodeScreen() {
        ForgotPasscodePage page = login();

        Assert.assertTrue(page.isPasscodeScreenDisplayed(),
                "Passcode screen should be displayed after login");
    }

    // ── 2) TAP FORGOT PASSCODE → USER VERIFICATION ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 2,
            dependsOnMethods = "testLoginUntilPasscodeScreen")
    @Story("Forgot Passcode")
    @Description("Tap 'Forgot your passcode?' and verify the User Verification screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testTapForgotPasscodeShowsUserVerification() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.tapForgotPasscode();

        Assert.assertTrue(page.isUserVerificationScreenDisplayed(),
                "User Verification screen should be displayed");
    }

    // ── 3) INVALID DOB (open calendar, confirm default) → ALERT ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 3,
            dependsOnMethods = "testTapForgotPasscodeShowsUserVerification")
    @Story("Invalid Date Of Birth")
    @Description("Open the date spinner, confirm a non-matching date and verify the rejection alert")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidDobShowsAlert() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.openCalendarAndConfirmDefault();
        page.tapUserVerificationNext();

        assertDobMismatch(page);
    }

    // ── 4-6) RE-TAP NEXT (date unchanged) → SAME ALERT ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 4,
            dependsOnMethods = "testInvalidDobShowsAlert")
    @Story("Invalid Date")
    @Description("Re-tap Next with the non-matching date and verify the rejection alert")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidDateShowsAlert() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.tapUserVerificationNext();

        assertDobMismatch(page);
    }

    @Test(groups = {"auth", "passcode", "negative"}, priority = 5,
            dependsOnMethods = "testInvalidDateShowsAlert")
    @Story("Invalid Month")
    @Description("Re-tap Next with the non-matching date and verify the rejection alert")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidMonthShowsAlert() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.tapUserVerificationNext();

        assertDobMismatch(page);
    }

    @Test(groups = {"auth", "passcode", "negative"}, priority = 6,
            dependsOnMethods = "testInvalidMonthShowsAlert")
    @Story("Invalid Year")
    @Description("Re-tap Next with the non-matching date and verify the rejection alert")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidYearShowsAlert() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.tapUserVerificationNext();

        assertDobMismatch(page);
    }

    // ── 7) DRAG DOWN → BACK TO PASSCODE SCREEN ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 7,
            dependsOnMethods = "testInvalidYearShowsAlert")
    @Story("Drag Down To Go Back")
    @Description("Drag down to return to the passcode screen and verify the 'Forgot your passcode?' text")
    @Severity(SeverityLevel.NORMAL)
    public void testDragDownReturnsToPasscodeScreen() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.dragDownToGoBack();

        Assert.assertTrue(page.isBackOnPasscodeScreen(),
                "Should return to the passcode screen showing 'Forgot your passcode?'");
        Assert.assertEquals(page.getForgotPasscodeValueText(), "Forgot your passcode?",
                "'Forgot your passcode?' text should be displayed on the passcode screen");
    }

    // ── HELPERS ──

    protected void assertDobMismatch(ForgotPasscodePage page) {
        String message = page.getNotificationMessage(20);
        Assert.assertTrue(message.equalsIgnoreCase(DOB_MISMATCH_ALERT),
                "Date mismatch alert should be displayed, but was: '" + message + "'");
    }

    @Step("Login until the passcode screen with the negative tier user")
    protected ForgotPasscodePage login() {
        ConfigManager config = cfg();
        return new ForgotPasscodeFlow().loginUntilPasscodeScreen(
                config.get(keyPrefix() + ".mobileNumber"),
                config.get(keyPrefix() + ".id"),
                config.get(keyPrefix() + ".verificationCode", "1234"));
    }
}
