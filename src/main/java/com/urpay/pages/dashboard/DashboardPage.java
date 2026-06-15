package com.urpay.pages.dashboard;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

public class DashboardPage extends BasePage {

    private static final int NAV_BAR_Y = 2310;
    private static final int NAV_HOME_X = 146;
    private static final int NAV_PAYMENTS_X = 349;
    private static final int NAV_TRANSFER_X = 551;
    private static final int NAV_STORE_X = 754;
    private static final int NAV_MORE_X = 956;

    @AndroidFindBy(accessibility = "testID-master-amount-main")
    private WebElement yourBalanceLabel;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Top Up')]")
    private WebElement topUpButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Transactions')]")
    private WebElement transactionsButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Wallet')]")
    private WebElement walletButton;

    @Step("Navigate to Home tab")
    public void navigateToHome() { tapAtCoordinates(NAV_HOME_X, NAV_BAR_Y); }

    @Step("Navigate to Payments tab")
    public void navigateToPayments() { tapAtCoordinates(NAV_PAYMENTS_X, NAV_BAR_Y); }

    @Step("Navigate to Transfer tab")
    public void navigateToTransfer() { tapAtCoordinates(NAV_TRANSFER_X, NAV_BAR_Y); }

    @Step("Navigate to Store tab")
    public void navigateToStore() { tapAtCoordinates(NAV_STORE_X, NAV_BAR_Y); }

    @Step("Navigate to More tab")
    public void navigateToMore() { tapAtCoordinates(NAV_MORE_X, NAV_BAR_Y); }

    @Step("Tap Top Up button")
    public void clickTopUp() { tap(topUpButton); }

    @Step("Tap Transactions button")
    public void clickTransactions() { tap(transactionsButton); }

    @Step("Tap Wallet button")
    public void clickWallet() { tap(walletButton); }

    public boolean isLoaded() {
        dismissNotifications();
        return isDisplayed(yourBalanceLabel, 15);
    }

    @Step("Dismiss any popups (Notifications, maintenance)")
    public void dismissNotifications() {
        try {
            java.util.List<org.openqa.selenium.WebElement> laterBtns = waitUtils.findQuick(
                    io.appium.java_client.AppiumBy.xpath("//*[@text='Later']"), 3);
            if (!laterBtns.isEmpty()) {
                laterBtns.get(0).click();
                log.info("Dismissed Notifications popup");
            }
        } catch (Exception e) {
            log.debug("No popups to dismiss");
        }
    }

    @Step("Get balance text from dashboard")
    public String getBalanceText() {
        return getText(yourBalanceLabel);
    }
}
