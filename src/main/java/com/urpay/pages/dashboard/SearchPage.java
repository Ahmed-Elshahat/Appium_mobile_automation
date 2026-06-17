package com.urpay.pages.dashboard;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Dashboard search — used to find services like "Telecom Recharge".
 *
 * Locators from Katalon:
 *   Object Repository/android/Dev/SearchOnDashboard/searchIcon.rs → testID-right-icon-0
 *   Object Repository/android/Dev/SearchOnDashboard/searchInputFiled.rs → testID-Search-Input
 *   Object Repository/android/Dev/SearchOnDashboard/searchFirstResult.rs → testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0
 */
public class SearchPage extends BasePage {

    @AndroidFindBy(accessibility = "testID-right-icon-0")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-0")
    private WebElement searchIcon;

    @AndroidFindBy(accessibility = "testID-Search-Input")
    @iOSXCUITFindBy(accessibility = "testID-Search-Input")
    private WebElement searchInputField;

    @AndroidFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0")
    @iOSXCUITFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0")
    private WebElement firstSearchResult;

    @Step("Tap search icon")
    public void openSearch() {
        waitUtils.waitForClickable(
                io.appium.java_client.AppiumBy.accessibilityId("testID-right-icon-0"), 10).click();
    }

    @Step("Type search query: {query}")
    public void typeQuery(String query) {
        org.openqa.selenium.WebElement input = waitUtils.waitForClickable(
                io.appium.java_client.AppiumBy.accessibilityId("testID-Search-Input"), 10);
        input.clear();
        input.sendKeys(query);
    }

    @Step("Tap first search result")
    public void tapFirstResult() {
        waitUtils.waitForClickable(
                io.appium.java_client.AppiumBy.accessibilityId(
                        "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0"), 10).click();
    }

    @Step("Search for service: {query}")
    public void searchAndSelect(String query) {
        dismissPopups();
        openSearch();
        typeQuery(query);
        tapFirstResult();
    }

    /** Dismiss Notifications or other popups that may block search icon */
    private void dismissPopups() {
        new DashboardPage().dismissPopups();
    }

    public boolean isSearchFieldVisible() {
        return isDisplayed(searchInputField, 5);
    }
}
