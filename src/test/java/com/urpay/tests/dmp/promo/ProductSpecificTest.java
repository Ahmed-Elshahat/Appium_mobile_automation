package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Product-Specific (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Product-Specific Promo Code} (setup
 * {@code setup Product-Specific Promo Code}, code {@code 4FixedSRZainFamily}): apply the
 * product-specific discount at checkout and verify the success message.
 *
 * <p>Data note: this code applies only to a Zain-family product, so it needs the specific product
 * in the cart (config {@code dmpe.productSpecific.sku} may be set to force a matching product);
 * with an arbitrary first product the backend returns the "isn't valid" message.
 */
@Epic("DMP")
@Feature("Promo Codes - Product Specific")
public class ProductSpecificTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.productSpecific";
    }
}
