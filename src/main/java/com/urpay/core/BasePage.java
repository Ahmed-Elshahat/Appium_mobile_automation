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

import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.Platform;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.SwipeUtils;
import com.urpay.utils.WaitUtils;

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
    protected final MobilePlatformActions platformActions;
    protected final Platform platform;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final long DEFAULT_TIMEOUT = 15;

    protected BasePage() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waitUtils = new WaitUtils(driver, DEFAULT_TIMEOUT);
        this.swipeUtils = new SwipeUtils(driver);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.platform = platformActions.getPlatform();
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(DEFAULT_TIMEOUT)), this);
    }

    // ── Tap / Click ────────────────────────────────────────────────

    protected void tap(WebElement element) {
        try {
            waitUtils.waitForClickable(element).click();
        } catch (org.openqa.selenium.TimeoutException e) {
            if (grantSystemPermissionIfPresent(1) > 0) {
                waitUtils.waitForClickable(element).click();
            } else {
                throw e;
            }
        }
        checkForErrorBanner();
    }

    protected void tap(WebElement element, long timeoutSec) {
        try {
            waitUtils.waitForClickable(element, timeoutSec).click();
        } catch (org.openqa.selenium.TimeoutException e) {
            if (grantSystemPermissionIfPresent(1) > 0) {
                waitUtils.waitForClickable(element, timeoutSec).click();
            } else {
                throw e;
            }
        }
        checkForErrorBanner();
    }

    protected void tap(By locator) {
        try {
            waitUtils.waitForClickable(locator).click();
        } catch (org.openqa.selenium.TimeoutException e) {
            // An Android runtime-permission dialog (contacts / camera / etc.) can appear mid-flow
            // after an app-data clear, covering the target and hiding the app's view tree so the
            // locator never resolves. Grant it and retry once; otherwise surface the real failure.
            if (grantSystemPermissionIfPresent(1) > 0) {
                waitUtils.waitForClickable(locator).click();
            } else {
                throw e;
            }
        }
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

    // ── Scroll / Swipe (delegated to SwipeUtils / PlatformActions) ──

    protected WebElement scrollToText(String text) {
        return platformActions.scrollToText(text);
    }

    /** Open an in-app deep link (urpay://...) — foregrounds the app and routes to the target screen. */
    protected void openDeepLink(String deepLink) {
        platformActions.openDeepLink(deepLink);
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

    // ── System permission dialogs (Android runtime grants) ─────────

    /**
     * "Allow" button of an Android runtime-permission dialog raised by the OS permission
     * controller (contacts, camera, location, notifications …). Matched by the OS resource-id,
     * which is stable across locales and identical on local Samsung and LambdaTest cloud devices
     * (the window package is google/aosp but the id namespace stays com.android.permissioncontroller).
     */
    private static final By SYSTEM_PERMISSION_ALLOW = By.xpath(
            "//*[starts-with(@resource-id,'com.android.permissioncontroller:id/permission_allow')]");

    /**
     * Dismiss any Android system permission dialog(s) currently covering the screen by tapping
     * "Allow". Handles a few chained dialogs, and is a fast no-op when none are present — so it is
     * safe to call defensively before, or on the failure path of, a tap that a runtime-permission
     * request could block. Returns the number of dialogs granted.
     *
     * @param firstWaitSec how long to wait for the first dialog to surface (later chained dialogs
     *                     use a short fixed poll).
     */
    protected int grantSystemPermissionIfPresent(long firstWaitSec) {
        if (!platform.isAndroid()) {
            return 0;
        }
        int granted = 0;
        for (int i = 0; i < 4; i++) {
            long wait = (i == 0) ? firstWaitSec : 2;
            List<WebElement> allow = waitUtils.findQuick(SYSTEM_PERMISSION_ALLOW, wait);
            if (allow.isEmpty()) {
                break;
            }
            try {
                allow.get(0).click();
                granted++;
                log.info("Granted system permission dialog ({})", granted);
            } catch (Exception ignored) {
                break;
            }
        }
        return granted;
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
