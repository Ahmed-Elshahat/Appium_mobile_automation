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
 * DMP Search by product name.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Cases/DMP/Search/Move to Search → Test Cases/DMP/Search/SearchByProductName
 *   (taps the search input, types a product name, and verifies the product appears in the
 *   results list — Object Repository/android/DMP/mobileProductTextFirstOccurance / productID).
 *
 * The search term is read from config ({@code dmp.searchProduct}); the test verifies a result
 * whose text contains that term is present.
 */
@Epic("DMP")
@Feature("Search")
public class SearchByProductNameTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "regression"})
    @Description("Search for a product by name and verify it appears in the search results")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchByProductName() {
        String product = config.get("dmp.searchProduct", "Twitch");

        new LoginFlow().loginWith(
                config.get("dmpSearchProd.mobileNumber"),
                config.get("dmpSearchProd.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpMarketPlacePage marketplace = new DmpFlow().openMarketPlace();
        DmpSearchPage search = marketplace.openSearch();
        Assert.assertTrue(search.isLoaded(), "The Search screen should be loaded");

        search.searchFor(product);
        Assert.assertTrue(search.isProductInResults(product),
                "The product '" + product + "' should appear in the search results");
    }
}
