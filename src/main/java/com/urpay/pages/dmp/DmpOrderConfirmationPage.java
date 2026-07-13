package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Order Confirmation ("Order Placed!") page — the final screen of the digital
 * marketplace order journey.
 *
 * Migrated from Katalon {@code Test Cases/DMP/Order/ToValidatePaymentOrder}
 * (Object Repository/android/DMP/OrderPlacePage/orderPlaceTextView + doneButton), which
 * verifies the "Order Placed!" text and taps the "Done" button.
 */
public class DmpOrderConfirmationPage extends BasePage {

    // "Order Placed!" success marker (stable text anchor).
    @AndroidFindBy(xpath = "//*[contains(@text,'Order Placed') "
            + "or contains(@content-desc,'testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5')]")
    private WebElement orderPlacedText;

    // "Done" button (testID-primary--main, or text "Done").
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary--main' or @text='Done']")
    private WebElement doneButton;

    // Payment verification-code (OTP) first field — shown when the order requires OTP confirmation.
    @AndroidFindBy(accessibility = "testID-OTP-Input-Field-0")
    private WebElement otpField0;

    @Step("Check the order was placed successfully")
    public boolean isOrderPlaced() {
        return isDisplayed(orderPlacedText, 30);
    }

    /**
     * Enter the payment verification code if the OTP screen is shown. Mirrors Katalon
     * {@code Keypad.fillVerificationCode}: wait for the OTP field, then press the native digit
     * keys WITHOUT clicking the field first (clicking the React-Native OTP container defocuses the
     * auto-focused input, so the digits would not register). Orders that auto-confirm without OTP
     * simply skip this step.
     */
    @Step("Enter the payment verification code if the OTP screen is shown")
    public void enterPaymentOtpIfPresent(String code) {
        if (isDisplayed(otpField0, 20)) {
            platformActions.enterDigits(code);
        } else {
            log.info("No payment OTP screen shown — order auto-confirmed");
        }
    }

    @Step("Tap the 'Done' button")
    public void tapDone() {
        tap(doneButton, 15);
    }
}
