package com.urpay.pages.dmp;

import java.time.Duration;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Order Confirmation ("Order Placed!") page — the final screen of the digital
 * marketplace order journey.
 *
 * Migrated from Katalon {@code Test Cases/DMP/Order/ToValidatePaymentOrder}
 * (Object Repository/android/DMP/OrderPlacePage/orderPlaceTextView + doneButton), which
 * verifies the "Order Placed!" text and taps the "Done" button.
 *
 * The DMP checkout/OTP transition intermittently returns a page whose element attributes contain
 * a raw newline, which corrupts the UiAutomator2 JSON response so a normal {@code findElement}
 * throws {@code JsonMappingException}. The tolerant polls below swallow that transient parse error
 * and keep polling until the page settles, instead of treating the first failure as "absent".
 */
public class DmpOrderConfirmationPage extends BasePage {

    // "Done" button (testID-primary--main, or text "Done").
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary--main' or @text='Done']")
    private WebElement doneButton;

    private static final By OTP_FIELD_0 = AppiumBy.accessibilityId("testID-OTP-Input-Field-0");
    private static final By ORDER_PLACED = AppiumBy.xpath(
            "//*[contains(@text,'Order Placed') "
            + "or contains(@content-desc,'testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5')]");

    @Step("Check the order was placed successfully")
    public boolean isOrderPlaced() {
        return waitPresentTolerant(ORDER_PLACED, 30);
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
        if (waitPresentTolerant(OTP_FIELD_0, 45)) {
            platformActions.enterDigits(code);
        } else {
            log.info("No payment OTP screen shown — order auto-confirmed");
        }
    }

    @Step("Tap the 'Done' button")
    public void tapDone() {
        tap(doneButton, 15);
    }

    /**
     * Poll for an element's presence, tolerating the intermittent {@code JsonMappingException}
     * (raw newline in the UiAutomator2 response) by catching every probe exception and retrying
     * on the next poll until the element is found or the timeout elapses.
     */
    private boolean waitPresentTolerant(By locator, long timeoutSec) {
        Function<WebDriver, Boolean> probe = d -> {
            try {
                return !d.findElements(locator).isEmpty();
            } catch (Exception parseErrorOrTransient) {
                return false; // tolerate JSON-parse / transient errors and keep polling
            }
        };
        try {
            return new FluentWait<>((WebDriver) driver)
                    .withTimeout(Duration.ofSeconds(timeoutSec))
                    .pollingEvery(Duration.ofMillis(750))
                    .until(probe);
        } catch (Exception timeout) {
            return false;
        }
    }
}
