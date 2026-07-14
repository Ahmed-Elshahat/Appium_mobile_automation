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
 * DMP Promo Codes — error-message cases (LANE E).
 *
 * Migrated from Katalon (DMP_1 branch) suite {@code Promo Codes} plus the
 * {@code PromoCodeTestCases/{Invalid Code,Expired Code,Already Used Code,Case Sensitivity}}
 * test cases: login → NavigateToDMP → reach checkout with a product → apply an
 * invalid / expired / already-used / wrong-case promo code → verify the app's error message.
 *
 * The four cases are chained (priority + dependsOnMethods) so they share one session and the
 * single checkout screen reached by {@link #testLoginAndReachCheckout()}.
 */
@Epic("DMP")
@Feature("Promo Codes - Error Messages")
public class PromoCodeErrorMessagesTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();
    private DmpCheckoutPromoPage checkout;

    @Test(priority = 0, groups = {"dmp", "dmp-promo", "regression"})
    @Description("Login and reach the DMP checkout screen with a product for promo-code entry")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginAndReachCheckout() {
        new LoginFlow().loginWith(
                config.get("dmpe.errors.mobileNumber"),
                config.get("dmpe.errors.id"),
                config.get("dmpe.errors.verificationCode", "1234"),
                config.get("dmpe.errors.passCode", "2233"));

        checkout = new DmpPromoFlow().openCheckoutWithFirstProduct();

        Assert.assertTrue(checkout.isLoaded(),
                "The DMP checkout (promo) screen should be displayed after Buy now");
    }

    @Test(priority = 1, dependsOnMethods = "testLoginAndReachCheckout",
            groups = {"dmp", "dmp-promo", "regression"})
    @Description("Applying an invalid promo code shows the invalid-code error message")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidPromoCodeShowsError() {
        assertErrorForCode(
                config.get("dmpe.errors.invalidCode"),
                config.get("dmpe.errors.invalidMessage"));
    }

    @Test(priority = 2, dependsOnMethods = "testLoginAndReachCheckout",
            groups = {"dmp", "dmp-promo", "regression"})
    @Description("Applying an expired promo code shows the expired-code error message")
    @Severity(SeverityLevel.NORMAL)
    public void testExpiredPromoCodeShowsError() {
        assertErrorForCode(
                config.get("dmpe.errors.expiredCode"),
                config.get("dmpe.errors.expiredMessage"));
    }

    @Test(priority = 3, dependsOnMethods = "testLoginAndReachCheckout",
            groups = {"dmp", "dmp-promo", "regression"})
    @Description("Applying an already-used promo code shows the incorrect-code error message")
    @Severity(SeverityLevel.NORMAL)
    public void testAlreadyUsedPromoCodeShowsError() {
        assertErrorForCode(
                config.get("dmpe.errors.alreadyUsedCode"),
                config.get("dmpe.errors.alreadyUsedMessage"));
    }

    @Test(priority = 4, dependsOnMethods = "testLoginAndReachCheckout",
            groups = {"dmp", "dmp-promo", "regression"})
    @Description("Applying a wrong-case promo code shows the incorrect-code error message")
    @Severity(SeverityLevel.NORMAL)
    public void testCaseSensitivePromoCodeMismatchShowsError() {
        assertErrorForCode(
                config.get("dmpe.errors.caseSensitiveCode"),
                config.get("dmpe.errors.caseSensitiveMessage"));
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
