package com.urpay.pages.dmp.orders;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.pages.auth.OtpPage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Checkout — "Redeem Mokafaa Points" section, for the LANE D Mokafaa-points order journey.
 *
 * Migrated from Katalon Test Cases/DMP/Cart/ToValidateRedeemMokafaaPoints
 * (Object Repository/android/DMP/checkoutPage/MokafaaPoints/*):
 *   RedeemMokafaaPointsBtn  → accessibility testID-Images.fa200edb-8ce0-463f-a141-a0ca55e2010e
 *   MokfaaPointsQuantityInput → accessibility testID-TextInput.9c00e5ac-dbc4-4ca8-b9bf-e7e8d29e4e29
 *   NextBtn / ConfirmBtn / DoneBtnThanksScreen → ReactText buttons (text 'Next' / 'Confirm' / 'Done')
 *   thankyouText → testID-Text.7e9fc765-… or contains(@text,'Thank')
 *
 * Redeeming Mokafaa points is its OWN transaction (points → wallet balance) that requires a payment
 * verification code (Keypad.fillVerificationCode). The migrated flow enters the amount, advances
 * Next → Confirm, enters the OTP via the proven {@link OtpPage}, then taps Done on the thank-you
 * screen.
 *
 * The Next / Confirm / Done buttons are React-Native ReactText nodes whose visible text child is
 * NOT clickable (same pattern as the checkout Place Order button), so this page taps them at their
 * centre coordinates rather than via {@code element.click()}.
 */
public class DmpMokafaaCheckoutPage extends BasePage {

    // "Redeem Mokafaa Points" entry on the checkout page.
    @AndroidFindBy(accessibility = "testID-Images.fa200edb-8ce0-463f-a141-a0ca55e2010e")
    private WebElement redeemMokafaaButton;

    // Points-quantity input on the redemption screen.
    @AndroidFindBy(accessibility = "testID-TextInput.9c00e5ac-dbc4-4ca8-b9bf-e7e8d29e4e29")
    private WebElement pointsQuantityInput;

    private static final By REDEEM_BTN =
            AppiumBy.accessibilityId("testID-Images.fa200edb-8ce0-463f-a141-a0ca55e2010e");
    private static final By POINTS_INPUT =
            AppiumBy.accessibilityId("testID-TextInput.9c00e5ac-dbc4-4ca8-b9bf-e7e8d29e4e29");
    private static final By NEXT_BTN = AppiumBy.xpath("//*[@text='Next']");
    private static final By CONFIRM_BTN = AppiumBy.xpath("//*[@text='Confirm']");
    private static final By DONE_BTN = AppiumBy.xpath("//*[@text='Done']");
    private static final By THANK_YOU = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5' or contains(@text,'Thank')]");

    @Step("Check the Redeem Mokafaa Points entry is available on Checkout")
    public boolean isRedeemAvailable(long timeoutSec) {
        return isDisplayed(redeemMokafaaButton, timeoutSec);
    }

    /**
     * Redeem {@code pointsAmount} Mokafaa points into the wallet: open the redemption screen, enter
     * the amount, advance Next → Confirm, enter the payment verification code, then tap Done on the
     * thank-you screen.
     *
     * @param pointsAmount     the Mokafaa points quantity to redeem (config-driven).
     * @param verificationCode the payment verification code (config {@code dmpd.mokafaa.verificationCode}).
     */
    @Step("Redeem {pointsAmount} Mokafaa points (verification code entered)")
    public void redeemPoints(String pointsAmount, String verificationCode) {
        if (!isDisplayed(redeemMokafaaButton, 20)) {
            dumpPageSource("checkout-noRedeemMokafaaButton");
        }
        tap(redeemMokafaaButton, 20);
        type(pointsQuantityInput, pointsAmount);
        hideKeyboard();

        tapByCoordinates(NEXT_BTN, 20);
        tapByCoordinates(CONFIRM_BTN, 20);

        new OtpPage().enterOtp(verificationCode);

        waitUtils.isPresent(THANK_YOU, 30);
        tapByCoordinates(DONE_BTN, 20);
    }

    @Step("Check the Mokafaa redemption Thank You screen is shown")
    public boolean isThankYouShown(long timeoutSec) {
        return isPresent(THANK_YOU, timeoutSec);
    }

    /** Tap a React-Native text button at its centre coordinates (its visible text is non-clickable). */
    private void tapByCoordinates(By locator, long timeoutSec) {
        try {
            WebElement el = waitUtils.waitForVisible(locator, timeoutSec);
            org.openqa.selenium.Rectangle r = el.getRect();
            tapAtCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
        } catch (Exception e) {
            log.debug("Coordinate tap on {} failed, using element click: {}", locator, e.getMessage());
            tap(locator, timeoutSec);
        }
    }
}
