package com.urpay.tests.dmp.promo;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpPromoFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.promo.DmpCheckoutPromoPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Promo Code — Maximum Discount Limit (LANE E).
 *
 * Migrated from the Katalon (DMP_1 branch) suite {@code Maximum Discount Limit Promo Code(Pending)}
 * (setup {@code DMP/Promocodes/Setup Test Data for promo Code suite}).
 *
 * <p>The suite was marked {@code (Pending)} in Katalon itself: it was never wired up with an actual
 * maximum-discount-cap assertion. The only test cases it marks {@code isRun=true} are the two
 * error-message checks — <b>Already-Used</b> and <b>Case-Sensitivity mismatch</b> — reached with one
 * product at checkout. This migration faithfully reproduces those two live checks (it does not invent
 * a discount-cap assertion the source never implemented).
 *
 * <p>The two cases are chained (priority + dependsOnMethods) so they share one session and the single
 * checkout screen reached by {@link #testLoginAndReachCheckout()}.
 */
@Epic("DMP")
@Feature("Promo Codes - Maximum Discount Limit")
public class MaximumDiscountLimitTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();
    private DmpCheckoutPromoPage checkout;

    @Test(priority = 0, groups = {"dmp", "dmp-promo", "regression"})
    @Description("Login and reach the DMP checkout screen with a product for promo-code entry")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginAndReachCheckout() {
        new LoginFlow().loginWith(
                config.get("dmpe.maxDiscount.mobileNumber"),
                config.get("dmpe.maxDiscount.id"),
                config.get("dmpe.maxDiscount.verificationCode", "1234"),
                config.get("dmpe.maxDiscount.passCode", "2233"));

        checkout = new DmpPromoFlow().openCheckoutWithFirstProduct();

        Assert.assertTrue(checkout.isLoaded(),
                "The DMP checkout (promo) screen should be displayed after Buy now");
    }

    @Test(priority = 1, dependsOnMethods = "testLoginAndReachCheckout",
            groups = {"dmp", "dmp-promo", "regression"})
    @Description("Applying an already-used promo code shows the incorrect-code error message")
    @Severity(SeverityLevel.NORMAL)
    public void testAlreadyUsedPromoCodeShowsError() {
        assertErrorForCode(
                config.get("dmpe.maxDiscount.alreadyUsedCode"),
                config.get("dmpe.maxDiscount.alreadyUsedMessage"));
    }

    @Test(priority = 2, dependsOnMethods = "testLoginAndReachCheckout",
            groups = {"dmp", "dmp-promo", "regression"})
    @Description("Applying a wrong-case promo code shows the incorrect-code error message")
    @Severity(SeverityLevel.NORMAL)
    public void testCaseSensitivePromoCodeMismatchShowsError() {
        assertErrorForCode(
                config.get("dmpe.maxDiscount.caseSensitiveCode"),
                config.get("dmpe.maxDiscount.caseSensitiveMessage"));
    }

    /** Apply a code, assert the app's message equals the expected error, then clear it if removable. */
    private void assertErrorForCode(String code, String expectedMessage) {
        checkout.setAndApplyPromoCode(code);
        String actual = checkout.getResultMessage();
        if (checkout.isRemoveButtonDisplayed()) {
            checkout.removePromoCode();
        }
        Assert.assertEquals(actual, expectedMessage,
                "Unexpected promo-code error message for code '" + code + "'");
    }
}
