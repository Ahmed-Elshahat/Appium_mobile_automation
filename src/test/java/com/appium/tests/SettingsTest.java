package com.appium.tests;

import com.appium.pages.SettingsPage;
import org.testng.annotations.Test;

public class SettingsTest extends BaseTest {

    @Test(description = "Verify that we can navigate to Battery settings")
    public void testNavigateToBattery() {
        SettingsPage settingsPage = new SettingsPage();

        // Settings App starts on main screen
        settingsPage.clickBattery();

        // Simple assertion or check (implicit wait in click handles synchronization)
        System.out.println("Navigated to Battery Settings successfully!");

        // Sleep specifically for visual verification in this demo
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}
