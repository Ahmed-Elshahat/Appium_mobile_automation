package com.urpay.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {

    @AndroidFindBy(accessibility = "testID-input-direct-mobile")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-mobile")
    private WebElement mobileNumberField;

    @AndroidFindBy(accessibility = "testID-input-direct-id")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-id")
    private WebElement idField;

    @AndroidFindBy(accessibility = "testID-primary--main")
    @iOSXCUITFindBy(accessibility = "testID-primary--main")
    private WebElement loginButton;

    @Step("Enter mobile number: {mobile}")
    public void enterMobileNumber(String mobile) {
        tap(mobileNumberField);
        type(mobileNumberField, mobile);
    }

    @Step("Enter ID: {id}")
    public void enterId(String id) {
        tap(idField);
        type(idField, id);
    }

    @Step("Tap Login button")
    public void clickLogin() {
        hideKeyboard();
        tap(loginButton, 30);
    }

    @Step("Login with mobile={mobile}, id={id}")
    public void login(String mobile, String id) {
        enterMobileNumber(mobile);
        enterId(id);
        clickLogin();
    }

    public boolean isLoaded() {
        return isDisplayed(mobileNumberField, 10);
    }
}
