package com.urpay.pages.dmp.orders;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

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
 */
public class DmpOrderCheckoutPage extends BasePage {

    // Place-order call-to-action.
    @AndroidFindBy(xpath = "//*[@text='Place Order' or @text='Pay' "
            + "or contains(@content-desc,'testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061')]")
    private WebElement placeOrderButton;

    @Step("Check the Checkout page is loaded")
    public boolean isLoaded() {
        return isDisplayed(placeOrderButton, 20);
    }

    @Step("Place the order")
    public void placeOrder() {
        if (!isDisplayed(placeOrderButton, 3)) {
            swipeUp();
        }
        tap(placeOrderButton, 20);
    }
}
