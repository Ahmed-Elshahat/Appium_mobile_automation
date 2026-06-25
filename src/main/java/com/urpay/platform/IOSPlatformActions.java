package com.urpay.platform;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.utils.SwipeUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;

/**
 * iOS-specific platform actions using XCUITest capabilities.
 */
public class IOSPlatformActions implements MobilePlatformActions {

    private static final Logger log = LoggerFactory.getLogger(IOSPlatformActions.class);
    private final IOSDriver driver;

    public IOSPlatformActions(IOSDriver driver) {
        this.driver = driver;
    }

    @Override
    public void enterDigits(String digits) {
        // iOS: tap each digit key on the numeric keyboard by accessibility label
        for (char d : digits.toCharArray()) {
            try {
                WebElement key = driver.findElement(AppiumBy.accessibilityId(String.valueOf(d)));
                key.click();
            } catch (Exception e) {
                // Fallback: use sendKeys on the focused element
                log.debug("Digit key '{}' not found by accessibilityId, using sendKeys", d);
                WebElement active = driver.switchTo().activeElement();
                active.sendKeys(String.valueOf(d));
            }
        }
    }

    @Override
    public void clearDigits(int count) {
        for (int i = 0; i < count; i++) {
            try {
                WebElement active = driver.switchTo().activeElement();
                active.sendKeys("\b");
            } catch (Exception ignored) {
                // No focused input / nothing to delete — safe to ignore.
            }
        }
    }

    @Override
    public void dismissKeyboard() {
        try {
            // Try tapping "Done" or "Return" key
            WebElement doneKey = driver.findElement(AppiumBy.accessibilityId("Done"));
            doneKey.click();
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
        // iOS: Use mobile:scroll with predicateString
        Map<String, Object> params = new HashMap<>();
        params.put("direction", "down");
        params.put("predicateString", "label == '" + text + "' OR value == '" + text + "'");
        driver.executeScript("mobile: scroll", params);
        return driver.findElement(AppiumBy.iOSNsPredicateString(
                "label == '" + text + "' OR value == '" + text + "'"));
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
        Map<String, Object> params = new HashMap<>();
        params.put("direction", "left");
        params.put("predicateString", "label == '" + text + "' OR value == '" + text + "'");
        try {
            driver.executeScript("mobile: scroll", params);
        } catch (Exception e) {
            log.warn("Horizontal scroll to '{}' via mobile:scroll failed: {}", text, e.getMessage());
        }
    }

    @Override
    public void inputTextDirect(String text) {
        // iOS: Use pasteboard + paste, or setValue on focused element
        try {
            driver.setClipboardText(text);
            // Tap and hold to paste
            WebElement active = driver.switchTo().activeElement();
            active.sendKeys(text);
        } catch (Exception e) {
            log.warn("inputTextDirect fallback to sendKeys: {}", e.getMessage());
            driver.switchTo().activeElement().sendKeys(text);
        }
    }

    @Override
    public Platform getPlatform() {
        return Platform.IOS;
    }
}
