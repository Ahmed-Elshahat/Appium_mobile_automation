package com.urpay.pages.dmp.orders;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Checkout page for the LANE C digital-order journeys.
 *
 * Migrated from the place-order step of Katalon
 * Test Cases/DMP/Order/ToValidateMoveToCheckoutPage (Object Repository
 * DMP/OrderPlacePage/placeOrder).
 *
 * NEW LANE C page. The place-order action is anchored on visible text ('Place Order' / 'Pay')
 * with the revamped place-order testID as a fallback, since the legacy checkout locator is an
 * unstable SVG node.
 *
 * Katalon taps Place Order TWICE before the payment "Verification Code" screen appears, so this
 * page re-taps the Place Order button if it is still shown after the first tap (the OTP field
 * only surfaces after the order is submitted).
 */
public class DmpOrderCheckoutPage extends BasePage {

    // Place-order call-to-action.
    @AndroidFindBy(xpath = "//*[@text='Place Order' or @text='Pay' "
            + "or contains(@content-desc,'testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061')]")
    private WebElement placeOrderButton;

    private static final By PLACE_ORDER = AppiumBy.xpath(
            "//*[@text='Place Order' or @text='Pay' "
            + "or contains(@content-desc,'testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061')]");

    // Checkout deep link (Katalon ToNavigateToCheckoutScreenUsingDeepLink → urpay://MarketPlaceCheckout).
    private static final String CHECKOUT_DEEP_LINK = "urpay://MarketPlaceCheckout";

    @Step("Check the Checkout page is loaded")
    public boolean isLoaded() {
        return isDisplayed(placeOrderButton, 20);
    }

    @Step("Open the Checkout screen via deep link")
    public DmpOrderCheckoutPage openByDeepLink() {
        openDeepLink(CHECKOUT_DEEP_LINK);
        waitUtils.isPresent(PLACE_ORDER, 30);
        return this;
    }

    @Step("Place the order")
    public void placeOrder() {
        if (!isDisplayed(placeOrderButton, 3)) {
            swipeUp();
        }
        waitUtils.isPresent(PLACE_ORDER, 30);

        // element.click() on the revamped RN Place Order ViewGroup is a NO-OP (the visible
        // "Place Order" TextView child is non-clickable), so tap the button's centre coordinates
        // instead — mirrors Katalon's Mobile.tapAtPosition on the same button and reliably fires
        // the RN onPress that submits the order. A single tap submits; do NOT re-tap (a second tap
        // during the navigation transition can aggravate the app's screen-removal animation).
        tapPlaceOrder();
    }

    /** Tap the Place Order button at its centre coordinates (reliable RN onPress trigger). */
    private void tapPlaceOrder() {
        try {
            WebElement btn = waitUtils.waitForVisible(PLACE_ORDER, 20);
            org.openqa.selenium.Rectangle r = btn.getRect();
            tapAtCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
        } catch (Exception e) {
            log.debug("Coordinate tap on Place Order failed, using element click: {}", e.getMessage());
            tap(PLACE_ORDER, 15);
        }
    }
}
