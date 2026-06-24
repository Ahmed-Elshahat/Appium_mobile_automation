package com.urpay.pages.settings;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Terms & Conditions page — shows tabs for General, Cards, MoneyGram, Mokafaa, Complaints.
 * Accessed from Consents → Terms & Conditions.
 *
 * Katalon source: Object Repository/android/WalletVas/TermsConditionsPage/
 */
public class TermsConditionsPage extends BasePage {

    // ── Page title ───────────────────────────────────
    private static final By TERMS_TITLE =
            AppiumBy.xpath("//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42']");

    // ── Tab buttons (resource-id based) ──────────────
    private static final By CARD_TERMS_TAB =
            AppiumBy.xpath("//*[@resource-id='w-tabs-0-data-w-tab-1']");

    private static final By MONEYGRAM_TAB =
            AppiumBy.xpath("//*[@resource-id='w-tabs-0-data-w-tab-2']");

    private static final By MOKAFAA_TAB =
            AppiumBy.xpath("//*[@resource-id='w-tabs-0-data-w-tab-3']");

    private static final By COMPLAINTS_TAB =
            AppiumBy.xpath("//*[@resource-id='w-tabs-0-data-w-tab-4']");

    // ── Card terms sub-tabs ──────────────────────────
    private static final By DIGITAL_CARD_TAB =
            AppiumBy.xpath("//*[@resource-id='w-tabs-1-data-w-tab-0']");

    private static final By PHYSICAL_CARD_TAB =
            AppiumBy.xpath("//*[@resource-id='w-tabs-1-data-w-tab-1']");

    // ── Subtitles/content ────────────────────────────
    private static final By GENERAL_TERMS_SUBTITLE =
            AppiumBy.xpath("//*[contains(@text,'General Terms') and contains(@text,'Conditions')]");

    private static final By CARD_TERMS_SUBTITLE =
            AppiumBy.xpath("//*[@text='Urpay Cards Terms And Conditions']");

    private static final By MONEYGRAM_SUBTITLE =
            AppiumBy.xpath("//*[@text='Moneygram Terms And Conditions For Sending Money']");

    private static final By MOKAFAA_SUBTITLE =
            AppiumBy.xpath("//*[@text='Mokafaa Terms & Conditions']");

    private static final By COMPLAINTS_TITLE =
            AppiumBy.xpath("//*[@text='How To File A Complaint']");

    private static final By THESE_TERMS_TEXT =
            AppiumBy.xpath("//*[@text='These Terms']");

    private static final By ACKNOWLEDGEMENT_TEXT =
            AppiumBy.xpath("//*[@text='Acknowledgement']");

    // ── Toggle and reject ────────────────────────────
    private static final By TOGGLE_BTN =
            AppiumBy.accessibilityId("testID-switcher-undefined");

    private static final By REJECT_TERMS_TEXT =
            AppiumBy.accessibilityId("testID-Text.reject-terms-title");

    private static final By BACK_BUTTON =
            AppiumBy.accessibilityId("testID-right-icon-item");

    // ══════════════════════════════════════════════════
    //  PAGE STATE
    // ══════════════════════════════════════════════════

    @Step("Get Terms & Conditions page title")
    public String getTermsTitle() {
        return getText(TERMS_TITLE);
    }

    public boolean isTermsPageLoaded() {
        return isPresent(TERMS_TITLE, 10);
    }

    // ══════════════════════════════════════════════════
    //  GENERAL TERMS
    // ══════════════════════════════════════════════════

    @Step("Get General Terms subtitle")
    public String getGeneralTermsSubtitle() {
        return getText(GENERAL_TERMS_SUBTITLE);
    }

    public boolean isGeneralTermsSubtitleVisible() {
        return isPresent(GENERAL_TERMS_SUBTITLE, 10);
    }

    @Step("Tap toggle switch")
    public void tapToggle() {
        tap(TOGGLE_BTN);
    }

    @Step("Get reject confirmation text")
    public String getRejectText() {
        return getText(REJECT_TERMS_TEXT);
    }

    public boolean isRejectDialogVisible() {
        return isPresent(REJECT_TERMS_TEXT, 10);
    }

    @Step("Tap back button")
    public void tapBack() {
        tap(BACK_BUTTON);
    }

    // ══════════════════════════════════════════════════
    //  CARD TERMS
    // ══════════════════════════════════════════════════

    @Step("Tap Card Terms tab")
    public void tapCardTermsTab() {
        tap(CARD_TERMS_TAB);
    }

    @Step("Get Card Terms subtitle")
    public String getCardTermsSubtitle() {
        return getText(CARD_TERMS_SUBTITLE);
    }

    public boolean isCardTermsSubtitleVisible() {
        return isPresent(CARD_TERMS_SUBTITLE, 10);
    }

    @Step("Tap Digital Card tab")
    public void tapDigitalCardTab() {
        tap(DIGITAL_CARD_TAB);
    }

    @Step("Tap Physical Card tab")
    public void tapPhysicalCardTab() {
        tap(PHYSICAL_CARD_TAB);
    }

    public boolean isTheseTermsVisible() {
        return isPresent(THESE_TERMS_TEXT, 10);
    }

    @Step("Scroll to Acknowledgement section")
    public boolean scrollToAcknowledgement() {
        scrollToText("Acknowledgement");
        return isPresent(ACKNOWLEDGEMENT_TEXT, 10);
    }

    @Step("Scroll back to Card Terms subtitle")
    public void scrollBackToCardTermsTitle() {
        scrollToText("Urpay Cards Terms And Conditions");
    }

    // ══════════════════════════════════════════════════
    //  MONEYGRAM TERMS
    // ══════════════════════════════════════════════════

    @Step("Tap MoneyGram Terms tab")
    public void tapMoneyGramTab() {
        tap(MONEYGRAM_TAB);
    }

    @Step("Get MoneyGram subtitle")
    public String getMoneyGramSubtitle() {
        return getText(MONEYGRAM_SUBTITLE);
    }

    public boolean isMoneyGramSubtitleVisible() {
        return isPresent(MONEYGRAM_SUBTITLE, 10);
    }

    @Step("Scroll to MoneyGram fraud warning content")
    public boolean scrollToMoneyGramFraudWarning() {
        scrollToText("Moneygram Non-Us Agent, Mgo, Mobile App Fraud Warning");
        return isPresent(AppiumBy.xpath("//*[@text='Important Information - Fraud Warning About Your Money Transfer']"), 10);
    }

    // ══════════════════════════════════════════════════
    //  MOKAFAA TERMS
    // ══════════════════════════════════════════════════

    @Step("Tap Mokafaa Terms tab")
    public void tapMokafaaTab() {
        tap(MOKAFAA_TAB);
    }

    @Step("Get Mokafaa subtitle")
    public String getMokafaaSubtitle() {
        return getText(MOKAFAA_SUBTITLE);
    }

    public boolean isMokafaaSubtitleVisible() {
        return isPresent(MOKAFAA_SUBTITLE, 10);
    }

    // ══════════════════════════════════════════════════
    //  COMPLAINTS
    // ══════════════════════════════════════════════════

    @Step("Tap Complaints tab")
    public void tapComplaintsTab() {
        tap(COMPLAINTS_TAB);
    }

    @Step("Get Complaints title")
    public String getComplaintsTitle() {
        return getText(COMPLAINTS_TITLE);
    }

    public boolean isComplaintsTitleVisible() {
        return isPresent(COMPLAINTS_TITLE, 10);
    }
}
