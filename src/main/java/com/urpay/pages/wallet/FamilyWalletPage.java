package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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

    // ── Fallback: find Family Wallets by visible label (platform-aware: Android @text | iOS @label/@value/@name) ────
    private static final By FAMILY_WALLET_TEXT = AppiumBy.xpath(
            "//*[@text='Family Wallets']"
            + " | //*[@label='Family Wallets' or @value='Family Wallets' or @name='Family Wallets']");

    // ── Any dashboard service tile (platform-aware: Android @content-desc | iOS @name) ──
    private static final By ANY_SERVICE_TILE = AppiumBy.xpath(
            "//*[starts-with(@content-desc,'testID-viewElemen')]"
            + " | //*[starts-with(@name,'testID-viewElemen')]");

    // ── Family Wallet screen marker (first kid row renders once the screen loads) ──
    private static final By FAMILY_MEMBER_MARKER =
            AppiumBy.accessibilityId("testID-data-0");

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

    @Step("Scroll services into view and tap Family Wallet")
    public void tapFamilyWallet() {
        // The dashboard Services menu is a HORIZONTAL carousel of tile-grid pages. The Family Wallet
        // tile (testID-viewElemenDashboardFamily) lives on the 3rd page, and React Native does NOT
        // render off-page tiles into the accessibility tree — so vertical scrolling alone never
        // surfaces it. Confirmed-on-device sequence: (1) scroll DOWN until the Services grid sits in
        // the upper half of the screen (a clean horizontal-swipe zone, clear of the bottom nav), then
        // (2) swipe the grid LEFT page-by-page until the Family tile renders, then tap + verify.
        final int screenHeight = driver.manage().window().getSize().getHeight();
        final int upperBand = (int) (screenHeight * 0.50);

        // 1. Scroll the Services grid up into the upper half of the screen.
        for (int i = 0; i < 12; i++) {
            WebElement svc = firstServiceTile();
            int y = (svc == null) ? -1 : safeY(svc);
            if (y >= 0 && y <= upperBand) {
                break;
            }
            shortScrollDown();
        }

        // 2. Page the carousel horizontally LEFT until the Family tile is rendered, then tap + verify.
        for (int attempt = 0; attempt < 8; attempt++) {
            WebElement tile = findFamilyTile();
            if (tile != null) {
                try {
                    tile.click();
                } catch (Exception ignored) {}
                if (isFamilyWalletScreenShown()) {
                    return;
                }
            }
            swipeServicesLeft();
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Could not find Family Wallets button after scrolling");
    }

    /** Locate the Family Wallet tile (by id, then by text); null if not currently in the tree. */
    private WebElement findFamilyTile() {
        List<WebElement> els = waitUtils.findQuick(FAMILY_WALLET_BTN, 1);
        if (els.isEmpty()) {
            els = waitUtils.findQuick(FAMILY_WALLET_TEXT, 1);
        }
        return els.isEmpty() ? null : els.get(0);
    }

    /** First dashboard service tile currently rendered, or null if the Services grid isn't in view. */
    private WebElement firstServiceTile() {
        List<WebElement> tiles = waitUtils.findQuick(ANY_SERVICE_TILE, 1);
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    /** Swipe the services carousel one page to the LEFT, anchored on the services row's height. */
    private void swipeServicesLeft() {
        Dimension size = driver.manage().window().getSize();
        int y;
        WebElement svc = firstServiceTile();
        if (svc != null) {
            try {
                var r = svc.getRect();
                y = r.getY() + (r.getHeight() / 2);
            } catch (Exception e) {
                y = (int) (size.getHeight() * 0.35);
            }
        } else {
            y = (int) (size.getHeight() * 0.35);
        }
        int startX = (int) (size.getWidth() * 0.80);
        int endX = (int) (size.getWidth() * 0.20);
        swipeUtils.performSwipe(startX, y, endX, y);
    }

    /** Y coordinate of an element, or -1 if it cannot be read. */
    private int safeY(WebElement el) {
        try {
            return el.getRect().getY();
        } catch (Exception e) {
            return -1;
        }
    }

    /** True once the Family Wallet screen (first kid row) has opened. */
    private boolean isFamilyWalletScreenShown() {
        return !waitUtils.findQuick(FAMILY_MEMBER_MARKER, 4).isEmpty();
    }

    /** A short (~15% of screen) upward scroll to reveal the Services grid without overshooting. */
    private void shortScrollDown() {
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.60);
        int endY = (int) (size.getHeight() * 0.45);
        swipeUtils.performSwipe(x, startY, x, endY);
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
