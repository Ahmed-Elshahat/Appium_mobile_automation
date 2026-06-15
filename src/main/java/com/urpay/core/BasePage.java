package com.urpay.core;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.utils.SwipeUtils;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.qameta.allure.Allure;

/**
 * Base class for all Page Objects.
 *
 * SOLID applied:
 *   SRP — Element interactions only. Waits delegated to WaitUtils, swipes to SwipeUtils.
 *   DIP — Gets driver via DriverFactory (DriverProvider interface).
 *   LSP — Subclasses substitute without breaking contracts.
 *
 * Rules enforced:
 *   - No Thread.sleep() — all waits via WaitUtils.
 *   - No inline WebDriverWait — all waits via WaitUtils.
 *   - No assertions — assertions belong in test classes only.
 *   - No reporting — reporting belongs in listeners only.
 *   - All WebElement fields PRIVATE in subclasses, exposed via action methods.
 */
public abstract class BasePage {

    protected final AppiumDriver driver;
    protected final WaitUtils waitUtils;
    protected final SwipeUtils swipeUtils;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final long DEFAULT_TIMEOUT = 15;

    protected BasePage() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waitUtils = new WaitUtils(driver, DEFAULT_TIMEOUT);
        this.swipeUtils = new SwipeUtils(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(DEFAULT_TIMEOUT)), this);
    }

    // ── Tap / Click ────────────────────────────────────────────────

    protected void tap(WebElement element) {
        waitUtils.waitForClickable(element).click();
        checkForErrorBanner();
    }

    protected void tap(WebElement element, long timeoutSec) {
        waitUtils.waitForClickable(element, timeoutSec).click();
        checkForErrorBanner();
    }

    protected void tap(By locator) {
        waitUtils.waitForClickable(locator).click();
        checkForErrorBanner();
    }

    // ── Type / Set Text ────────────────────────────────────────────

    protected void type(WebElement element, String text) {
        WebElement el = waitUtils.waitForClickable(element);
        el.clear();
        el.sendKeys(text);
    }

    protected void type(By locator, String text) {
        WebElement el = waitUtils.waitForClickable(locator);
        el.clear();
        el.sendKeys(text);
    }

    // ── Get Text ───────────────────────────────────────────────────

    protected String getText(WebElement element) {
        return waitUtils.waitForVisible(element).getText();
    }

    protected String getText(By locator) {
        return waitUtils.waitForVisible(locator).getText();
    }

    // ── Element State (no assertions — returns boolean for test to assert) ──

    protected boolean isDisplayed(WebElement element, long timeoutSec) {
        return waitUtils.isDisplayed(element, timeoutSec);
    }

    protected boolean isDisplayed(WebElement element) {
        return waitUtils.isDisplayed(element, DEFAULT_TIMEOUT);
    }

    protected boolean isPresent(By locator, long timeoutSec) {
        return waitUtils.isPresent(locator, timeoutSec);
    }

    // ── Navigation ─────────────────────────────────────────────────

    protected void pressBack() {
        driver.navigate().back();
    }

    protected void hideKeyboard() {
        try {
            if (driver instanceof HidesKeyboard) {
                ((HidesKeyboard) driver).hideKeyboard();
            }
        } catch (Exception e) {
            log.debug("Keyboard not visible, skip hide");
        }
    }

    // ── Scroll / Swipe (delegated to SwipeUtils) ──────────────────

    protected WebElement scrollToText(String text) {
        return driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))" +
                        ".scrollTextIntoView(\"" + text + "\")"));
    }

    protected void swipeUp() {
        swipeUtils.swipeUp();
    }

    protected void swipeDown() {
        swipeUtils.swipeDown();
    }

    protected void tapAtCoordinates(int x, int y) {
        swipeUtils.tapAtCoordinates(x, y);
    }

    // ── Global Error Detection ─────────────────────────────────────

    private static final By ERROR_BANNER = By.xpath(
            "//*[contains(@text,'Service') and contains(@text,'unavailable')] | " +
            "//*[contains(@text,'service') and contains(@text,'unavailable')] | " +
            "//*[contains(@text,'Something went wrong')] | " +
            "//*[contains(@text,'Internal Server Error')] | " +
            "//*[contains(@text,'Try again later')] | " +
            "//*[contains(@text,'try again later')] | " +
            "//*[contains(@text,'connection timed out')] | " +
            "//*[contains(@text,'Network error')]");

    /**
     * Quick check for error banners after every tap.
     * Uses explicit WebDriverWait via WaitUtils.findQuick — no implicit wait toggling.
     * If an error is detected, captures screenshot and attaches to Allure.
     */
    protected void checkForErrorBanner() {
        try {
            List<WebElement> errors = waitUtils.findQuick(ERROR_BANNER, 1);
            if (!errors.isEmpty()) {
                String errorText = errors.get(0).getText();
                log.error("⚠ ERROR BANNER DETECTED: {}", errorText);

                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (screenshot.length > 0) {
                    Allure.addAttachment(
                            "BUG: " + errorText, "image/png",
                            new ByteArrayInputStream(screenshot), ".png");
                }
            }
        } catch (Exception ignored) {}
    }
}
