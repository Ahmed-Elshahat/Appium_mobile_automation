package com.urpay.tests.dmp.gift;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpGiftFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.gift.DmpGiftPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP LANE D — Validate placing order as a gift and check it sent to gift receiver.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/FullCycleDigitalMarketPlaceOrder/
 *   Validate placing order as a gift and check it sent to gift receiver
 *
 * Journey: login → Store → open the product via deep link → add to cart → open cart →
 * "Send as gift" → add the gift-receiver mobile number → select gift card → enter the gift
 * message → Next → pay/confirm the gift order → "Order Placed!" (the gift is sent to the
 * receiver).
 */
@Epic("DMP")
@Feature("Gift Order")
public class PlaceOrderAsGiftTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-gift", "regression"})
    @Description("Place a digital marketplace order as a gift and confirm it is sent to the gift receiver")
    @Severity(SeverityLevel.CRITICAL)
    public void testPlaceOrderAsGift() {
        new LoginFlow().loginWith(
                config.get("dmpd.gift.mobileNumber"),
                config.get("dmpd.gift.id"),
                config.get("dmpd.gift.verificationCode", "1234"),
                config.get("dmpd.gift.passCode", "2233"));

        DmpGiftFlow flow = new DmpGiftFlow();
        DmpGiftPage gift = flow.placeOrderAsGift(
                config.get("dmpd.gift.productSku", "6280066009007"),
                config.get("dmpd.gift.receiverNumber", "0506020187"),
                config.get("dmpd.gift.message", "testGift"));

        Assert.assertTrue(gift.isPayConfirmScreenDisplayed(),
                "The gift pay/confirm screen should be reached after preparing the gift for the receiver");

        boolean orderPlaced = flow.payForGift(gift, config.get("dmpd.gift.verificationCode", "1234"))
                .isOrderPlaced();
        Assert.assertTrue(orderPlaced,
                "The gift order should be placed ('Order Placed!') and sent to the gift receiver");
    }
}
