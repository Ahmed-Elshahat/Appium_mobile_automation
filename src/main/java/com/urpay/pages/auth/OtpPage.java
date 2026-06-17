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
        tap(otpField0);
        otpField0.sendKeys(code);
    }

    public boolean isLoaded() {
        return isDisplayed(otpField0, 10);
    }
}
