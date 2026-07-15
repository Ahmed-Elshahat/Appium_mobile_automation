package com.urpay.pages.dmp.gift;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP "Send as E-Gift" screens for the LANE D gift-order journey.
 *
 * Migrated from Katalon (DMP_1 branch) Test Cases/DMP/E-Gift, driven by the suite
 * "Validate placing order as a gift and check it sent to gift receiver":
 *   E-Gift/SendAsEgift    → tap "Send as gift" on the cart screen.
 *   E-Gift/AddNewNumber   → Add New Number → type the receiver mobile → Next → Done → Add.
 *   E-Gift/SelectGiftCard → tap the primary "Select gift card" call-to-action.
 *   E-Gift/EnterGiftMessage → type the gift message → Next (lands on the pay/confirm screen).
 *   Order/PayConfirmBtnForGift → Confirm → payment verification code → "Order Placed!" → Done.
 *
 * LANE D-owned page (namespace {@code pages/dmp/gift}). Locators extracted from the E-Gift
 * Object Repository .rs files; the revamped primary buttons (whose middle testID segment is
 * hashed on the cloud build) are anchored on visible text first with a structural
 * {@code testID-primary-…-main} fallback (the proven AutoTopup obfuscation pattern).
 */
public class DmpGiftPage extends BasePage {

    // "Send as gift" action on the cart screen (Katalon E-Gift/SendAsEgift). Anchored on the
    // specific .1-suffixed View testID and the visible text — the bare IconView testID
    // (b922d74f) is a REUSED generic SVG icon (it also renders on Store promo banners), so it is
    // deliberately excluded to avoid false-positive matches.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-View.8b1e609e-e9e2-422b-9ba8-78d45ceb332b.1' "
            + "or @text='Send as gift' or contains(@text,'Send as gift')]")
    private WebElement sendAsGiftButton;

    // "Add New Number" button on the recipient screen (Katalon AddNewNumberButton,
    // testID-secondary-action-main).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-action-main' "
            + "or @text='Add New Number' or contains(@text,'Add New')]")
    private WebElement addNewNumberButton;

    // Receiver mobile-number input (Katalon AddNumberTextField / EnterPhoneNumberTextView).
    @AndroidFindBy(xpath = "//android.widget.EditText")
    private WebElement phoneNumberField;

    // "Next" primary on the add-number screen (Katalon NextButton,
    // testID-primary-onCheckContactNumber-main).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onCheckContactNumber-main' "
            + "or @text='Next' "
            + "or (starts-with(@content-desc,'testID-primary-') "
            + "and substring(@content-desc,string-length(@content-desc)-4)='-main')]")
    private WebElement checkNumberNextButton;

    // "Add" confirm after entering the number (Katalon AddBtnAfteraddnumber, ReactText c8f08fb6 "Add").
    @AndroidFindBy(xpath = "//android.widget.TextView[@content-desc="
            + "'testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Add']")
    private WebElement addNumberConfirmButton;

    // Primary "Select gift card" call-to-action (Katalon SelectGiftCardButton, testID-primary--main).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary--main' or @text='Next' or @text='Continue' "
            + "or (starts-with(@content-desc,'testID-primary-') "
            + "and substring(@content-desc,string-length(@content-desc)-4)='-main')]")
    private WebElement selectGiftCardButton;

    // Gift-message input (Katalon GiftMessage, testID-TextInput.694e392a-…).
    @AndroidFindBy(accessibility = "testID-TextInput.694e392a-d744-415d-ba6b-6d39e78dacdc")
    private WebElement giftMessageField;

    // "Next" on the gift-message screen (Katalon nextbtn, ReactText c8f08fb6 "Next").
    @AndroidFindBy(xpath = "//android.widget.TextView[@content-desc="
            + "'testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Next']")
    private WebElement messageNextButton;

    // Pay/confirm ("Order Place") screen "Confirm" button (Katalon OrderPlacePage/confirmBtn,
    // testID-primary--main / text "Confirm").
    @AndroidFindBy(xpath = "//*[@text='Confirm' or @content-desc='testID-primary--main' "
            + "or (starts-with(@content-desc,'testID-primary-') "
            + "and substring(@content-desc,string-length(@content-desc)-4)='-main')]")
    private WebElement confirmButton;

    private static final By CONFIRM_SCREEN =
            AppiumBy.xpath("//*[@text='Confirm' or contains(@text,'Order summary') "
                    + "or contains(@text,'Order details') or contains(@text,'Total')]");

    // Add-to-cart control on the product details page (Katalon Cartdraw): the button immediately
    // preceding the wide "Buy now" CTA (testID-TouchableOpacity.1e77c248-…). Tapping "Buy now"
    // itself is a no-op for a digital product (leaves the cart empty), so the gift flow adds to the
    // cart via this button instead.
    private static final By CARTDRAW = AppiumBy.xpath(
            "//*[@content-desc='testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061']"
            + "/preceding-sibling::*[1]");
    // Cart toolbar icon (Katalon CarButtonInsideproduct, testID-right-icon-0).
    private static final By VIEW_CART = AppiumBy.accessibilityId("testID-right-icon-0");
    // Empty-cart marker (self-heal signal: the add-to-cart tap did not register).
    private static final By CART_EMPTY = AppiumBy.xpath(
            "//*[contains(@text,'Cart is Empty') or contains(@text,'cart is empty')]");

    // "Send as gift" presence check (By mirror of {@link #sendAsGiftButton}).
    private static final By SEND_AS_GIFT = AppiumBy.xpath(
            "//*[@content-desc='testID-View.8b1e609e-e9e2-422b-9ba8-78d45ceb332b.1' "
            + "or @text='Send as gift' or contains(@text,'Send as gift')]");

    @Step("Add the product to the cart via the cart button (Katalon Cartdraw)")
    public void addToCart() {
        tapCartdraw();
    }

    /** Tap the Cartdraw add-to-cart button at its centre coordinates (reliable RN onPress trigger). */
    private void tapCartdraw() {
        try {
            WebElement btn = waitUtils.waitForVisible(CARTDRAW, 20);
            org.openqa.selenium.Rectangle r = btn.getRect();
            tapAtCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
        } catch (Exception e) {
            log.debug("Coordinate tap on Cartdraw failed, using element click: {}", e.getMessage());
            tap(CARTDRAW, 20);
        }
    }

    @Step("Open the cart (cart toolbar icon), re-adding the item if the cart is empty")
    public void openCart() {
        tap(VIEW_CART, 20);
        // Self-heal the intermittent RN add-to-cart no-op: if the cart is empty, go back to the
        // product and re-add before continuing to the gift screens.
        if (isPresent(CART_EMPTY, 3)) {
            log.warn("Cart empty after add-to-cart — re-adding via Cartdraw");
            pressBack();
            tapCartdraw();
            tap(VIEW_CART, 20);
        }
    }

    @Step("Tap 'Send as gift' on the cart screen")
    public void tapSendAsGift() {
        // On the current build the digital product's "Buy now" goes straight to Checkout → payment
        // OTP with no gift option, and the cart-based "Send as gift" the Katalon suite relies on is
        // absent for digital products. Surface that as an actionable diagnosis instead of an opaque
        // 20s locator timeout.
        if (!isPresent(SEND_AS_GIFT, 20)) {
            dumpPageSource("gift-noSendAsGift");
            throw new org.openqa.selenium.NoSuchElementException(
                    "'Send as gift' was not found on the cart/checkout for this product. On the current "
                    + "build the digital product 'Buy now' navigates straight to Checkout → payment OTP "
                    + "with no gift option — the cart-based gift path the Katalon suite relies on appears "
                    + "to have been removed/changed for digital products. Needs the current gift entry "
                    + "point (or a giftable product SKU) from the app team.");
        }
        tap(sendAsGiftButton, 20);
    }

    @Step("Add the gift-receiver mobile number '{receiverNumber}'")
    public void addReceiverNumber(String receiverNumber) {
        tap(addNewNumberButton, 20);
        type(phoneNumberField, receiverNumber);
        tap(checkNumberNextButton, 20);
        platformActions.dismissKeyboard();
        tap(addNumberConfirmButton, 20);
        platformActions.dismissKeyboard();
    }

    @Step("Select the gift card")
    public void selectGiftCard() {
        tap(selectGiftCardButton, 20);
    }

    @Step("Enter the gift message '{message}' and tap Next")
    public void enterGiftMessageAndNext(String message) {
        type(giftMessageField, message);
        platformActions.dismissKeyboard();
        tap(messageNextButton, 20);
    }

    @Step("Check the gift pay/confirm screen is reached")
    public boolean isPayConfirmScreenDisplayed() {
        return isPresent(CONFIRM_SCREEN, 25);
    }

    /**
     * Complete payment for the gift order — migrated from Katalon Order/PayConfirmBtnForGift:
     * tap Confirm, enter the payment verification code, then verify "Order Placed!" and tap Done.
     *
     * @param verificationCode the payment verification code (config {@code dmpd.gift.verificationCode}).
     * @return the Order Confirmation ("Order Placed!") page for the test to assert on.
     */
    @Step("Confirm and pay for the gift order")
    public DmpOrderConfirmationPage payAndConfirm(String verificationCode) {
        tap(confirmButton, 20);
        DmpOrderConfirmationPage confirmation = new DmpOrderConfirmationPage();
        confirmation.enterPaymentOtpIfPresent(verificationCode);
        return confirmation;
    }
}
