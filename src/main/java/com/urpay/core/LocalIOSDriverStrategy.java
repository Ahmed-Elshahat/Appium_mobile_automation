package com.urpay.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * OCP: Strategy for creating local iOS drivers.
 * Mirrors LocalAndroidDriverStrategy with iOS-specific options.
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

        // Auto-accept system alerts (permissions)
        options.setCapability("autoAcceptAlerts", true);

        setOptional(options, config);

        try {
            String appiumUrl = config.get("appiumUrl", DEFAULT_APPIUM_URL);
            return new IOSDriver(new URL(appiumUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Appium URL: " + config.get("appiumUrl"), e);
        }
    }

    private void setOptional(XCUITestOptions options, ConfigManager config) {
        String deviceName = config.get("deviceName", "");
        if (!deviceName.isEmpty()) options.setDeviceName(deviceName);

        String platformVersion = config.get("platformVersion", "");
        if (!platformVersion.isEmpty()) options.setPlatformVersion(platformVersion);

        String udid = config.get("udid", "");
        if (!udid.isEmpty()) options.setUdid(udid);

        String appPath = config.get("appPath", "");
        if (!appPath.isEmpty()) options.setApp(appPath);

        // WDA settings for stability
        String wdaLocalPort = config.get("wdaLocalPort", "");
        if (!wdaLocalPort.isEmpty()) {
            options.setCapability("wdaLocalPort", Integer.parseInt(wdaLocalPort));
        }
    }
}
