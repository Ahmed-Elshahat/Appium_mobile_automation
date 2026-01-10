/**
 * @author Ahmed-Elshahat
 */
package com.appium.drivers;

import io.appium.java_client.AppiumDriver;

public final class DriverManager {

    private DriverManager() {
    }

    private static final ThreadLocal<org.openqa.selenium.WebDriver> DRIVER = new ThreadLocal<>();

    public static org.openqa.selenium.WebDriver getDriver() {
        return DRIVER.get();
    }

    public static void setDriver(org.openqa.selenium.WebDriver driver) {
        DRIVER.set(driver);
    }

    public static void unload() {
        DRIVER.remove();
    }
}
