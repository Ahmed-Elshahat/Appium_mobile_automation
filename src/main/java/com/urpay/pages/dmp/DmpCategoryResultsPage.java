package com.urpay.pages.dmp;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * DMP Category results screen — shown after selecting a "Suggested categories" tile on the
 * search screen.
 *
 * Migrated from the verification step of Katalon
 * {@code Test Cases/DMP/Categories/Navigate to First Category}, which reads the category
 * header text and asserts it matches the selected category.
 */
public class DmpCategoryResultsPage extends BasePage {

    @Step("Check the '{categoryName}' category header is shown")
    public boolean isCategoryShown(String categoryName) {
        return isPresent(AppiumBy.xpath("//*[@text='" + categoryName + "']"), 20);
    }
}
