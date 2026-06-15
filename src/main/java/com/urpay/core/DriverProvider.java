package com.urpay.core;

import io.appium.java_client.AppiumDriver;

/**
 * DIP: Abstraction for driver provision.
 * Decouples BasePage and BaseTest from concrete DriverFactory.
 */
public interface DriverProvider {

    void initDriver();

    AppiumDriver getDriver();

    void quitDriver();

    boolean isDriverActive();
}
