package com.urpay.pages.dmp.orders;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * DMP Store dashboard — product selection for the LANE C digital-order journeys.
 *
 * Migrated from Katalon (DMP_1):
 *   Test Cases/DMP/Dashboard/MoveToNewArrivalDevices + SelectProductFromDashBoard
 *   Test Cases/DMP/Suggestion/MoveToSuggestionProduct + SelectProductFromSuggestionProduct
 *   Test Cases/DMP/Cart/To Validate Move To Specific Product Details By Deep Link
 *
 * This is a NEW LANE C page that reads the same Store screen as the frozen
 * {@code DmpMarketPlacePage} but adds the dashboard product-tag tab selection
 * (New Arrivals / Suggested), the dashboard product-card testID
 * ({@code testID-View.6f7c2762-…}) and the specific-product deep link — actions the
 * foundation page does not expose.
 *
 * Locators anchor on visible tab text first (robust) with the stable
 * {@code testID-tags-menu-*} index as a fallback (Katalon ArrivalBTN_FirstMenu /
 * suggestionBTN), since most DMP testIDs are UUID-based.
 */
public class DmpOrderStorePage extends BasePage {

    // Product-tag tabs on the Store dashboard.
    private static final By NEW_ARRIVALS_TAB =
            AppiumBy.xpath("//*[@content-desc='testID-tags-menu-1' or @text='New Arrivals']");
    private static final By SUGGESTED_TAB =
            AppiumBy.xpath("//*[@content-desc='testID-tags-menu-2' or @text='Suggested']");

    // Dashboard product card (Katalon SelectProductFromDashBoard). The New Arrivals carousel is
    // the 1st instance of this content-desc; the Suggested carousel is the 2nd.
    private static final String PRODUCT_CARD =
            "//*[@content-desc='testID-View.6f7c2762-b234-4b50-bf47-4366a5d88abd.%d']";

    // Selected-product deep link (Katalon SmartNavigator.navigateToProductDetailsThroughDeepLink).
    private static final String PRODUCT_DEEP_LINK = "urpay://MarketPlace/ProductDetails?sku=";

    @Step("Select product {index} from the New Arrivals tab")
    public DmpOrderProductPage selectNewArrivalsProduct(int index) {
        ensureVisible("New Arrivals", NEW_ARRIVALS_TAB);
        tap(NEW_ARRIVALS_TAB, 20);
        tap(AppiumBy.xpath("(" + String.format(PRODUCT_CARD, index) + ")[1]"), 20);
        return new DmpOrderProductPage();
    }

    @Step("Select product {index} from the Suggested tab")
    public DmpOrderProductPage selectSuggestedProduct(int index) {
        ensureVisible("Suggested", SUGGESTED_TAB);
        tap(SUGGESTED_TAB, 20);
        tap(AppiumBy.xpath("(" + String.format(PRODUCT_CARD, index) + ")[2]"), 20);
        return new DmpOrderProductPage();
    }

    @Step("Open the product with SKU {sku} via deep link")
    public DmpOrderProductPage openProductByDeepLink(String sku) {
        openDeepLink(PRODUCT_DEEP_LINK + sku);
        return new DmpOrderProductPage();
    }

    /** Scroll a Store dashboard element into view by its visible text if it is not already shown. */
    private void ensureVisible(String text, By locator) {
        if (isPresent(locator, 3)) {
            return;
        }
        try {
            scrollToText(text);
        } catch (Exception e) {
            log.debug("Could not scroll '{}' into view: {}", text, e.getMessage());
        }
    }
}
