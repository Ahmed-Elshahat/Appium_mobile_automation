package com.urpay.tests.dmp;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.DmpMarketPlacePage;
import com.urpay.pages.dmp.DmpSearchPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Search by valid category.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/
 *     Search Feature (Category ,Sort ,Price Range)/Search by valid category
 *   → setupTestData → login → NavigateToDMP → Search/Move to Search →
 *     Categories/Navigate to First Category (tap "App Store" tile, verify header == "App Store").
 */
@Epic("DMP")
@Feature("Search & Category")
public class SearchByCategoryTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "regression"})
    @Description("Open Search, select the first (App Store) category and verify the category header")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchByValidCategory() {
        new LoginFlow().loginWith(
                config.get("dmpSearchCat.mobileNumber"),
                config.get("dmpSearchCat.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpMarketPlacePage marketplace = new DmpFlow().openMarketPlace();
        DmpSearchPage search = marketplace.openSearch();
        Assert.assertTrue(search.isLoaded(), "The Search screen should be loaded");

        com.urpay.pages.dmp.DmpCategoryResultsPage results = search.selectCategory("App Store");
        Assert.assertTrue(results.isCategoryShown("App Store"),
                "The 'App Store' category results should be shown");
    }
}
