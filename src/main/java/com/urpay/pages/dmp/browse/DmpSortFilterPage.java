package com.urpay.pages.dmp.browse;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP sort / filter bottom sheet — opened from the All Items listing filter button.
 *
 * LANE A (Sorting & Grouping) — parallel migration namespace {@code pages/dmp/browse}.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Cases/DMP/Sorting/To Validate Select Sorting Type (Script1737022966627.groovy)
 *   → taps the high→low or low→high price radio then Apply.
 *
 * Locators (Object Repository/Android/DMP/FilterPage):
 *   highToLowRadioButton = accessibility {@code testID-radio-item-1}  (price high → low)
 *   lowToHighRadioButton = accessibility {@code testID-radio-item-0}  (price low  → high)
 *   applyButton          = {@code testID-primary-onPress-main} / text "Apply"
 */
public class DmpSortFilterPage extends BasePage {

    // Sort-by-price radio options.
    @AndroidFindBy(accessibility = "testID-radio-item-1")
    private WebElement highToLowRadio;

    @AndroidFindBy(accessibility = "testID-radio-item-0")
    private WebElement lowToHighRadio;

    // Apply button (text-tolerant: obfuscated builds hash the middle testID segment).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onPress-main' or @text='Apply']")
    private WebElement applyButton;

    @Step("Check the sort / filter sheet is displayed")
    public boolean isLoaded() {
        return isDisplayed(applyButton, 15);
    }

    @Step("Sort products by price (descending={descending}) and apply")
    public DmpAllItemsPage sortByPrice(boolean descending) {
        tap(descending ? highToLowRadio : lowToHighRadio, 20);
        tap(applyButton, 20);
        return new DmpAllItemsPage();
    }
}
