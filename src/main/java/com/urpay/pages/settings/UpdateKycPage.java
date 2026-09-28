package com.urpay.pages.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Update KYC — Profile → Personal Information form (job sector, employer, income source,
 * job category, income range) → Save → success → Done.
 *
 * <p>Migrated from Katalon: Scripts/Wallet_VAS/UpdateKYC/* +
 * Object Repository/android/WalletVas/UpdateKYC/*.
 */
public class UpdateKycPage extends BasePage {

    // ── Navigation ── (profileBtn.rs: avatar initial; reuse ChangePhoneNumberPage's proven avatar wrapper)
    private static final By PROFILE_AVATAR = AppiumBy.xpath(
            "//android.view.ViewGroup[@clickable='true']"
            + "[.//*[starts-with(@content-desc,'testID-avatar-')"
            + " and not(contains(@content-desc,'ShowMy'))]]");

    // personalInfoBtn.rs — clickable row wrapper first (document order), then label / chevron icon.
    private static final By PERSONAL_INFO_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.0'"
            + " or @text='Personal information' or @text='Personal Information'"
            + " or @content-desc='testID-Text.c06eec32-711a-4563-863f-c6157b3485bd.0'"
            + " or @content-desc='testID-Icons.2fd53aaf-4eef-4b00-b9f1-f857568934aa.0']");

    // ── KYC form (jobSector.rs / employerTxt.rs / incomeSource.rs / jobCategory.rs / incomeSalary.rs) ──
    private static final By JOB_SECTOR = AppiumBy.accessibilityId("testID-multi-select-employmentStatus");
    private static final By EMPLOYER_INPUT = AppiumBy.accessibilityId("testID-input-direct-employer");
    private static final By INCOME_SOURCE = AppiumBy.accessibilityId("testID-multi-select-basicIncomeSource");
    private static final By JOB_CATEGORY = AppiumBy.accessibilityId("testID-multi-select-jobCategory");
    private static final By INCOME_RANGE = AppiumBy.accessibilityId("testID-multi-select-incomeRange");

    private static final By KYC_FORM_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-multi-select-employmentStatus'"
            + " or @content-desc='testID-input-direct-employer'"
            + " or @content-desc='testID-multi-select-basicIncomeSource']");

    // firstOption/secondOption/thirdOption.rs
    private static final By OPTION_ITEMS = AppiumBy.xpath(
            "//*[starts-with(@content-desc,'testID-search-item-')"
            + " or starts-with(@content-desc,'testID-radio-item-')]");

    // saveBtn.rs (text 'Save'); primary testID middle segment is hashed on cloud builds.
    private static final By SAVE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary--main' or @text='Save']");

    // infoSuccessMsgText.rs / thankYouText.rs / successMessage.rs
    private static final By SUCCESS_MARKER = AppiumBy.xpath(
            "//*[contains(@text,'saved successfully') or contains(@text,'updated successfully')"
            + " or @content-desc='testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5'"
            + " or @text='Thank You!']");

    // doneBtn.rs — now labelled 'Home' and hashed (e.g. testID-primary-g3U-main); only primary on the success screen.
    private static final By DONE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-handleCallback-main' or @text='Done'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')]");

    private static final By TEXT_CHILDREN = By.className("android.widget.TextView");

    // FamilyWallet/backProfileBtn.rs
    private static final By BACK_BTN = AppiumBy.accessibilityId("testID-left-icon-back");

    // These sectors hide Employer / Income Source / Job Category, leaving little to update.
    private static final String NON_EMPLOYED_SECTORS = "(?i).*(home ?maker|student|unemployed|retired|not working).*";

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Tap profile avatar on Dashboard")
    public void tapProfileAvatar() {
        tap(PROFILE_AVATAR);
    }

    @Step("Tap Personal Information (Update KYC)")
    public void tapPersonalInformation() {
        if (!isPresent(PERSONAL_INFO_BTN, 10)) {
            scrollToText("Personal information");
        }
        tap(PERSONAL_INFO_BTN);
    }

    public boolean isPersonalInfoEntryDisplayed(long timeoutSec) {
        return isPresent(PERSONAL_INFO_BTN, timeoutSec);
    }

    public boolean isKycFormDisplayed(long timeoutSec) {
        return isPresent(KYC_FORM_MARKER, timeoutSec);
    }

    // ══════════════════════════════════════════════════
    //  READ VALUES
    // ══════════════════════════════════════════════════

    public String getJobSector() {
        return readDropdownValue(JOB_SECTOR);
    }

    public String getIncomeSource() {
        return readDropdownValue(INCOME_SOURCE);
    }

    public String getJobCategory() {
        return readDropdownValue(JOB_CATEGORY);
    }

    public String getIncomeRange() {
        return readDropdownValue(INCOME_RANGE);
    }

    public String getEmployer() {
        WebElement field = revealField(EMPLOYER_INPUT);
        return field == null ? "" : field.getText();
    }

    // ══════════════════════════════════════════════════
    //  UPDATE ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Select a random employed Job Sector different from the current one")
    public String selectRandomJobSector() {
        return selectRandomOption(JOB_SECTOR, "Job Sector", NON_EMPLOYED_SECTORS);
    }

    /** Employer is hidden for non-employed sectors (e.g. 'Home maker'); returns false when absent. */
    @Step("Enter employer if the field is shown: {employer}")
    public boolean enterEmployerIfShown(String employer) {
        if (revealField(EMPLOYER_INPUT) == null) {
            log.info("Employer field not shown for the selected job sector — skipping");
            return false;
        }
        type(EMPLOYER_INPUT, employer);
        platformActions.dismissKeyboard();
        return true;
    }

    @Step("Select a random Income Source different from the current one")
    public String selectRandomIncomeSource() {
        return selectRandomOption(INCOME_SOURCE, "Income Source");
    }

    @Step("Select a random Job Category different from the current one")
    public String selectRandomJobCategory() {
        return selectRandomOption(JOB_CATEGORY, "Job Category");
    }

    @Step("Select a random Income Range different from the current one")
    public String selectRandomIncomeRange() {
        return selectRandomOption(INCOME_RANGE, "Income Range");
    }

    @Step("Tap Save on Personal Information")
    public void tapSave() {
        for (int i = 0; i < 5 && !isPresent(SAVE_BTN, 2); i++) {
            swipeUp();
        }
        tap(SAVE_BTN);
    }

    public boolean isSuccessDisplayed(long timeoutSec) {
        return isPresent(SUCCESS_MARKER, timeoutSec);
    }

    @Step("Tap Done on the success screen")
    public void tapDone() {
        tap(DONE_BTN);
    }

    @Step("Back out of Personal Information and Profile")
    public void backToDashboard() {
        for (int i = 0; i < 2 && isPresent(BACK_BTN, 3); i++) {
            tap(BACK_BTN);
        }
    }

    /** Diagnostic — write the current screen tree to a local file for inspection. */
    @Step("Dump Update KYC screen: {tag}")
    public void dumpScreen(String tag) {
        dumpPageSource(tag);
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logcat"));
            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("logcat", "updatekyc-" + tag + ".xml"),
                    driver.getPageSource());
        } catch (Exception e) {
            log.warn("Update KYC dump failed: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  INTERNALS
    // ══════════════════════════════════════════════════

    /** Scrolls the form to find the field; null when the field is not rendered for this sector. */
    private WebElement revealField(By field) {
        if (isPresent(field, 3)) {
            return waitUtils.waitForVisible(field);
        }
        for (int i = 0; i < 2; i++) {
            swipeUp();
            if (isPresent(field, 2)) {
                return waitUtils.waitForVisible(field);
            }
        }
        for (int i = 0; i < 3; i++) {
            swipeDown();
            if (isPresent(field, 2)) {
                return waitUtils.waitForVisible(field);
            }
        }
        return null;
    }

    /** Last non-empty TextView inside the multi-select = the selected value (Katalon TextView[2]). */
    private String readDropdownValue(By dropdown) {
        WebElement container = revealField(dropdown);
        if (container == null) {
            return "";
        }
        List<WebElement> texts = container.findElements(TEXT_CHILDREN);
        String value = "";
        for (WebElement t : texts) {
            String s = t.getText();
            if (s != null && !s.isBlank()) {
                value = s.trim();
            }
        }
        return value;
    }

    private String selectRandomOption(By dropdown, String label) {
        return selectRandomOption(dropdown, label, null);
    }

    private String selectRandomOption(By dropdown, String label, String excludeRegex) {
        if (revealField(dropdown) == null) {
            log.info("{} not shown for the selected job sector — skipping", label);
            return "";
        }
        String current = readDropdownValue(dropdown);
        tap(dropdown);
        List<WebElement> options = waitUtils.findQuick(OPTION_ITEMS, 10);
        if (options.isEmpty()) {
            dumpScreen("no-options-" + label.replace(' ', '-'));
            throw new IllegalStateException("No options shown for " + label);
        }
        List<WebElement> candidates = new ArrayList<>();
        for (WebElement o : options) {
            String name = optionLabel(o);
            if (!current.equals(name) && (excludeRegex == null || !name.matches(excludeRegex))) {
                candidates.add(o);
            }
        }
        if (candidates.isEmpty()) {
            candidates = options;
        }
        WebElement chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        String chosenLabel = optionLabel(chosen);
        chosen.click();
        waitUtils.isPresent(dropdown, 5);
        String selected = readDropdownValue(dropdown);
        log.info("{}: '{}' -> '{}' (picked '{}' of {} options)", label, current, selected, chosenLabel, options.size());
        return selected;
    }

    private String optionLabel(WebElement option) {
        String text = option.getText();
        if (text != null && !text.isBlank()) {
            return text.trim();
        }
        for (WebElement t : option.findElements(TEXT_CHILDREN)) {
            String s = t.getText();
            if (s != null && !s.isBlank()) {
                return s.trim();
            }
        }
        return "";
    }
}
