package com.urpay.pages.auth;

import com.urpay.core.BasePage;

import io.qameta.allure.Step;

public class PasscodePage extends BasePage {

    @Step("Enter passcode via platform actions")
    public void enterPasscode(String passcode) {
        platformActions.inputTextDirect(passcode);
    }
}
