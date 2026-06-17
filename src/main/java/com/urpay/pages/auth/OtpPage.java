package com.urpay.pages.auth;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

public class OtpPage extends BasePage {

    @AndroidFindBy(accessibility = "testID-OTP-Input-Field-0")
    @iOSXCUITFindBy(accessibility = "testID-OTP-Input-Field-0")
    private WebElement otpField0;

    @Step("Enter OTP: {code}")
    public void enterOtp(String code) {
        try {
            waitUtils.waitForClickable(otpField0, 15);
            otpField0.click();
        } catch (Exception ignored) {
            log.debug("OTP field click attempt failed — may auto-focus");
        }
        platformActions.enterDigits(code);
    }

    public boolean isLoaded() {
        return isDisplayed(otpField0, 10);
    }

    public boolean isVisible(long timeoutSec) {
        return isDisplayed(otpField0, timeoutSec);
    }
}
