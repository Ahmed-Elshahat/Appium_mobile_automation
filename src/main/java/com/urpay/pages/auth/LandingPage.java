package com.urpay.pages.auth;

import com.urpay.core.BasePage;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

public class LandingPage extends BasePage {

    @AndroidFindBy(accessibility = "testID-secondary-login-main")
    private WebElement loginButton;

    @AndroidFindBy(accessibility = "testID-primary-register-main")
    private WebElement registerButton;

    @Step("Tap Login button on landing page")
    public void clickLogin() {
        tap(loginButton, 30);
    }

    @Step("Tap Register button on landing page")
    public void clickRegister() {
        tap(registerButton, 30);
    }

    public boolean isLoaded() {
        return isDisplayed(loginButton, 30);
    }
}
