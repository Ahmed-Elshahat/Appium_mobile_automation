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
     * Scroll a scrollable container to bring an element with the given accessibility id
     * (Android content-desc / iOS name) into view. Unlike {@link #scrollToText(String)} this
     * matches the stable testID, and UiScrollable stops exactly when the element renders
     * (handles React-Native virtualization without overshooting the target).
     * Android: UiScrollable.scrollIntoView(descriptionContains).
     * iOS: mobile:scroll with name predicate.
     */
    WebElement scrollToContentDesc(String contentDesc);

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
     * Open an in-app deep link (e.g. urpay://MORENAV/Settings). Brings the app to the foreground
     * and routes directly to the target screen — avoids fragile multi-step back navigation.
     * Android: mobile: deepLink (am start VIEW intent). iOS: mobile: deepLink with bundleId.
     */
    void openDeepLink(String deepLink);

    /**
     * Get the current platform identifier.
     */
    Platform getPlatform();
}
