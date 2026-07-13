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
 * DMP Order From New Arrival.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Validate order from new arrival/
 *     Order From new arrival device
 *   → setup → login → NavigateToDMP → removeCart → New Arrivals tag → select product →
 *     Buy now → checkout → complete delivery location → place order → OTP → Order Placed.
 */
@Epic("DMP")
@Feature("Order From New Arrival")
public class OrderFromNewArrivalTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "regression"})
    @Description("Order the first New Arrivals product and verify the order is placed")
    @Severity(SeverityLevel.CRITICAL)
    public void testOrderFromNewArrival() {
        new LoginFlow().loginWith(
                config.get("dmp.mobileNumber"),
                config.get("dmp.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpOrderConfirmationPage confirmation = new DmpFlow().orderFromNewArrival(
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.deliveryLocation", "Haij"));

        Assert.assertTrue(confirmation.isOrderPlaced(),
                "The new-arrival order should be placed successfully (Order Placed! screen shown)");
    }
}
