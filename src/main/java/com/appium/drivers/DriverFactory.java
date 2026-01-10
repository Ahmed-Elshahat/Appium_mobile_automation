/**
 * @author Ahmed-Elshahat
 */
package com.appium.drivers;

import com.appium.config.ConfigFactory;
import com.appium.constants.Platform;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public final class DriverFactory {

    private static io.appium.java_client.service.local.AppiumDriverLocalService service;

    private DriverFactory() {
    }

    public static void startAppiumServer() {
        if (service == null || !service.isRunning()) {
            io.appium.java_client.service.local.AppiumServiceBuilder builder = new io.appium.java_client.service.local.AppiumServiceBuilder();
            builder.withIPAddress("127.0.0.1");
            builder.usingAnyFreePort();
            builder.withArgument(io.appium.java_client.service.local.flags.GeneralServerFlag.SESSION_OVERRIDE);
            builder.withArgument(io.appium.java_client.service.local.flags.GeneralServerFlag.LOG_LEVEL, "error");

            service = io.appium.java_client.service.local.AppiumDriverLocalService.buildService(builder);
            service.start();
            System.out.println("Appium Server started at: " + service.getUrl());
        }
    }

    public static void stopAppiumServer() {
        if (service != null && service.isRunning()) {
            service.stop();
            System.out.println("Appium Server stopped.");
        }
    }

    public static void initDriver() {
        // Auto-start server if configured or by default
        startAppiumServer();

        if (DriverManager.getDriver() == null) {
            String platformName = ConfigFactory.getConfig().platformName();

            // Use local service URL if available, else config URL
            URL appiumUrl;
            if (service != null && service.isRunning()) {
                appiumUrl = service.getUrl();
            } else {
                try {
                    appiumUrl = URI.create(ConfigFactory.getConfig().appiumServerUrl()).toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid Appium Server URL", e);
                }
            }

            if (Platform.ANDROID.name().equalsIgnoreCase(platformName)) {
                UiAutomator2Options options = new UiAutomator2Options();
                options.setDeviceName(ConfigFactory.getConfig().deviceName());
                options.setAppPackage(ConfigFactory.getConfig().appPackage());
                options.setAppActivity(ConfigFactory.getConfig().appActivity());
                options.setAutomationName(ConfigFactory.getConfig().automationName());
                if (ConfigFactory.getConfig().appPath() != null && !ConfigFactory.getConfig().appPath().isEmpty()) {
                    options.setApp(ConfigFactory.getConfig().appPath());
                }

                DriverManager.setDriver(new AndroidDriver(appiumUrl, options));
            } else if (Platform.IOS.name().equalsIgnoreCase(platformName)) {
                XCUITestOptions options = new XCUITestOptions();
                options.setDeviceName(ConfigFactory.getConfig().deviceName());
                options.setAutomationName("XCUITest");
                if (ConfigFactory.getConfig().platformVersion() != null) {
                    options.setPlatformVersion(ConfigFactory.getConfig().platformVersion());
                }
                // Add more iOS options as needed
                if (ConfigFactory.getConfig().appPath() != null && !ConfigFactory.getConfig().appPath().isEmpty()) {
                    options.setApp(ConfigFactory.getConfig().appPath());
                } else if (ConfigFactory.getConfig().bundleId() != null
                        && !ConfigFactory.getConfig().bundleId().isEmpty()) {
                    options.setBundleId(ConfigFactory.getConfig().bundleId());
                }
                DriverManager.setDriver(new IOSDriver(appiumUrl, options));
            } else {
                throw new IllegalArgumentException("Unsupported Platform: " + platformName);
            }
        }
    }

    public static void quitDriver() {
        if (DriverManager.getDriver() != null) {
            DriverManager.getDriver().quit();
            DriverManager.unload();
        }
        stopAppiumServer();
    }
}
