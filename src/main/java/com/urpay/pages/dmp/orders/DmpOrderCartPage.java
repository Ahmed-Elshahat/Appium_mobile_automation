package com.urpay.pages.dmp.orders;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Cart page for the LANE C digital-order journeys.
 *
 * Migrated from the cart / checkout step of Katalon
 * Test Cases/DMP/Order/ToValidateMoveToCheckoutPage.
 *
 * NEW LANE C page. The checkout call-to-action label on the revamped build is
 * 'Go to Checkout' (foundation note), with 'Checkout' / 'Proceed to Checkout' kept as
 * fallbacks.
 */
public class DmpOrderCartPage extends BasePage {

    // Cart total-amount label.
    @AndroidFindBy(xpath = "//*[@text='Total' or contains(@text,'Total')]")
    private WebElement totalLabel;

    // 'Go to Checkout' call-to-action.
    @AndroidFindBy(xpath = "//*[@text='Go to Checkout' or @text='Checkout' or @text='Proceed to Checkout']")
    private WebElement checkoutButton;

    private static final By CHECKOUT_BTN = AppiumBy.xpath(
            "//*[@text='Go to Checkout' or @text='Checkout' or @text='Proceed to Checkout']");

    // Empty-cart markers (self-heal signal): the add-to-cart tap silently failed to add the item.
    private static final By CART_EMPTY = AppiumBy.xpath(
            "//*[contains(@text,'Cart is Empty') or contains(@text,'cart is empty') "
            + "or contains(@text,'Explore Products') or contains(@text,'Explore products')]");

    // Quantity increment (+) button on the cart product line (Katalon CartPageProcessor
    // testID-incrementItem_undefined), first product.
    private static final By INCREMENT_BUTTON = AppiumBy.xpath(
            "(//*[@content-desc='testID-incrementItem_undefined' or @name='testID-incrementItem_undefined' "
            + "or @label='testID-incrementItem_undefined'])[1]");

    // Cart total amount (revamped testID-amount-label-* rows).
    private static final By TOTAL_AMOUNT = AppiumBy.xpath(
            "//*[@content-desc='testID-amount-label-0' or @name='testID-amount-label-0' "
            + "or @content-desc='testID-amount-label-4' or @name='testID-amount-label-4']");

    @Step("Check the Cart page is loaded")
    public boolean isLoaded() {
        return isDisplayed(checkoutButton, 20) || isDisplayed(totalLabel, 5);
    }

    /** True if the cart has a checkout call-to-action (i.e. it holds at least one item). */
    public boolean hasCheckoutButton(long timeoutSec) {
        return isPresent(CHECKOUT_BTN, timeoutSec);
    }

    /** True if the cart shows the empty state (the item was not added). */
    public boolean isEmpty(long timeoutSec) {
        return isPresent(CART_EMPTY, timeoutSec);
    }

    @Step("Proceed to checkout (Go to Checkout)")
    public DmpOrderCheckoutPage goToCheckout() {
        if (!isDisplayed(checkoutButton, 3)) {
            swipeUp();
        }
        tap(checkoutButton, 20);
        return new DmpOrderCheckoutPage();
    }

    /**
     * Raise the product quantity until the cart total reaches at least {@code minTotal} SAR (or
     * {@code maxTaps} is hit). Mirrors the Katalon minimum-purchase setup
     * (CartPageProcessor.clickOnIncrementButtonForProduct) but drives by the LIVE cart total so a
     * cheap item still crosses the threshold (e.g. 10SAROFF200Min needs >= 200 SAR). No Thread.sleep.
     */
    @Step("Increase cart quantity until the total reaches at least {minTotal} SAR (max {maxTaps} taps)")
    public void increaseQuantityUntilTotalAtLeast(double minTotal, int maxTaps) {
        if (!isPresent(INCREMENT_BUTTON, 8)) {
            log.warn("Cart increment button (testID-incrementItem_undefined) not found; cannot raise "
                    + "the cart to {} SAR.", minTotal);
            dumpPageSource("promo-cart-no-increment");
            return;
        }
        if (getCartTotal() < 0) {
            dumpPageSource("promo-cart-total-unreadable");
        }
        for (int i = 0; i < maxTaps; i++) {
            double total = getCartTotal();
            if (total >= minTotal) {
                log.info("Cart total {} SAR reached the >= {} SAR threshold after {} increment(s).",
                        total, minTotal, i);
                return;
            }
            tap(INCREMENT_BUTTON, 10);
        }
        log.warn("Reached max {} increment taps; cart total still below {} SAR.", maxTaps, minTotal);
    }

    /** Read the cart total amount (VAT incl.); -1 when unreadable. */
    @Step("Read the cart total amount")
    public double getCartTotal() {
        for (WebElement e : waitUtils.findQuick(TOTAL_AMOUNT, 5)) {
            double n = extractNumber(safeText(e));
            if (n >= 0) {
                return n;
            }
        }
        return -1;
    }

    private String safeText(WebElement e) {
        try {
            String t = e.getText();
            return t == null ? "" : t.trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private double extractNumber(String text) {
        if (text == null || text.isEmpty()) {
            return -1;
        }
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("[0-9]+(?:[.,][0-9]+)?").matcher(text.replace(",", ""));
        if (m.find()) {
            try {
                return Double.parseDouble(m.group());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
