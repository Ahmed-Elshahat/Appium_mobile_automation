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

    // Navigate via icon on card products page
    private static final org.openqa.selenium.By BENEFITS_ICON = io.appium.java_client.AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-avatar-card-tick')] | //*[@text='Card Benefits']");

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Card Benefits button")
    public void tapBenefits() {
        tap(BENEFITS_ICON);
    }

    @Step("Navigate back from benefits")
    public void goBack() {
        pressBack();
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS — find text by index on benefits page
    // ══════════════════════════════════════════════════

    public String getCardName() {
        // First prominent text AFTER page title = card name (skip "Card Benefits" header)
        return getText(io.appium.java_client.AppiumBy.xpath(
                "(//*[@class='android.widget.TextView' and string-length(@text) > 3 and @text!='Card Benefits'])[1]"));
    }

    public String getCashbackDescription() {
        // Second description text
        return getText(io.appium.java_client.AppiumBy.xpath(
                "(//*[@class='android.widget.TextView' and string-length(@text) > 20])[1]"));
    }

    public String getFeesDescription() {
        // Third description text
        return getText(io.appium.java_client.AppiumBy.xpath(
                "(//*[@class='android.widget.TextView' and string-length(@text) > 20])[2]"));
    }
}
