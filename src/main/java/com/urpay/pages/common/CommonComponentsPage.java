package com.urpay.pages.common;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Shared UI components that appear across multiple screens.
 *
 * Cross-cutting elements only — screen-specific locators belong in their
 * own page objects (e.g. popups → DashboardPage, OTP → OtpPage,
 * passcode → PasscodePage, search → SearchPage).
 *
 * Categories:
 *   - Done / Next action buttons
 *   - Notification message
 *   - Post-OTP result waiting
 *   - Keyboard dismiss
 */
public class CommonComponentsPage extends BasePage {

    // ── Action buttons ─────────────────────────────────
    private static final By DONE_BTN = AppiumBy.xpath(
            "//*[@text='Done' or @content-desc='testID-primary-action-main']");
    private static final By NEXT_BTN = AppiumBy.xpath(
            "//*[@text='Next' or @content-desc='testID-primary-navigateToNextStep-main']");

    // ── Notification ───────────────────────────────────
    private static final By NOTIFICATION_MSG =
            AppiumBy.accessibilityId("testID-notification-message");

    // ── Post-OTP result screen ─────────────────────────
    private static final By RESULT_SCREEN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-action-main'] | "
            + "//*[contains(@text,'Service') or contains(@text,'unavailable') "
            + "or contains(@text,'declined') or contains(@text,'error') "
            + "or contains(@text,'Error') or contains(@text,'try again') "
            + "or contains(@text,'Success') or contains(@text,'success')]");

    // ══════════════════════════════════════════════════
    //  ACTION BUTTONS
    // ══════════════════════════════════════════════════

    @Step("Tap Done button")
    public void tapDone() {
        tap(DONE_BTN);
    }

    @Step("Tap Done button")
    public void tapDone(long timeoutSec) {
        waitUtils.waitForClickable(DONE_BTN, timeoutSec).click();
    }

    @Step("Tap Next button")
    public void tapNext() {
        tap(NEXT_BTN);
    }

    @Step("Tap Next button")
    public void tapNext(long timeoutSec) {
        waitUtils.waitForClickable(NEXT_BTN, timeoutSec).click();
    }

    public boolean isNextVisible(long timeoutSec) {
        return isPresent(NEXT_BTN, timeoutSec);
    }

    // ══════════════════════════════════════════════════
    //  NOTIFICATION
    // ══════════════════════════════════════════════════

    @Step("Wait for notification message")
    public void waitForNotification(long timeoutSec) {
        waitUtils.waitForVisible(NOTIFICATION_MSG, timeoutSec);
    }

    @Step("Wait for notification to dismiss")
    public void waitForNotificationToDismiss(long timeoutSec) {
        waitUtils.waitForInvisible(NOTIFICATION_MSG, timeoutSec);
    }

    public boolean isNotificationVisible(long timeoutSec) {
        return isPresent(NOTIFICATION_MSG, timeoutSec);
    }

    public String getNotificationMessage() {
        return getText(NOTIFICATION_MSG);
    }

    // ══════════════════════════════════════════════════
    //  POST-OTP RESULT
    // ══════════════════════════════════════════════════

    @Step("Wait for result screen after OTP")
    public void waitForResultAfterOtp(long timeoutSec) {
        try {
            waitUtils.waitForVisible(RESULT_SCREEN, timeoutSec);
        } catch (Exception e) {
            log.warn("waitForResultAfterOtp: no success/error screen within {}s", timeoutSec);
        }
    }

    // ══════════════════════════════════════════════════
    //  KEYBOARD
    // ══════════════════════════════════════════════════

    @Step("Dismiss keyboard")
    public void dismissKeyboard() {
        platformActions.dismissKeyboard();
    }
}
