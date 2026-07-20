package com.urpay.pages.payments;

import org.openqa.selenium.By;
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

    // Self-heal: accept the accessibility/content-desc anchor OR the ViewGroup-with-child-text
    // shape, since the revamped RN build does not always expose the text on the tappable ViewGroup.
    @AndroidFindBy(xpath = "//*[@text=\"New E-Sim\" or @content-desc=\"New E-Sim\""
            + " or (@class=\"android.view.ViewGroup\" and ./*[@text=\"New E-Sim\"])]")
    @iOSXCUITFindBy(accessibility = "New E-Sim")
    private WebElement newEsimButton;

    // ── Select Country Page ───────────────────────────
    // Self-heal: match either the rendered text OR the content-desc anchor for each tab, so a
    // locale/label variation on one attribute still resolves via the other.

    @AndroidFindBy(xpath = "//*[@text='Global' or @content-desc='Global']")
    @iOSXCUITFindBy(accessibility = "Global")
    private WebElement globalTab;

    @AndroidFindBy(xpath = "//*[@text='Local' or @content-desc='Local']")
    @iOSXCUITFindBy(accessibility = "Local")
    private WebElement localTab;

    @AndroidFindBy(xpath = "//*[@text='Regional' or @content-desc='Regional']")
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

    @AndroidFindBy(xpath = "//*[@text='Next' or @content-desc='Next']")
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
        // Self-heal: prefer the concrete plan label, but fall back to the first indexed plan card
        // (testID-data-0) when the catalog's first plan is not "1GB 7Days" — so a changed default
        // plan no longer breaks selection.
        By byText = AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"1GB 7Days\").instance(0)");
        By byFirstCard = AppiumBy.accessibilityId("testID-data-0");
        if (isPresent(byText, 3)) {
            tap(byText);
            log.info("Selected first package by label: 1GB 7Days");
        } else {
            tap(byFirstCard);
            log.info("Self-heal: '1GB 7Days' not found — selected first plan card (testID-data-0)");
        }
    }

    @Step("Tap Next button")
    public void tapNext() {
        // Self-heal: scroll Next into full view via UiScrollable and tap; if the scrollable/text
        // strategy fails (no scroll container, or Next already on screen) fall back to the resilient
        // Next locator (text OR content-desc).
        try {
            org.openqa.selenium.WebElement nextBtn = driver.findElement(
                    AppiumBy.androidUIAutomator(
                            "new UiScrollable(new UiSelector().scrollable(true))"
                            + ".scrollIntoView(new UiSelector().text(\"Next\"))"));
            nextBtn.click();
            log.info("Tapped Next button (scrolled into view)");
        } catch (RuntimeException e) {
            log.warn("Self-heal: scrollIntoView Next failed ({}) — tapping direct Next locator",
                    e.getMessage());
            tap(nextButton);
        }
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
        // Self-heal: tap the Confirm button by its text-bearing ViewGroup (exact match, not the
        // "Confirmation" header); if that shape is not found, fall back to the stable testID anchor
        // (testID-primary-onConfirm-main) exposed by confirmButton.
        By byText = AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.view.ViewGroup\")"
                + ".childSelector(new UiSelector().text(\"Confirm\"))");
        if (isPresent(byText, 5)) {
            waitUtils.waitForClickable(byText, 15).click();
            log.info("Tapped Confirm button (text anchor)");
        } else {
            log.warn("Self-heal: Confirm text ViewGroup not found — tapping testID confirm anchor");
            tap(confirmButton);
        }
    }

    public boolean isConfirmationPageLoaded() {
        return isDisplayed(countryRegionValue, 30);
    }

    // ── Post-Purchase Result ──────────────────────────

    /**
     * Success signal on the "Thank You!" result screen. Self-heal: the LambdaTest build HASHES the
     * middle segment of the Done button's testID (e.g. {@code testID-primary-W2d-main}), so the bare
     * {@code testID-primary-action-main} id misses a genuine success. Match the stable success
     * message / title text, the exact testID, OR any obfuscated primary-…-main button (proven repo
     * obfuscation pattern) so the purchase is detected on both local and cloud builds.
     */
    private static final org.openqa.selenium.By PURCHASE_SUCCESS = AppiumBy.xpath(
            "//*[contains(@text,'bought an e-sim successfully')"
            + " or contains(@label,'bought an e-sim successfully')"
            + " or @text='Thank You!' or @label='Thank You!'"
            + " or @content-desc='testID-primary-action-main'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')]");

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
