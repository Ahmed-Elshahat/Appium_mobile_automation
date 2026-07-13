package com.urpay.pages.dmp.orders;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Cart page for the LANE C digital-order journeys.
 *
 * Migrated from the cart / checkout step of Katalon
 * Test Cases/DMP/Order/ToValidateMoveToCheckoutPage.
 *
 * NEW LANE C page. The checkout call-to-action label on the revamped build is
 * 'Go to Checkout' (foundation note), with 'Checkout' / 'Proceed to Checkout' kept as
 * fallbacks.
 */
public class DmpOrderCartPage extends BasePage {

    // Cart total-amount label.
    @AndroidFindBy(xpath = "//*[@text='Total' or contains(@text,'Total')]")
    private WebElement totalLabel;

    // 'Go to Checkout' call-to-action.
    @AndroidFindBy(xpath = "//*[@text='Go to Checkout' or @text='Checkout' or @text='Proceed to Checkout']")
    private WebElement checkoutButton;

    @Step("Check the Cart page is loaded")
    public boolean isLoaded() {
        return isDisplayed(checkoutButton, 20) || isDisplayed(totalLabel, 5);
    }

    @Step("Proceed to checkout (Go to Checkout)")
    public DmpOrderCheckoutPage goToCheckout() {
        if (!isDisplayed(checkoutButton, 3)) {
            swipeUp();
        }
        tap(checkoutButton, 20);
        return new DmpOrderCheckoutPage();
    }
}
