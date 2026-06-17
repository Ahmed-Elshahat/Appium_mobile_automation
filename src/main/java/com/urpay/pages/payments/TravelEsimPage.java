package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Travel E-SIM page — handles My Orders, Country Selection, Global Profile,
 * and Confirmation pages within the Travel eSIM flow.
 *
 * Locators from Katalon Object Repository:
 *   My orders page/New Esim button.rs → ViewGroup with text "New E-Sim"
 *   SelectCountry Page/Global_TextButton.rs → text "Global"
 *   Global_Profile/GlobalFirstOptionPrice.rs → testID-data-0
 *   Global_Profile/NextButton.rs → text "Next"
 *   ConfirmationPage/SelectCountryRegionValue.rs → testID-label-value-0
 *   ConfirmationPage/PackageName.rs → testID-label-value-2
 *   ConfirmationPage/PackageValidaty.rs → testID-label-value-4
 */
public class TravelEsimPage extends BasePage {

    // ── My Orders Page ────────────────────────────────

    @AndroidFindBy(xpath = "//*[@class=\"android.view.ViewGroup\" and ./*[@text=\"New E-Sim\"]]")
    @iOSXCUITFindBy(accessibility = "New E-Sim")
    private WebElement newEsimButton;

    // ── Select Country Page ───────────────────────────

    @AndroidFindBy(xpath = "//*[@text='Global']")
    @iOSXCUITFindBy(accessibility = "Global")
    private WebElement globalTab;

    @AndroidFindBy(xpath = "//*[@text='Local']")
    @iOSXCUITFindBy(accessibility = "Local")
    private WebElement localTab;

    @AndroidFindBy(xpath = "//*[@text='Regional']")
    @iOSXCUITFindBy(accessibility = "Regional")
    private WebElement regionalTab;

    // ── Global Profile Page ───────────────────────────

    @AndroidFindBy(accessibility = "testID-data-0")
    @iOSXCUITFindBy(accessibility = "testID-data-0")
    private WebElement firstGlobalOption;

    @AndroidFindBy(xpath = "//*[@text='Next']")
    @iOSXCUITFindBy(accessibility = "Next")
    private WebElement nextButton;

    // ── Confirmation Page ─────────────────────────────

    @AndroidFindBy(accessibility = "testID-label-value-0")
    @iOSXCUITFindBy(accessibility = "testID-label-value-0")
    private WebElement countryRegionValue;

    @AndroidFindBy(accessibility = "testID-label-value-2")
    @iOSXCUITFindBy(accessibility = "testID-label-value-2")
    private WebElement packageNameValue;

    @AndroidFindBy(accessibility = "testID-label-value-4")
    @iOSXCUITFindBy(accessibility = "testID-label-value-4")
    private WebElement packageValidityValue;

    @AndroidFindBy(accessibility = "testID-primary-onConfirm-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirm-main")
    private WebElement confirmButton;

    // ── My Orders Page Actions ────────────────────────

    @Step("Tap 'New E-Sim' button")
    public void tapNewEsim() {
        waitUtils.waitForClickable(
                AppiumBy.xpath("//*[@class=\"android.view.ViewGroup\" and ./*[@text=\"New E-Sim\"]]"), 10)
                .click();
        // Wait for SelectCountry page to load (tabs: Local, Regional, Global)
        waitUtils.waitForVisible(AppiumBy.xpath("//*[@text='Global' or @text='Local' or @text='Regional']"), 15);
    }

    public boolean isMyOrdersPageLoaded() {
        return isDisplayed(newEsimButton, 10);
    }

    // ── Select Country Page Actions ───────────────────

    @Step("Select Global tab")
    public void selectGlobalTab() {
        waitUtils.waitForClickable(AppiumBy.xpath("//*[@text='Global']"), 10).click();
        // Wait for plans list to load after tab switch (network call)
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }

    @Step("Select Local tab")
    public void selectLocalTab() {
        waitUtils.waitForClickable(AppiumBy.xpath("//*[@text='Local']"), 10).click();
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }

    @Step("Select Regional tab")
    public void selectRegionalTab() {
        waitUtils.waitForClickable(AppiumBy.xpath("//*[@text='Regional']"), 10).click();
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }

    // ── Global Profile Page Actions ───────────────────

    @Step("Select first global option")
    public void selectFirstGlobalOption() {
        waitUtils.waitForClickable(AppiumBy.accessibilityId("testID-data-0"), 10).click();
    }

    @Step("Tap Next button")
    public void tapNext() {
        // Retry tapping Next up to 3 times — React Native may not register first tap
        // or backend may briefly reject, returning to selection page
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                org.openqa.selenium.WebElement nextBtn = waitUtils.waitForClickable(
                        AppiumBy.xpath("//android.view.ViewGroup[.//android.widget.TextView[@text='Next']]"), 10);
                nextBtn.click();

                // Wait for confirmation page to appear (testID-label-value-0)
                waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
                return; // Success — confirmation page loaded
            } catch (Exception e) {
                log.warn("tapNext attempt {}/3 failed: {}", attempt, e.getMessage());
                if (attempt == 3) {
                    throw new org.openqa.selenium.TimeoutException(
                            "Confirmation page did not load after 3 Next button attempts. "
                            + "Last error: " + e.getMessage());
                }
                // Re-select the first option in case tap deselected it
                try {
                    waitUtils.waitForClickable(AppiumBy.accessibilityId("testID-data-0"), 5).click();
                } catch (Exception ignored) {
                    log.debug("Re-select attempt ignored — item may still be selected");
                }
            }
        }
    }

    // ── Confirmation Page Actions ─────────────────────

    @Step("Get Country/Region value")
    public String getCountryRegionValue() {
        return getText(countryRegionValue);
    }

    @Step("Get Package Name")
    public String getPackageName() {
        return getText(packageNameValue);
    }

    @Step("Get Package Validity")
    public String getPackageValidity() {
        return getText(packageValidityValue);
    }

    @Step("Tap Confirm button")
    public void tapConfirm() {
        waitUtils.waitForClickable(AppiumBy.accessibilityId("testID-primary-onConfirm-main"), 10).click();
    }

    public boolean isConfirmationPageLoaded() {
        return isDisplayed(countryRegionValue, 30);
    }
}
