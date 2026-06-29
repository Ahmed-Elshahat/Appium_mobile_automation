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
 * Forgot Passcode — wrong-passcode negative cases (the {@code -NegativeCases -02} suites).
 *
 * Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/&lt;Tier&gt;Tier/ForgotPasscode&lt;Tier&gt;Tier-NegativeCases -02
 *
 * Logs in up to the passcode screen, then enters an INVALID passcode twice and verifies the
 * "incorrect passcode" alert on each attempt.
 *
 * SAFETY: the journey deliberately stops at TWO wrong attempts (Katalon does the same). A THIRD
 * wrong attempt locks the account for one hour, so this suite never enters a third — but a re-run
 * without the backend reset (which is best-effort here) could push a previous run's count to the
 * limit. Run sparingly.
 */
public abstract class AbstractForgotPasscodeLoginNegativeTest extends BaseTest {

    private static final String WRONG_PASSCODE_ALERT =
            "The entered passcode is incorrect. Please try again";

    /** Config-key prefix, e.g. {@code "forgotPasscodeFullNegPasscode"}. */
    protected abstract String keyPrefix();

    private ConfigManager cfg() {
        return ConfigManager.getInstance();
    }

    private String invalidPasscode() {
        return cfg().get(keyPrefix() + ".invalidPasscode", "2365");
    }

    private String otp() {
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

    // ── 2) FIRST WRONG PASSCODE → ALERT ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 2,
            dependsOnMethods = "testLoginUntilPasscodeScreen")
    @Story("Wrong Passcode (Attempt 1)")
    @Description("Enter an invalid passcode and verify the 'incorrect passcode' alert")
    @Severity(SeverityLevel.NORMAL)
    public void testFirstWrongPasscodeShowsAlert() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterLoginPasscode(invalidPasscode());

        String message = page.getNotificationMessage(20);
        Assert.assertEquals(message, WRONG_PASSCODE_ALERT,
                "Incorrect-passcode alert should be displayed on the first wrong attempt");
    }

    // ── 3) SECOND WRONG PASSCODE → ALERT (STOPS — NO THIRD ATTEMPT) ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 3,
            dependsOnMethods = "testFirstWrongPasscodeShowsAlert")
    @Story("Wrong Passcode (Attempt 2)")
    @Description("Enter the invalid passcode a second time and verify the 'incorrect passcode' alert")
    @Severity(SeverityLevel.NORMAL)
    public void testSecondWrongPasscodeShowsAlert() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterLoginPasscode(invalidPasscode());

        String message = page.getNotificationMessage(20);
        Assert.assertEquals(message, WRONG_PASSCODE_ALERT,
                "Incorrect-passcode alert should be displayed on the second wrong attempt");
    }

    // ── HELPER ──

    @Step("Login until the passcode screen with the wrong-passcode negative tier user")
    private ForgotPasscodePage login() {
        ConfigManager config = cfg();
        return new ForgotPasscodeFlow().loginUntilPasscodeScreen(
                config.get(keyPrefix() + ".mobileNumber"),
                config.get(keyPrefix() + ".id"),
                config.get(keyPrefix() + ".verificationCode", "1234"));
    }
}
