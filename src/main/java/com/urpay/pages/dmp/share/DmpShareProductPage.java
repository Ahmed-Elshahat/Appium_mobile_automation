package com.urpay.pages.dmp.share;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Product Details — Share action (LANE B).
 *
 * Migrated from Katalon (DMP_1 branch)
 * Test Cases/DMP/Share Products/ToValidateClickOnShareProduct: tap the product Share button, then
 * verify the share sheet/pop-up appears.
 *
 * Reads the frozen Product Details screen (share control content-desc suffix {@code .Share-Bold})
 * without modifying it.
 */
public class DmpShareProductPage extends BasePage {

    // Product-details Share control — content-desc suffix ".Share-Bold".
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'.Share-Bold') or contains(@name,'.Share-Bold') "
            + "or contains(@label,'.Share-Bold')]")
    private WebElement shareButton;

    // Android system share chooser (Sharesheet / ResolverActivity) shown after tapping Share.
    @AndroidFindBy(xpath = "//*[@resource-id='android:id/resolver_list' "
            + "or @resource-id='android:id/chooser_header' "
            + "or contains(@resource-id,'android:id/profile_button') "
            + "or contains(@resource-id,'android:id/contentPanel') "
            + "or @text='Copy link' or @text='Copy' or @text='Nearby Share' or @text='Share']")
    private WebElement shareSheet;

    @Step("Check the Product Details Share button is displayed")
    public boolean isShareButtonDisplayed() {
        return isDisplayed(shareButton, 20);
    }

    @Step("Tap the product Share button")
    public void tapShare() {
        tap(shareButton, 20);
    }

    @Step("Check the share sheet is displayed")
    public boolean isShareSheetDisplayed() {
        return isDisplayed(shareSheet, 20);
    }

    // TEMP (live-harden): capture the share-sheet page source to Allure so the exact chooser
    // markers can be locked in, then removed. Remove once isShareSheetDisplayed is verified GREEN.
    @Step("Capture the share sheet page source [{tag}]")
    public void captureShareSheet(String tag) {
        dumpPageSource(tag);
    }
}
