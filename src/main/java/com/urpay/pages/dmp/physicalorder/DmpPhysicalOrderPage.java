package com.urpay.pages.dmp.physicalorder;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

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

    // In-stock vs out-of-stock markers on the product-details screen.
    private static final By ADD_TO_CART = AppiumBy.xpath(
            "//*[@text='Add to cart' or @text='Add to Cart' or @text='Add To Cart']");
    private static final By OUT_OF_STOCK = AppiumBy.xpath(
            "//*[@text='Out of stock' or @text='Out of Stock' or @text='Notify Me' "
            + "or @text=\"We'll notify you\" or contains(@text,'notify you')]");

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
