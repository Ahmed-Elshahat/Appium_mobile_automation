package com.urpay.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;
import com.urpay.pages.dmp.orders.DmpMokafaaCheckoutPage;
import com.urpay.pages.dmp.orders.DmpOrderCartPage;
import com.urpay.pages.dmp.orders.DmpOrderCheckoutPage;
import com.urpay.pages.dmp.orders.DmpOrderProductPage;
import com.urpay.pages.dmp.orders.DmpOrderStorePage;

import io.qameta.allure.Step;

/**
 * DMP Mokafaa-points order flow (LANE D).
 *
 * Migrated from Katalon (DMP_1, New Test Suites After Revamp):
 *   FullCycleDigitalMarketPlaceOrder/Validate Placing Order Using Mokafaa Points
 *
 * Journey (two payment transactions):
 *   Store → open the Mokafaa product via deep link → add to cart → Go to Checkout →
 *   Redeem Mokafaa points (enter amount → Next → Confirm → verification code → Done) →
 *   re-open Checkout via deep link → Place Order → payment verification code → Order Placed.
 *
 * Reuses the FROZEN foundation ({@code DmpFlow.openMarketPlace}, {@code OtpPage},
 * {@code DmpOrderConfirmationPage}) and the LANE C {@code pages/dmp/orders/*} order pages
 * (the coordinate-tap Place Order fix). Only the Mokafaa redemption is new.
 *
 * SOLID / project rules: ZERO Thread.sleep(); NO assertions (returns a page object); NO hardcoded
 * data (SKU / points amount / verification code passed in from config by the test).
 */
public class DmpMokafaaFlow {

    private static final Logger log = LoggerFactory.getLogger(DmpMokafaaFlow.class);
    private static final int ADD_TO_CART_ATTEMPTS = 3;

    /**
     * Place an order paid with redeemed Mokafaa points.
     *
     * @param sku              the Mokafaa product SKU / id (config {@code dmpd.mokafaa.productSku}).
     * @param pointsAmount     the Mokafaa points quantity to redeem (config {@code dmpd.mokafaa.pointsToRedeem}).
     * @param verificationCode the payment verification code (config {@code dmpd.mokafaa.verificationCode}).
     * @return the Order Confirmation page for the test to assert on.
     */
    @Step("Place an order using redeemed Mokafaa points (SKU {sku})")
    public DmpOrderConfirmationPage placeOrderUsingMokafaaPoints(
            String sku, String pointsAmount, String verificationCode) {
        new DmpFlow().openMarketPlace();

        // Self-heal the intermittent Add-to-Cart no-op: re-open the product (deep link) and re-add
        // until the cart holds an item (checkout CTA present).
        DmpOrderCheckoutPage checkout = addToCartAndReachCheckout(sku);

        new DmpMokafaaCheckoutPage().redeemPoints(pointsAmount, verificationCode);

        checkout.openByDeepLink().placeOrder();
        new OtpPage().enterOtp(verificationCode);
        return new DmpOrderConfirmationPage();
    }

    /**
     * Open the Mokafaa product, add it to the cart and reach the Checkout page — self-healing the
     * intermittent Add-to-Cart no-op by re-opening (deep link) and re-adding up to
     * {@link #ADD_TO_CART_ATTEMPTS} times if the cart is empty.
     */
    private DmpOrderCheckoutPage addToCartAndReachCheckout(String sku) {
        DmpOrderCartPage cart = null;
        for (int attempt = 1; attempt <= ADD_TO_CART_ATTEMPTS; attempt++) {
            DmpOrderProductPage product = new DmpOrderStorePage().openProductByDeepLink(sku);
            product.addToCart();
            cart = product.tapViewCart();
            if (cart.hasCheckoutButton(8) && !cart.isEmpty(1)) {
                return cart.goToCheckout();
            }
            log.warn("Self-heal: cart empty / no checkout CTA after add-to-cart "
                    + "(attempt {}/{}) — re-opening product and re-adding", attempt, ADD_TO_CART_ATTEMPTS);
        }
        return cart.goToCheckout();
    }
}
