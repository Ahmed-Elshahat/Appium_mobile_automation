package com.urpay.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * OCP: Strategy for creating LambdaTest remote drivers.
 * To add BrowserStack, create BrowserStackDriverStrategy — no modifications here.
 */
public class LambdaTestDriverStrategy implements DriverCreationStrategy {

    private static final String DEFAULT_APP_PACKAGE = "com.urpay.consumer.sit";
    private static final String DEFAULT_APP_ACTIVITY = "com.urpay.consumer.sit.MainActivity";

    @Override
    public AppiumDriver createDriver(ConfigManager config) {
        String platform = config.get("platform", "android");
        String ltUser = config.get("lt.username", "");
        String ltKey = config.get("lt.accessKey", "");
        String ltUrl = config.get("lt.url", "https://mobile-hub.lambdatest.com/wd/hub");

        DesiredCapabilities caps = buildCapabilities(config, platform);
        attachLtOptions(caps, config, ltUser, ltKey);

        try {
            String remoteUrl = String.format("https://%s:%s@%s",
                    ltUser, ltKey, ltUrl.replaceFirst("https?://", ""));
            if ("ios".equalsIgnoreCase(platform)) {
                return new IOSDriver(new URL(remoteUrl), caps);
            } else {
                return new AndroidDriver(new URL(remoteUrl), caps);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid LambdaTest URL", e);
        }
    }

    private DesiredCapabilities buildCapabilities(ConfigManager config, String platform) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "ios".equalsIgnoreCase(platform) ? "iOS" : "Android");
        caps.setCapability("deviceName", config.get("deviceName", "Samsung Galaxy S23"));
        caps.setCapability("platformVersion", config.get("platformVersion", "14.0"));

        if ("ios".equalsIgnoreCase(platform)) {
            caps.setCapability("automationName", "XCUITest");
            caps.setCapability("bundleId", config.get("bundleId", DEFAULT_APP_PACKAGE));
        } else {
            caps.setCapability("automationName", "UiAutomator2");
            caps.setCapability("appPackage", config.get("appPackage", DEFAULT_APP_PACKAGE));
            caps.setCapability("appActivity", config.get("appActivity", DEFAULT_APP_ACTIVITY));
        }
        return caps;
    }

    private void attachLtOptions(DesiredCapabilities caps, ConfigManager config,
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
        ltOptions.put("idleTimeout", 300);
        ltOptions.put("newCommandTimeout", 300);
        ltOptions.put("appiumVersion", "2.12.1");

        String appUrl = config.get("lt.appUrl", "");
        if (!appUrl.isEmpty()) {
            ltOptions.put("app", appUrl);
            // Also set at top level for compatibility
            caps.setCapability("app", appUrl);
        }

        caps.setCapability("lt:options", ltOptions);
    }
}
