package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Product Details page — reached by tapping a product card (Store list or Wishlist).
 *
 * Migrated from the "Add to Cart" step of Katalon
 * {@code Test Cases/DMP/Wishlist/PlaceOrderFromWishlist} which swipes down to the
 * "Add to cart" action (Object Repository/android/DMP/AddCart) and taps it.
 *
 * The add-to-cart action is anchored on visible text first ("Add to Cart"), with the
 * revamped testID suffix {@code .Cart} as a fallback, since most DMP testIDs are UUID-based.
 */
public class DmpProductDetailsPage extends BasePage {

    // Primary "Buy now" call-to-action on the revamped product details (there is no
    // "Add to Cart" button here — list product cards carry the add-to-cart icon instead).
    @AndroidFindBy(xpath = "//*[contains(@text,'Buy now')]")
    private WebElement buyNowButton;

    // "Add to Cart" call-to-action (text-first; scrolled into view before tapping).
    @AndroidFindBy(xpath = "//*[@text='Add to Cart' or @text='Add to cart' "
            + "or contains(@content-desc,'.Cart') or contains(@name,'.Cart')]")
    private WebElement addToCartButton;

    // "View Cart" snackbar that appears after adding a product.
    @AndroidFindBy(xpath = "//*[@text='View Cart' or @text='View cart' or contains(@text,'View Cart')]")
    private WebElement viewCartSnackbar;

    // Wish (heart) toggle on the product details header — content-desc suffix ".HeartV2".
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'.HeartV2') or contains(@name,'.HeartV2') "
            + "or contains(@content-desc,'.WishList') or contains(@name,'.WishList')]")
    private WebElement wishHeartButton;

    @Step("Check the Product Details page is loaded")
    public boolean isLoaded() {
        return isDisplayed(buyNowButton, 20);
    }

    /** TEMP diagnostic — attach the product-details page source to Allure for locator hardening. */
    @Step("Capture product details page source (diagnostic)")
    public void captureForDiagnostics() {
        dumpPageSource("dmp-product-details");
    }

    @Step("Tap 'Buy now' to purchase this product")
    public void buyNow() {
        tap(buyNowButton, 20);
    }

    @Step("Add the product to the cart")
    public void addToCart() {
        scrollToAddToCart();
        tap(addToCartButton, 20);
    }

    @Step("Add the product to the wishlist (tap the heart)")
    public void addToWishlist() {
        tap(wishHeartButton, 20);
    }

    @Step("Tap the 'View Cart' snackbar if it is shown")
    public boolean tapViewCartSnackbarIfPresent() {
        if (isDisplayed(viewCartSnackbar, 5)) {
            tap(viewCartSnackbar, 10);
            return true;
        }
        return false;
    }

    /** Scroll the "Add to Cart" action into view (product details can require a downward swipe). */
    private void scrollToAddToCart() {
        if (isDisplayed(addToCartButton, 3)) {
            return;
        }
        scrollToText("Add to Cart");
    }
}
