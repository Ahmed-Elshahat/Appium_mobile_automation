package com.urpay.pages.dashboard;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

public class DashboardPage extends BasePage {

    // Navigation coordinates are calculated as ratios to support different screen sizes
    private static final double NAV_BAR_Y_RATIO = 0.96;
    private static final double NAV_HOME_X_RATIO = 0.13;
    private static final double NAV_PAYMENTS_X_RATIO = 0.32;
    private static final double NAV_TRANSFER_X_RATIO = 0.50;
    private static final double NAV_STORE_X_RATIO = 0.69;
    private static final double NAV_MORE_X_RATIO = 0.87;

    @AndroidFindBy(accessibility = "testID-master-amount-main")
    @iOSXCUITFindBy(accessibility = "testID-master-amount-main")
    private WebElement yourBalanceLabel;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Top Up')]")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Top Up'")
    private WebElement topUpButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Transactions')]")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Transactions'")
    private WebElement transactionsButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Wallet')]")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Wallet'")
    private WebElement walletButton;

    @Step("Navigate to Home tab")
    public void navigateToHome() { tapNavAt(NAV_HOME_X_RATIO); }

    @Step("Navigate to Payments tab")
    public void navigateToPayments() { tapNavAt(NAV_PAYMENTS_X_RATIO); }

    @Step("Navigate to Transfer tab")
    public void navigateToTransfer() { tapNavAt(NAV_TRANSFER_X_RATIO); }

    @Step("Navigate to Store tab")
    public void navigateToStore() { tapNavAt(NAV_STORE_X_RATIO); }

    @Step("Navigate to More tab")
    public void navigateToMore() { tapNavAt(NAV_MORE_X_RATIO); }

    private void tapNavAt(double xRatio) {
        Dimension size = driver.manage().window().getSize();
        int x = (int) (size.getWidth() * xRatio);
        int y = (int) (size.getHeight() * NAV_BAR_Y_RATIO);
        tapAtCoordinates(x, y);
    }

    @Step("Tap Top Up button")
    public void clickTopUp() { tap(topUpButton); }

    @Step("Tap Transactions button")
    public void clickTransactions() { tap(transactionsButton); }

    @Step("Tap Wallet button")
    public void clickWallet() { tap(walletButton); }

    public boolean isLoaded() {
        dismissNotifications();
        // Check balance label OR search icon (balance may be hidden with ******)
        return isDisplayed(yourBalanceLabel, 10)
                || isPresent(io.appium.java_client.AppiumBy.accessibilityId("testID-right-icon-0"), 5);
    }

    @Step("Dismiss any popups (Notifications, maintenance, consent)")
    public void dismissNotifications() {
        try {
            // "Later" button for notifications popup
            java.util.List<org.openqa.selenium.WebElement> laterBtns = waitUtils.findQuick(
                    io.appium.java_client.AppiumBy.xpath("//*[@text='Later']"), 3);
            if (!laterBtns.isEmpty()) {
                laterBtns.get(0).click();
                log.info("Dismissed Notifications popup");
            }
            // "×" close button for maintenance banner
            java.util.List<org.openqa.selenium.WebElement> closeBtns = waitUtils.findQuick(
                    io.appium.java_client.AppiumBy.xpath("//*[@text='×' or @text='✕' or @text='X']"), 1);
            if (!closeBtns.isEmpty()) {
                closeBtns.get(0).click();
                log.info("Dismissed maintenance banner");
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
