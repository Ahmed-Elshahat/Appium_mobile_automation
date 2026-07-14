package com.urpay.flows;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.dmp.DmpCartPage;
import com.urpay.pages.dmp.DmpMarketPlacePage;
import com.urpay.pages.dmp.DmpProductDetailsPage;
import com.urpay.pages.dmp.promo.DmpCheckoutPromoPage;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * DMP Promo-Code flow (LANE E).
 *
 * Migrated from the Katalon (DMP_1 branch) Promo Code suites, which navigate:
 *   login → NavigateToDMP → open a product → reach the checkout ("Order details") screen →
 *   apply/validate a promo code in the promo-code field.
 *
 * The revamped app reaches the checkout screen (which carries the promo-code field + "Place Order"
 * CTA) by tapping "Buy now" on a product, so this flow reuses the frozen
 * {@link DmpFlow#openMarketPlace()}, {@link DmpMarketPlacePage#openProduct(int)},
 * {@link DmpProductDetailsPage#buyNow()} and {@link DmpCartPage#goToCheckoutIfPresent()}
 * read-only, and returns this lane's {@link DmpCheckoutPromoPage} for the test to act/assert on.
 *
 * SOLID / project rules: ZERO Thread.sleep(); NO assertions; returns the landing page.
 */
public class DmpPromoFlow {

    private static final Logger log = LoggerFactory.getLogger(DmpPromoFlow.class);

    // Katalon SmartNavigator.navigateToProductDetailsThroughDeepLink.
    private static final String PRODUCT_DETAILS_DEEP_LINK = "urpay://MarketPlace/ProductDetails?sku=";

    private final AppiumDriver driver;

    public DmpPromoFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
    }

    /**
     * Open the first Store product and proceed to the checkout screen where promo codes are applied.
     * Mirrors the Katalon promo suites' path to checkout, using the catalogue product (the deep-link
     * product-details route crashes some SIT product screens — see ShareProductsTest).
     *
     * @return the checkout promo page for the test to apply/validate a promo code.
     */
    @Step("Open the first Store product and go to checkout for promo entry")
    public DmpCheckoutPromoPage openCheckoutWithFirstProduct() {
        DmpMarketPlacePage marketplace = new DmpFlow().openMarketPlace();
        DmpProductDetailsPage details = marketplace.openProduct(0);
        details.isLoaded();
        return proceedToCheckout(details);
    }

    /**
     * Open a specific product by SKU (Katalon deep link) and proceed to the checkout screen.
     *
     * @param sku the product SKU (e.g. config {@code dmpe.<suite>.sku}).
     * @return the checkout promo page for the test to apply/validate a promo code.
     */
    @Step("Open product (sku {sku}) by deep link and go to checkout for promo entry")
    public DmpCheckoutPromoPage openCheckoutWithProductByDeepLink(String sku) {
        new DmpFlow().openMarketPlace();
        openProductDetailsByDeepLink(sku);
        DmpProductDetailsPage details = new DmpProductDetailsPage();
        details.isLoaded();
        return proceedToCheckout(details);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private DmpCheckoutPromoPage proceedToCheckout(DmpProductDetailsPage details) {
        details.buyNow();
        new DmpCartPage().goToCheckoutIfPresent();
        DmpCheckoutPromoPage checkout = new DmpCheckoutPromoPage();
        checkout.isLoaded();
        return checkout;
    }

    /** Fire the product-details deep link via the UiAutomator2 mobile:deepLink command. */
    private void openProductDetailsByDeepLink(String sku) {
        String appPackage = ConfigManager.getInstance().get("appPackage", "com.urpay.consumer.sit");
        Map<String, Object> params = new HashMap<>();
        params.put("url", PRODUCT_DETAILS_DEEP_LINK + sku);
        params.put("package", appPackage);
        ((JavascriptExecutor) driver).executeScript("mobile: deepLink", params);
        log.info("Opened product details via deep link for sku {}", sku);
    }
}
