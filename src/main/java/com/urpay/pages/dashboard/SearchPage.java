package com.urpay.pages.dashboard;

import org.openqa.selenium.By;
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
        // A CHAT icon was added to the dashboard header, which can shift/share the generic
        // "testID-right-icon-N" indices with the search icon (screen has multiple right-side
        // icons now, not just search). Tap index 0 first; if it does NOT open the search input
        // (i.e. we tapped the wrong icon, e.g. chat), go back and try index 1, then a plain
        // text/description fallback — verifying the actual outcome instead of assuming position.
        By searchInput = io.appium.java_client.AppiumBy.accessibilityId("testID-Search-Input");
        By icon0 = io.appium.java_client.AppiumBy.accessibilityId("testID-right-icon-0");
        if (waitUtils.isPresent(icon0, 3)) {
            waitUtils.waitForClickable(icon0, 10).click();
            if (waitUtils.isPresent(searchInput, 3)) {
                return;
            }
            log.warn("testID-right-icon-0 did not open search (likely the new chat icon) — trying testID-right-icon-1");
            pressBack();
        }
        By icon1 = io.appium.java_client.AppiumBy.accessibilityId("testID-right-icon-1");
        if (waitUtils.isPresent(icon1, 3)) {
            waitUtils.waitForClickable(icon1, 10).click();
            if (waitUtils.isPresent(searchInput, 3)) {
                return;
            }
            log.warn("testID-right-icon-1 did not open search either — trying description/name fallback");
            pressBack();
        }
        // Last resort: some builds label the icon itself (content-desc/name containing "search").
        By byDescription = io.appium.java_client.AppiumBy.xpath(
                "//*[contains(translate(@content-desc,'SEARCH','search'),'search') "
                + "or contains(translate(@name,'SEARCH','search'),'search')]");
        waitUtils.waitForClickable(byDescription, 10).click();
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
        // Right after login a popup/banner can intercept the search-icon tap, so the
        // search input never appears. Retry once after re-dismissing popups.
        if (!isSearchFieldVisible()) {
            dismissPopups();
            openSearch();
        }
        typeQuery(query);
        // Search results can be slow, or the field may not have registered the text on
        // a re-navigation. If the first result hasn't rendered, re-type once, then wait
        // longer before tapping.
        org.openqa.selenium.By firstResult = io.appium.java_client.AppiumBy.accessibilityId(
                "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0");
        if (!waitUtils.isPresent(firstResult, 6)) {
            typeQuery(query);
        }
        waitUtils.waitForClickable(firstResult, 15).click();
    }

    /** Dismiss Notifications or other popups that may block search icon */
    private void dismissPopups() {
        new DashboardPage().dismissPopups();
    }

    public boolean isSearchFieldVisible() {
        return isDisplayed(searchInputField, 5);
    }
}
