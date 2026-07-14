package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Percentage discount on Cart Level with Maximum Amount (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Percentage Amount Discount on Cart Level with
 * Maximum Amount} (setup {@code setup discount percentage Amount on Cart level with maximum amount
 * test suite (new)}, code {@code 10%OffCartLevelMax3SAR}): apply the capped cart-level percentage
 * discount at checkout and verify the success message.
 */
@Epic("DMP")
@Feature("Promo Codes - Percentage Cart Level with Max")
public class PercentageCartLevelMaxTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.percentCartMax";
    }
}
