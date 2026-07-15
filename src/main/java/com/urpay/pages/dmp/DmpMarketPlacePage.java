package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP MarketPlace main page — the revamped "Store" landing screen reached via the deep link
 * {@code urpay://DMPContainer/MarketPlaceMainPage} (Katalon NavigateToDMP).
 *
 * The current app build ships the REVAMPED DMP: a "Store" header (location, wishlist, cart),
 * a search bar, category tiles (Food / Recharge / Smart Phones / …), a Products / My Orders
 * tab pair, and a product list filtered by Best Sellers / New Arrivals / Suggested tags.
 * (The legacy "Top products" section from the old numbered Katalon suites no longer exists.)
 *
 * Locators use stable text / indexed-testID anchors verified from a live page-source dump —
 * most DMP elements carry UUID testIDs, so text and the stable {@code testID-tags-menu-*}
 * indices are the reliable anchors.
 */
public class DmpMarketPlacePage extends BasePage {

    // Store header title.
    @AndroidFindBy(xpath = "//*[@text='Store']")
    private WebElement storeTitle;

    // Products / My Orders tab pair.
    @AndroidFindBy(xpath = "//*[@text='Products']")
    private WebElement productsTab;

    @AndroidFindBy(xpath = "//*[@text='My Orders']")
    private WebElement myOrdersTab;

    // Best Sellers product tag (default-selected filter). Stable testID-tags-menu-Text-0.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-tags-menu-Text-0' or @text='Best Sellers']")
    private WebElement bestSellersTag;

    // Any product card shows the "Starting from" price label, OR the tappable product-card testID
    // prefix (self-heal 2026-07-15: the revamped Best-Sellers cards no longer always render the
    // 'Starting from' label, so also accept the presence of a product card = list is displayed).
    @AndroidFindBy(xpath = "//*[@text='Starting from' "
            + "or starts-with(@content-desc,'testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.') "
            + "or starts-with(@name,'testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.')]")
    private WebElement firstProductPriceLabel;

    // Store header wishlist (heart) icon — content-desc suffix ".WishList".
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'.WishList') or contains(@name,'.WishList')]")
    private WebElement wishlistIcon;

    // Store search bar (opens the dedicated search screen). Shows an animated "Search for …" hint.
    @AndroidFindBy(xpath = "//*[starts-with(@text,'Search for') or contains(@text,'Search for')]")
    private WebElement searchBar;

    @Step("Check the Store (MarketPlace main) page is loaded")
    public boolean isLoaded() {
        return isDisplayed(storeTitle, 20);
    }

    @Step("Open the Wishlist from the Store header")
    public DmpWishlistPage openWishlist() {
        tap(wishlistIcon, 20);
        return new DmpWishlistPage();
    }

    @Step("Open the Search screen from the Store")
    public DmpSearchPage openSearch() {
        tap(searchBar, 20);
        return new DmpSearchPage();
    }

    @Step("Open the My Orders tab from the Store")
    public DmpOrderHistoryPage openMyOrders() {
        tap(myOrdersTab, 20);
        return new DmpOrderHistoryPage();
    }

    @Step("Open the product at index {index} from the Store product list")
    public DmpProductDetailsPage openProduct(int index) {
        String testId = "testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd." + index;
        tap(io.appium.java_client.AppiumBy.xpath(
                "//*[@content-desc='" + testId + "' or @label='" + testId + "' or @name='" + testId + "']"), 20);
        return new DmpProductDetailsPage();
    }

    @Step("Select the 'New Arrivals' product tag")
    public void selectNewArrivalsTag() {
        tap(io.appium.java_client.AppiumBy.xpath(
                "//*[@content-desc='testID-tags-menu-1' or @content-desc='testID-tags-menu-Text-1' "
                        + "or @text='New Arrivals']"), 20);
    }

    @Step("Open the New Arrivals product at index {index}")
    public DmpProductDetailsPage openNewArrivalProduct(int index) {
        String testId = "testID-View.6f7c2762-b234-4b50-bf47-4366a5d88abd." + index;
        tap(io.appium.java_client.AppiumBy.xpath(
                "(//android.view.ViewGroup[@content-desc='" + testId + "' or @name='" + testId + "'])[1]"), 20);
        return new DmpProductDetailsPage();
    }

    @Step("Check the Best Sellers product tag is displayed")
    public boolean isBestSellersTagDisplayed() {
        return isDisplayed(bestSellersTag, 15);
    }

    @Step("Check the product list is displayed (a product card is visible)")
    public boolean isProductListDisplayed() {
        return isDisplayed(firstProductPriceLabel, 15);
    }
}
