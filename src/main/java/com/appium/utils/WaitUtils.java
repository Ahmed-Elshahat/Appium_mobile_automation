/**
 * @author Ahmed-Elshahat
 */
package com.appium.utils;

import com.appium.config.ConfigFactory;
import com.appium.drivers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class WaitUtils {

    private WaitUtils() {
    }

    private static final int DEFAULT_TIMEOUT = 10;

    public static WebElement waitForVisibility(By by) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitForClickability(By by) {
        return getWait().until(ExpectedConditions.elementToBeClickable(by));
    }

    public static WebElement waitForPresence(By by) {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static boolean waitForInvisibility(By by) {
        try {
            return getWait().until(ExpectedConditions.invisibilityOfElementLocated(by));
        } catch (TimeoutException e) {
            return false;
        }
    }

    private static WebDriverWait getWait() {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
    }
}
