package com.urpay.pages.wallet;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the Notifications screen.
 *
 * Accessed via the bell icon on the Dashboard.
 * Contains two tabs: Transactions and Promotions.
 *
 * Locators migrated from Katalon Object Repository:
 *   Katalon/Object Repository/android/WalletVas/Notifications/
 *
 * Locator notes:
 *   - NotificationsIcon uses parent content-desc with Bell suffix — best available.
 *   - NoNotificationsText uses a generated testID — flagged for stable testID ticket.
 *   - Header/Tab locators use text-based XPath — acceptable for static labels.
 */
public class NotificationsPage extends BasePage {

    // ── Bell icon (on Dashboard) ───────────────────────
    // Katalon used a complex SVG XPath; we use the parent's accessibility id which is more stable
    private static final By NOTIFICATIONS_ICON = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-IconView') and contains(@content-desc,'Bell')]");

    // ── Header ─────────────────────────────────────────
    @AndroidFindBy(xpath = "//*[@text='Notifications']")
    private WebElement notificationsHeader;

    // ── Tabs ───────────────────────────────────────────
    @AndroidFindBy(xpath = "//*[@text='Transactions']")
    private WebElement transactionsTab;

    @AndroidFindBy(xpath = "//*[@text='Promotions']")
    private WebElement promotionsTab;

    // ── Transaction notification ───────────────────────
    private static final By FIRST_TRANSACTION_NOTIFICATION = AppiumBy.xpath(
            "//*[@class='android.view.ViewGroup' and .//*[contains(@text,'New Beneficiary has been added')]]");

    private static final By FIRST_TRANSACTION_HEADER = AppiumBy.xpath(
            "//*[@text='New International benefciary']");

    // ── Empty state ────────────────────────────────────
    // TODO: Request stable testID — current one is auto-generated UUID
    @AndroidFindBy(accessibility = "testID-Text.1fbf1702-3e9d-4610-992e-6b17ff2028db")
    private WebElement noNotificationsText;

    // ── Back button ────────────────────────────────────
    @AndroidFindBy(accessibility = "testID-right-icon-item")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Notifications bell icon on Dashboard")
    public void openNotifications() {
        tap(NOTIFICATIONS_ICON);
    }

    @Step("Tap Transactions tab")
    public void tapTransactionsTab() {
        tap(transactionsTab);
    }

    @Step("Tap Promotions tab")
    public void tapPromotionsTab() {
        tap(promotionsTab);
    }

    @Step("Tap Back button to return from Notifications")
    public void tapBack() {
        tap(backButton);
    }

    // ══════════════════════════════════════════════════
    //  QUERIES (no assertions — return values for tests)
    // ══════════════════════════════════════════════════

    public boolean isLoaded() {
        return isDisplayed(notificationsHeader, 10);
    }

    public String getHeaderText() {
        return getText(notificationsHeader);
    }

    public String getTransactionsTabText() {
        return getText(transactionsTab);
    }

    public String getPromotionsTabText() {
        return getText(promotionsTab);
    }

    public boolean isFirstTransactionNotificationVisible() {
        return isPresent(FIRST_TRANSACTION_NOTIFICATION, 10);
    }

    public String getFirstTransactionNotificationHeader() {
        return getText(FIRST_TRANSACTION_HEADER);
    }

    public String getNoNotificationsText() {
        return getText(noNotificationsText);
    }

    public boolean isNoNotificationsTextVisible() {
        return isDisplayed(noNotificationsText, 5);
    }

    public boolean isBackButtonVisible() {
        return isDisplayed(backButton, 5);
    }
}
