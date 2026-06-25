package com.urpay.flows;

import com.urpay.pages.settings.ChangePasscodePage;

import io.qameta.allure.Step;

/**
 * Change Passcode flow.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/DefaultTier/ChangePasscodeDefaultTier
 *
 * Composes {@link ChangePasscodePage} actions into the multi-screen journey:
 *   Current passcode → New passcode → Confirm new passcode → OTP → Thank You,
 *   plus the back/close re-entry sequences the Katalon suite exercises.
 *
 * Rules: no Thread.sleep, no assertions (returns the page for the test to verify).
 */
public class ChangePasscodeFlow {

    private final ChangePasscodePage page;

    public ChangePasscodeFlow() {
        this.page = new ChangePasscodePage();
    }

    @Step("Navigate Dashboard → Change Passcode (Current passcode screen)")
    public ChangePasscodePage navigateToChangePasscode() {
        page.navigateToChangePasscode();
        return page;
    }

    /**
     * Restore the original passcode (Katalon CommonFunctions/setDefaultPasscode).
     * Navigates again and changes the passcode back: changed → original → original → OTP.
     *
     * @return the page, ready for Thank-You verification by the test
     */
    @Step("Restore default passcode")
    public ChangePasscodePage restoreDefaultPasscode(String changedPasscode,
                                                     String originalPasscode, String otp) {
        page.navigateToChangePasscode();
        page.enterPasscode(changedPasscode);
        page.isNewPasscodeScreenDisplayed();     // gate: New screen ready
        page.enterPasscode(originalPasscode);
        page.isConfirmPasscodeScreenDisplayed(); // gate: Confirm screen ready
        page.enterPasscode(originalPasscode);
        page.isOtpScreenDisplayed();             // gate: OTP screen ready
        page.enterOtp(otp);
        return page;
    }
}
