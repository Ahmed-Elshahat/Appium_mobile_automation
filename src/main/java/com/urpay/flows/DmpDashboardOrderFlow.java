package com.urpay.flows;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * SELF-HEALING: the revamped RN Add-to-Cart button occasionally fails to register (leaving the
 * cart empty), so {@link #addToCartAndReachCheckout} re-opens the product and re-adds it until the
 * cart holds an item (a checkout CTA appears), up to a few attempts.
 *
 * SOLID / project rules: ZERO Thread.sleep(); NO assertions (returns a page object); NO
 * hardcoded data (SKU / verification code are passed in from config by the test).
 */
public class DmpDashboardOrderFlow {

    private static final Logger log = LoggerFactory.getLogger(DmpDashboardOrderFlow.class);
    private static final int ADD_TO_CART_ATTEMPTS = 3;

    @Step("Order the first New Arrivals product from the Store dashboard")
    public DmpOrderConfirmationPage orderFromNewArrival(String verificationCode) {
        return checkoutAndPay(() -> {
            new DmpFlow().openMarketPlace();
            return new DmpOrderStorePage().selectNewArrivalsProduct(0);
        }, verificationCode);
    }

    @Step("Order the first Suggested product from the Store dashboard")
    public DmpOrderConfirmationPage orderFromSuggest(String verificationCode) {
        return checkoutAndPay(() -> {
            new DmpFlow().openMarketPlace();
            return new DmpOrderStorePage().selectSuggestedProduct(0);
        }, verificationCode);
    }

    @Step("Order a specific product (SKU {sku}) selected via deep link")
    public DmpOrderConfirmationPage orderSelectedProduct(String sku, String verificationCode) {
        new DmpFlow().openMarketPlace();
        // The product deep link re-opens the details from any screen, so it doubles as the
        // self-heal re-navigation.
        return checkoutAndPay(() -> new DmpOrderStorePage().openProductByDeepLink(sku), verificationCode);
    }

    /** Shared tail: add to cart (self-healed) → Go to Checkout → place order → payment OTP → Order Placed. */
    private DmpOrderConfirmationPage checkoutAndPay(
            Supplier<DmpOrderProductPage> openProduct, String verificationCode) {
        DmpOrderCheckoutPage checkout = addToCartAndReachCheckout(openProduct);
        checkout.placeOrder();
        // Payment "Verification Code" field == the login verification field
        // (Katalon fillVerificationCode). Reuse the PROVEN OtpPage (click-to-focus then digits).
        new OtpPage().enterOtp(verificationCode);
        return new DmpOrderConfirmationPage();
    }

    /**
     * Open the product, add it to the cart and reach the Checkout page — self-healing the
     * intermittent Add-to-Cart no-op: if the cart is empty (no checkout CTA), re-open the product
     * and re-add, up to {@link #ADD_TO_CART_ATTEMPTS} times.
     */
    private DmpOrderCheckoutPage addToCartAndReachCheckout(Supplier<DmpOrderProductPage> openProduct) {
        DmpOrderCartPage cart = null;
        for (int attempt = 1; attempt <= ADD_TO_CART_ATTEMPTS; attempt++) {
            DmpOrderProductPage product = openProduct.get();
            product.addToCart();
            cart = product.tapViewCart();
            if (cart.hasCheckoutButton(8) && !cart.isEmpty(1)) {
                return cart.goToCheckout();
            }
            log.warn("Self-heal: cart empty / no checkout CTA after add-to-cart "
                    + "(attempt {}/{}) — re-opening product and re-adding", attempt, ADD_TO_CART_ATTEMPTS);
        }
        // Retries exhausted — proceed so the failure surfaces with a clear checkout locator error.
        return cart.goToCheckout();
    }
}
