package com.urpay.platform;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.helpers.AdbHelper;
import com.urpay.utils.SwipeUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

/**
 * Android-specific platform actions using UiAutomator2 capabilities.
 */
public class AndroidPlatformActions implements MobilePlatformActions {

    private static final Logger log = LoggerFactory.getLogger(AndroidPlatformActions.class);
    private final AndroidDriver driver;

    public AndroidPlatformActions(AndroidDriver driver) {
        this.driver = driver;
    }

    @Override
    public void enterDigits(String digits) {
        for (char d : digits.toCharArray()) {
            driver.pressKey(new KeyEvent(AndroidKey.valueOf("DIGIT_" + d)));
        }
    }

    @Override
    public void clearDigits(int count) {
        for (int i = 0; i < count; i++) {
            try {
                driver.pressKey(new KeyEvent(AndroidKey.DEL));
            } catch (Exception ignored) {
                // No focused input / nothing to delete — safe to ignore.
            }
        }
    }

    @Override
    public void dismissKeyboard() {
        try {
            driver.pressKey(new KeyEvent(AndroidKey.ENTER));
        } catch (Exception e) {
            try {
                driver.hideKeyboard();
            } catch (Exception ignored) {
                log.debug("Keyboard not visible, skip dismiss");
            }
        }
    }

    @Override
    public WebElement scrollToText(String text) {
        return driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))" +
                        ".scrollTextIntoView(\"" + text + "\")"));
    }

    @Override
    public WebElement scrollToElement(By locator, int maxAttempts) {
        SwipeUtils swipe = new SwipeUtils(driver);
        for (int i = 0; i < maxAttempts; i++) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) return el;
            } catch (Exception ignored) {}
            swipe.swipeUp();
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Element not found after " + maxAttempts + " swipes: " + locator);
    }

    @Override
    public void scrollHorizontalToText(String text) {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true))" +
                            ".setAsHorizontalList()" +
                            ".scrollIntoView(new UiSelector().text(\"" + text + "\"))"));
        } catch (Exception e) {
            log.warn("Horizontal scroll to '{}' via UiScrollable failed: {}", text, e.getMessage());
        }
    }

    @Override
    public void inputTextDirect(String text) {
        AdbHelper.inputText(text);
    }

    @Override
    public Platform getPlatform() {
        return Platform.ANDROID;
    }
}
