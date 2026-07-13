package com.urpay.flows;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.dmp.DmpMarketPlacePage;
import com.urpay.pages.dmp.DmpProductDetailsPage;
import com.urpay.pages.dmp.share.DmpShareProductPage;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * DMP Share Products flow (LANE B).
 *
 * Migrated from Katalon (DMP_1 branch) suite ValidatShareingProductsWithOthers:
 *   NavigateToDMP → move to a specific product's details (deep link
 *   {@code urpay://MarketPlace/ProductDetails?sku=<sku>}) → tap Share → verify the share sheet.
 *
 * Reuses the frozen {@link DmpFlow#openMarketPlace()} and
 * {@link DmpMarketPlacePage#openProduct(int)} read-only.
 */
public class DmpShareFlow {

    private static final Logger log = LoggerFactory.getLogger(DmpShareFlow.class);

    // Katalon SmartNavigator.navigateToProductDetailsThroughDeepLink.
    private static final String PRODUCT_DETAILS_DEEP_LINK = "urpay://MarketPlace/ProductDetails?sku=";

    private final AppiumDriver driver;

    public DmpShareFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
    }

    /**
     * Open a product's details via deep link (by SKU) and tap Share.
     *
     * @param sku the product SKU (config {@code dmpb.share.sku}).
     * @return the share page for the test to assert the share sheet is shown.
     */
    @Step("Open a DMP product's details by deep link (sku {sku}) and share it")
    public DmpShareProductPage shareProductByDeepLink(String sku) {
        new DmpFlow().openMarketPlace();
        openProductDetailsByDeepLink(sku);
        DmpShareProductPage share = new DmpShareProductPage();
        share.tapShare();
        return share;
    }

    /**
     * Open the first Store product from the catalogue and tap Share (deep-link-free fallback used
     * when a fixed SKU is not available on the account).
     *
     * @return the share page for the test to assert the share sheet is shown.
     */
    @Step("Open the first Store product and share it")
    public DmpShareProductPage shareFirstStoreProduct() {
        DmpMarketPlacePage marketplace = new DmpFlow().openMarketPlace();
        DmpProductDetailsPage details = marketplace.openProduct(0);
        details.isLoaded();
        DmpShareProductPage share = new DmpShareProductPage();
        share.tapShare();
        return share;
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
