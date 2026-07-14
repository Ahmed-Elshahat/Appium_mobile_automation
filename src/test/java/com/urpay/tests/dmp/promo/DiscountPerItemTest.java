package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Discount Per Item (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Discount Per Item} (setup
 * {@code PromoCodeTestCases/Setup Test Cases/setup Discount-Specific Promo Code}):
 * apply the per-item discount code {@code 1SAROffPerItem} at checkout and verify the success message.
 */
@Epic("DMP")
@Feature("Promo Codes - Discount Per Item")
public class DiscountPerItemTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.discountPerItem";
    }
}
