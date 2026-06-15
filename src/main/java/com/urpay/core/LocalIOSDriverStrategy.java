package com.urpay.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * OCP: Strategy for creating local iOS drivers.
 */
public class LocalIOSDriverStrategy implements DriverCreationStrategy {

    private static final String DEFAULT_BUNDLE_ID = "com.urpay.consumer.sit";
    private static final String DEFAULT_APPIUM_URL = "http://127.0.0.1:4723";

    @Override
    public AppiumDriver createDriver(ConfigManager config) {
        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");
        options.setBundleId(config.get("bundleId", DEFAULT_BUNDLE_ID));
        options.setNoReset(true);
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        String deviceName = config.get("deviceName", "");
        if (!deviceName.isEmpty()) options.setDeviceName(deviceName);

        String platformVersion = config.get("platformVersion", "");
        if (!platformVersion.isEmpty()) options.setPlatformVersion(platformVersion);

        try {
            String appiumUrl = config.get("appiumUrl", DEFAULT_APPIUM_URL);
            return new IOSDriver(new URL(appiumUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Appium URL: " + config.get("appiumUrl"), e);
        }
    }
}
