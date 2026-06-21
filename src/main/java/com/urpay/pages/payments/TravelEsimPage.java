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
        return isDisplayed(newEsimButton, 3);
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

    @Step("Select first package (7 Days)")
    public void selectFirstPackage() {
        // Use UiAutomator to find and click the card — more reliable for React Native
        // The parent ViewGroup of the text is the actual clickable card
        org.openqa.selenium.WebElement card = waitUtils.waitForClickable(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"1GB 7Days\").instance(0)"), 10);
        card.click();
        log.info("Selected first package: Asia 1GB 7Days");
        // Verify selection by waiting briefly then checking if Next button color changes
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    @Step("Tap Next button")
    public void tapNext() {
        // Retry tapping Next up to 3 times — React Native may not navigate on first tap
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                // Scroll to make Next button fully visible, then click via UiAutomator
                org.openqa.selenium.WebElement nextBtn = driver.findElement(
                        AppiumBy.androidUIAutomator(
                                "new UiScrollable(new UiSelector().scrollable(true))" +
                                ".scrollIntoView(new UiSelector().text(\"Next\"))"));
                nextBtn.click();
                log.info("Tapped Next button (attempt {})", attempt);

                // Wait for confirmation page to appear
                waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
                log.info("Confirmation page loaded after attempt {}", attempt);
                return;
            } catch (Exception e) {
                log.warn("Next button tap attempt {} failed: {}", attempt, e.getMessage());
                if (attempt == 3) {
                    throw new org.openqa.selenium.TimeoutException(
                            "Confirmation page did not load after 3 attempts tapping Next", e);
                }
                // Re-select the first package in case tap deselected it
                try {
                    driver.findElement(AppiumBy.androidUIAutomator(
                            "new UiSelector().textContains(\"1GB 7Days\").instance(0)")).click();
                    log.info("Re-selected first package before retry");
                } catch (Exception ignored) {
                    log.debug("Could not re-select package: {}", ignored.getMessage());
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
        // Scroll to Confirm button and tap via UiAutomator (accessibility ID may not match)
        org.openqa.selenium.WebElement confirmBtn = driver.findElement(
                AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true))" +
                        ".scrollIntoView(new UiSelector().text(\"Confirm\"))"));
        confirmBtn.click();
        log.info("Tapped Confirm button");
    }

    public boolean isConfirmationPageLoaded() {
        return isDisplayed(countryRegionValue, 30);
    }
}
