package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Percentage discount on item level (no max) with maximum quantity (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code percentage Amount Discount on item level without
 * maximum amount and with maximum quantity} (setup {@code setup discount percentage Amount Discount
 * on item level without maximum amount and with maximum quantity}, code {@code 10%OffItemLevel}):
 * apply the per-item percentage discount at checkout and verify the success message.
 */
@Epic("DMP")
@Feature("Promo Codes - Percentage Item Level with Max Qty")
public class PercentageItemLevelMaxQtyTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.percentItemMaxQty";
    }
}
