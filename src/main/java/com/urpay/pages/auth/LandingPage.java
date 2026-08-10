package com.urpay.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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

        // Google Play Services location-accuracy prompt that intermittently overlays the landing page.
        private static final By LOCATION_ACCURACY_NO_THANKS = AppiumBy.xpath(
            "//*[@resource-id='android:id/button2'"
            + " or @text='No thanks' or @text='NO THANKS']");

    @Step("Tap Login button on landing page")
    public void clickLogin() {
        tap(loginButton, 30);
    }

    @Step("Tap Register button on landing page")
    public void clickRegister() {
        dismissLocationAccuracyPromptIfPresent();
        try {
            tap(REGISTER_BTN);
        } catch (TimeoutException e) {
            dismissLocationAccuracyPromptIfPresent();
            tap(REGISTER_BTN);
        }
    }

    private void dismissLocationAccuracyPromptIfPresent() {
        try {
            var buttons = waitUtils.findQuick(LOCATION_ACCURACY_NO_THANKS, 2);
            if (!buttons.isEmpty() && buttons.get(0).isDisplayed()) {
                buttons.get(0).click();
                log.info("Dismissed Location Accuracy prompt using 'No thanks'");
            }
        } catch (Exception ignored) {
            // Prompt not present or already gone.
        }
    }

    public boolean isLoaded() {
        return isDisplayed(loginButton, 30);
    }
}
