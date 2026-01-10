/**
 * @author Ahmed-Elshahat
 */
package com.appium.pages;

import org.openqa.selenium.By;

public class SettingsPage extends BasePage {

    // Locators for Settings App (Stock Android)
    private final By btnBattery = By.xpath("//android.widget.TextView[@text='Battery']");
    private final By btnDisplay = By.xpath("//android.widget.TextView[@text='Display']");

    // For verifying we are on the new screen
    private final By lblBatteryPercentage = By.xpath("//android.widget.TextView[contains(@text, '%')]");

    public SettingsPage clickBattery() {
        click(btnBattery, "Battery Menu Item");
        return this;
    }

    public SettingsPage clickDisplay() {
        click(btnDisplay, "Display Menu Item");
        return this;
    }
}
