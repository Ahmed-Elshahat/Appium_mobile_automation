package com.urpay.flows;

import com.urpay.pages.auth.ForgotPasscodePage;

import io.qameta.allure.Step;

/**
 * Forgot Passcode flow.
 *
 * Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/&lt;Tier&gt;Tier/ForgotPasscode&lt;Tier&gt;Tier
 *
 * Composes {@link LoginFlow} (login up to the passcode screen, without entering the passcode)
 * with {@link ForgotPasscodePage} so a test can drive the reset journey:
 *   passcode screen → Forgot Passcode → Date-of-Birth verification → new passcode → confirm →
 *   success toast → Dashboard.
 *
 * Rules: no Thread.sleep, no assertions (returns the page for the test to verify).
 */
public class ForgotPasscodeFlow {

    private final ForgotPasscodePage page;

    public ForgotPasscodeFlow() {
        this.page = new ForgotPasscodePage();
    }

    /**
     * Login with the given credentials and stop on the passcode screen (Katalon
     * {@code ToValidateLoginTillPasscodeScreen}). The passcode is NOT entered — the journey
     * continues by tapping "Forgot your passcode?".
     *
     * @return the {@link ForgotPasscodePage} positioned on the passcode screen
     */
    @Step("Login until the passcode screen for the Forgot Passcode journey")
    public ForgotPasscodePage loginUntilPasscodeScreen(String mobile, String id, String otp) {
        new LoginFlow().loginUntilPasscodeScreen(mobile, id, otp);
        return page;
    }
}
