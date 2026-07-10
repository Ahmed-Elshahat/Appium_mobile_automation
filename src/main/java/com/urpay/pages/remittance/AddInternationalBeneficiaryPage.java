package com.urpay.pages.remittance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Add International (MTO) Beneficiary page — the add-beneficiary wizard shared by every money
 * transfer operator (MoneyGram, H2H, Western Union, Transfast). Steps: country → delivery
 * option → currency → name/nickname → (citizenship) → confirm → name verification →
 * relationship → confirm → OTP → IVR "Verification Call".
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/InternationalTran/AddMgBeneficiary/Script1707735660007.groovy
 *   Object Repository/android/Remittance/AddMgBeneficiary/**
 *
 * NOTE: adding a beneficiary ends on an IVR phone-verification ("Verification Call") that cannot
 * be driven on the cloud device; the beneficiary is created PENDING and must be activated in the
 * wallet DB (mirrors Katalon BeneficiaryKeyword.activateBeneficiary) before it can receive a
 * transfer — see {@code InternationalTransferFlow.addBeneficiary}.
 *
 * Rules: private elements, @Step actions, NO assertions.
 */
public class AddInternationalBeneficiaryPage extends BasePage {

    // ── Entry ──────────────────────────────────────────
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-transferToNewBeneficiary-main' "
            + "or @text='Add Beneficiary' or @text='Add beneficiary' or @text='Add New Beneficiary']")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'testID-primary-transferToNewBeneficiary-main' "
            + "OR label == 'Add Beneficiary'")
    private WebElement addNewBeneficiaryButton;

    // ── Country ────────────────────────────────────────
    @AndroidFindBy(accessibility = "testID-Search-Input")
    @iOSXCUITFindBy(accessibility = "testID-Search-Input")
    private WebElement countrySearchField;

    /** First country result (Katalon SelectCountry testID carries a build UUID → generic .0 match). */
    private static final By FIRST_COUNTRY = AppiumBy.xpath(
            "//*[@content-desc='testID-View.7e939745-cfb7-4723-8fc4-f57e9a793d44.0']"
            + " | //*[contains(@content-desc,'testID-View') and contains(@content-desc,'.0')]");

    // ── Common primary "Next step" ─────────────────────
    // (Located dynamically in tapNextStep — the testID is obfuscated on some builds.)

    // ── Currency / relationship first radio ────────────
    // (Located dynamically in tapFirstSelectableOption — testID varies radio-item/search-item.)

    // ── Beneficiary details ────────────────────────────
    // (fullName located dynamically in enterFullName — the field is fullName on some corridors
    //  and separate first/middle/last on others.)

    @AndroidFindBy(accessibility = "testID-input-direct-nickName")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-nickName")
    private WebElement nicknameField;

    /** Citizenship multi-select (only some countries) + its first option. */
    private static final By CITIZENSHIP_DDL =
            AppiumBy.accessibilityId("testID-multi-select-citizenship");
    private static final By FIRST_SEARCH_ITEM =
            AppiumBy.accessibilityId("testID-search-item-0");

    /** Next on the beneficiary-details screen (Katalon nextButtonInBeneficiaryDetailsPage). */
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary--main' or @text='Next']")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'testID-primary--main' OR label == 'Next'")
    private WebElement nextAtDetailsButton;

    /** Confirm on the name-verification popup (Katalon ConfirmAtNameVerificationPage). */
    @AndroidFindBy(accessibility = "testID-primary-action-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-action-main")
    private WebElement confirmNameVerificationButton;

    /** Final confirm on the add-beneficiary review screen. */
    private static final By CONFIRM_BENEFICIARY = AppiumBy.xpath(
            "(//*[@content-desc='testID-primary-validate-main'"
            + " or @content-desc='testID-primary-onConfirm-main'"
            + " or @content-desc='testID-primary-nextStep-main' or @text='Confirm'])[1]");

    /** IVR "Verification Call" screen marker (add-beneficiary tail). */
    private static final By VERIFICATION_CALL =
            AppiumBy.xpath("//*[@text='Verification Call' or contains(@text,'Verification Call')]");

    /** First saved beneficiary row container (UUID testID → generic '.0' match). */
    private static final By FIRST_BENEFICIARY_ROW = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-View') and contains(@content-desc,'.0')]");

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap 'Add new beneficiary'")
    public void tapAddNewBeneficiary() {
        tap(addNewBeneficiaryButton);
    }

    @Step("Search and select country: {country}")
    public void selectCountry(String country) {
        type(countrySearchField, country);
        hideKeyboard();
        tap(FIRST_COUNTRY);
    }

    @Step("Tap Next step")
    public void tapNextStep() {
        // The Next-step testID is obfuscated on some builds (like the transfer screens), so try the
        // stable testID first, then the clickable ancestor of the Next/Continue text.
        By byId = AppiumBy.accessibilityId("testID-primary-nextStep-main");
        if (isPresent(byId, 3)) {
            tap(byId);
            return;
        }
        By byText = AppiumBy.xpath(
                "//*[@text='Next' or @text='Next Step' or @text='Next step' or @text='Continue' "
                + "or @content-desc='testID-primary--main' or @content-desc='testID-primary-onPress-main']"
                + "/ancestor-or-self::*[@clickable='true'][1]");
        if (isPresent(byText, 4)) {
            tap(byText);
            return;
        }
        // Neither resolved — capture the screen for locator discovery, then fail on the testID.
        dumpPageSource("addBeneficiaryNextStep");
        log.info("Add-beneficiary Next-step screen visible texts: {}", getVisibleTexts());
        tap(byId);
    }

    @Step("Select delivery option: {deliveryOption}")
    public void selectDeliveryOption(String deliveryOption) {
        // The label may vary ("Bank Deposit" vs "Account Deposit", "Cash pickup - Cebuana & others");
        // match exact text first, then contains (case-insensitive via translate).
        By exact = AppiumBy.xpath("//android.widget.TextView[@text='" + deliveryOption + "']");
        if (isPresent(exact, 4)) {
            tap(exact);
            return;
        }
        // Fallback: contains the last word (case-insensitive)
        String last = deliveryOption.contains(" ")
                ? deliveryOption.substring(deliveryOption.lastIndexOf(' ') + 1)
                : deliveryOption;
        By caseInsensitive = AppiumBy.xpath(
                "//android.widget.TextView[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                + last.toLowerCase() + "')]");
        tap(caseInsensitive);
    }

    @Step("Select the first currency option")
    public void selectFirstCurrency() {
        tapFirstSelectableOption("addBeneficiaryCurrency");
    }

    @Step("Enter beneficiary name: {fullName}")
    public void enterFullName(String fullName) {
        // Single fullName field (Bank Deposit / Cash Pickup) OR separate first/middle/last fields
        // (Send-to-Wallet). Fill whichever this corridor shows.
        By full = AppiumBy.accessibilityId("testID-input-direct-fullName");
        if (isPresent(full, 3)) {
            type(full, fullName);
            hideKeyboard();
            return;
        }
        String[] parts = fullName.trim().split("\\s+");
        String first = parts.length > 0 ? parts[0] : fullName;
        String last = parts.length > 1 ? parts[parts.length - 1] : first;
        // Some corridors require a middle name — reuse the first name if only two parts were given.
        String middle = parts.length >= 3 ? parts[1] : first;
        enterInputIfPresent("firstName", first);
        enterInputIfPresent("middleName", middle);
        enterInputIfPresent("lastName", last);
        hideKeyboard();
    }

    @Step("Enter beneficiary nickname: {nickname}")
    public void enterNickname(String nickname) {
        type(nicknameField, nickname);
        hideKeyboard();
    }

    @Step("Select first citizenship if the dropdown is shown")
    public void selectFirstCitizenshipIfPresent() {
        if (isPresent(CITIZENSHIP_DDL, 3)) {
            tap(CITIZENSHIP_DDL);
            tap(FIRST_SEARCH_ITEM);
        }
    }

    @Step("Tap Next on the beneficiary details screen")
    public void tapNextAtDetails() {
        // Bank Deposit forms can be long — scroll down to make Next visible
        By nextLoc = AppiumBy.xpath("//*[@content-desc='testID-primary--main' or @text='Next']");
        if (!isDisplayed(nextAtDetailsButton, 2)) {
            new com.urpay.utils.SwipeUtils(getDriver(), 0.30).swipeUp();
            if (!isDisplayed(nextAtDetailsButton, 2)) {
                new com.urpay.utils.SwipeUtils(getDriver(), 0.30).swipeUp();
            }
        }
        tap(nextAtDetailsButton);
    }

    @Step("Confirm on the name-verification popup")
    public void confirmNameVerification() {
        if (isDisplayed(confirmNameVerificationButton, 5)) {
            tap(confirmNameVerificationButton);
        }
    }

    @Step("Select the first beneficiary relationship")
    public void selectFirstRelationship() {
        tapFirstSelectableOption("addBeneficiaryRelationship");
    }

    /**
     * Tap the first selectable option on a radio/list screen. The testID varies by screen/build
     * (testID-radio-item-0 or testID-search-item-0); if neither resolves, capture the screen and
     * best-effort tap the first radio/search item.
     */
    private void tapFirstSelectableOption(String dumpTag) {
        By radio = AppiumBy.accessibilityId("testID-radio-item-0");
        if (isPresent(radio, 4)) {
            tap(radio);
            return;
        }
        By searchItem = AppiumBy.accessibilityId("testID-search-item-0");
        if (isPresent(searchItem, 3)) {
            tap(searchItem);
            return;
        }
        dumpPageSource(dumpTag);
        log.info("{} visible texts: {}", dumpTag, getVisibleTexts());
        By any = AppiumBy.xpath(
                "(//*[contains(@content-desc,'testID-radio-item') "
                + "or contains(@content-desc,'testID-search-item')])[1]");
        if (isPresent(any, 3)) {
            tap(any);
            return;
        }
        tap(radio);
    }

    // ── Beneficiary type (Others / Myself) ─────────────
    @Step("Select beneficiary type: {type}")
    public void selectBeneficiaryType(String type) {
        // On the details screen the type is a tag row: testID-tags-menu-0 = Others, -1 = Myself.
        By tag = "Myself".equalsIgnoreCase(type)
                ? AppiumBy.xpath("//*[@content-desc='testID-tags-menu-1' "
                        + "or @content-desc='testID-tags-menu-Text-1' or @text='Myself']")
                : AppiumBy.xpath("//*[@content-desc='testID-tags-menu-0' "
                        + "or @content-desc='testID-tags-menu-Text-0' or @text='Others']");
        if (isPresent(tag, 4)) {
            tap(tag);
            log.info("Selected beneficiary type '{}'", type);
        }
    }

    /** True once the Beneficiary Details screen is shown. The name field is a single fullName on
     *  some corridors (Bank Deposit / Cash Pickup) and separate first/middle/last on others
     *  (Send-to-Wallet), so match either. Used to skip the optional currency step. */
    public boolean isOnDetailsScreen(long timeoutSec) {
        return isPresent(AppiumBy.xpath(
                "//*[@content-desc='testID-input-direct-fullName' "
                + "or @content-desc='testID-input-direct-firstName']"), timeoutSec);
    }

    /** True once the final Confirmation screen is shown. Match only the distinctive title /
     *  "By confirming" text — NOT a Confirm/Next button, which also appears on the details and
     *  relationship screens and would cause the relationship step to be skipped prematurely. */
    public boolean isOnConfirmationScreen(long timeoutSec) {
        return isPresent(AppiumBy.xpath(
                "//*[@text='Confirmation' or contains(@text,'By confirming')]"), timeoutSec);
    }

    // ── Delivery-specific details (Bank Deposit: bank / branch / account / routing / city) ──
    /**
     * Fill the delivery-specific beneficiary fields that appear for some corridors/options
     * (Bank Deposit needs bank/branch/account/routing; Cash Pickup / Send-to-Wallet usually don't).
     * All CONDITIONAL — a field absent for this corridor is skipped. Dropdowns select by name if
     * given, else the first option; inputs are typed. Emits a discovery dump of the details screen.
     */
    @Step("Fill delivery-specific beneficiary details (bank/branch/account/city — if present)")
    public void fillDeliveryDetails(String bankName, String branch, String accountNumber,
            String routingNumber, String city, String purposeOfFunds) {
        log.info("Add-beneficiary details visible texts: {}", getVisibleTexts());
        // Real Bank-Deposit field testIDs (Pakistan corridor): BANKNAME / cityName dropdowns,
        // accountNumber input. Other corridors may expose branch/routing/purposeOfFunds; all
        // conditional so an absent field is skipped.
        selectDropdownIfPresent("BANKNAME", bankName);
        selectDropdownIfPresent("branch", branch);
        selectDropdownIfPresent("cityName", city);
        selectDropdownIfPresent("city", city);
        selectDropdownIfPresent("purposeOfFunds", purposeOfFunds);
        // Send-to-Wallet: mobile wallet operator (dropdown) + wallet number (the account field).
        selectDropdownIfPresent("beneficiaryWalletOperator", "");
        enterInputIfPresent("beneficiaryWalletNumber", accountNumber);
        enterInputIfPresent("accountNumber", accountNumber);
        enterInputIfPresent("iban", accountNumber);
        enterInputIfPresent("bankRoutingNumber", routingNumber);
        enterInputIfPresent("routingNumber", routingNumber);
        hideKeyboard();
    }

    /** Tap a testID-multi-select-&lt;key&gt; dropdown (if present) and pick by name, else first option. */
    private void selectDropdownIfPresent(String key, String value) {
        By ddl = AppiumBy.accessibilityId("testID-multi-select-" + key);
        if (!isPresent(ddl, 2)) {
            return;
        }
        tap(ddl);
        if (value != null && !value.isEmpty()) {
            By byName = AppiumBy.xpath("//*[contains(@text,'" + value + "')]");
            if (isPresent(byName, 3)) {
                tap(byName);
                return;
            }
        }
        tap(FIRST_SEARCH_ITEM);
    }

    /** Type into a testID-input-direct-&lt;key&gt; field only if it exists and a value is given. */
    private void enterInputIfPresent(String key, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        By input = AppiumBy.accessibilityId("testID-input-direct-" + key);
        if (isPresent(input, 2)) {
            type(input, value);
            hideKeyboard();
            log.info("Entered {} = {}", key, value);
        }
    }

    // ── Edit an existing beneficiary ───────────────────
    @Step("Open the first saved beneficiary for edit")
    public void openFirstBeneficiaryForEdit() {
        if (isPresent(FIRST_BENEFICIARY_ROW, 6)) {
            tap(FIRST_BENEFICIARY_ROW);
        }
        // Tapping the row expands the card — open its details to edit/delete.
        By viewDetails = AppiumBy.xpath(
                "//*[@text='View beneficiary Details' or contains(@text,'View beneficiary') "
                + "or contains(@text,'beneficiary Details')]");
        if (isPresent(viewDetails, 5)) {
            tap(viewDetails);
        }
        dumpPageSource("beneficiaryEditOpen");
        log.info("Beneficiary details/edit screen visible texts: {}", getVisibleTexts());
    }

    @Step("Edit beneficiary names (first/middle/last/nick — if present)")
    public void editNames(String firstName, String middleName, String lastName, String nickName) {
        enterInputIfPresent("firstName", firstName);
        enterInputIfPresent("middleName", middleName);
        enterInputIfPresent("lastName", lastName);
        enterInputIfPresent("nickName", nickName);
        hideKeyboard();
    }

    @Step("Save beneficiary edits")
    public void tapSave() {
        tap(AppiumBy.xpath("//*[@content-desc='testID-primary--main' or @text='Save']"));
    }

    // ── Remove an existing beneficiary ─────────────────
    @Step("Delete the beneficiary")
    public void tapDelete() {
        tap(AppiumBy.xpath(
                "//*[@text='Delete Beneficiary' or @text='Delete' "
                + "or @content-desc='testID-secondary-NG9-main' "
                + "or @content-desc='testID-primary-deleteBeneficiary-main']"));
    }

    @Step("Confirm delete")
    public void confirmDelete() {
        tap(AppiumBy.xpath(
                "//*[@content-desc='testID-primary-action-main' "
                + "or @text='Yes' or @text='Confirm' or @text='Delete']"));
    }

    public boolean isBeneficiaryDeleted(String name, long timeoutSec) {
        // Deleted → the named row is no longer present.
        return !isPresent(AppiumBy.xpath("//*[contains(@text,'" + name + "')]"), timeoutSec);
    }

    @Step("Scroll to and confirm adding the beneficiary")
    public void scrollToConfirmAndTap() {
        scrollToEnd();
        // Try the clickable ancestor-or-self of the confirm element/text first.
        By confirm = AppiumBy.xpath(
                "//*[@content-desc='testID-primary--main' "
                + "or @content-desc='testID-primary-onConfirm-main' "
                + "or @content-desc='testID-primary-validate-main' or @text='Confirm']"
                + "/ancestor-or-self::*[@clickable='true'][1]");
        if (isPresent(confirm, 6)) {
            tap(confirm);
        }
        // The Confirm button is a fixed full-width button at the bottom whose RN onPress does not
        // always fire via element taps — if we're still on the Confirmation screen, tap it by
        // coordinates (centre, ~93% down).
        if (isOnConfirmationScreen(3)) {
            try {
                org.openqa.selenium.Dimension size = driver.manage().window().getSize();
                tapAtCoordinates((int) (size.getWidth() * 0.5), (int) (size.getHeight() * 0.93));
                log.info("Coordinate-tapped the Confirm button (element tap did not advance)");
            } catch (Exception e) {
                log.warn("Coordinate tap on Confirm failed: {}", e.getMessage());
            }
        }
    }

    public boolean isVerificationCallShown(long timeoutSec) {
        return isPresent(VERIFICATION_CALL, timeoutSec);
    }

    /** Visible non-blank TextView labels — diagnostic for discovering corridor-specific fields. */
    public java.util.List<String> getVisibleTexts() {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            for (WebElement el : driver.findElements(AppiumBy.className("android.widget.TextView"))) {
                try {
                    String t = el.getText();
                    if (t != null && !t.trim().isEmpty()) {
                        out.add(t.trim());
                    }
                } catch (Exception ignored) {
                    // stale — skip
                }
            }
        } catch (Exception ignored) {
            // not resolvable
        }
        return out;
    }

    private void scrollToEnd() {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).flingToEnd(4)"));
        } catch (Exception ignored) {
            // not scrollable / already at end
        }
    }
}
