package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Card Information page — card number, holder, expiry, CVV with copy buttons.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/cardInformation/
 */
public class CardInfoPage extends BasePage {

    // cardInformation/cardInfoBtn.rs → xpath with content-desc
    @AndroidFindBy(accessibility = "testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.0")
    private WebElement cardInfoButton;

    // cardInformation/cardNumberValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-0")
    private WebElement cardNumberValue;

    // cardInformation/copyBtn_cardNumber.rs
    @AndroidFindBy(accessibility = "testID-secondary-action-main")
    private WebElement copyCardNumberButton;

    // cardInformation/cardHolderName.rs
    @AndroidFindBy(accessibility = "testID-label-value-2")
    private WebElement cardHolderName;

    // cardInformation/cardHolder_copyBtn.rs
    @AndroidFindBy(accessibility = "testID-secondary-action-2")
    private WebElement copyCardHolderButton;

    // cardInformation/cardExpiryDate.rs
    @AndroidFindBy(accessibility = "testID-label-value-4")
    private WebElement expiryDate;

    // cardInformation/expiryDate_copyBtn.rs
    @AndroidFindBy(accessibility = "testID-secondary-action-4")
    private WebElement copyExpiryButton;

    // cardInformation/ccvValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-6")
    private WebElement cvvValue;

    // cardInformation/ccv_copyBtn.rs
    @AndroidFindBy(accessibility = "testID-secondary-action-6")
    private WebElement copyCvvButton;

    // Notification message for copy confirmations
    @AndroidFindBy(accessibility = "testID-notification-message")
    private WebElement notificationMessage;

    // ══════════════════════════════════════════════════
    //  ENTRY
    // ══════════════════════════════════════════════════

    @Step("Tap Card Info button")
    public void tapCardInfo() {
        tap(cardInfoButton);
    }

    // ══════════════════════════════════════════════════
    //  COPY ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Copy Card Number")
    public void tapCopyCardNumber() {
        tap(copyCardNumberButton);
    }

    @Step("Tap Copy Card Holder Name")
    public void tapCopyCardHolder() {
        tap(copyCardHolderButton);
    }

    @Step("Tap Copy Expiry Date")
    public void tapCopyExpiry() {
        tap(copyExpiryButton);
    }

    @Step("Tap Copy CVV")
    public void tapCopyCvv() {
        tap(copyCvvButton);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS (no assertions — test decides)
    // ══════════════════════════════════════════════════

    public String getCardNumber() {
        return getText(cardNumberValue);
    }

    public String getCardHolderName() {
        return getText(cardHolderName);
    }

    public String getExpiryDate() {
        return getText(expiryDate);
    }

    public String getCvv() {
        return getText(cvvValue);
    }

    public String getNotificationMessage() {
        waitUtils.waitForVisible(
                AppiumBy.accessibilityId("testID-notification-message"), 10);
        return getText(notificationMessage);
    }

    /** Read notification text if visible; returns null if not present or auto-dismissed */
    public String readNotificationIfVisible() {
        try {
            org.openqa.selenium.WebElement el = waitUtils.waitForVisible(
                    AppiumBy.accessibilityId("testID-notification-message"), 5);
            return el.getText();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isCardInfoLoaded() {
        return isPresent(AppiumBy.accessibilityId("testID-label-value-0"), 10);
    }

    public boolean isNotificationVisible() {
        return isPresent(AppiumBy.accessibilityId("testID-notification-message"), 5);
    }
}
