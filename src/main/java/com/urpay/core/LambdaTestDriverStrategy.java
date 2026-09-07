package com.urpay.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

/**
 * OCP: Strategy for creating LambdaTest remote drivers.
 * Supports both Android and iOS with typed options (no deprecated DesiredCapabilities).
 */
public class LambdaTestDriverStrategy implements DriverCreationStrategy {

    private static final String DEFAULT_APP_PACKAGE = "com.urpay.consumer.sit";
    private static final String DEFAULT_APP_ACTIVITY = "com.urpay.consumer.sit.MainActivity";
    private static final String DEFAULT_BUNDLE_ID = "com.urpay.consumer.sit";

    @Override
    public AppiumDriver createDriver(ConfigManager config) {
        String platform = config.get("platform", "android");
        String ltUser = config.get("lt.username", "");
        String ltKey = config.get("lt.accessKey", "");
        String ltUrl = config.get("lt.url", "https://mobile-hub.lambdatest.com/wd/hub");

        Map<String, Object> ltOptions = buildLtOptions(config, ltUser, ltKey);

        try {
            String remoteUrl = String.format("https://%s:%s@%s",
                    ltUser, ltKey, ltUrl.replaceFirst("https?://", ""));

            if ("ios".equalsIgnoreCase(platform)) {
                return createIOSDriver(config, ltOptions, remoteUrl);
            } else {
                return createAndroidDriver(config, ltOptions, remoteUrl);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid LambdaTest URL", e);
        }
    }

    private AppiumDriver createAndroidDriver(ConfigManager config,
                                              Map<String, Object> ltOptions,
                                              String remoteUrl) throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(config.get("deviceName", "Samsung Galaxy S23"));
        options.setPlatformVersion(config.get("platformVersion", "14.0"));
        options.setAppPackage(config.get("appPackage", DEFAULT_APP_PACKAGE));
        options.setAppActivity(config.get("appActivity", DEFAULT_APP_ACTIVITY));

        String appUrl = config.get("lt.appUrl", "");
        if (!appUrl.isEmpty()) options.setApp(appUrl);

        // Enable logcat capture so the health checker can scan for FATAL EXCEPTION / ANR /
        // process-death signatures. LambdaTest defaults skipLogcatCapture=true, which would
        // leave driver.getLogs("logcat") empty and crash detection blind.
        options.setCapability("skipLogcatCapture", false);

        options.setCapability("lt:options", ltOptions);
        return new AndroidDriver(new URL(remoteUrl), options);
    }

    private AppiumDriver createIOSDriver(ConfigManager config,
                                          Map<String, Object> ltOptions,
                                          String remoteUrl) throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");
        options.setDeviceName(config.get("deviceName", "iPhone 15 Pro"));
        options.setPlatformVersion(config.get("platformVersion", "17.0"));
        options.setBundleId(config.get("bundleId", DEFAULT_BUNDLE_ID));

        String appUrl = config.get("lt.appUrl", "");
        if (!appUrl.isEmpty()) options.setApp(appUrl);

        options.setCapability("lt:options", ltOptions);
        return new IOSDriver(new URL(remoteUrl), options);
    }

    private Map<String, Object> buildLtOptions(ConfigManager config,
                                                String ltUser, String ltKey) {
        Map<String, Object> ltOptions = new HashMap<>();
        ltOptions.put("username", ltUser);
        ltOptions.put("accessKey", ltKey);
        ltOptions.put("build", config.get("lt.build", "URPay-Regression"));
        ltOptions.put("name", "URPay Smoke Test");
        ltOptions.put("isRealMobile", true);
        ltOptions.put("autoGrantPermissions", true);
        ltOptions.put("autoAcceptAlerts", true);
        ltOptions.put("w3c", true);
        ltOptions.put("video", true);
        // Enable LambdaTest server-side device (logcat) + crash log capture. This is a
        // LambdaTest cap (default false), distinct from Appium's skipLogcatCapture which LT
        // forces true on cloud. With this on, the session's device_logs_url / crash_logs_url
        // are populated and can be fetched post-session via the REST API for crash detection.
        ltOptions.put("devicelog", true);
        ltOptions.put("network", true);
        ltOptions.put("idleTimeout", 300);
        ltOptions.put("newCommandTimeout", 300);
        ltOptions.put("appiumVersion", "2.12.1");
        // Optional device geolocation (e.g. "SA" for Saudi Arabia) so map-based screens (DMP physical
        // delivery-location picker) resolve to the right country instead of the cloud device's GPS.
        String geoLocation = config.get("lt.geoLocation", "");
        if (!geoLocation.isEmpty()) {
            ltOptions.put("geoLocation", geoLocation);
        }
        return ltOptions;
    }
}
