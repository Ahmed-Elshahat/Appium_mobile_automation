package com.urpay.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {

    // ── Environment Dropdown ──
    // TODO: Replace with actual testIDs once captured from page source
    private static final By ENV_DROPDOWN = AppiumBy.accessibilityId("testID-env-dropdown");
    private static final By ENV_OPTION_SIT = AppiumBy.xpath("//*[@text='SIT' or @label='SIT']");
    private static final By ENV_OPTION_UAT = AppiumBy.xpath("//*[@text='UAT' or @label='UAT']");

    @AndroidFindBy(accessibility = "testID-input-direct-mobile")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-mobile")
    private WebElement mobileNumberField;

    @AndroidFindBy(accessibility = "testID-input-direct-id")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-id")
    private WebElement idField;

    @AndroidFindBy(accessibility = "testID-primary--main")
    @iOSXCUITFindBy(accessibility = "testID-primary--main")
    private WebElement loginButton;

    @Step("Select environment: {env}")
    public void selectEnvironment(String env) {
        tap(ENV_DROPDOWN);
        if ("UAT".equalsIgnoreCase(env)) {
            waitUtils.waitForClickable(ENV_OPTION_UAT, 5);
            tap(ENV_OPTION_UAT);
        } else {
            waitUtils.waitForClickable(ENV_OPTION_SIT, 5);
            tap(ENV_OPTION_SIT);
        }
        log.info("Environment selected: {}", env);
    }

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
