package com.appium.pages;

import com.appium.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BasePage {

    protected void click(By by, String elementName) {
        WaitUtils.waitForClickability(by).click();
        // Log info: Clicked on elementName
    }

    protected void sendKeys(By by, String value, String elementName) {
        WebElement element = WaitUtils.waitForVisibility(by);
        element.clear();
        element.sendKeys(value);
        // Log info: Entered value in elementName
    }

    protected String getText(By by) {
        return WaitUtils.waitForVisibility(by).getText();
    }

    protected void waitForVisibility(By by) {
        WaitUtils.waitForVisibility(by);
    }
}
