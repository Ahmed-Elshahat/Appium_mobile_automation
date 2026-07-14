package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Percent of selected products (Family) without maximum amount (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Percent of selected products_Family without
 * maximum amount} (setup {@code setup Percent of selected products_Family without maximum amount},
 * code {@code OffCartLevel}): apply the family-scoped percentage discount at checkout and verify the
 * success message.
 *
 * <p>Data note: this code applies only to Zain-family products, so it needs a matching product in
 * the cart; with an arbitrary first product the backend returns the "incorrect" message.
 */
@Epic("DMP")
@Feature("Promo Codes - Percent of Selected Products (Family)")
public class PercentOfFamilyTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.percentFamily";
    }
}
