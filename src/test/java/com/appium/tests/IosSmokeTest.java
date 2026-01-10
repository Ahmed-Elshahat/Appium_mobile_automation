/**
 * @author Ahmed-Elshahat
 */
package com.appium.tests;

import com.appium.drivers.DriverManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IosSmokeTest extends BaseTest {

    @Test
    public void testSettingsAppLaunch() {
        // Just verify driver is alive and app is launched
        Assert.assertNotNull(DriverManager.getDriver(), "Driver should be initialized");
        System.out.println("iOS Driver initialized successfully!");

        // Settings app landing page usually has "Settings" title or similar.
        // For smoke test, successful launch without exception is a huge win.
        try {
            Thread.sleep(5000); // Wait to see the app
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
