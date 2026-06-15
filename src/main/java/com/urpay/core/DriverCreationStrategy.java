package com.urpay.core;

/**
 * OCP: Strategy interface for creating Appium drivers.
 * Adding a new cloud provider (BrowserStack, SauceLabs) only requires
 * a new implementation — no modification to DriverFactory.
 */
public interface DriverCreationStrategy {

    io.appium.java_client.AppiumDriver createDriver(ConfigManager config);
}
