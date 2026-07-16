package com.urpay.pages.dmp.physicalorder;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.core.ConfigManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP physical-product order screens for the LANE D physical-order journey.
 *
 * Migrated from Katalon (DMP_1 branch) Test Cases/DMP:
 *   Wishlist/ToMoveToAllItemsPage (open the Store search) + Wishlist/ToSearchByProductName +
 *   Wishlist/ToValidateClickOnProduct (open result 0) + Promocodes/ToClickOnAddToCart
 *   (scroll to "Add to cart") + Promocodes/To Validate Click on View Cart +
 *   Order/ToValidateMoveToCheckoutPage.
 *
 * Physical products go through the cart ("Add to cart"), not the digital "Buy now" instant path.
 * LANE D-owned page (namespace {@code pages/dmp/physicalorder}).
 */
public class DmpPhysicalOrderPage extends BasePage {

    // Store search bar (opens the search screen).
    @AndroidFindBy(xpath = "//*[starts-with(@text,'Search for') or contains(@text,'Search for') "
            + "or @content-desc='testID-View.08ee158d-87c4-492a-8de9-afc3a9083dbc.0']")
    private WebElement storeSearchBar;

    // Store "Smart Phones" top category tile — opens the in-stock physical Devices listing
    // (the current catalog's searched "iphone 16 Plus" is out of stock; the Smart Phones listing's
    // first card is an in-stock Samsung device, mirroring Lane A's proven navigation).
    @AndroidFindBy(xpath = "//*[@text='Smart Phones' or @content-desc='Smart Phones']")
    private WebElement smartPhonesTile;

    // Product search input on the search / all-items screen (Katalon searchForProductEditText).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-TextInput.7e0015ce-2927-4d26-af63-bab91907305d' "
            + "or @name='testID-TextInput.7e0015ce-2927-4d26-af63-bab91907305d' "
            + "or @class='android.widget.EditText']")
    private WebElement searchInput;

    // Add-to-cart call-to-action (physical products; text-first).
    @AndroidFindBy(xpath = "//*[@text='Add to cart' or @text='Add to Cart' or @text='Add To Cart']")
    private WebElement addToCartButton;

    // View-cart control (snackbar / toolbar cart icon).
    @AndroidFindBy(xpath = "//*[@text='View Cart' or @text='View cart' "
            + "or @content-desc='testID-right-icon-0' or @content-desc='testID-right-icon-1']")
    private WebElement viewCartButton;

    // Cart 'Go to Checkout' call-to-action.
    @AndroidFindBy(xpath = "//*[@text='Go to Checkout' or @text='Checkout' or @text='Proceed to Checkout']")
    private WebElement checkoutButton;

    private static final String PRODUCT_CARD_PREFIX =
            "testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.";

    // Product-details deep link (open a specific product by its Magento SKU).
    private static final String PRODUCT_DEEP_LINK = "urpay://MarketPlace/ProductDetails?sku=";

    // In-stock vs out-of-stock markers on the product-details screen.
    private static final By ADD_TO_CART = AppiumBy.xpath(
            "//*[@text='Add to cart' or @text='Add to Cart' or @text='Add To Cart']");
    // Some revamped physical products (e.g. the iPhone) expose a 'Buy now (<price>)' CTA instead of
    // 'Add to cart' (the variant is pre-selected via the SKU deep link). The bottom primary CTA is a
    // LinearGradient TouchableOpacity (same testID family as the cart 'Place Order').
    private static final By BUY_NOW = AppiumBy.xpath(
            "//*[@content-desc='testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061' "
            + "or starts-with(@text,'Buy now') or contains(@text,'Buy now') or contains(@text,'Buy Now')]");
    // Color swatch: the first clickable ViewGroup right after the 'Select color' label (no testID).
    private static final By COLOR_SWATCH = AppiumBy.xpath(
            "//*[@text='Select color']/following::android.view.ViewGroup[@clickable='true'][1]");

    private static final By LOADER_ANY = AppiumBy.xpath("//*[starts-with(@content-desc,'testID-Loader')]");
    // Markers that the delivery step was reached after 'Buy now' (map add-location form OR Manage-location list).
    private static final By DELIVERY_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-input-direct-undefined' or @content-desc='testID-Search-Input' "
            + "or @content-desc='testID-primary-WA4-main' or @content-desc='testID-primary-p20-main' "
            + "or @text='Manage location' or @text='Delivery location']");
    // Either purchase CTA means the product details opened and are purchasable.
    private static final By PURCHASE_CTA = AppiumBy.xpath(
            "//*[@text='Add to cart' or @text='Add to Cart' or @text='Add To Cart' "
            + "or starts-with(@text,'Buy now') or contains(@text,'Buy now') or contains(@text,'Buy Now')]");
    private static final By OUT_OF_STOCK = AppiumBy.xpath(
            "//*[contains(@text,'out of stock') or contains(@text,'Out of stock') or contains(@text,'Out of Stock') "
            + "or @text='Notify Me' or contains(@text,'notify you') or contains(@text,'try again later')]");

    @Step("Open the Store search")
    public DmpPhysicalOrderPage openSearch() {
        tap(storeSearchBar, 20);
        return this;
    }

    @Step("Open the physical Devices listing via the Store 'Smart Phones' category tile")
    public DmpPhysicalOrderPage openSmartPhonesListing() {
        for (int i = 0; i < 3 && !isDisplayed(smartPhonesTile, 1); i++) {
            swipeDown();
        }
        tap(smartPhonesTile, 20);
        return this;
    }

    /**
     * Select a brand / category filter chip on the Devices listing (e.g. "Samsung" / "Apple").
     * The chips render before the product cards, so the first text match is the chip. Tolerant: if
     * the chip is absent the listing is left as-is.
     */
    @Step("Select the '{filter}' filter chip")
    public DmpPhysicalOrderPage selectFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return this;
        }
        try {
            tap(AppiumBy.xpath("(//*[@text='" + filter + "' or contains(@text,'" + filter + "')])[1]"), 15);
        } catch (Exception e) {
            log.info("Filter chip '{}' not available ({}); leaving listing unfiltered", filter, e.getMessage());
        }
        return this;
    }

    @Step("Search for the product '{name}'")
    public DmpPhysicalOrderPage searchProduct(String name) {
        tap(searchInput, 20);
        searchInput.clear();
        searchInput.sendKeys(name);
        hideKeyboard();
        return this;
    }

    /**
     * Filter the currently-open Devices listing by {@code name} using the listing's search box.
     * Tolerant: if the search field is not present the listing's existing products are kept (the
     * in-stock hunt then scans them), so a missing search box never hard-fails the flow.
     */
    @Step("Filter the Devices listing by '{name}'")
    public DmpPhysicalOrderPage searchInListing(String name) {
        if (name == null || name.trim().isEmpty()) {
            return this;
        }
        try {
            tap(searchInput, 15);
            searchInput.clear();
            searchInput.sendKeys(name);
            hideKeyboard();
        } catch (Exception e) {
            log.info("Devices listing search box not available ({}); scanning unfiltered listing", e.getMessage());
        }
        return this;
    }

    @Step("Open the first product result")
    public DmpPhysicalOrderPage openFirstResult() {
        String testId = PRODUCT_CARD_PREFIX + 0;
        tap(AppiumBy.xpath("//*[@content-desc='" + testId + "' or @label='" + testId
                + "' or @name='" + testId + "']"), 20);
        return this;
    }

    /**
     * Open the first IN-STOCK product from the listing: try each card in turn, and if its details
     * show the "Add to cart" CTA keep it; if it is out of stock, go back and try the next. Returns
     * {@code true} when an in-stock product's details are open, {@code false} if none of the first
     * {@code maxProducts} cards are purchasable (a catalog/data condition, not a UI defect).
     */
    @Step("Open the first in-stock product (checking up to {maxProducts} cards)")
    public boolean openFirstInStockProduct(int maxProducts) {
        for (int i = 0; i < maxProducts; i++) {
            By card = AppiumBy.xpath("//*[@content-desc='" + PRODUCT_CARD_PREFIX + i + "']");
            for (int s = 0; s < 4 && !isPresent(card, 1); s++) {
                swipeUp();
            }
            if (!isPresent(card, 2)) {
                break; // no more products in the listing
            }
            tap(card, 15);
            if (isPresent(ADD_TO_CART, 5)) {
                log.info("Product at index {} is in stock", i);
                return true;
            }
            log.info("Product at index {} is out of stock; trying the next", i);
            pressBack();
        }
        return false;
    }

    /**
     * Open a specific physical product by its Magento SKU via deep link (bypasses the browse /
     * in-stock hunt). Returns {@code true} when the details show the "Add to cart" CTA (in stock +
     * addable), {@code false} if it is out of stock or the deep link did not resolve the SKU.
     */
    @Step("Open physical product by SKU deep link '{sku}'")
    public boolean openProductByDeepLink(String sku) {
        // Use the APP-SCOPED deep link (mobile: deepLink with the package) — the same mechanism that
        // opens digital products in the promo flow (FixedMinPurchase). The generic BasePage.openDeepLink
        // drops the MarketPlace/ProductDetails route to the device launcher (home screen) instead.
        String pkg = ConfigManager.getInstance().get("appPackage", "com.urpay.consumer.sit");
        // Bring the app to the foreground first — mobile: deepLink flakily drops to the launcher when the
        // app is not already the active app, which then breaks the following deep-link route.
        try {
            ((io.appium.java_client.android.AndroidDriver) driver).activateApp(pkg);
        } catch (Exception ignored) {
            // activateApp unsupported / already active — proceed to the deep link
        }
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("url", PRODUCT_DEEP_LINK + sku);
        params.put("package", pkg);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("mobile: deepLink", params);
        boolean opened = isPresent(PURCHASE_CTA, 20);
        if (opened && isPresent(OUT_OF_STOCK, 2)) {
            // The Buy now button renders even when the item is out of stock; the app shows an
            // "out of stock" banner and Buy now does nothing. Treat as not-openable (data condition).
            log.warn("Product '{}' opened but the app shows it OUT OF STOCK.", sku);
            return false;
        }
        if (!opened) {
            // Diagnostic: capture what the deep link actually landed on (iPhone details w/ variant
            // selectors? a not-found/home screen?) so the SKU-encoding / variant step can be fixed.
            dumpPageSource("physical-deeplink-" + sku.replace("/", "_"));
        }
        return opened;
    }

    /** True if the open product details expose the 'Add to cart' CTA (vs the 'Buy now' instant CTA). */
    public boolean hasAddToCart(long timeoutSec) {
        return isPresent(ADD_TO_CART, timeoutSec);
    }

    /** Tap 'Buy now' on the product details and proceed to the checkout / delivery-location screen. */
    @Step("Buy now and proceed to the checkout / delivery-location screen")
    public DmpDeliveryLocationPage buyNowToDelivery() {
        // Buy now / color occasionally misfires (RN timing) leaving us on the product page — verify the
        // delivery screen appears and retry the color-swatch + Buy now taps if it doesn't.
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (isPresent(DELIVERY_MARKER, 2)) {
                return new DmpDeliveryLocationPage();
            }
            // The revamped physical details require an explicit color-swatch tap before 'Buy now' proceeds.
            try {
                tap(COLOR_SWATCH, 8);
            } catch (Exception e) {
                log.info("Color swatch not tappable ({}); proceeding to Buy now", e.getMessage());
            }
            try {
                tap(BUY_NOW, 15);
            } catch (Exception e) {
                log.info("Buy now not tappable ({})", e.getMessage());
            }
            waitForLoaderGone();
            if (isPresent(DELIVERY_MARKER, 15)) {
                return new DmpDeliveryLocationPage();
            }
            log.warn("Buy now did not reach the delivery screen (attempt {}/3); retrying", attempt);
        }
        return new DmpDeliveryLocationPage();
    }

    private void waitForLoaderGone() {
        try {
            waitUtils.waitForInvisible(LOADER_ANY);
        } catch (Exception ignored) {
            // loader already gone or never shown
        }
    }

    @Step("Add the physical product to the cart")
    public DmpPhysicalOrderPage addToCart() {
        if (!isDisplayed(addToCartButton, 3)) {
            try {
                scrollToText("Add to cart");
            } catch (Exception ignored) {
                log.debug("'Add to cart' not reachable by text scroll; relying on wait");
            }
        }
        tap(addToCartButton, 20);
        return this;
    }

    @Step("Open the cart (View Cart)")
    public DmpPhysicalOrderPage openCart() {
        tap(viewCartButton, 20);
        return this;
    }

    @Step("Proceed to checkout (Go to Checkout)")
    public DmpDeliveryLocationPage goToCheckout() {
        if (!isDisplayed(checkoutButton, 3)) {
            swipeUp();
        }
        tap(checkoutButton, 20);
        return new DmpDeliveryLocationPage();
    }

    @Step("Check a product result is shown")
    public boolean hasResult() {
        By card = AppiumBy.xpath("//*[@content-desc='" + PRODUCT_CARD_PREFIX + "0']");
        return isPresent(card, 15);
    }
}
