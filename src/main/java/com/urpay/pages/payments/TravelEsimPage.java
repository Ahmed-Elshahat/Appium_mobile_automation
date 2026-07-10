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

    // First country in the alphabetical list (e.g. "Aruba")
    @AndroidFindBy(xpath = "//*[@text='Aruba']")
    @iOSXCUITFindBy(xpath = "//*[@label='Aruba']")
    private WebElement firstCountryItem;

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
        tap(newEsimButton);
        waitUtils.waitForVisible(AppiumBy.xpath("//*[@text='Global' or @text='Select a country']"), 15);
    }

    public boolean isMyOrdersPageLoaded() {
        return isDisplayed(newEsimButton, 3);
    }

    // ── Select Country Page Actions ───────────────────

    @Step("Select Global tab")
    public void selectGlobalTab() {
        tap(globalTab);
        // Wait for plans list to load after tab switch (network call)
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }

    @Step("Select first country from list")
    public void selectFirstCountry() {
        tap(firstCountryItem);
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
        log.info("Selected first country — waiting for packages");
    }
    @Step("Select Local tab")
    public void selectLocalTab() {
        tap(localTab);
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }

    @Step("Select Regional tab")
    public void selectRegionalTab() {
        tap(regionalTab);
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }

    // ── Global Profile Page Actions ───────────────────

    @Step("Select first package (7 Days)")
    public void selectFirstPackage() {
        // Use UiAutomator for reliable React Native card tap
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"1GB 7Days\").instance(0)")).click();
        log.info("Selected first package: 1GB 7Days");
    }

    @Step("Tap Next button")
    public void tapNext() {
        // Use UiScrollable to scroll Next into full view, then tap
        org.openqa.selenium.WebElement nextBtn = driver.findElement(
                AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true))"
                        + ".scrollIntoView(new UiSelector().text(\"Next\"))"));
        nextBtn.click();
        log.info("Tapped Next button");
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
        log.info("Confirmation page loaded");
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
        // Find and tap the Confirm button directly (exact text match, not "Confirmation" header)
        org.openqa.selenium.WebElement btn = waitUtils.waitForClickable(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.ViewGroup\")"
                        + ".childSelector(new UiSelector().text(\"Confirm\"))"), 15);
        btn.click();
        log.info("Tapped Confirm button");
    }

    public boolean isConfirmationPageLoaded() {
        return isDisplayed(countryRegionValue, 30);
    }

    // ── Post-Purchase Result ──────────────────────────

    /** Success: Done button on result screen (accessibility ID — reliable) */
    private static final org.openqa.selenium.By PURCHASE_SUCCESS =
            AppiumBy.accessibilityId("testID-primary-action-main");

    /** Error: text-based fallback for service errors */
    private static final org.openqa.selenium.By PURCHASE_ERROR = AppiumBy.xpath(
            "//*[contains(@text,'error') or contains(@label,'error')"
            + " or contains(@text,'declined') or contains(@label,'declined')"
            + " or contains(@text,'unavailable') or contains(@label,'unavailable')"
            + " or contains(@text,'try again') or contains(@label,'try again')]");

    public boolean isPurchaseSuccessful(long timeoutSec) {
        if (isPresent(PURCHASE_ERROR, 1)) {
            return false;
        }
        return isPresent(PURCHASE_SUCCESS, timeoutSec);
    }
}
