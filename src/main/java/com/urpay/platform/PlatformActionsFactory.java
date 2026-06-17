package com.urpay.platform;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * Factory for creating platform-specific actions.
 *
 * Usage:
 *   AppiumDriver driver = DriverFactory.getInstance().getDriver();
 *   MobilePlatformActions actions = PlatformActionsFactory.create(driver);
 *   actions.enterDigits("1234");
 */
public final class PlatformActionsFactory {

    private PlatformActionsFactory() {}

    /**
     * Create the appropriate MobilePlatformActions based on the runtime driver type.
     */
    public static MobilePlatformActions create(AppiumDriver driver) {
        if (driver instanceof AndroidDriver) {
            return new AndroidPlatformActions((AndroidDriver) driver);
        } else if (driver instanceof IOSDriver) {
            return new IOSPlatformActions((IOSDriver) driver);
        }
        throw new IllegalStateException(
                "Unsupported driver type: " + driver.getClass().getSimpleName());
    }

    /**
     * Determine the platform from the driver instance.
     */
    public static Platform detectPlatform(AppiumDriver driver) {
        if (driver instanceof IOSDriver) return Platform.IOS;
        return Platform.ANDROID;
    }
}
