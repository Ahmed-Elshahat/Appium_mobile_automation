package com.urpay.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * OCP: Strategy for creating local Android drivers.
 * Adding new Android-specific options requires modifying only this class.
 */
public class LocalAndroidDriverStrategy implements DriverCreationStrategy {

    private static final String DEFAULT_APP_PACKAGE = "com.urpay.consumer.sit";
    private static final String DEFAULT_APP_ACTIVITY = "com.urpay.consumer.sit.MainActivity";
    private static final String DEFAULT_APPIUM_URL = "http://127.0.0.1:4723";

    @Override
    public AppiumDriver createDriver(ConfigManager config) {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setAppPackage(config.get("appPackage", DEFAULT_APP_PACKAGE));
        options.setAppActivity(config.get("appActivity", DEFAULT_APP_ACTIVITY));
        options.setNoReset(true);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(300));
        options.setAutoGrantPermissions(true);

        setOptional(options, config);

        try {
            String appiumUrl = config.get("appiumUrl", DEFAULT_APPIUM_URL);
            return new AndroidDriver(new URL(appiumUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Appium URL: " + config.get("appiumUrl"), e);
        }
    }

    private void setOptional(UiAutomator2Options options, ConfigManager config) {
        String deviceName = config.get("deviceName", "");
        if (!deviceName.isEmpty()) options.setDeviceName(deviceName);

        String platformVersion = config.get("platformVersion", "");
        if (!platformVersion.isEmpty()) options.setPlatformVersion(platformVersion);

        String udid = config.get("udid", "");
        if (!udid.isEmpty()) options.setUdid(udid);

        String appPath = config.get("appPath", "");
        if (!appPath.isEmpty()) options.setApp(appPath);
    }
}
