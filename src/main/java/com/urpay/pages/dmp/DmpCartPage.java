package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Cart / Checkout page — reached via the "View Cart" snackbar or the deep link
 * {@code urpay://DMPContainer/DigitalProductCart}.
 *
 * Migrated from the checkout step of Katalon
 * {@code Test Cases/DMP/Wishlist/PlaceOrderFromWishlist}: swipe to the total-amount field
 * then tap the checkout / pay action.
 *
 * The checkout action is anchored on visible text ("Checkout" / "Proceed to Checkout" / "Pay")
 * with the revamped Pay testID as a fallback, since the legacy locator is an unstable SVG node.
 */
public class DmpCartPage extends BasePage {

    // Cart "Go to Checkout" button (present only on the intermediate cart screen).
    @AndroidFindBy(xpath = "//*[@text='Go to Checkout' or @text='Proceed to Checkout']")
    private WebElement goToCheckoutButton;

    // Checkout-page "Place Order" primary CTA (revamped testID, or text).
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061') "
            + "or contains(@text,'Place Order') or @text='Pay']")
    private WebElement placeOrderButton;

    // "Checkout" page title marker.
    @AndroidFindBy(xpath = "//*[@text='Checkout' or @text='Order details']")
    private WebElement checkoutTitle;

    @Step("Check the Checkout page is loaded")
    public boolean isLoaded() {
        return isDisplayed(checkoutTitle, 20);
    }

    @Step("Tap 'Go to Checkout' if the intermediate cart is shown")
    public void goToCheckoutIfPresent() {
        if (isDisplayed(goToCheckoutButton, 8)) {
            tap(goToCheckoutButton, 15);
        }
    }

    @Step("Place the order")
    public void placeOrder() {
        tap(placeOrderButton, 20);
    }

    /** Full cart → checkout → place-order progression used by order journeys. */
    @Step("Proceed through checkout and place the order")
    public void proceedToCheckout() {
        goToCheckoutIfPresent();
        placeOrder();
    }
}
