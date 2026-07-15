package com.urpay.tests.dmp.promo;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpPromoFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.promo.DmpCheckoutPromoPage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * Shared base for the DMP "valid promo code" suites (LANE E).
 *
 * Migrated from the Katalon (DMP_1 branch) Promo Code happy-path suites (Discount Per Item,
 * Fixed Amount, Percent of selected products, Percentage Cart/Item level, etc.). Those suites all
 * follow the same path — login → NavigateToDMP → reach checkout with a product → apply a VALID
 * promo code — and, in the Katalon source, the total-amount math assertions are all commented out
 * ({@code //verifyEqual}); the only live assertions are the success message
 * ({@code Validate PromoCode Success Message}) and the presence of the Remove-promo button.
 *
 * Each concrete suite binds its config key prefix (login user + promo code + expected message) via
 * {@link #keyPrefix()}. Optionally a {@code <prefix>.sku} opens a specific product by deep link;
 * otherwise the first Store product is used (the deep-link product route crashes some SIT screens).
 */
public abstract class AbstractDmpValidPromoTest extends BaseTest {

    protected final ConfigManager config = ConfigManager.getInstance();

    /** Config key prefix for this suite, e.g. {@code "dmpe.discountPerItem"}. */
    protected abstract String keyPrefix();

    @Test(groups = {"dmp", "dmp-promo", "regression"})
    @Description("Applying a valid promo code shows the success message and the Remove button")
    @Severity(SeverityLevel.CRITICAL)
    public void testApplyValidPromoShowsSuccess() {
        String p = keyPrefix();

        new LoginFlow().loginWith(
                config.get(p + ".mobileNumber"),
                config.get(p + ".id"),
                config.get(p + ".verificationCode", "1234"),
                config.get(p + ".passCode", "2233"));

        // Minimum-purchase promos (e.g. 10SAROFF200Min) require a cart >= N SAR; select a specific
        // >= 200 SAR product by name when <prefix>.productName is set (a digital card such as the
        // 1875 SAR "iTunes 500"), otherwise use the default first-product / SKU path.
        String productName = config.get(p + ".productName", "");
        DmpCheckoutPromoPage checkout = productName.isEmpty()
                ? openCheckout(p)
                : new DmpPromoFlow().openCheckoutWithProductByName(productName);
        Assert.assertTrue(checkout.isLoaded(),
                "The DMP checkout (promo) screen should be displayed after Buy now");

        checkout.setAndApplyPromoCode(config.get(p + ".code"));

        // Wait for the promo result to render (the success message mounts a few seconds after Apply)
        // then validate it — a single immediate read can catch the screen before the text appears.
        String success = checkout.waitForSuccessMessage(40);
        String observed = success.isEmpty() ? checkout.getFailureMessage() : success;
        Assert.assertEquals(success, config.get(p + ".successMessage"),
                "A valid promo code should show the success message; the app showed: '" + observed + "'");
        Assert.assertTrue(checkout.isRemoveButtonDisplayed(),
                "The Remove-promo button should appear after a successful promo application");
    }

    /** Open the checkout screen either by the configured SKU (deep link) or via the first Store product. */
    private DmpCheckoutPromoPage openCheckout(String prefix) {
        String sku = config.get(prefix + ".sku", "");
        DmpPromoFlow flow = new DmpPromoFlow();
        return sku.isEmpty()
                ? flow.openCheckoutWithFirstProduct()
                : flow.openCheckoutWithProductByDeepLink(sku);
    }
}
