package com.urpay.tests.dmp;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Full-Cycle Digital MarketPlace Order.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP Test Suites/New Test Suites After Revamp/FullCycleDigitalMarketPlaceOrder
 *   → Test Cases/DMP/Wishlist/PlaceOrderFromWishlist
 *
 * Journey: login → Store → Wishlist → select first product → add to cart → cart →
 * checkout/pay → enter payment verification code → assert "Order Placed!".
 */
@Epic("DMP")
@Feature("Full Cycle Digital MarketPlace Order")
public class PlaceOrderFromWishlistTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "regression"})
    @Description("Place a full digital marketplace order from the wishlist and verify the order is placed")
    @Severity(SeverityLevel.CRITICAL)
    public void testPlaceOrderFromWishlist() {
        new LoginFlow().loginWith(
                config.get("dmp.mobileNumber"),
                config.get("dmp.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpOrderConfirmationPage confirmation =
                new DmpFlow().placeOrderFromWishlist(config.get("dmp.verificationCode", "1234"));

        Assert.assertTrue(confirmation.isOrderPlaced(),
                "The order should be placed successfully (Order Placed! screen shown)");
    }
}
