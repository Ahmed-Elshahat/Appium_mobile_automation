package com.urpay.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

public class LandingPage extends BasePage {

    @AndroidFindBy(accessibility = "testID-secondary-login-main")
    @iOSXCUITFindBy(accessibility = "testID-secondary-login-main")
    private WebElement loginButton;

    @AndroidFindBy(accessibility = "testID-primary-register-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-register-main")
    private WebElement registerButton;

    // On the obfuscated (SIT/cloud) build the middle action segment of primary-register-main is
    // stripped to empty -> testID-primary--main. On the landing page the sole primary button is
    // Register, so match the exact id, the obfuscated id, and the visible label — cross-platform.
    private static final By REGISTER_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-register-main'"
            + " or @content-desc='testID-primary--main'"
            + " or @name='testID-primary-register-main' or @name='testID-primary--main'"
            + " or @text='Register' or @label='Register']");

    @Step("Tap Login button on landing page")
    public void clickLogin() {
        tap(loginButton, 30);
    }

    @Step("Tap Register button on landing page")
    public void clickRegister() {
        tap(REGISTER_BTN);
    }

    public boolean isLoaded() {
        return isDisplayed(loginButton, 30);
    }
}
