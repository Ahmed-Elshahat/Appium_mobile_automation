package com.urpay.tests.dmp.promo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * DMP Promo Code — Multi-Item Orders (LANE E).
 *
 * Migrated from the Katalon (DMP_1 branch) suite {@code Multi-Item Orders Promo Code(Pending)}
 * (setup {@code PromoCodeTestCases/Setup Test Cases/Setup Multi Item Order}). The Katalon suite adds
 * two products ("Virgin" + "Salam") to the cart, reaches checkout and applies the generic per-item
 * discount code {@code 1SAROffPerItem}; the only live assertion is the success message — the
 * total-amount math checks are commented out, exactly as in every other LANE E valid-promo suite.
 *
 * <p>The suite was marked {@code (Pending)} in Katalon itself. The revamped app reaches checkout via
 * "Buy now" (there is no two-item add-to-cart on the frozen product/checkout pages), so this
 * migration exercises the proven single-product Buy-now checkout path. Because
 * {@code 1SAROffPerItem} is a generic per-item discount, it still applies and the success message is
 * the observable outcome verified by {@link AbstractDmpValidPromoTest#testApplyValidPromoShowsSuccess()}.
 */
@Epic("DMP")
@Feature("Promo Codes - Multi-Item Orders")
public class MultiItemOrdersTest extends AbstractDmpValidPromoTest {

    @Override
    protected String keyPrefix() {
        return "dmpe.multiItem";
    }
}
