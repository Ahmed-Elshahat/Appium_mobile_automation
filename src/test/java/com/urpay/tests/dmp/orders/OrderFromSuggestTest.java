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
 * DMP LANE C — Order From Suggest.
 *
 * Migrated from Katalon (DMP_1):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Order From Suggest/
 *   Validate Placing Order from suggest
 *
 * Journey: login → Store → Suggested tab → select first product → add to cart →
 * View Cart → Go to Checkout → place order → payment OTP → assert "Order Placed!".
 */
@Epic("DMP")
@Feature("Digital Order — Suggested")
public class OrderFromSuggestTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-order", "regression"})
    @Description("Order the first Suggested product from the Store dashboard and verify the order is placed")
    @Severity(SeverityLevel.CRITICAL)
    public void testOrderFromSuggest() {
        new LoginFlow().loginWith(
                config.get("dmp.mobileNumber"),
                config.get("dmp.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpOrderConfirmationPage confirmation =
                new DmpDashboardOrderFlow().orderFromSuggest(config.get("dmp.verificationCode", "1234"));

        Assert.assertTrue(confirmation.isOrderPlaced(),
                "The order should be placed successfully (Order Placed! screen shown)");
    }
}
