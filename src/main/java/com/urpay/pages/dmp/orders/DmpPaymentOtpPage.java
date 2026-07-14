package com.urpay.pages.dmp.orders;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP payment "Verification Code" screen for the LANE C digital-order journeys.
 *
 * The frozen {@link com.urpay.pages.auth.OtpPage} handles the LOGIN OTP; the payment OTP shown
 * after Place Order is a separate screen ("Verification Code" / "Please enter the code sent to …").
 * On the LambdaTest device the RN hidden input does not always auto-focus, so this page clicks the
 * field wrapper to focus before entering the digits (same class of fix used for the login passcode).
 *
 * LANE C-owned (namespace {@code pages/dmp/orders}). Reads the payment OTP screen without modifying
 * the frozen foundation.
 */
public class DmpPaymentOtpPage extends BasePage {

    @AndroidFindBy(accessibility = "testID-OTP-Input-Field-0")
    private WebElement otpField0;

    // Verification Code screen markers.
    private static final By VERIF_TITLE = AppiumBy.xpath(
            "//*[@text='Verification Code' or contains(@text,'enter the code') "
            + "or contains(@text,'Enter the code')]");

    // Clickable OTP field wrapper (focuses the hidden RN input). Field 0 accessibility id, or any
    // OTP input field, or the screen container.
    private static final By OTP_FOCUS_TARGET = AppiumBy.xpath(
            "//*[@content-desc='testID-OTP-Input-Field-0' or starts-with(@content-desc,'testID-OTP-Input-Field') "
            + "or @content-desc='testID-OTP.screen']");

    @Step("Check the payment Verification Code screen is displayed")
    public boolean isVisible(long timeoutSec) {
        return isPresent(VERIF_TITLE, timeoutSec) || isDisplayed(otpField0, timeoutSec);
    }

    @Step("Enter the payment verification code {code}")
    public void enterPaymentOtp(String code) {
        isVisible(25);
        focusInput();
        platformActions.enterDigits(code);
    }

    /** Focus the OTP input so the pressed digits register (RN input does not auto-focus on cloud). */
    private void focusInput() {
        try {
            tap(OTP_FOCUS_TARGET, 15);
        } catch (Exception e) {
            log.debug("OTP focus tap failed, relying on default focus: {}", e.getMessage());
        }
    }
}
