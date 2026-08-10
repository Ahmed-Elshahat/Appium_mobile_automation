package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Wallet Balance (dashboard) operations — reveal, read and refresh the balance.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/WalletBalance/VerifyTotalMoneyCalculatedSuccessDashboard
 *   ({@code Wallet_VAS/WalletBalance/Get Wallet Balance/*}). The DB-preset recharge was removed
 *   upstream, so the suite now verifies the dashboard balance stays consistent across a refresh.
 *
 * <p>Locator notes (from the Object Repository {@code .rs} files):
 * <ul>
 *   <li>{@code balanceAmount} → {@code testID-master-amount-main}</li>
 *   <li>{@code eyeIconBtn} → SVG reveal toggle nested under {@code testID-Icons.3c4eedac-…}</li>
 * </ul>
 */
public class WalletBalancePage extends BasePage {

    // Katalon android/WalletVas/WalletBalance/balanceAmount
    private static final By BALANCE_AMOUNT =
            AppiumBy.xpath("//android.widget.TextView[@content-desc='testID-master-amount-main']");

    // Katalon android/WalletVas/WalletBalance/eyeIconBtn — SVG show/hide toggle.
    private static final By EYE_ICON = AppiumBy.xpath(
            "//*[@class='com.horcrux.svg.GroupView' and ./parent::*"
            + "[@content-desc='testID-Icons.3c4eedac-9f12-49c9-ae8b-01a187949e18']]"
            + "/*[@class='com.horcrux.svg.PathView']");

    @Step("Reveal the wallet balance (tap the eye icon only if the balance is not already shown)")
    public void revealBalance() {
        // If the balance amount is already visible, nothing to do.
        if (!waitUtils.findQuick(BALANCE_AMOUNT, 5).isEmpty()) {
            return;
        }
        // Balance is hidden — tap the eye icon to reveal it.
        List<WebElement> eye = waitUtils.findQuick(EYE_ICON, 5);
        if (!eye.isEmpty()) {
            try {
                eye.get(0).click();
            } catch (Exception ignored) {
                // toggle went stale — nothing to do
            }
        }
    }

    @Step("Read the wallet balance from the dashboard")
    public String getBalance() {
        return getText(BALANCE_AMOUNT);
    }

    @Step("Pull down to refresh the dashboard")
    public void refresh() {
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        swipeUtils.performSwipe(x, (int) (size.getHeight() * 0.35), x, (int) (size.getHeight() * 0.80));
        // Wait for the balance to re-render after the pull-to-refresh before reading it again.
        isPresent(BALANCE_AMOUNT, 20);
    }
}
