package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Percentage for minimum purchases in the cart without maximum amount (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Percentage for minimum purchases in the cart
 * without maximum amount} (setup {@code setup Percentage for minimum purchases in the cart without
 * maximum amount}, code {@code 10%OFF200Min}): apply the minimum-purchase percentage discount at
 * checkout and verify the success message.
 *
 * <p>Data note: this code requires a cart total ≥ 200 SAR; below that the backend returns
 * "Discount available for orders over 200", so the happy path needs a ≥ 200 SAR cart.
 */
@Epic("DMP")
@Feature("Promo Codes - Percentage Minimum Purchase")
public class PercentMinPurchaseTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.percentMinPurchase";
    }
}
