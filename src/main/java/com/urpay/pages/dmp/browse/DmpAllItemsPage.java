package com.urpay.pages.dmp.browse;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP "All Items" listing — the voucher/product grid reached from the Store "View All"
 * (vouchers) button. Shows category chips, a filter/sort entry, and a scrollable product grid.
 *
 * LANE A (Sorting &amp; Grouping) — parallel migration namespace {@code pages/dmp/browse}.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Keywords/com/uspace/dmp/AllItemsPageProcessor (clickOnFilterButton, getProducts)
 *   Test Cases/DMP/Wishlist/ToValidateClickOnCategory (category chip tap)
 *   Object Repository/Android/DMP/{ViewAllAndHideButtons/viewAllVouchers, AllItems/filterButton,
 *     Categories/*CategoryButton}.
 *
 * Price extraction mirrors {@code AllItemsPageProcessor.getProducts}: each product card
 * {@code testID-TouchableOpacity.9378f6b3-…-137f2bfe09cd.<index>} contains a {@code testID-amount-main}
 * node whose TextViews split the price into integer + decimal parts (with "SAR" / "Starting from"
 * noise), rebuilt as {@code <int>.<dec>}.
 */
public class DmpAllItemsPage extends BasePage {

    private static final String PRODUCT_CARD_PREFIX =
            "testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.";
    private static final String AMOUNT_TESTID = "testID-amount-main";

    // Store "View All" (vouchers section) — opens this All Items listing.
    private static final By VIEW_ALL_VOUCHERS = AppiumBy.xpath(
            "//*[@content-desc='testID-secondary-onViewMore-main' or @text='View All']");

    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-onViewMore-main' or @text='View All']")
    private WebElement viewAllVouchers;

    // Filter / sort entry on the All Items page.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.DigitalProductsFilterEditable']")
    private WebElement filterButton;

    private static final By FILTER_BUTTON = AppiumBy.xpath(
            "//*[@content-desc='testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.DigitalProductsFilterEditable']");

    @Step("Open the All Items listing via the Store 'View All' (vouchers)")
    public DmpAllItemsPage openFromVouchers() {
        // Katalon swipes the Store down to reveal the Vouchers section before tapping View All.
        for (int i = 0; i < 6 && !isPresent(VIEW_ALL_VOUCHERS, 1); i++) {
            swipeUp();
        }
        dumpPageSource("allItems-beforeViewAll"); // TEMP: first-run locator capture
        tap(viewAllVouchers, 20);
        return this;
    }

    // Physical devices entry: the Store "Smart Phones" top category tile. The revamped build removed
    // the standalone "Devices" View-All section that Katalon's viewAllDevices tapped; smart-phone /
    // tablet products are now reached through the top category tiles. The resulting "Devices" listing
    // reuses the SAME filter button + product-card testIDs as the digital voucher listing.
    private static final By SMART_PHONES_TILE = AppiumBy.xpath(
            "//*[@text='Smart Phones' or @content-desc='Smart Phones']");

    @Step("Open the physical Devices listing via the Store 'Smart Phones' category tile")
    public DmpAllItemsPage openFromDevices() {
        // Smart Phones tile sits in the Store's top category row; guard with a light scroll up in
        // case a promo banner covers it, then tap to open the Devices (physical products) listing.
        for (int i = 0; i < 3 && !isPresent(SMART_PHONES_TILE, 1); i++) {
            swipeDown();
        }
        tap(SMART_PHONES_TILE, 20);
        return this;
    }

    @Step("Select the '{category}' category chip")
    public DmpAllItemsPage selectCategory(String category) {
        tap(AppiumBy.xpath("(//*[contains(@text,'" + category + "')])[1]"), 20);
        return this;
    }

    // Product search input on the All Items listing (Katalon DMP/searchForProductEditText).
    private static final By SEARCH_PRODUCT_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-TextInput.7e0015ce-2927-4d26-af63-bab91907305d'"
            + " or @label='testID-TextInput.7e0015ce-2927-4d26-af63-bab91907305d'"
            + " or @name='testID-TextInput.7e0015ce-2927-4d26-af63-bab91907305d']");

    // Empty-state markers (Katalon DMP/Search/NoResultFoundTextField + pleaseCheckYourSearchTextField).
    private static final By NO_RESULTS = AppiumBy.xpath(
            "(//*[@content-desc='testID-Text.1fbf1702-3e9d-4610-992e-6b17ff2028db'"
            + " or @content-desc='testID-Text.b6ebab44-99b8-4e5a-b5eb-d1a4aea5054f'"
            + " or contains(@text,'No Result') or contains(@text,'no result')"
            + " or contains(@text,'couldn') or contains(@text,'Sorry')])[1]");

    @Step("Search the listing for product '{name}'")
    public void searchProduct(String name) {
        type(SEARCH_PRODUCT_INPUT, name);
        hideKeyboard();
    }

    @Step("Check any product result is shown (a product card price is present)")
    public boolean hasSearchResults() {
        return isPresent(AppiumBy.xpath("//*[@content-desc='" + AMOUNT_TESTID + "']"), 12);
    }

    @Step("Check the 'No Results' empty state is shown")
    public boolean isNoResultsShown() {
        return isPresent(NO_RESULTS, 15);
    }

    @Step("Open the sort / filter sheet")
    public DmpSortFilterPage openFilter() {
        tap(filterButton, 20);
        return new DmpSortFilterPage();
    }

    @Step("Check the All Items listing is displayed")
    public boolean isLoaded() {
        return isPresent(FILTER_BUTTON, 20);
    }

    /**
     * Sample up to {@code maxItems} product prices from the listing, top to bottom, scrolling each
     * card into view. Returns fewer entries when the listing is shorter. Mirrors Katalon
     * {@code AllItemsPageProcessor.getProducts}.
     */
    @Step("Extract up to {maxItems} product prices from the listing")
    public List<Double> getProductPrices(int maxItems) {
        List<Double> prices = new ArrayList<>();
        for (int i = 0; i < maxItems; i++) {
            String amountXpath = "//*[@content-desc='" + PRODUCT_CARD_PREFIX + i + "']"
                    + "//*[@content-desc='" + AMOUNT_TESTID + "']";
            By amount = AppiumBy.xpath(amountXpath);
            for (int s = 0; s < 3 && !isPresent(amount, 1); s++) {
                swipeUp();
            }
            if (!isPresent(amount, 2)) {
                break; // no more product cards
            }
            Double price = extractPrice(amountXpath);
            if (price != null) {
                prices.add(price);
            }
        }
        if (prices.isEmpty()) {
            dumpPageSource("allItems-noPrices"); // TEMP: first-run diagnosis
        }
        return prices;
    }

    /** Rebuild a card's price from its {@code testID-amount-main} TextView parts. */
    private Double extractPrice(String amountXpath) {
        List<WebElement> texts = waitUtils.findQuick(
                AppiumBy.xpath(amountXpath + "//android.widget.TextView"), 2);
        List<String> parts = new ArrayList<>();
        for (WebElement t : texts) {
            String s = t.getText();
            if (s == null) {
                continue;
            }
            s = s.trim();
            if (s.isEmpty() || s.equalsIgnoreCase("SAR")
                    || s.equalsIgnoreCase("Starting from") || s.equals(".")) {
                continue;
            }
            parts.add(s);
        }
        if (parts.isEmpty()) {
            return null;
        }
        String rebuilt = (parts.size() >= 2)
                ? digitsOnly(parts.get(0)) + "." + digitsOnly(parts.get(1))
                : parts.get(0).replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(rebuilt);
        } catch (NumberFormatException e) {
            log.warn("Could not parse price from {} -> '{}'", parts, rebuilt);
            return null;
        }
    }

    private String digitsOnly(String s) {
        String d = s.replaceAll("[^0-9]", "");
        return d.isEmpty() ? "0" : d;
    }
}
