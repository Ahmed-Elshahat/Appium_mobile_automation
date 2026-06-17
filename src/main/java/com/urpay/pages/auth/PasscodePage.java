package com.urpay.pages.auth;

import com.urpay.core.BasePage;

import io.qameta.allure.Step;

public class PasscodePage extends BasePage {

    @Step("Enter passcode digits")
    public void enterPasscode(String passcode) {
        platformActions.enterDigits(passcode);
    }

    @Step("Enter passcode via direct input")
    public void enterPasscodeDirect(String passcode) {
        platformActions.inputTextDirect(passcode);
    }
}
