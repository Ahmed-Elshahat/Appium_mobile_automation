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
    // The LambdaTest cloud build hashes the <action> segment of testID-primary-<action>-main
    // buttons (e.g. action -> a hash), so fall back to the visible label.
    // AutoTopupPage/nextBtnSetup
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-action-main' or @text='Next']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-action-main' or @label='testID-primary-action-main' or @name='Next' or @label='Next']")
    private WebElement nextButton;

    // AutoTopupPage/confirmBtn
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onConfirmation-main' or @text='Confirm']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onConfirmation-main' or @label='testID-primary-onConfirmation-main' or @name='Confirm' or @label='Confirm']")
    private WebElement confirmButton;

    // AutoTopupPage/doneBtn
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onSubmit-main' or @text='Done']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onSubmit-main' or @label='testID-primary-onSubmit-main' or @name='Done' or @label='Done']")
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

    // AutoTopupPage/DeleteTopupBtn — confirm button inside the delete-confirmation modal.
    // The remote build OBFUSCATES the content-desc (observed as testID-primary-cqf-main), so the
    // original testID is kept as the primary match for local builds and the stable visible label
    // "Delete" is added as a resilient fallback. The exact text "Delete" is distinct from the
    // "Delete auto Top-up" entry button and the "Delete Auto Top-up" modal title, so it resolves
    // uniquely (mirrors the deleteTopupButton hardening, which is proven to click via @text here).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onDeleteAutoTopup-main' or @text='Delete']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onDeleteAutoTopup-main' or @label='testID-primary-onDeleteAutoTopup-main' or @name='Delete' or @label='Delete']")
    private WebElement confirmDeleteTopupButton;

    // AutoTopupPage/notificationMsg
    @AndroidFindBy(accessibility = "testID-notification-message")
    @iOSXCUITFindBy(accessibility = "testID-notification-message")
    private WebElement notificationMessage;

    // ── Edit entry points (plan summary view) ─────────
    // The cloud (LambdaTest) build hashes the <action> segment of
    // testID-secondary-<action>-<index> edit (pencil) buttons (buttonAction -> a hash). These
    // icons carry no visible text, so match on the stable prefix + preserved index suffix.
    // AutoTopupPage/editAmtBtn
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-buttonAction-main' or (starts-with(@content-desc,'testID-secondary-') and substring(@content-desc, string-length(@content-desc) - 4) = '-main')]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-secondary-buttonAction-main' or @label='testID-secondary-buttonAction-main' or (starts-with(@name,'testID-secondary-') and substring(@name, string-length(@name) - 4) = '-main')]")
    private WebElement editAmountButton;

    // AutoTopupPage/editFrequency
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-buttonAction-1' or (starts-with(@content-desc,'testID-secondary-') and substring(@content-desc, string-length(@content-desc) - 1) = '-1')]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-secondary-buttonAction-1' or @label='testID-secondary-buttonAction-1' or (starts-with(@name,'testID-secondary-') and substring(@name, string-length(@name) - 1) = '-1')]")
    private WebElement editFrequencyButton;

    // AutoTopupPage/editDay
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-buttonAction-2' or (starts-with(@content-desc,'testID-secondary-') and substring(@content-desc, string-length(@content-desc) - 1) = '-2')]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-secondary-buttonAction-2' or @label='testID-secondary-buttonAction-2' or (starts-with(@name,'testID-secondary-') and substring(@name, string-length(@name) - 1) = '-2')]")
    private WebElement editDayButton;

    // AutoTopupPage/editTime
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-buttonAction-4' or (starts-with(@content-desc,'testID-secondary-') and substring(@content-desc, string-length(@content-desc) - 1) = '-4')]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-secondary-buttonAction-4' or @label='testID-secondary-buttonAction-4' or (starts-with(@name,'testID-secondary-') and substring(@name, string-length(@name) - 1) = '-4')]")
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
    // Disable-confirmation modal button; cloud build hashes the <action> segment, so fall back
    // to the visible label. Exact button text is "Disable auto-top up" (distinct from the modal
    // title "Disable Auto Top-up" — different casing/spacing).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onDisableAutoTopup-main' or @text='Disable auto-top up']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onDisableAutoTopup-main' or @label='testID-primary-onDisableAutoTopup-main' or @name='Disable auto-top up' or @label='Disable auto-top up']")
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
        try {
            tap(nextButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Next button (testID-primary-action-main / text 'Next') "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
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
        try {
            tap(confirmButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Confirm button (testID-primary-onConfirmation-main / text 'Confirm') "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
    }

    @Step("Tap Done")
    public void tapDone() {
        try {
            tap(doneButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Done button (testID-primary-onSubmit-main / text 'Done') "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
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
        try {
            tap(editAmountButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Edit-amount button (testID-secondary-buttonAction-main) "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
    }

    @Step("Tap Edit frequency")
    public void tapEditFrequency() {
        try {
            tap(editFrequencyButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Edit-frequency button (testID-secondary-buttonAction-1) "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
    }

    @Step("Tap Edit day")
    public void tapEditDay() {
        try {
            tap(editDayButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Edit-day button (testID-secondary-buttonAction-2) "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
    }

    @Step("Tap Edit time")
    public void tapEditTime() {
        try {
            tap(editTimeButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Edit-time button (testID-secondary-buttonAction-4) "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
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
        try {
            tap(disableButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Disable button (testID-primary-onDisableAutoTopup-main / text 'Disable') "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
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
        dumpScreenDiagnostics("Auto Top-up screen did not settle (no plan-delete marker, no amount input)");
        return isPresent(PLAN_DELETE_MARKER, 1);
    }

    /**
     * One-time diagnostic: log the actual content-desc and text values present on screen so we can
     * see the real testIDs exposed by the running build (local phone vs the LambdaTest APK can
     * differ). Remove once the Auto Top-up screen locators are confirmed on the cloud build.
     */
    private void dumpScreenDiagnostics(String reason) {
        try {
            String src = driver.getPageSource();
            java.util.LinkedHashSet<String> descs = new java.util.LinkedHashSet<>();
            java.util.regex.Matcher dm =
                    java.util.regex.Pattern.compile("content-desc=\"([^\"]+)\"").matcher(src);
            while (dm.find()) {
                descs.add(dm.group(1));
            }
            java.util.LinkedHashSet<String> texts = new java.util.LinkedHashSet<>();
            java.util.regex.Matcher tm =
                    java.util.regex.Pattern.compile("\\btext=\"([^\"]+)\"").matcher(src);
            while (tm.find()) {
                texts.add(tm.group(1));
            }
            log.warn("DIAGNOSTIC: {}", reason);
            log.warn("DIAGNOSTIC content-desc values: {}", descs);
            log.warn("DIAGNOSTIC text values: {}", texts);
        } catch (Exception e) {
            log.warn("DIAGNOSTIC dump failed: {}", e.getMessage());
        }
    }

    @Step("Tap Delete auto top-up")
    public void tapDeleteTopup() {
        tap(deleteTopupButton, 30);
    }

    @Step("Confirm delete auto top-up")
    public void tapConfirmDeleteTopup() {
        try {
            tap(confirmDeleteTopupButton, 30);
        } catch (RuntimeException e) {
            dumpScreenDiagnostics("Confirm-delete button (testID-primary-onDeleteAutoTopup-main) "
                    + "not clickable after 30s: " + e.getMessage());
            throw e;
        }
    }
}
