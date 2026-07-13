package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Order History ("My Orders" → "View History") screen and its re-order actions.
 *
 * Migrated from Katalon (DMP_1 branch), Re-Order suite:
 *   Order History/MoveToMyOrder               → taps "My Orders" (Dashboard/myOrdersButton)
 *   Order History/To Validate move to Order History → taps "View History"
 *   Order History/SelectProductFromVireHistory → taps an order row (ViewGroup testID-View.56900c8d…)
 *   Order History/ValidateClickReOrderALL      → "Reorder All" → "Checkout" → place order → OTP
 */
public class DmpOrderHistoryPage extends BasePage {

    // "View History" action (stable text; testID-secondary-action-main fallback).
    @AndroidFindBy(xpath = "//*[contains(@text,'View History') "
            + "or @content-desc='testID-secondary-action-main' or @name='testID-secondary-action-main']")
    private WebElement viewHistoryButton;

    // "Reorder All" action.
    @AndroidFindBy(xpath = "//*[@text='Reorder All' or contains(@text,'Reorder')]")
    private WebElement reorderAllButton;

    // Checkout action on the reorder cart.
    @AndroidFindBy(xpath = "//*[@text='Go to Checkout' or @text='Checkout' or @text='Proceed to Checkout']")
    private WebElement checkoutButton;

    // Place-order / Pay action (revamped testID, or text).
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061') "
            + "or @text='Pay' or @text='Place Order']")
    private WebElement placeOrderButton;

    private static final String ORDER_ROW_TESTID = "testID-View.56900c8d-d08e-4947-a511-95b2505865a2";

    @Step("Check the My Orders screen is loaded")
    public boolean isLoaded() {
        return isDisplayed(viewHistoryButton, 20);
    }

    @Step("Open the order View History")
    public void openViewHistory() {
        tap(viewHistoryButton, 20);
    }

    @Step("Select the order at position {position} in history")
    public void selectOrder(int position) {
        tap(AppiumBy.xpath(
                "(//android.view.ViewGroup[@content-desc='" + ORDER_ROW_TESTID + "'])[" + position + "]"), 20);
    }

    @Step("Reorder all items from the selected order")
    public void reorderAll() {
        tap(reorderAllButton, 20);
    }

    @Step("Proceed to checkout for the re-order")
    public void checkout() {
        tap(checkoutButton, 20);
    }

    @Step("Place the re-order")
    public void placeOrder() {
        tap(placeOrderButton, 20);
    }
}
