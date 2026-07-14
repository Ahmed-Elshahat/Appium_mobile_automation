package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Fixed Amount discount for minimum purchases in the cart (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Fixed Amount Discount for minimum purchases in
 * the cart} (setup {@code setup Fixed Discount Amount for minimum purchases in the cart(new2)},
 * code {@code 10SAROFF200Min}): apply the minimum-purchase fixed discount at checkout and verify the
 * success message.
 *
 * <p>Data note: this code requires a cart total ≥ 200 SAR; below that the backend returns
 * "Discount available for orders over 200", so the happy path needs a ≥ 200 SAR cart.
 */
@Epic("DMP")
@Feature("Promo Codes - Fixed Amount Minimum Purchase")
public class FixedMinPurchaseTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.fixedMinPurchase";
    }
}
