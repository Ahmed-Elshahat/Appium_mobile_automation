package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Search screen — reached by tapping the Store search bar.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Cases/DMP/Search/Move to Search                 → opens this screen
 *   Test Cases/DMP/Categories/Navigate to First Category → taps a "Suggested categories"
 *       tile and verifies the resulting category header.
 *
 * Live layout (verified 2026-07-13): a "Search" header, a search input, and a
 * "Suggested categories" row (Alrajhi Offers / App Store / Food / Gaming / Offers).
 * Category tiles are anchored on their visible label text (most testIDs are UUID-based).
 */
public class DmpSearchPage extends BasePage {

    // Stable "Suggested categories" section header — the loaded marker for the search screen.
    @AndroidFindBy(xpath = "//*[@text='Suggested categories']")
    private WebElement suggestedCategoriesHeader;

    // Search input field — the only EditText on the search screen.
    @AndroidFindBy(className = "android.widget.EditText")
    private WebElement searchInput;

    @Step("Check the Search screen is loaded")
    public boolean isLoaded() {
        return isDisplayed(suggestedCategoriesHeader, 20);
    }

    @Step("Search for the product '{productName}'")
    public void searchFor(String productName) {
        tap(searchInput, 20);
        type(searchInput, productName);
        hideKeyboard();
    }

    @Step("Check a product matching '{productName}' appears in the search results")
    public boolean isProductInResults(String productName) {
        return isPresent(
                AppiumBy.xpath("//*[contains(@text,'" + productName + "')]"), 20);
    }

    @Step("Select the '{categoryName}' suggested category")
    public DmpCategoryResultsPage selectCategory(String categoryName) {
        tap(AppiumBy.xpath("//*[@text='" + categoryName + "']"), 20);
        return new DmpCategoryResultsPage();
    }

    @Step("Open the first search result matching '{name}'")
    public com.urpay.pages.dmp.DmpProductDetailsPage openResult(String name) {
        tap(AppiumBy.xpath("(//*[contains(@text,'" + name + "')])[1]"), 20);
        return new com.urpay.pages.dmp.DmpProductDetailsPage();
    }
}
