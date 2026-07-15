package com.urpay.pages.dmp.promo;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Checkout page — promo-code section (LANE E).
 *
 * Migrated from the Katalon custom keyword {@code com.uspace.dmp.CheckoutPageProcessor}
 * (Keywords/com/uspace/dmp/CheckoutPageProcessor.groovy) and the promo-code object repository
 * ({@code Object Repository/android/DMP/checkoutPage/*}).
 *
 * The checkout page is reached after tapping "Buy now" on a product (the revamped flow shows the
 * "Order details" checkout screen carrying the promo-code text field + "Place Order" CTA).
 *
 * SOLID / project rules:
 *   - ZERO Thread.sleep() — waits via inherited WaitUtils helpers only.
 *   - NO assertions (returns values for the test to verify).
 *   - Locators are private fields at the class top; text/testID anchored (accessibility id first).
 */
public class DmpCheckoutPromoPage extends BasePage {

    // Promo-code text field. OR: checkoutPage/promoCodeTextField = testID-input-direct-PromoCode.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-input-direct-PromoCode' "
            + "or @name='testID-input-direct-PromoCode' or @label='testID-input-direct-PromoCode']")
    private WebElement promoCodeField;

    // Apply button. OR: checkoutPage/applyPromoCodeButton = (//*[@content-desc='testID-secondary-action-main'])[2],
    // with a visible-text fallback ("Apply").
    @AndroidFindBy(xpath = "(//*[@content-desc='testID-secondary-action-main'])[2] "
            + "| //*[@text='Apply' or @text='apply']")
    private WebElement applyButton;

    // Remove button. OR: checkoutPage/removePromoCodeButton = testID-cancel-action-main.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-cancel-action-main' "
            + "or @name='testID-cancel-action-main' or @label='testID-cancel-action-main']")
    private WebElement removeButton;

    // Success message. OR: checkoutPage/promoCodeSuccessTextView = testID-TextV2.edf121f4-...
    private static final By SUCCESS_MESSAGE = AppiumBy.xpath(
            "//*[@content-desc='testID-TextV2.edf121f4-5bad-4d1d-97f6-7c69ab2ff603' "
            + "or @name='testID-TextV2.edf121f4-5bad-4d1d-97f6-7c69ab2ff603']");

    // Failure message. OR: checkoutPage/promoCodeFailureTextView = testID-TextV2.bc3339f4-...
    private static final By FAILURE_MESSAGE = AppiumBy.xpath(
            "//*[@content-desc='testID-TextV2.bc3339f4-3e1b-468d-a698-6b5604db4fd1' "
            + "or @name='testID-TextV2.bc3339f4-3e1b-468d-a698-6b5604db4fd1']");

    // Remove button locator (for existence checks without triggering PageFactory waits).
    // Primary testID is testID-cancel-action-main; broadened with a visible-text fallback ("Remove")
    // since some builds relabel the applied-code control.
    private static final By REMOVE_BUTTON = AppiumBy.xpath(
            "//*[@content-desc='testID-cancel-action-main' or @name='testID-cancel-action-main' "
            + "or @text='Remove' or @text='remove']");

    // Total amount (VAT incl.) label before a promo is applied. OR: totalPriceIncludeVatTextViewInCheckout
    // = testID-amount-label-0.
    private static final By TOTAL_BEFORE_PROMO = AppiumBy.xpath(
            "//*[@content-desc='testID-amount-label-0' or @name='testID-amount-label-0']");

    // Total amount (VAT incl.) label after a promo is applied. OR:
    // checkoutTotalPriceWithVATAfterPromoTextView = testID-amount-label-4.
    private static final By TOTAL_AFTER_PROMO = AppiumBy.xpath(
            "//*[@content-desc='testID-amount-label-4' or @name='testID-amount-label-4']");

    // Checkout-page marker (revamped screen title / total label present once loaded).
    private static final By CHECKOUT_LOADED = AppiumBy.xpath(
            "//*[@content-desc='testID-input-direct-PromoCode' "
            + "or @content-desc='testID-amount-label-0' or @text='Order details']");

    @Step("Check the DMP checkout (promo) page is loaded")
    public boolean isLoaded() {
        return isPresent(CHECKOUT_LOADED, 40);
    }

    /** Clear then type the promo code and hide the keyboard (Katalon toSetPromoCode). */
    @Step("Enter promo code {code}")
    public void setPromoCode(String code) {
        WebElement field = waitUtils.waitForClickable(promoCodeField, 30);
        field.clear();
        field.sendKeys(code);
        hideKeyboard();
    }

    /** Tap the total label (to defocus the field) then the Apply button (Katalon toApplyPromoCode). */
    @Step("Apply the entered promo code")
    public void applyPromoCode() {
        tapTotalLabel();
        tap(applyButton, 30);
    }

    /** Enter and apply a promo code in one step. */
    @Step("Set and apply promo code {code}")
    public void setAndApplyPromoCode(String code) {
        setPromoCode(code);
        applyPromoCode();
    }

    /** Tap the total label then the Remove button (Katalon toRemovePromoCode). */
    @Step("Remove the applied promo code")
    public void removePromoCode() {
        tapTotalLabel();
        tap(removeButton, 20);
    }

    @Step("Check the Remove-promo button is displayed")
    public boolean isRemoveButtonDisplayed() {
        boolean present = isPresent(REMOVE_BUTTON, 8);
        if (!present) {
            // Diagnostic: capture the applied-promo checkout state so the real Remove-control
            // testID can be identified for locator hardening.
            dumpPageSource("promo-applied-no-remove");
        }
        return present;
    }

    /** Read the promo-code success message (empty if not shown). */
    @Step("Read the promo-code success message")
    public String getSuccessMessage() {
        return readText(SUCCESS_MESSAGE, 15);
    }

    /**
     * Wait for the promo-code result to render after Apply, then return the SUCCESS message text.
     * The success message can take several seconds to appear after the Apply network round-trip, so
     * poll until either the success or failure message renders (up to {@code timeoutSec}) instead of
     * a single read that can fire before the text mounts. Returns the success text once it appears,
     * or "" if a failure/validation message rendered instead (or nothing appeared in time).
     * No Thread.sleep — each {@code readText} poll provides the wait interval (WaitUtils).
     */
    @Step("Wait up to {timeoutSec}s for the promo-code result message to appear")
    public String waitForSuccessMessage(long timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        do {
            String success = readText(SUCCESS_MESSAGE, 2);
            if (!success.isEmpty()) {
                return success;
            }
            if (!readText(FAILURE_MESSAGE, 1).isEmpty()) {
                return ""; // a failure/validation message rendered instead of success
            }
        } while (System.currentTimeMillis() < deadline);
        return "";
    }

    /** Read the promo-code failure message (empty if not shown). */
    @Step("Read the promo-code failure message")
    public String getFailureMessage() {
        return readText(FAILURE_MESSAGE, 15);
    }

    /**
     * Read whichever promo-code message (success or failure) is shown — mirrors the Katalon
     * {@code getPromoCodeSuccessFailureMessage} used by the Expired/Invalid cases.
     */
    @Step("Read the promo-code result message (success or failure)")
    public String getResultMessage() {
        String failure = readText(FAILURE_MESSAGE, 15);
        if (!failure.isEmpty()) {
            return failure;
        }
        return readText(SUCCESS_MESSAGE, 5);
    }

    /** Total amount (VAT incl.) shown before applying a promo. -1 when unreadable. */
    @Step("Read the total (VAT incl.) before promo")
    public double getTotalBeforePromo() {
        return extractNumber(readText(TOTAL_BEFORE_PROMO, 15));
    }

    /** Total amount (VAT incl.) shown after applying a promo. -1 when unreadable. */
    @Step("Read the total (VAT incl.) after promo")
    public double getTotalAfterPromo() {
        return extractNumber(readText(TOTAL_AFTER_PROMO, 15));
    }

    // ── helpers ──────────────────────────────────────────────────────

    /** Tap the total-amount label to defocus the promo field before Apply/Remove (Katalon does this). */
    private void tapTotalLabel() {
        List<WebElement> total = waitUtils.findQuick(TOTAL_BEFORE_PROMO, 5);
        if (!total.isEmpty()) {
            try {
                total.get(0).click();
            } catch (Exception ignored) {
                // best-effort defocus; not fatal.
            }
        }
    }

    private String readText(By locator, long timeoutSec) {
        List<WebElement> found = waitUtils.findQuick(locator, timeoutSec);
        if (found.isEmpty()) {
            return "";
        }
        String text = found.get(0).getText();
        return text == null ? "" : text.trim();
    }

    /** Parse the first decimal number out of a price string (e.g. "SAR 123.45" → 123.45). */
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
