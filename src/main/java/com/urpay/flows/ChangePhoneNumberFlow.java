package com.urpay.flows;

import com.urpay.pages.settings.ChangePhoneNumberPage;

import io.qameta.allure.Step;

/**
 * Change Phone Number flow.
 *
 * Migrated from Katalon Test Cases/Wallet_VAS/ChangePhoneNumber/*.
 *
 * Composes page actions into reusable journeys:
 *   - navigate Dashboard → avatar → Change mobile number
 *   - submit a new number (with "already in use" fallback)
 *
 * Rules: no Thread.sleep, no assertions (returns the page for the test to verify).
 */
public class ChangePhoneNumberFlow {

    private final ChangePhoneNumberPage page;

    public ChangePhoneNumberFlow() {
        this.page = new ChangePhoneNumberPage();
    }

    @Step("Navigate Dashboard → Change Phone Number screen")
    public ChangePhoneNumberPage navigateToChangePhoneNumber() {
        page.tapProfileAvatar();
        page.tapChangePhoneNumber();
        return page;
    }

    /**
     * Submit a new mobile number. If the app reports the number is already in use,
     * dismiss the popup and re-submit using the supplied fallback number, then enter OTP.
     *
     * @return the page, ready for Thank-You verification by the test
     */
    @Step("Submit new mobile number: {newNumber}")
    public ChangePhoneNumberPage submitNewNumber(String newNumber, String fallbackNumber,
                                                 String verificationCode) {
        page.enterNewMobileNumber(newNumber);
        page.tapNext();

        if (page.isNumberAlreadyInUseDisplayed()) {
            page.dismissAlreadyInUsePopup();
            page.clearAndReenterNumber(fallbackNumber);
            page.tapNext();
        }

        page.enterVerificationCode(verificationCode);
        return page;
    }

    /**
     * Restore the original number: navigate again, submit the original number and OTP.
     * Mirrors Katalon validateResetPasscode (returns the account to its starting state).
     *
     * @return the page, ready for Thank-You verification by the test
     */
    @Step("Restore original mobile number: {originalNumber}")
    public ChangePhoneNumberPage restoreOriginalNumber(String originalNumber, String verificationCode) {
        navigateToChangePhoneNumber();
        page.enterNewMobileNumber(originalNumber);
        page.tapNext();
        page.enterVerificationCode(verificationCode);
        return page;
    }
}
