package com.urpay.tests.dmp;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.DmpMarketPlacePage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Suite 01 — Validate Top Product.
 *
 * Migrated from Katalon Test Suites/DMP TestSuite/01_ValidateTopProduct:
 *   setupTestData → NavigateToDMP → Top-Product/Navigatetoalltopproduct
 *
 * The legacy Katalon case verified a "Top products" section on the old MarketPlace layout.
 * The current app ships the REVAMPED "Store" page, so the equivalent validation is that the
 * Store loads with its default Best Sellers product list — i.e. the featured/top products are
 * displayed.
 */
@Epic("DMP")
@Feature("Top Products")
public class ValidateTopProductTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "smoke"})
    @Description("Validate the Store loads with its Best Sellers product list on the DMP MarketPlace main page")
    @Severity(SeverityLevel.NORMAL)
    public void testTopProductsSectionVisible() {
        new LoginFlow().loginWith(
                config.get("dmp.mobileNumber"),
                config.get("dmp.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpMarketPlacePage marketplace = new DmpFlow().openMarketPlace();

        SoftAssert soft = new SoftAssert();
        soft.assertTrue(marketplace.isLoaded(), "Store (MarketPlace main) page should be loaded");
        soft.assertTrue(marketplace.isBestSellersTagDisplayed(), "Best Sellers product tag should be displayed");
        soft.assertTrue(marketplace.isProductListDisplayed(), "The product list should be displayed");
        soft.assertAll();
    }
}
