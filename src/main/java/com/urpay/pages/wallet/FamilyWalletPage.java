package com.urpay.pages.wallet;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Family Wallet page — handles navigation to Family Wallet,
 * kid profile selection, and related actions.
 *
 * Katalon source: Object Repository/android/WalletVas/FamilyWallet/
 */
public class FamilyWalletPage extends BasePage {

    // ── Family Wallet button on Dashboard services ────
    private static final By FAMILY_WALLET_BTN =
            AppiumBy.accessibilityId("testID-viewElemenDashboardFamily");

    // ── Fallback: find Family Wallets by text ────
    private static final By FAMILY_WALLET_TEXT =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Family Wallets\")");

    // ── Services container on Dashboard (swipe target) ────
    private static final By SERVICES_CONTAINER =
            AppiumBy.accessibilityId("testID-View.21377c72-ed2e-4c8d-8e34-7f6eae384aea.1");

    // ── First family member (kid) ─────────────────────
    @AndroidFindBy(accessibility = "testID-data-0")
    @iOSXCUITFindBy(accessibility = "testID-data-0")
    private WebElement firstFamilyMember;

    // ── Back button from kid profile ──────────────────
    @AndroidFindBy(accessibility = "testID-right-icon-item")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-item")
    private WebElement backProfileButton;

    // ── Exit button ───────────────────────────────────
    @AndroidFindBy(accessibility = "testID-right-icon-0")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-0")
    private WebElement exitButton;

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Scroll to services menu and tap Family Wallet")
    public void tapFamilyWallet() {
        // First try direct find without scrolling (by accessibilityId or text)
        if (tryTapFamilyWallet()) return;

        // Step 1: Scroll down a little to bring Services section fully into view
        swipeUp();

        // Step 2: Try tapping Family Wallet directly (it may already be visible)
        if (tryTapFamilyWallet()) return;

        // Step 3: If still not found, try swiping within services container
        try {
            WebElement container = driver.findElement(SERVICES_CONTAINER);
            swipeLeftWithinElement(container);
            swipeLeftWithinElement(container);
        } catch (Exception ignored) {
            // Container not found, try full-screen swipe left instead
            swipeUtils.swipeLeft();
        }

        // Step 4: Final attempt to tap
        if (!tryTapFamilyWallet()) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Could not find Family Wallets button after scrolling");
        }
    }

    /**
     * Try to tap Family Wallet by accessibility ID or text. Returns true if tapped.
     */
    private boolean tryTapFamilyWallet() {
        try {
            WebElement element = driver.findElement(FAMILY_WALLET_BTN);
            if (element.isDisplayed()) {
                element.click();
                return true;
            }
        } catch (Exception ignored) {}

        try {
            WebElement element = driver.findElement(FAMILY_WALLET_TEXT);
            if (element.isDisplayed()) {
                element.click();
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * Swipe left within a specific element's bounds (not full-screen).
     */
    private void swipeLeftWithinElement(WebElement element) {
        int x = element.getLocation().getX();
        int y = element.getLocation().getY();
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();

        int startX = x + (int) (width * 0.8);
        int endX = x + (int) (width * 0.2);
        int centerY = y + (height / 2);

        swipeUtils.performSwipe(startX, centerY, endX, centerY);
    }

    @Step("Wait for Family Wallet screen to load")
    public void waitUntilLoaded() {
        waitUtils.waitForVisible(firstFamilyMember, 30);
    }

    @Step("Tap first family member (kid profile)")
    public void tapFirstFamilyMember() {
        tap(firstFamilyMember);
    }

    @Step("Tap back button from profile")
    public void tapBack() {
        tap(backProfileButton);
    }

    @Step("Tap exit button")
    public void tapExit() {
        tap(exitButton);
    }

    public boolean isLoaded() {
        return isDisplayed(firstFamilyMember, 15);
    }
}
