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
        tap(newEsimButton);
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }

    public boolean isMyOrdersPageLoaded() {
        return isDisplayed(newEsimButton, 3);
    }

    // ── Select Country Page Actions ───────────────────

    @Step("Select Global tab")
    public void selectGlobalTab() {
        tap(globalTab);
        waitUtils.waitForVisible(AppiumBy.accessibilityId("testID-data-0"), 15);
    }
    @Step("Select first country from list")
    public void selectFirstCountry() {
        tap(firstGlobalOption);  // testID-data-0 = first item in country list
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
        tap(firstGlobalOption);
        log.info("Selected first package via testID-data-0");
    }

    @Step("Tap Next button")
    public void tapNext() {
        platformActions.scrollToText("Next");
        tap(nextButton);
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
        // The confirmation CTA sits below the fold; scroll it into view first.
        try {
            platformActions.scrollToText("Confirm");
        } catch (Exception ignored) {
            // label may differ or already be visible — continue
        }
        // Prefer the testID, but fall back to the clickable element bearing the
        // Confirm/Pay label — the confirm button's testID is not stable on the
        // current build (testID-primary-onConfirm-main was never clickable in the run).
        org.openqa.selenium.By confirm = AppiumBy.xpath(
                "//*[@content-desc='testID-primary-onConfirm-main']"
                + " | //*[@text='Confirm' or @text='Confirm Purchase' or @text='Pay' or @text='Pay Now']"
                + "/ancestor-or-self::*[@clickable='true'][1]");
        waitUtils.waitForClickable(confirm, 15).click();
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
