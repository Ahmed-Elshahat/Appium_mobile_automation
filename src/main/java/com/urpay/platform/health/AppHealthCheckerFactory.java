package com.urpay.platform.health;

import com.urpay.core.ConfigManager;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * Factory for creating the appropriate AppHealthChecker based on the driver type.
 */
public final class AppHealthCheckerFactory {

    private AppHealthCheckerFactory() {}

    public static AppHealthChecker create(AppiumDriver driver) {
        ConfigManager config = ConfigManager.getInstance();
        if (driver instanceof AndroidDriver) {
            String appPackage = config.get("appPackage", "com.urpay.consumer.sit");
            return new AndroidAppHealthChecker((AndroidDriver) driver, appPackage);
        } else if (driver instanceof IOSDriver) {
            String bundleId = config.get("bundleId", "com.urpay.consumer.sit");
            return new IOSAppHealthChecker((IOSDriver) driver, bundleId);
        }
        throw new IllegalStateException(
                "Unsupported driver type: " + driver.getClass().getSimpleName());
    }
}
