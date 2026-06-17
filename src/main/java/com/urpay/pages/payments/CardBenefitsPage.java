package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Card Benefits page — card name, fees, cashback descriptions.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/cardBenifits/
 */
public class CardBenefitsPage extends BasePage {

    // cardBenifits/cardBenifitsBtn.rs
    @AndroidFindBy(accessibility = "testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.2")
    private WebElement benefitsButton;

    // cardBenifits/cardName.rs
    @AndroidFindBy(accessibility = "testID-Text.dbe54475-2d8b-46a7-93be-4ecf10d0363b")
    private WebElement cardName;

    // cardBenifits/cashBackOffersDescribtion.rs (index 0 = cashback/multipay)
    @AndroidFindBy(accessibility = "testID-Text.aa9f0f2e-ade4-4dd3-9ebb-f94a9a72d808.0")
    private WebElement cashbackDescription;

    // cardBenifits/cardFeesDescribtion.rs (index 1 = fees)
    @AndroidFindBy(accessibility = "testID-Text.aa9f0f2e-ade4-4dd3-9ebb-f94a9a72d808.1")
    private WebElement feesDescription;

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Card Benefits button")
    public void tapBenefits() {
        tap(benefitsButton);
    }

    @Step("Navigate back from benefits")
    public void goBack() {
        pressBack();
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public String getCardName() {
        return getText(cardName);
    }

    public String getCashbackDescription() {
        return getText(cashbackDescription);
    }

    public String getFeesDescription() {
        return getText(feesDescription);
    }
}
