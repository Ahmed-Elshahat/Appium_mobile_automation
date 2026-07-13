package com.urpay.flows;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dmp.DmpCartPage;
import com.urpay.pages.dmp.DmpMarketPlacePage;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;
import com.urpay.pages.dmp.DmpOrderHistoryPage;
import com.urpay.pages.dmp.DmpLocationDetailsPage;
import com.urpay.pages.dmp.DmpProductDetailsPage;
import com.urpay.pages.dmp.DmpWishlistPage;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * DMP (Digital MarketPlace) navigation flow.
 *
 * Migrated from Katalon Test Cases/DMP/NavigateToDMP
 * (Scripts/DMP/NavigateToDMP/Script1708958887857.groovy) which navigates to the MarketPlace
 * main page through the deep link {@code urpay://DMPContainer/MarketPlaceMainPage}
 * (SmartNavigator.navigateToMarketPlaceMainPageThroughDeepLink) rather than a bottom-nav tap.
 *
 * SOLID / project rules:
 *   - ZERO Thread.sleep() — all waits via WaitUtils.
 *   - NO assertions (returns page objects for the test to verify).
 *   - NO hardcoded data (deep links are app-routing constants, mirrored from Katalon).
 */
public class DmpFlow {

    private static final Logger log = LoggerFactory.getLogger(DmpFlow.class);

    // Deep links (Katalon SmartNavigator).
    private static final String MARKETPLACE_DEEP_LINK = "urpay://DMPContainer/MarketPlaceMainPage";
    private static final String DIGITAL_PRODUCT_CART_DEEP_LINK = "urpay://DMPContainer/DigitalProductCart";

    // MarketPlace-loaded marker. The revamped Store page shows a Products / My Orders tab pair.
    private static final By MARKETPLACE_LOADED =
            AppiumBy.xpath("//*[@text='My Orders' or @text='Store']");

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final DashboardPage dashboardPage;

    public DmpFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 15);
        this.dashboardPage = new DashboardPage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to the DMP MarketPlace main page (deep link urpay://DMPContainer/MarketPlaceMainPage)")
    public DmpMarketPlacePage openMarketPlace() {
        dashboardPage.dismissPopups();
        openViaDeepLink(MARKETPLACE_DEEP_LINK);
        waits.waitForVisible(MARKETPLACE_LOADED, 30);
        log.info("MarketPlace main page opened via deep link");
        return new DmpMarketPlacePage();
    }

    @Step("Navigate to the DMP Digital Product cart (deep link urpay://DMPContainer/DigitalProductCart)")
    public void openDigitalProductCart() {
        openViaDeepLink(DIGITAL_PRODUCT_CART_DEEP_LINK);
        log.info("Digital Product cart opened via deep link");
    }

    // ══════════════════════════════════════════════════
    //  FULL-CYCLE DIGITAL MARKETPLACE ORDER
    // ══════════════════════════════════════════════════

    /**
     * Full digital marketplace order journey, migrated from Katalon
     * {@code Test Cases/DMP/Wishlist/PlaceOrderFromWishlist} (suite
     * FullCycleDigitalMarketPlaceOrder):
     * Store → Wishlist → select first product → add to cart → cart → checkout/pay →
     * enter payment verification code → Order Placed.
     *
     * Assumes login has already been performed and the shared DMP user has at least one
     * wished product (Katalon relies on the same pre-seeded wishlist).
     *
     * @param verificationCode the payment OTP / verification code (config {@code dmp.verificationCode}).
     * @return the Order Confirmation page for the test to assert on.
     */
    @Step("Place a digital marketplace order from the wishlist")
    public DmpOrderConfirmationPage placeOrderFromWishlist(String verificationCode) {
        DmpMarketPlacePage marketplace = openMarketPlace();
        DmpWishlistPage wishlist = marketplace.openWishlist();
        DmpProductDetailsPage details = wishlist.selectProduct(0);

        details.buyNow();
        DmpCartPage cart = new DmpCartPage();
        cart.proceedToCheckout();
        new OtpPage().enterOtp(verificationCode);
        return new DmpOrderConfirmationPage();
    }

    /**
     * Re-order journey, migrated from Katalon Re-Order suite
     * ({@code Order History/MoveToMyOrder → move to Order History → SelectProductFromVireHistory →
     * ValidateClickReOrderALL}): Store → My Orders → View History → select the most recent order →
     * Reorder All → checkout → place order → enter payment verification code → Order Placed.
     *
     * Requires the shared DMP user to have at least one previous order in history.
     *
     * @param verificationCode the payment OTP / verification code (config {@code dmp.verificationCode}).
     * @return the Order Confirmation page for the test to assert on.
     */
    @Step("Re-order the most recent order from order history")
    public DmpOrderConfirmationPage reorderFromHistory(String verificationCode) {
        DmpMarketPlacePage marketplace = openMarketPlace();
        DmpOrderHistoryPage history = marketplace.openMyOrders();
        history.openViewHistory();
        history.selectOrder(1);
        history.reorderAll();
        history.checkout();
        history.placeOrder();
        new OtpPage().enterOtp(verificationCode);
        return new DmpOrderConfirmationPage();
    }

    /**
     * Add the first Store product to the wishlist and return the Wishlist page, migrated from the
     * Katalon Product Wishlist Management suite (open product → {@code clickOnWishButton} →
     * navigate back → {@code moveToWishlistPage}).
     *
     * @return the Wishlist page for the test to assert the product is wished.
     */
    @Step("Add the first Store product to the wishlist")
    public DmpWishlistPage addFirstProductToWishlist() {
        DmpMarketPlacePage marketplace = openMarketPlace();
        DmpProductDetailsPage details = marketplace.openProduct(0);
        details.addToWishlist();
        return openMarketPlace().openWishlist();
    }

    /**
     * Order the first "New Arrivals" product, migrated from the Katalon suite
     * {@code New Test Suites After Revamp/Validate order from new arrival/Order From new arrival device}:
     * Store → New Arrivals tag → select product → Buy now → checkout → complete delivery location
     * (physical products) → place order → OTP → Order Placed.
     *
     * @param verificationCode the payment OTP / verification code.
     * @param deliveryLocation the delivery location typed into the address form (physical products).
     * @return the Order Confirmation page for the test to assert on.
     */
    @Step("Order the first New Arrivals product")
    public DmpOrderConfirmationPage orderFromNewArrival(String verificationCode, String deliveryLocation) {
        DmpMarketPlacePage marketplace = openMarketPlace();
        marketplace.selectNewArrivalsTag();
        DmpProductDetailsPage details = marketplace.openNewArrivalProduct(0);
        details.captureForDiagnostics();
        details.buyNow();

        DmpCartPage cart = new DmpCartPage();
        cart.goToCheckoutIfPresent();
        new DmpLocationDetailsPage().completeIfPresent(deliveryLocation);
        cart.placeOrder();
        DmpOrderConfirmationPage confirmation = new DmpOrderConfirmationPage();
        confirmation.enterPaymentOtpIfPresent(verificationCode);
        return confirmation;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    /** Fire a urpay:// deep link via the UiAutomator2 mobile:deepLink command. */
    private void openViaDeepLink(String url) {
        String appPackage = ConfigManager.getInstance().get("appPackage", "com.urpay.consumer.sit");
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("package", appPackage);
        ((JavascriptExecutor) driver).executeScript("mobile: deepLink", params);
    }
}
