package com.urpay.pages.dashboard;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.core.ConfigManager;

import io.appium.java_client.AppiumBy;
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

    // ── Popup dismiss ──────────────────────────────────
    private static final By LATER_BTN =
            AppiumBy.xpath("//*[@text='Later']");
    private static final By CLOSE_BTN =
            AppiumBy.xpath("//*[@text='×' or @text='✕' or @text='X']");
    // Cash Prizes and similar promo popups use a CrossV3/CrossV4 SVG icon — match the clickable parent.
    private static final By CROSS_POPUP_BTN =
            AppiumBy.xpath("//*[contains(@content-desc,'CrossV3') or contains(@content-desc,'CrossV4')]"
                    + "/ancestor-or-self::*[@clickable='true'][1]");
    // The app requests POST_NOTIFICATIONS at runtime AFTER login, so the Android system
    // notification-permission dialog renders a few seconds LATE on top of the dashboard. It lives
    // in a separate permissioncontroller window that hides the dashboard elements from the a11y
    // tree, so it must be dismissed before the dashboard marker can be found. Match the stable
    // system resource-ids first (immune to the app's testID obfuscation), with visible-text
    // fallbacks across OEM/locale variants.
    private static final By SYSTEM_DIALOG = AppiumBy.xpath(
            "//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button' "
            + "or @resource-id='com.android.permissioncontroller:id/permission_allow_foreground_only_button' "
            + "or @resource-id='com.android.permissioncontroller:id/permission_allow_one_time_button' "
            + "or @text='No thanks' or @text='NO THANKS' or @text='Allow' "
            + "or @text='ALLOW' or @text='While using the app' or @text='Only this time']");

    // ── Search icon (dashboard presence marker) ────────
    private static final By SEARCH_ICON =
            AppiumBy.accessibilityId("testID-right-icon-0");

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
        // The dashboard can be hidden behind a late Android notification-permission dialog (it
        // appears a few seconds after login and lives in a separate system window). Dismiss any
        // popup / system dialog, then check the marker — retry a few times to bridge the gap until
        // the late dialog has appeared and been cleared.
        for (int attempt = 0; attempt < 4; attempt++) {
            dismissPopups();
            if (isDisplayed(yourBalanceLabel, 5) || isPresent(SEARCH_ICON, 2)) {
                return true;
            }
        }
        return false;
    }

    @Step("Dismiss any popups (Later, close, system permission dialog)")
    public void dismissPopups() {
        // Instant check: with the global implicit wait (timeout=20s), findQuick on an
        // ABSENT popup blocks ~20s per locator (implicit+explicit wait mixing) — ~40s
        // wasted on every call when there is no popup. Drop implicit wait to 0 so
        // findElements returns immediately, then restore it.
        long implicit = ConfigManager.getInstance().getInt("timeout", 10);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        try {
            clickIfPresentNow(SYSTEM_DIALOG, "system permission dialog");
            clickIfPresentNow(LATER_BTN, "Later");
            clickIfPresentNow(CLOSE_BTN, "close button");
            clickIfPresentNow(CROSS_POPUP_BTN, "promo popup X");
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicit));
        }
    }

    /** Click the first matching element only if it is already on screen (no waiting). */
    private void clickIfPresentNow(By locator, String label) {
        try {
            List<WebElement> els = driver.findElements(locator);
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                els.get(0).click();
                log.info("Dismissed popup via '{}'", label);
            }
        } catch (Exception ignored) {
            // not present or went stale — nothing to dismiss
        }
    }

    @Step("Dismiss system dialogs (Allow, Skip, No thanks)")
    public void dismissSystemDialogs() {
        try {
            List<WebElement> dialogs = waitUtils.findQuick(SYSTEM_DIALOG, 2);
            for (WebElement el : dialogs) {
                try {
                    if (el.isDisplayed()) {
                        el.click();
                        log.info("Dismissed system dialog");
                        break;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public boolean isSearchIconVisible(long timeoutSec) {
        return isPresent(SEARCH_ICON, timeoutSec);
    }

    @Step("Get balance text from dashboard")
    public String getBalanceText() {
        return getText(yourBalanceLabel);
    }
}
