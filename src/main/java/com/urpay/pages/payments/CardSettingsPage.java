package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Card Settings page — online/ATM toggles, card lock, PIN change, cancel card.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/DisableOnlineTransactions/
 *   Object Repository/android/PaymentAndCards/Cards/EnabelAndDisabelATMTransaction/
 *   Object Repository/android/PaymentAndCards/Cards/CardLockObjects/
 *   Object Repository/android/PaymentAndCards/Cards/ChangeCardPinNumber/
 *   Object Repository/android/PaymentAndCards/Cards/CancelCard/
 */
public class CardSettingsPage extends BasePage {

    // ── Card Settings Entry ──
    // DisableOnlineTransactions/CardSettingsButoon.rs
    @AndroidFindBy(accessibility = "testID-View.7c68583b-f554-4e2c-93e1-313ab86e2a68.1")
    private WebElement cardSettingsButton;

    // ── Online Transactions Toggle ──
    // DisableOnlineTransactions/OnlineTransactionsButton.rs
    @AndroidFindBy(accessibility = "testID-Switch.d1583aa8-cf20-49ca-b2b1-f0a3dfccaa24.0")
    private WebElement onlineTransactionsToggle;

    // ── ATM Transaction Toggle ──
    // EnabelAndDisabelATMTransaction/ATMTransactionTogleBtn.rs → xpath: second testID-switcher-undefined
    @AndroidFindBy(xpath = "(//*[@content-desc='testID-switcher-undefined'])[2]")
    private WebElement atmTransactionToggle;

    // ── Card Lock ──
    // CardLockObjects/lockTogleBtn.rs → first testID-switcher-undefined
    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='testID-switcher-undefined']")
    private WebElement lockToggle;

    // CardLockObjects/cardLockPopUpTitle.rs
    @AndroidFindBy(accessibility = "testID-Text.3d7e8fbd-80dc-4be6-ada9-134850e90f25")
    private WebElement lockPopupTitle;

    // CardLockObjects/yesBtn.rs
    @AndroidFindBy(accessibility = "testID-primary-callAPI-main")
    private WebElement lockYesButton;

    // CardLockObjects/noBtn.rs
    @AndroidFindBy(accessibility = "testID-secondary-onClose-main")
    private WebElement lockNoButton;

    // ── Change PIN ──
    // ChangeCardPinNumber/CardSettingsBtn.rs
    @AndroidFindBy(accessibility = "testID-Text.c06eec32-711a-4563-863f-c6157b3485bd.1")
    private WebElement changePinSettingsButton;

    // ChangeCardPinNumber/ChangeCardPinBtn.rs
    @AndroidFindBy(accessibility = "testID-Text.eabea4a3-5063-423e-a2e0-f8e77e38bcc3.1")
    private WebElement changePinButton;

    // ── Cancel Card ──
    // CancelCard/CancelCardButton.rs
    @AndroidFindBy(accessibility = "testID-TouchableOpacity.c1851ba1-d9ba-4dfb-9b4a-0e95ec2c67e3.1")
    private WebElement cancelCardButton;

    // CancelCard/CardCancellationList.rs — dropdown arrow
    @AndroidFindBy(accessibility = "testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.DownArrow")
    private WebElement cancellationDropdown;

    // CancelCard/OtherReasonChoice.rs
    @AndroidFindBy(accessibility = "testID-data-picker-item-3")
    private WebElement otherReasonChoice;

    // CancelCard/CacelCardButton-AfterSelectingReason.rs
    @AndroidFindBy(accessibility = "testID-cancel--main")
    private WebElement confirmCancelButton;

    // CancelCard/NoThanksButton.rs
    @AndroidFindBy(accessibility = "testID-tertiary-noThankButton-main")
    private WebElement noThanksButton;

    // CancelCard/CreateNewCardButton.rs
    @AndroidFindBy(accessibility = "testID-primary-createNewCard-main")
    private WebElement createNewCardAfterCancel;

    // ── Notification Message (shared across toggles/lock/cancel) ──
    @AndroidFindBy(accessibility = "testID-notification-message")
    private WebElement notificationMessage;

    // ══════════════════════════════════════════════════
    //  CARD SETTINGS
    // ══════════════════════════════════════════════════

    @Step("Tap Card Settings button")
    public void tapCardSettings() {
        tap(cardSettingsButton);
    }

    // ══════════════════════════════════════════════════
    //  ONLINE TRANSACTIONS
    // ══════════════════════════════════════════════════

    @Step("Toggle Online Transactions")
    public void tapOnlineTransactionsToggle() {
        tap(onlineTransactionsToggle);
    }

    // ══════════════════════════════════════════════════
    //  ATM TRANSACTIONS
    // ══════════════════════════════════════════════════

    @Step("Toggle ATM Transactions")
    public void tapAtmTransactionToggle() {
        tap(atmTransactionToggle);
    }

    // ══════════════════════════════════════════════════
    //  CARD LOCK / UNLOCK
    // ══════════════════════════════════════════════════

    @Step("Tap Lock/Unlock toggle")
    public void tapLockToggle() {
        tap(lockToggle);
    }

    @Step("Tap Yes on lock popup")
    public void tapLockYes() {
        tap(lockYesButton);
    }

    @Step("Tap No on lock popup")
    public void tapLockNo() {
        tap(lockNoButton);
    }

    public boolean isLockPopupVisible() {
        return isDisplayed(lockPopupTitle, 5);
    }

    // ══════════════════════════════════════════════════
    //  CHANGE PIN
    // ══════════════════════════════════════════════════

    @Step("Tap Change PIN settings button")
    public void tapChangePinSettings() {
        tap(changePinSettingsButton);
    }

    @Step("Tap Change Card PIN button")
    public void tapChangePin() {
        tap(changePinButton);
    }

    // ══════════════════════════════════════════════════
    //  CANCEL CARD
    // ══════════════════════════════════════════════════

    @Step("Tap Cancel Card button")
    public void tapCancelCard() {
        tap(cancelCardButton);
    }

    @Step("Tap Cancellation Reason dropdown")
    public void tapCancellationDropdown() {
        tap(cancellationDropdown);
    }

    @Step("Select 'Other' cancellation reason")
    public void selectOtherReason() {
        tap(otherReasonChoice);
    }

    @Step("Tap Confirm Cancel button")
    public void tapConfirmCancel() {
        tap(confirmCancelButton);
    }

    @Step("Tap No Thanks button after cancellation")
    public void tapNoThanks() {
        tap(noThanksButton);
    }

    @Step("Tap Create New Card after cancellation")
    public void tapCreateNewCardAfterCancel() {
        tap(createNewCardAfterCancel);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public String getNotificationMessage() {
        waitUtils.waitForVisible(
                AppiumBy.accessibilityId("testID-notification-message"), 10);
        return getText(notificationMessage);
    }

    public boolean isNotificationVisible() {
        return isPresent(AppiumBy.accessibilityId("testID-notification-message"), 5);
    }
}
