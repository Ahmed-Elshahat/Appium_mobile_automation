package com.urpay.platform;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;

/**
 * Platform-neutral abstraction for actions that differ between Android and iOS.
 *
 * SOLID:
 *   ISP — Only platform-varying behavior. Common Appium actions remain in BasePage/SwipeUtils.
 *   DIP — Flows depend on this interface, not concrete AndroidDriver/IOSDriver.
 *   OCP — New platforms (e.g. Flutter) can be added by implementing this interface.
 *
 * Factory: Use {@link PlatformActionsFactory#create(AppiumDriver)} to obtain an instance.
 */
public interface MobilePlatformActions {

    /**
     * Enter digits on a custom keypad (OTP, passcode, PIN).
     * Android: Uses native key events (AndroidKey.DIGIT_x).
     * iOS: Uses XCUITest keyboard or element taps.
     */
    void enterDigits(String digits);

    /**
     * Delete the given number of previously entered digits from a custom keypad.
     * Used to clear partial/stale entries before re-entering (e.g. a focus-safe retry).
     * Android: native key events (AndroidKey.DEL). iOS: backspace on the active element.
     */
    void clearDigits(int count);

    /**
     * Dismiss the software keyboard.
     * Android: pressKey(ENTER) or hideKeyboard().
     * iOS: tap done/return or hideKeyboard().
     */
    void dismissKeyboard();

    /**
     * Scroll to an element containing the specified text.
     * Android: UiScrollable.scrollTextIntoView().
     * iOS: mobile:scroll with predicate.
     */
    WebElement scrollToText(String text);

    /**
     * Scroll a scrollable container to bring an element into view.
     * Android: UiScrollable.scrollIntoView().
     * iOS: mobile:scroll direction until element found.
     */
    WebElement scrollToElement(By locator, int maxAttempts);

    /**
     * Scroll horizontally within a carousel/list to find text.
     * Android: UiScrollable.setAsHorizontalList().scrollIntoView().
     * iOS: mobile:scroll direction=left with predicate.
     */
    void scrollHorizontalToText(String text);

    /**
     * Input text bypassing the app's custom keyboard.
     * Android: ADB shell input text.
     * iOS: Direct setValue or pasteboard.
     */
    void inputTextDirect(String text);

    /**
     * Get the current platform identifier.
     */
    Platform getPlatform();
}
