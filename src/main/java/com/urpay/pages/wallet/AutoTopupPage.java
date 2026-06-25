package com.urpay.pages.wallet;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Auto Top-up page — handles auto top-up plan creation for a kid's wallet.
 *
 * Covers:
 *   - Navigation entry: kid settings → auto top-up
 *   - Plan creation wizard (amount → frequency → day/date/time → confirm → verification code)
 *   - Existing-plan deletion (pre-condition before creating a new plan)
 *   - Reading the configured auto top-up amount
 *
 * Katalon source:
 *   Object Repository/{platform}/WalletVas/FamilyWallet/AutoTopupPage/
 *   Object Repository/{platform}/WalletVas/FamilyWallet/settingScreen/settingsBtn
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopup-NavigateToAutoTopupScreen
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopUp-ToValidatethatDailyAutoTopupCreatedSuccessfully
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopUp-ToValidatethatWeeklyAutoTopupCreatedSuccessfully
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopUp-ToValidatethatMonthlyAutoTopupCreatedSuccessfully
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoToup-ToVerifyAutotopPlanCreatedSuccessfully
 */
public class AutoTopupPage extends BasePage {

    // ── Navigation: kid settings → auto top-up ────────
    // settingScreen/settingsBtn — the gear's exact testID changed from the Katalon
    // 'testID-right-icon-SettingIcon' wrapper to an icon node whose content-desc only
    // contains 'SettingIcon' (e.g. testID-IconView.<uuid>.SettingIcon). Match by substring,
    // mirroring the app team's own locator in Remittance/BeneficiaryManagement/editContactButton.
    // TODO: request a stable testID for the kid-profile settings button.
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'SettingIcon')]")
    @iOSXCUITFindBy(xpath = "//*[contains(@name,'SettingIcon')]")
    private WebElement settingsButton;

    // AutoTopupPage/autoTopupBtn — primary testID verified live on R5CX73LLSPE; the visible
    // "Auto Top-up" label is added as a resilient fallback (mirrors Katalon's multi-attribute
    // xpath) so a slow/drifted remote build that doesn't expose the content-desc in time still
    // resolves. Single OR-xpath only (no union '|' / relative './/' that UiAutomator2 can throw on).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-onAutoTopup-main' or @text='Auto Top-up']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-secondary-onAutoTopup-main' or @label='testID-secondary-onAutoTopup-main' or @name='Auto Top-up']")
    private WebElement autoTopupButton;

    // ── Plan amount ───────────────────────────────────
    // AutoTopupPage/autoTopupAmt — testID carries a dynamic UUID suffix
    // (testID-TextInput.d75fe536-...), so match by the stable testID-TextInput. prefix.
    // TODO: request a stable testID for the auto top-up amount input.
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'testID-TextInput.')]")
    @iOSXCUITFindBy(xpath = "//*[contains(@name,'testID-TextInput.')]")
    private WebElement amountInput;

    // ── Wizard navigation ─────────────────────────────
    // AutoTopupPage/nextBtnSetup
    @AndroidFindBy(accessibility = "testID-primary-action-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-action-main")
    private WebElement nextButton;

    // AutoTopupPage/confirmBtn
    @AndroidFindBy(accessibility = "testID-primary-onConfirmation-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirmation-main")
    private WebElement confirmButton;

    // AutoTopupPage/doneBtn
    @AndroidFindBy(accessibility = "testID-primary-onSubmit-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onSubmit-main")
    private WebElement doneButton;

    // ── Frequency / day / date / time selectors ───────
    // AutoTopupPage/frquencyList
    @AndroidFindBy(accessibility = "testID-multi-select-frequency")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-frequency")
    private WebElement frequencyList;

    // AutoTopupPage/dayList
    @AndroidFindBy(accessibility = "testID-multi-select-recurringDay")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-recurringDay")
    private WebElement dayList;

    // AutoTopupPage/dateList
    @AndroidFindBy(accessibility = "testID-multi-select-recurringDate")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-recurringDate")
    private WebElement dateList;

    // AutoTopupPage/timeList
    @AndroidFindBy(accessibility = "testID-multi-select-time")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-time")
    private WebElement timeList;

    // ── Dropdown options (shared across frequency/day/date/time pickers) ──
    // AutoTopupPage/firstOption
    @AndroidFindBy(accessibility = "testID-search-item-0")
    @iOSXCUITFindBy(accessibility = "testID-search-item-0")
    private WebElement firstOption;

    // AutoTopupPage/secondOption
    @AndroidFindBy(accessibility = "testID-search-item-1")
    @iOSXCUITFindBy(accessibility = "testID-search-item-1")
    private WebElement secondOption;

    // AutoTopupPage/thridOption
    @AndroidFindBy(accessibility = "testID-search-item-2")
    @iOSXCUITFindBy(accessibility = "testID-search-item-2")
    private WebElement thirdOption;

    // ── Verification / result ─────────────────────────
    // AutoTopupPage/actualAmt
    @AndroidFindBy(accessibility = "testID-master-amount-main")
    @iOSXCUITFindBy(accessibility = "testID-master-amount-main")
    private WebElement actualAmount;

    // ── Delete existing plan ──────────────────────────
    // AutoTopupPage/deleteTopup — testID verified locally; the visible "Delete auto Top-up"
    // label is added as a resilient fallback so a slow/drifted remote build that doesn't expose
    // the content-desc still resolves (mirrors the autoTopupButton hardening).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-openDeleteModal-main' or @text='Delete auto Top-up']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-openDeleteModal-main' or @label='testID-primary-openDeleteModal-main' or @name='Delete auto Top-up']")
    private WebElement deleteTopupButton;

    // AutoTopupPage/DeleteTopupBtn
    @AndroidFindBy(accessibility = "testID-primary-onDeleteAutoTopup-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onDeleteAutoTopup-main")
    private WebElement confirmDeleteTopupButton;

    // AutoTopupPage/notificationMsg
    @AndroidFindBy(accessibility = "testID-notification-message")
    @iOSXCUITFindBy(accessibility = "testID-notification-message")
    private WebElement notificationMessage;

    // ── Edit entry points (plan summary view) ─────────
    // AutoTopupPage/editAmtBtn
    @AndroidFindBy(accessibility = "testID-secondary-buttonAction-main")
    @iOSXCUITFindBy(accessibility = "testID-secondary-buttonAction-main")
    private WebElement editAmountButton;

    // AutoTopupPage/editFrequency
    @AndroidFindBy(accessibility = "testID-secondary-buttonAction-1")
    @iOSXCUITFindBy(accessibility = "testID-secondary-buttonAction-1")
    private WebElement editFrequencyButton;

    // AutoTopupPage/editDay
    @AndroidFindBy(accessibility = "testID-secondary-buttonAction-2")
    @iOSXCUITFindBy(accessibility = "testID-secondary-buttonAction-2")
    private WebElement editDayButton;

    // AutoTopupPage/editTime
    @AndroidFindBy(accessibility = "testID-secondary-buttonAction-4")
    @iOSXCUITFindBy(accessibility = "testID-secondary-buttonAction-4")
    private WebElement editTimeButton;

    // ── Plan summary value texts (verification) ───────
    // AutoTopupPage/frequencyTxt
    @AndroidFindBy(accessibility = "testID-subTitle-1")
    @iOSXCUITFindBy(accessibility = "testID-subTitle-1")
    private WebElement frequencyText;

    // AutoTopupPage/everyDayTxt
    @AndroidFindBy(accessibility = "testID-subTitle-2")
    @iOSXCUITFindBy(accessibility = "testID-subTitle-2")
    private WebElement dayText;

    // AutoTopupPage/timeText
    @AndroidFindBy(accessibility = "testID-subTitle-4")
    @iOSXCUITFindBy(accessibility = "testID-subTitle-4")
    private WebElement timeText;

    // ── Dropdown option labels (read selected option text) ──
    // AutoTopupPage/secondOptionTxt — the Katalon nested path
    // (.../android.view.ViewGroup/android.widget.TextView) no longer matches; read any text
    // node under the option container instead.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-search-item-1']//android.widget.TextView")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-search-item-1']//XCUIElementTypeStaticText")
    private WebElement secondOptionText;

    // AutoTopupPage/thridOptionTxt — inner testID carries a dynamic UUID, so read the
    // first text node under the option container instead.
    // TODO: request a stable testID for the third option label.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-search-item-2']//android.widget.TextView")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-search-item-2']//XCUIElementTypeStaticText")
    private WebElement thirdOptionText;

    // ── Enable / Disable plan ─────────────────────────
    // AutoTopupPage/statusToggle
    @AndroidFindBy(accessibility = "testID-switcher-toggle")
    @iOSXCUITFindBy(accessibility = "testID-switcher-toggle")
    private WebElement statusToggle;

    // AutoTopupPage/disableBtn
    @AndroidFindBy(accessibility = "testID-primary-onDisableAutoTopup-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onDisableAutoTopup-main")
    private WebElement disableButton;

    // AutoTopupPage/statusTxt
    @AndroidFindBy(accessibility = "testID-Text.Enabled.Toggle")
    @iOSXCUITFindBy(accessibility = "testID-Text.Enabled.Toggle")
    private WebElement statusText;

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    // Family Wallets list marker (kid card) — used to detect whether we are on the
    // family member list (after a plan is created the app returns here) vs the kid profile.
    private static final org.openqa.selenium.By FAMILY_MEMBER_CARD =
            AppiumBy.accessibilityId("testID-data-0");

    // Auto Top-up screen markers (raw By for fast checks, bypassing the PageFactory wait):
    //   plan view shows the delete entry point; creation form shows the amount text input.
    private static final org.openqa.selenium.By PLAN_DELETE_MARKER =
            AppiumBy.xpath("//*[@content-desc='testID-primary-openDeleteModal-main' or @text='Delete auto Top-up']");
    private static final org.openqa.selenium.By AMOUNT_INPUT_MARKER =
            AppiumBy.xpath("//*[contains(@content-desc,'testID-TextInput.')]");

    /** Quick check: are we on the Family Wallets list (kid card visible)? */
    public boolean isFamilyListShown() {
        return isPresent(FAMILY_MEMBER_CARD, 3);
    }

    /** Quick check: are we already on the Auto Top-up screen (plan view or creation form)? */
    public boolean isAutoTopupScreenShown() {
        return isPresent(PLAN_DELETE_MARKER, 2) || isPresent(AMOUNT_INPUT_MARKER, 2);
    }

    @Step("Tap kid settings button")
    public void tapSettings() {
        tap(settingsButton, 30);
    }

    @Step("Tap Auto Top-up button")
    public void tapAutoTopup() {
        tap(autoTopupButton, 30);
    }

    // ══════════════════════════════════════════════════
    //  PLAN CREATION WIZARD
    // ══════════════════════════════════════════════════

    @Step("Enter auto top-up amount: {amount}")
    public void enterAmount(String amount) {
        type(amountInput, amount);
    }

    @Step("Tap Next")
    public void tapNext() {
        tap(nextButton, 30);
    }

    @Step("Open frequency dropdown")
    public void tapFrequencyList() {
        tap(frequencyList, 30);
    }

    @Step("Open day dropdown")
    public void tapDayList() {
        tap(dayList, 30);
    }

    @Step("Open date dropdown")
    public void tapDateList() {
        tap(dateList, 30);
    }

    @Step("Open time dropdown")
    public void tapTimeList() {
        tap(timeList, 30);
    }

    @Step("Select first option")
    public void tapFirstOption() {
        tap(firstOption, 30);
    }

    @Step("Select second option")
    public void tapSecondOption() {
        tap(secondOption, 30);
    }

    @Step("Select third option")
    public void tapThirdOption() {
        tap(thirdOption, 30);
    }

    @Step("Tap Confirm")
    public void tapConfirm() {
        tap(confirmButton, 30);
    }

    @Step("Tap Done")
    public void tapDone() {
        tap(doneButton, 30);
    }

    // ══════════════════════════════════════════════════
    //  VERIFICATION / RESULT
    // ══════════════════════════════════════════════════

    @Step("Read created auto top-up amount")
    public String getActualAmount() {
        return getText(actualAmount);
    }

    @Step("Read notification message")
    public String getNotificationMessage() {
        return getText(notificationMessage);
    }

    public boolean isPlanCreated() {
        return isDisplayed(actualAmount, 30);
    }

    // ══════════════════════════════════════════════════
    //  EDIT PLAN
    // ══════════════════════════════════════════════════

    @Step("Tap Edit amount")
    public void tapEditAmount() {
        tap(editAmountButton, 30);
    }

    @Step("Tap Edit frequency")
    public void tapEditFrequency() {
        tap(editFrequencyButton, 30);
    }

    @Step("Tap Edit day")
    public void tapEditDay() {
        tap(editDayButton, 30);
    }

    @Step("Tap Edit time")
    public void tapEditTime() {
        tap(editTimeButton, 30);
    }

    @Step("Read plan frequency text")
    public String getFrequencyText() {
        return getText(frequencyText);
    }

    @Step("Read plan day text")
    public String getDayText() {
        return getText(dayText);
    }

    @Step("Read plan time text")
    public String getTimeText() {
        return getText(timeText);
    }

    @Step("Read second option label")
    public String getSecondOptionText() {
        return getText(secondOptionText);
    }

    @Step("Read third option label")
    public String getThirdOptionText() {
        return getText(thirdOptionText);
    }

    // ══════════════════════════════════════════════════
    //  ENABLE / DISABLE PLAN
    // ══════════════════════════════════════════════════

    @Step("Tap status toggle")
    public void tapStatusToggle() {
        tap(statusToggle, 30);
    }

    @Step("Confirm disable auto top-up")
    public void tapDisable() {
        tap(disableButton, 30);
    }

    @Step("Read plan status text")
    public String getStatusText() {
        return getText(statusText);
    }

    // ══════════════════════════════════════════════════
    //  DELETE EXISTING PLAN
    // ══════════════════════════════════════════════════

    /**
     * Whether a plan already exists (delete entry point visible). The remote device can render
     * the Auto Top-up screen slowly, so poll until it settles into one of its two states —
     * the plan view (delete entry point) or the creation form (amount input) — and decide on
     * whichever appears first. Avoids a false negative that would skip the cleanup delete and
     * then fail trying to type an amount on the plan view.
     */
    public boolean hasExistingPlan() {
        long deadline = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < deadline) {
            if (isPresent(PLAN_DELETE_MARKER, 1)) {
                return true;
            }
            if (isPresent(AMOUNT_INPUT_MARKER, 1)) {
                return false;
            }
        }
        return isPresent(PLAN_DELETE_MARKER, 1);
    }

    @Step("Tap Delete auto top-up")
    public void tapDeleteTopup() {
        tap(deleteTopupButton, 30);
    }

    @Step("Confirm delete auto top-up")
    public void tapConfirmDeleteTopup() {
        tap(confirmDeleteTopupButton, 30);
    }
}
