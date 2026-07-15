package com.urpay.tests.dmp.orders;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpDashboardOrderFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP LANE C — Validate Placing Order With Selected Product.
 *
 * Migrated from Katalon (DMP_1):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/FullCycleDigitalMarketPlaceOrder/
 *   Validate Placing Order With Selected Product
 *
 * Journey: login → Store → open a specific product via deep link
 * (urpay://MarketPlace/ProductDetails?sku=&lt;sku&gt;) → add to cart → Go to Checkout →
 * place order → payment OTP → assert "Order Placed!".
 */
@Epic("DMP")
@Feature("Digital Order — Selected Product")
public class PlaceOrderWithSelectedProductTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-order", "regression"})
    @Description("Place an order for a specific product selected by deep link and verify the order is placed")
    @Severity(SeverityLevel.CRITICAL)
    public void testPlaceOrderWithSelectedProduct() {
        new LoginFlow().loginWith(
                config.get("dmpSelectedProduct.mobileNumber"),
                config.get("dmpSelectedProduct.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpOrderConfirmationPage confirmation = new DmpDashboardOrderFlow().orderSelectedProduct(
                config.get("dmp.selectedProductSku"),
                config.get("dmp.verificationCode", "1234"));

        Assert.assertTrue(confirmation.isOrderPlaced(),
                "The order should be placed successfully (Order Placed! screen shown)");
    }
}
