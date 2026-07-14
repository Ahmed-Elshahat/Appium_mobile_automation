package com.urpay.pages.dmp.orders;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Product Details page for the LANE C digital-order journeys.
 *
 * Migrated from Katalon Test Cases/DMP/Cart/AddProductToCart +
 * toValidateClickOnViewCartButton (cartToolbarButton).
 *
 * NEW LANE C page (the frozen {@code DmpProductDetailsPage} anchors only on 'Add to Cart').
 * On the revamped build the live call-to-action is 'Buy now (₪X.XX)' rather than
 * 'Add to Cart', so the add-to-cart locator matches BOTH, with the {@code .Cart} testID
 * suffix as a further fallback.
 */
public class DmpOrderProductPage extends BasePage {

    // Add-to-cart / Buy-now call-to-action (text-first; scrolled into view before tapping).
    // NOTE: do NOT match the header '.Cart' icon here — it sits at the top of the DOM and would be
    // tapped instead of the actual bottom CTA.
    @AndroidFindBy(xpath = "//*[@text='Add to Cart' or @text='Add to cart' "
            + "or starts-with(@text,'Buy now') or contains(@text,'Buy now') "
            + "or starts-with(@text,'Buy Now') or contains(@text,'Buy Now')]")
    private WebElement addToCartButton;

    // Cart toolbar icon / 'View Cart' snackbar (Katalon cartToolbarButton testID-right-icon-0).
    @AndroidFindBy(xpath = "//*[@text='View Cart' or @text='View cart' "
            + "or @content-desc='testID-right-icon-0' or @content-desc='testID-right-icon-1']")
    private WebElement viewCartButton;

    @Step("Check the Product Details page is loaded")
    public boolean isLoaded() {
        return isDisplayed(addToCartButton, 20);
    }

    @Step("Add the product to the cart")
    public void addToCart() {
        if (!isDisplayed(addToCartButton, 3)) {
            scrollToCta();
        }
        tap(addToCartButton, 20);
    }

    @Step("Open the cart from the product screen")
    public DmpOrderCartPage tapViewCart() {
        tap(viewCartButton, 20);
        return new DmpOrderCartPage();
    }

    /** Bring the add-to-cart / Buy-now action into view (details can require a downward swipe). */
    private void scrollToCta() {
        try {
            scrollToText("Buy now");
        } catch (Exception e) {
            try {
                scrollToText("Add to Cart");
            } catch (Exception ignored) {
                log.debug("Add-to-cart CTA not reachable by text scroll; relying on wait");
            }
        }
    }
}
