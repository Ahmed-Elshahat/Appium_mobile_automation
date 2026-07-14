package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Fixed Amount discount on item level without maximum amount (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Fixed Amount Discount on item level without
 * maximum amount} (setup {@code setup percentage Amount Discount on on Cart level without maximum
 * amount}, code {@code 2SAROffCartLevel}): apply the fixed item-level discount at checkout and
 * verify the success message.
 */
@Epic("DMP")
@Feature("Promo Codes - Fixed Amount Item Level")
public class FixedAmountItemLevelTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.fixedItemLevel";
    }
}
