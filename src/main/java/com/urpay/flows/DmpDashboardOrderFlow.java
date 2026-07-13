package com.urpay.flows;

import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;
import com.urpay.pages.dmp.orders.DmpOrderCartPage;
import com.urpay.pages.dmp.orders.DmpOrderCheckoutPage;
import com.urpay.pages.dmp.orders.DmpOrderProductPage;
import com.urpay.pages.dmp.orders.DmpOrderStorePage;

import io.qameta.allure.Step;

/**
 * DMP dashboard digital-order flow (LANE C).
 *
 * Composes the LANE C {@code pages/dmp/orders/*} page objects into the three digital-order
 * journeys migrated from Katalon (DMP_1, New Test Suites After Revamp):
 *   - Order From new arrival device
 *   - Order From Suggest (Validate Placing Order from suggest)
 *   - Validate Placing Order With Selected Product
 *
 * All three share the same tail: Store dashboard product select → add to cart →
 * Go to Checkout → place order → payment OTP → Order Placed!
 *
 * Reuses the FROZEN foundation read-only: {@code DmpFlow.openMarketPlace()} to land on the
 * Store, {@code OtpPage.enterOtp(code)} for the payment verification code, and
 * {@code DmpOrderConfirmationPage} for the test to assert on.
 *
 * SOLID / project rules: ZERO Thread.sleep(); NO assertions (returns a page object); NO
 * hardcoded data (SKU / verification code are passed in from config by the test).
 */
public class DmpDashboardOrderFlow {

    @Step("Order the first New Arrivals product from the Store dashboard")
    public DmpOrderConfirmationPage orderFromNewArrival(String verificationCode) {
        new DmpFlow().openMarketPlace();
        DmpOrderProductPage product = new DmpOrderStorePage().selectNewArrivalsProduct(0);
        return checkoutAndPay(product, verificationCode);
    }

    @Step("Order the first Suggested product from the Store dashboard")
    public DmpOrderConfirmationPage orderFromSuggest(String verificationCode) {
        new DmpFlow().openMarketPlace();
        DmpOrderProductPage product = new DmpOrderStorePage().selectSuggestedProduct(0);
        return checkoutAndPay(product, verificationCode);
    }

    @Step("Order a specific product (SKU {sku}) selected via deep link")
    public DmpOrderConfirmationPage orderSelectedProduct(String sku, String verificationCode) {
        new DmpFlow().openMarketPlace();
        DmpOrderProductPage product = new DmpOrderStorePage().openProductByDeepLink(sku);
        return checkoutAndPay(product, verificationCode);
    }

    /** Shared tail: add to cart → Go to Checkout → place order → payment OTP → Order Placed. */
    private DmpOrderConfirmationPage checkoutAndPay(DmpOrderProductPage product, String verificationCode) {
        product.addToCart();
        DmpOrderCartPage cart = product.tapViewCart();
        DmpOrderCheckoutPage checkout = cart.goToCheckout();
        checkout.placeOrder();
        new OtpPage().enterOtp(verificationCode);
        return new DmpOrderConfirmationPage();
    }
}
