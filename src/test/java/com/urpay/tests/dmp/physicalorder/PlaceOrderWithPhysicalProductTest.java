package com.urpay.tests.dmp.physicalorder;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpPhysicalOrderFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.physicalorder.DmpDeliveryLocationPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP LANE D — Validate Placing Order With Physical Product.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/PhysicalMarketPlace/
 *   Validate Placing Order With Physical Product
 *
 * Journey: login → Store → search a physical product → add to cart → View Cart → Go to Checkout →
 * enter / edit / add delivery locations. The Katalon suite ends at the delivery-location step
 * (no payment), so the primary assertion is that the delivery-location screen is reached.
 */
@Epic("DMP")
@Feature("Physical Order")
public class PlaceOrderWithPhysicalProductTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-physical-order", "regression"})
    @Description("Place an order for a physical product up to the delivery-location step")
    @Severity(SeverityLevel.CRITICAL)
    public void testPlaceOrderWithPhysicalProduct() {
        new LoginFlow().loginWith(
                config.get("dmpd.physical.mobileNumber"),
                config.get("dmpd.physical.id"),
                config.get("dmpd.physical.verificationCode", "1234"),
                config.get("dmpd.physical.passCode", "2233"));

        DmpPhysicalOrderFlow flow = new DmpPhysicalOrderFlow();
        DmpDeliveryLocationPage location =
                flow.reachDeliveryLocation(config.get("dmpd.physical.product", "iphone 16 Plus"));

        Assert.assertTrue(location.isLoaded(),
                "The delivery-location screen should be reached after checkout for a physical product");

        flow.enterDeliveryLocations(location,
                config.get("dmpd.physical.location1", "Riyadh KSA"),
                config.get("dmpd.physical.location1Edited", "Riyadh KSA Test"),
                config.get("dmpd.physical.location2", "Egpyt"));
    }
}
