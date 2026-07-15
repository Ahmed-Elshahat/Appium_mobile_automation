package com.urpay.tests.dmp.orders;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpMokafaaFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP LANE D — Validate Placing Order Using Mokafaa Points.
 *
 * Migrated from Katalon (DMP_1):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/FullCycleDigitalMarketPlaceOrder/
 *   Validate Placing Order Using Mokafaa Points
 *
 * Journey: login → Store → open the Mokafaa product via deep link → add to cart → Checkout →
 * redeem Mokafaa points (amount → Next → Confirm → verification code → Done) → re-open Checkout →
 * Place Order → payment verification code → assert "Order Placed!".
 *
 * NOTE: this journey needs an account that HOLDS enough Mokafaa points to redeem
 * ({@code dmpd.mokafaa.pointsToRedeem}). The Katalon setup also seeds the wallet balance via a
 * backend API (VPN-only); the exact post-redemption balance assertion is therefore omitted here
 * (Katalon soft-asserts it), and the placed order is the primary verification.
 */
@Epic("DMP")
@Feature("Digital Order — Mokafaa Points")
public class PlaceOrderUsingMokafaaPointsTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-order", "regression"})
    @Description("Redeem Mokafaa points and place a digital order, then verify the order is placed")
    @Severity(SeverityLevel.CRITICAL)
    public void testPlaceOrderUsingMokafaaPoints() {
        new LoginFlow().loginWith(
                config.get("dmpd.mokafaa.mobileNumber"),
                config.get("dmpd.mokafaa.id"),
                config.get("dmpd.mokafaa.verificationCode", "1234"),
                config.get("dmpd.mokafaa.passCode", "2233"));

        DmpOrderConfirmationPage confirmation = new DmpMokafaaFlow().placeOrderUsingMokafaaPoints(
                config.get("dmpd.mokafaa.productSku"),
                config.get("dmpd.mokafaa.pointsToRedeem"),
                config.get("dmpd.mokafaa.verificationCode", "1234"));

        Assert.assertTrue(confirmation.isOrderPlaced(),
                "The order should be placed successfully using Mokafaa points (Order Placed! screen shown)");
    }
}
