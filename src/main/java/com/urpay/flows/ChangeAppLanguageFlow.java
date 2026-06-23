package com.urpay.flows;

import com.urpay.pages.settings.ChangeAppLanguagePage;

import io.qameta.allure.Step;

/**
 * Change App Language flow — navigates to language settings
 * and switches between English and Arabic.
 *
 * Katalon source: Test Cases/Wallet_VAS/ChangeAppLanguage/
 */
public class ChangeAppLanguageFlow {

    private final ChangeAppLanguagePage languagePage;

    public ChangeAppLanguageFlow() {
        this.languagePage = new ChangeAppLanguagePage();
    }

    @Step("Navigate to Language settings from Dashboard")
    public ChangeAppLanguagePage navigateToLanguageSettings() {
        languagePage.navigateToLanguageSettings();
        return languagePage;
    }

    @Step("Switch app language to Arabic")
    public ChangeAppLanguagePage switchToArabic() {
        languagePage.switchToArabic();
        return languagePage;
    }

    @Step("Switch app language to English")
    public ChangeAppLanguagePage switchToEnglish() {
        languagePage.switchToEnglish();
        return languagePage;
    }
}
