package com.urpay.flows;

import com.urpay.pages.dmp.DmpOrderConfirmationPage;
import com.urpay.pages.dmp.gift.DmpGiftPage;
import com.urpay.pages.dmp.orders.DmpOrderStorePage;

import io.qameta.allure.Step;

/**
 * DMP gift-order flow (LANE D).
 *
 * Migrated from Katalon (DMP_1 branch, New Test Suites After Revamp):
 *   FullCycleDigitalMarketPlaceOrder/Validate placing order as a gift and check it sent to gift receiver
 *
 * Journey (matching the suite's enabled test cases in order):
 *   Store → open the product via deep link → add to cart → open cart → "Send as gift" →
 *   Add New Number (receiver mobile) → select gift card → enter gift message → Next →
 *   pay/confirm screen.
 *
 * Reuses the FROZEN foundation ({@code DmpFlow.openMarketPlace}) and the LANE C
 * {@code pages/dmp/orders/*} product/cart pages read-only (the proven coordinate-tap
 * add-to-cart). Only the E-Gift screens are new (LANE D {@code pages/dmp/gift}).
 *
 * SOLID / project rules: ZERO Thread.sleep(); NO assertions (returns a page object); NO hardcoded
 * data (SKU / receiver number / message / verification code passed in from config by the test).
 */
public class DmpGiftFlow {

    /**
     * Prepare a gift order up to the pay/confirm screen — mirrors the Katalon suite's enabled
     * steps (the suite's PayConfirmBtnForGift step is disabled, so the gift order is prepared for
     * the receiver but payment is completed separately via {@link #payForGift}).
     *
     * @param sku            the product SKU / id to gift (config {@code dmpd.gift.productSku}).
     * @param receiverNumber the gift-receiver mobile number (config {@code dmpd.gift.receiverNumber}).
     * @param message        the gift message (config {@code dmpd.gift.message}).
     * @return the gift page positioned on the pay/confirm screen for the test to assert on.
     */
    @Step("Prepare a gift order for receiver '{receiverNumber}' (SKU {sku})")
    public DmpGiftPage placeOrderAsGift(String sku, String receiverNumber, String message) {
        DmpFlow dmp = new DmpFlow();
        dmp.openMarketPlace();

        DmpGiftPage gift = new DmpGiftPage();
        new DmpOrderStorePage().openProductByDeepLink(sku);
        gift.addToCart();
        gift.openCart();

        gift.tapSendAsGift();
        gift.addReceiverNumber(receiverNumber);
        gift.selectGiftCard();
        gift.enterGiftMessageAndNext(message);
        return gift;
    }

    /**
     * Complete payment for the prepared gift order — migrated from Katalon
     * Order/PayConfirmBtnForGift: Confirm → payment verification code → "Order Placed!".
     *
     * @param gift             the gift page positioned on the pay/confirm screen.
     * @param verificationCode the payment verification code (config {@code dmpd.gift.verificationCode}).
     * @return the Order Confirmation ("Order Placed!") page for the test to assert on.
     */
    @Step("Pay for and confirm the gift order")
    public DmpOrderConfirmationPage payForGift(DmpGiftPage gift, String verificationCode) {
        return gift.payAndConfirm(verificationCode);
    }
}
