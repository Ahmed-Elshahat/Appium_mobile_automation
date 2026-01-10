/**
 * @author Ahmed-Elshahat
 */
package com.appium.utils;

import com.appium.config.ConfigFactory;
import com.appium.constants.Platform;
import com.appium.drivers.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

public class DeepLinkUtils {

    private DeepLinkUtils() {
    }

    /**
     * Navigates to a specific URL/Deep Link based on the platform.
     * 
     * @param url         The deep link URL (e.g., "theapp://login")
     * @param packageName (Android only) The package name of the app handling the
     *                    link
     */
    public static void openDeepLink(String url, String packageName) {
        WebDriver driver = DriverManager.getDriver();
        String platform = ConfigFactory.getConfig().platformName();

        if (Platform.ANDROID.name().equalsIgnoreCase(platform)) {
            // Android: Use "mobile: deepLink"
            // Args: url, package
            Map<String, String> args = new HashMap<>();
            args.put("url", url);
            args.put("package", packageName);
            ((JavascriptExecutor) driver).executeScript("mobile: deepLink", args);

        } else if (Platform.IOS.name().equalsIgnoreCase(platform)) {
            // iOS: simpler using standard get() for many deep links,
            // or "mobile: deepLink" if needed for older versions.
            // Modern Appium iOS often handles deep links via driver.get(url) if the app
            // supports it.
            // Alternatively, verify "mobile: deepLink" support for XCUITest.

            // Attempt standard get()
            driver.get(url);

            // Fallback or explicit method if above fails:
            // Map<String, String> args = new HashMap<>();
            // args.put("url", url);
            // args.put("bundleId", packageName); // bundleId instead of package
            // ((JavascriptExecutor) driver).executeScript("mobile: deepLink", args);
        }
    }
}
