package com.urpay.pages.auth;

import com.urpay.core.BasePage;
import com.urpay.helpers.AdbHelper;
import io.qameta.allure.Step;

public class PasscodePage extends BasePage {

    @Step("Enter passcode via ADB")
    public void enterPasscode(String passcode) {
        AdbHelper.inputText(passcode);
    }
}
