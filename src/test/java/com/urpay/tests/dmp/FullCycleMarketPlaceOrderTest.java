package com.urpay.tests.dmp;

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
 * DMP Full-Cycle Digital MarketPlace Order.
 *
 * Migrated from Katalon (DMP_1):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/FullCycleDigitalMarketPlaceOrder/
 *   "Validate Placing Order For Market Place Test Suite(Two Random Products with PromoCode)"
 *
 * The suite's two-random-product selection and promo-code application steps are ALL commented out
 * in the current Katalon build (the enabled chain hard-codes product SKU 6280066009007 and skips
 * promo/quantity), so the live journey is:
 *   setup → login → NavigateToDMP → Move To Specific Product Details By Deep Link (SKU) →
 *   ToValidateMoveToCheckoutPage (Checkout → Place Order) → to Validate Total View at Payment Page →
 *   ToValidatePaymentOrder (fillVerificationCode → "Order Placed!" → Done) →
 *   ToValidateCheckWalletBalanceIsDeducted (verifyEqual commented out in Katalon).
 *
 * That path is identical to the proven-GREEN {@link DmpDashboardOrderFlow#orderSelectedProduct}
 * (deep-link the SKU → add to cart → Go to Checkout → coordinate-tap Place Order → payment OTP →
 * Order Placed), which supersedes the frozen buy-now flow whose element {@code click()} on the
 * revamped React-Native Place Order button was a no-op.
 */
@Epic("DMP")
@Feature("Full Cycle Digital MarketPlace Order")
public class FullCycleMarketPlaceOrderTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-order", "regression"})
    @Description("Place a full-cycle digital marketplace order for a product opened by deep link "
            + "and verify the order is placed")
    @Severity(SeverityLevel.CRITICAL)
    public void testFullCycleMarketPlaceOrder() {
        new LoginFlow().loginWith(
                config.get("dmpFullCycle.mobileNumber"),
                config.get("dmpFullCycle.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpOrderConfirmationPage confirmation = new DmpDashboardOrderFlow().orderSelectedProduct(
                config.get("dmp.selectedProductSku"),
                config.get("dmp.verificationCode", "1234"));

        Assert.assertTrue(confirmation.isOrderPlaced(),
                "The order should be placed successfully (Order Placed! screen shown)");
    }
}
