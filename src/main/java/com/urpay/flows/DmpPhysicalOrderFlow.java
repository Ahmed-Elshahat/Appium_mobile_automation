package com.urpay.flows;

import com.urpay.core.ConfigManager;
import com.urpay.pages.dmp.physicalorder.DmpDeliveryLocationPage;
import com.urpay.pages.dmp.physicalorder.DmpPhysicalOrderPage;

import io.qameta.allure.Step;

/**
 * DMP physical-product order flow (LANE D).
 *
 * Migrated from Katalon (DMP_1 branch) PhysicalMarketPlace/Validate Placing Order With Physical
 * Product: Store → search a physical product → add to cart → View Cart → Go to Checkout →
 * enter / edit / add delivery locations. This suite validates the delivery-location flow and does
 * NOT complete payment.
 *
 * Reuses the FROZEN {@link DmpFlow#openMarketPlace()} read-only; all other screens are LANE D pages.
 */
public class DmpPhysicalOrderFlow {

    /**
     * Navigate from the Store to the delivery-location screen: search the product, open the first
     * result, add it to the cart, open the cart and go to checkout.
     *
     * @param product physical product name to search for (config {@code dmpd.physical.product}).
     * @return the delivery-location page for the test to assert on / enter locations.
     */
    @Step("Reach the delivery-location screen for physical product '{product}'")
    public DmpDeliveryLocationPage reachDeliveryLocation(String product) {
        DmpPhysicalOrderPage order = new DmpPhysicalOrderPage();
        // Prefer a configured SKU: deep link straight to a known IN-STOCK product (e.g. the iPhone 16
        // Pro Max MMED3AB/A) — the Smart Phones brand listings are largely out of stock in SIT.
        String sku = ConfigManager.getInstance().get("dmpd.physical.sku", "");
        if (!sku.isEmpty()) {
            // The product deep link occasionally drops to the launcher (RN cold-route race);
            // openProductByDeepLink re-activates the app each try, so just re-fire it a few times.
            boolean opened = order.openProductByDeepLink(sku);
            for (int attempt = 2; attempt <= 4 && !opened; attempt++) {
                opened = order.openProductByDeepLink(sku);
            }
            if (!opened) {
                throw new IllegalStateException(
                        "Physical product SKU '" + sku + "' did not open an in-stock, addable product "
                        + "(out of stock, or the deep link did not resolve the SKU after 4 attempts).");
            }
            // Revamped physical details expose 'Buy now' (variant pre-selected via the SKU) rather than
            // 'Add to cart'; route through whichever CTA is present.
            if (order.hasAddToCart(3)) {
                order.addToCart();
                order.openCart();
                return order.goToCheckout();
            }
            return order.buyNowToDelivery();
        }
        // Fallback: enter the Smart Phones Devices listing, apply the brand filter, order the first
        // IN-STOCK device ({@code product} is used as the filter chip label).
        new DmpFlow().openMarketPlace();
        order.openSmartPhonesListing();
        order.selectFilter(product);
        if (!order.openFirstInStockProduct(8)) {
            throw new IllegalStateException(
                    "No in-stock '" + product + "' product found in the Devices listing (matches were out of "
                    + "stock or absent) — cannot add to cart. Catalog/data condition, not a UI defect.");
        }
        order.addToCart();
        order.openCart();
        return order.goToCheckout();
    }

    /**
     * Enter a delivery location, save it, edit it, then add another location — mirrors Katalon
     * {@code AddLocationName}.
     */
    @Step("Enter, edit and add delivery locations")
    public void enterDeliveryLocations(DmpDeliveryLocationPage location,
            String first, String firstEdited, String second) {
        location.enterLocationName(first).tapNext();
        location.tapEdit().enterLocationName(firstEdited).tapNext();
        location.tapAddAnotherLocation().enterLocationName(second).tapNext();
    }
}
