package com.urpay.pages.payments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Top Up Settings page — auto top-up configuration and card management.
 *
 * Covers:
 *   - Auto top-up setup wizard
 *   - Top-up / Lower limit configuration
 *   - Auto top-up enable/disable toggle
 *   - Delete auto top-up card
 *   - Manage saved top-up cards (from profile)
 *
 * Katalon source:
 *   Object Repository/{platform}/PaymentAndCards/TopUpUsingBankCard/
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/openAutoToUpCardSettings
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/setUpAutoTopUpForFirstTime
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/AutoTopUpDisableAndEnable
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/DeleteAutoTopUpCard
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/DeleteTopupCard
 */
public class TopUpSettingsPage extends BasePage {

    // ── Top-Up Settings Entry ─────────────────────────
    // Visible after scrolling down on the Add Money screen
    private static final By TOP_UP_SETTINGS_BTN = AppiumBy.xpath(
            "//*[@text='Top Up Settings' or @text='Top-Up Settings'"
            + " or contains(@text,'Settings')]");

    // ── Auto Top-Up Tab / Arrow ───────────────────────
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'testID-TouchableWithoutFeedback.af5035ae')][1]")
    @iOSXCUITFindBy(xpath = "//*[contains(@name,'testID-TouchableWithoutFeedback.af5035ae')][1]")
    private WebElement autoTopUpTab;

    @AndroidFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.undefined")
    @iOSXCUITFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.undefined")
    private WebElement autoTopUpArrow;

    // ── Setup Auto Top-Up ─────────────────────────────
    @AndroidFindBy(accessibility = "testID-primary-onSetTopup-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onSetTopup-main")
    private WebElement setAutoTopUpButton;

    @AndroidFindBy(accessibility = "testID-primary-onSubmit-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onSubmit-main")
    private WebElement nextAutoTopUpCreationButton;

    // ── Limit Configuration ───────────────────────────
    // First input-container = top-up limit, second = lower limit
    private static final By SET_TOPUP_LIMIT_BTN = AppiumBy.xpath(
            "(//*[@content-desc='testID-input-container-undefined'])[1]");

    private static final By SET_LOWER_LIMIT_BTN = AppiumBy.xpath(
            "(//*[@content-desc='testID-input-container-undefined'])[2]");

    @AndroidFindBy(xpath = "//*[contains(@content-desc,'testID-TextInput.')]")
    @iOSXCUITFindBy(xpath = "//*[contains(@name,'testID-TextInput.')]")
    private WebElement limitInputField;

    private static final By SAVE_BTN = AppiumBy.xpath(
            "//*[@text='Save' or @text='SAVE']");

    // ── Confirm Auto Top-Up ───────────────────────────
    @AndroidFindBy(accessibility = "testID-primary-onSave-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onSave-main")
    private WebElement nextSaveLimitsButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-check-box-main']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-check-box-main']")
    private WebElement acceptCheckBox;

    @AndroidFindBy(accessibility = "testID-primary-onConfirm-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirm-main")
    private WebElement confirmAutoTopUpButton;

    @AndroidFindBy(accessibility = "testID-primary-action-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-action-main")
    private WebElement doneAutoTopUpButton;

    // ── Auto Top-Up Toggle ────────────────────────────
    @AndroidFindBy(accessibility = "testID-switcher-undefined")
    @iOSXCUITFindBy(accessibility = "testID-switcher-undefined")
    private WebElement autoTopUpToggle;

    // ── Delete Auto Top-Up ────────────────────────────
    @AndroidFindBy(accessibility = "testID-primary-onPressCancel-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPressCancel-main")
    private WebElement deleteAutoTopUpButton;

    @AndroidFindBy(accessibility = "testID-primary-onCancelAutoTopup-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onCancelAutoTopup-main")
    private WebElement confirmDeleteAutoTopUpButton;

    // ── Delete question / No Auto Top-Up state ────────
    private static final By DELETE_AUTO_TOPUP_QUESTION = AppiumBy.xpath(
            "//*[@text='Delete Auto Top-up?' or @text='Delete Auto Top-Up?']");

    private static final By NO_AUTO_TOPUP_BANNER = AppiumBy.xpath(
            "//*[contains(@text,'No auto') or contains(@text,'no auto')]");

    // ── Profile → Manage Cards ────────────────────────
    private static final By PROFILE_AVATAR = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-avatar-')]");

    private static final By MANAGE_TOPUP_CARDS_TAB = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-TouchableWithoutFeedback.af5035ae')"
            + " and contains(@content-desc,'.3')]"
            + " | //*[@text='Manage Top Up Cards' or @text='Manage Top-Up Cards']");

    private static final By DELETE_TOPUP_CARD_BTN = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-TouchableOpacity.') and contains(@text,'Delete')]"
            + " | //*[@text='Delete Card']");

    private static final By DELETE_CARD_CONFIRM_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary--main']"
            + " | //*[@text='Delete' or @text='Confirm']");

    // ── Notification Message ──────────────────────────
    @AndroidFindBy(accessibility = "testID-notification-message")
    @iOSXCUITFindBy(accessibility = "testID-notification-message")
    private WebElement notificationMessage;

    // ── Back Button ───────────────────────────────────
    @AndroidFindBy(accessibility = "testID-left-icon-back")
    @iOSXCUITFindBy(accessibility = "testID-left-icon-back")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Tap Top Up Settings button")
    public void tapTopUpSettings() {
        tap(TOP_UP_SETTINGS_BTN);
    }

    @Step("Tap Auto Top-Up tab")
    public void tapAutoTopUpTab() {
        tap(autoTopUpTab);
    }

    @Step("Tap Auto Top-Up arrow")
    public void tapAutoTopUpArrow() {
        tap(autoTopUpArrow);
    }

    @Step("Tap back button")
    public void tapBack() {
        tap(backButton);
    }

    // ══════════════════════════════════════════════════
    //  AUTO TOP-UP SETUP
    // ══════════════════════════════════════════════════

    @Step("Tap Set Auto Top-Up button")
    public void tapSetAutoTopUp() {
        tap(setAutoTopUpButton);
    }

    @Step("Tap Next to create auto top-up")
    public void tapNextAutoTopUpCreation() {
        tap(nextAutoTopUpCreationButton);
    }

    // ══════════════════════════════════════════════════
    //  LIMIT CONFIGURATION
    // ══════════════════════════════════════════════════

    @Step("Tap Set Top-Up Limit button")
    public void tapSetTopUpLimit() {
        tap(SET_TOPUP_LIMIT_BTN);
    }

    @Step("Tap Set Lower Limit button")
    public void tapSetLowerLimit() {
        tap(SET_LOWER_LIMIT_BTN);
    }

    @Step("Enter limit amount: {amount}")
    public void enterLimitAmount(String amount) {
        type(limitInputField, amount);
    }

    @Step("Tap Save button")
    public void tapSave() {
        tap(SAVE_BTN);
    }

    // ══════════════════════════════════════════════════
    //  CONFIRM AUTO TOP-UP
    // ══════════════════════════════════════════════════

    @Step("Tap Next to save auto top-up limits")
    public void tapNextSaveLimits() {
        tap(nextSaveLimitsButton);
    }

    @Step("Tap Accept checkbox")
    public void tapAcceptCheckBox() {
        tap(acceptCheckBox);
    }

    @Step("Tap Confirm Auto Top-Up")
    public void tapConfirmAutoTopUp() {
        tap(confirmAutoTopUpButton);
    }

    @Step("Tap Done (auto top-up setup complete)")
    public void tapDoneAutoTopUp() {
        tap(doneAutoTopUpButton);
    }

    // ══════════════════════════════════════════════════
    //  AUTO TOP-UP TOGGLE
    // ══════════════════════════════════════════════════

    @Step("Toggle auto top-up switch")
    public void tapAutoTopUpToggle() {
        tap(autoTopUpToggle);
    }

    // ══════════════════════════════════════════════════
    //  DELETE AUTO TOP-UP
    // ══════════════════════════════════════════════════

    @Step("Tap Delete Auto Top-Up button")
    public void tapDeleteAutoTopUp() {
        tap(deleteAutoTopUpButton);
    }

    @Step("Tap Confirm Delete Auto Top-Up")
    public void tapConfirmDeleteAutoTopUp() {
        tap(confirmDeleteAutoTopUpButton);
    }

    // ══════════════════════════════════════════════════
    //  PROFILE → MANAGE CARDS
    // ══════════════════════════════════════════════════

    @Step("Tap profile avatar")
    public void tapProfileAvatar() {
        tap(PROFILE_AVATAR);
    }

    @Step("Tap Manage Top Up Cards tab")
    public void tapManageTopUpCards() {
        tap(MANAGE_TOPUP_CARDS_TAB);
    }

    @Step("Tap Delete Top-Up Card button")
    public void tapDeleteTopUpCard() {
        tap(DELETE_TOPUP_CARD_BTN);
    }

    @Step("Confirm delete top-up card")
    public void tapConfirmDeleteCard() {
        tap(DELETE_CARD_CONFIRM_BTN);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public String getNotificationMessage() {
        return getText(notificationMessage);
    }

    public boolean isNotificationVisible(long timeoutSec) {
        return isDisplayed(notificationMessage, timeoutSec);
    }

    @Step("Wait for notification to dismiss")
    public void waitForNotificationToDismiss(long timeoutSec) {
        waitUtils.waitForInvisible(
                AppiumBy.accessibilityId("testID-notification-message"), timeoutSec);
    }

    public String getDeleteAutoTopUpQuestion() {
        return getText(DELETE_AUTO_TOPUP_QUESTION);
    }

    public boolean isDeleteQuestionVisible(long timeoutSec) {
        return isPresent(DELETE_AUTO_TOPUP_QUESTION, timeoutSec);
    }

    public boolean isNoAutoTopUpBannerVisible(long timeoutSec) {
        return isPresent(NO_AUTO_TOPUP_BANNER, timeoutSec);
    }

    public boolean isAutoTopUpToggleVisible(long timeoutSec) {
        return isDisplayed(autoTopUpToggle, timeoutSec);
    }

    public boolean isTopUpSettingsVisible(long timeoutSec) {
        return isPresent(TOP_UP_SETTINGS_BTN, timeoutSec);
    }
}
