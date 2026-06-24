package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Qatta module landing page.
 *
 * Migrated from Katalon:
 *   Test Cases/Wallet_VAS/Qatta/ToNavigateToQatta
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/ToCreateNewQattaGroup (entry steps)
 *   Object Repository/android/WalletVas/QattaModule/*
 *
 * Navigation path (per Katalon):
 *   Dashboard services carousel → swipe to Qatta tile → tap → Qatta landing
 *   → tap "Group" qatta option → tap "Add new group".
 *
 * Locator notes:
 *   - Qatta tile, group tab keep their dedicated testIDs.
 *   - "Add new group" reuses the generic ReactText testID, so it is disambiguated by text.
 */
public class QattaPage extends BasePage {

    // ── Dashboard services → Qatta tile ──────────────
    private static final By QATTA_TILE =
            AppiumBy.accessibilityId("testID-viewElemenSplitArrow");

    // ── Qatta landing ────────────────────────────────
    // Tabs: "Single Qatta" (…3.0) and "Group Qatta" (…3.1). The group tab MUST be
    // selected before "Add new Qatta", otherwise a single qatta is created.
    private static final By GROUP_QATTA_OPTION =
            AppiumBy.accessibilityId("testID-Text.e7ce227b-5372-4836-b541-bb62889725d3.1");

    // "Single Qatta" tab (default selection); select it explicitly before "Add new Qatta"
    // to guarantee a single qatta is created rather than a group.
    private static final By SINGLE_QATTA_OPTION =
            AppiumBy.accessibilityId("testID-Text.e7ce227b-5372-4836-b541-bb62889725d3.0");

    // "Add new …" entry — label is state-dependent: "Add new Qatta" when the group list is
    // empty, "Add new group" once groups exist. Match the common "Add new" prefix.
    private static final By ADD_NEW_GROUP_BTN = AppiumBy.xpath(
            "//android.widget.TextView[starts-with(@text,'Add new')]");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    /**
     * Bring the Qatta tile into view and open it.
     *
     * On the dashboard the "Services" row (which holds the Qatta tile) sits just below the fold,
     * so a few SHORT vertical scrolls are needed to surface it before tapping. Scrolling stops as
     * soon as the tile is actually on screen, so we never overshoot the Services row. Tapping an
     * off-screen tile (found in the view tree but below the viewport) silently misses, which is
     * why a plain find-and-tap left the app stranded on the dashboard.
     */
    @Step("Open Qatta from dashboard services")
    public void openQatta() {
        for (int i = 0; i < 6 && !isQattaTileOnScreen(); i++) {
            shortScrollDown();
        }
        tap(QATTA_TILE);
    }

    /** True only when the Qatta tile is rendered within the visible viewport (not just in the tree). */
    private boolean isQattaTileOnScreen() {
        List<WebElement> els = waitUtils.findQuick(QATTA_TILE, 1);
        if (els.isEmpty()) {
            return false;
        }
        try {
            int y = els.get(0).getRect().getY();
            int screenHeight = driver.manage().window().getSize().getHeight();
            return y > 0 && y < (int) (screenHeight * 0.9);
        } catch (Exception e) {
            return false;
        }
    }

    /** A short (~15% of screen) upward scroll to reveal the Services row without overshooting. */
    private void shortScrollDown() {
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.60);
        int endY = (int) (size.getHeight() * 0.45);
        swipeUtils.performSwipe(x, startY, x, endY);
    }

    @Step("Open the group qatta option")
    public void tapGroupQattaOption() {
        tap(GROUP_QATTA_OPTION);
    }

    @Step("Open the single qatta option")
    public void tapSingleQattaOption() {
        // "Single Qatta" is the default-active tab; when active it does not render as a separate
        // tappable element (only the inactive Group tab does). Tap it only if it is present,
        // otherwise it is already selected — mirroring Katalon which taps "Add new" directly.
        List<WebElement> tabs = waitUtils.findQuick(SINGLE_QATTA_OPTION, 2);
        if (!tabs.isEmpty()) {
            tabs.get(0).click();
        } else {
            log.info("Single Qatta tab not present (already selected) — proceeding");
        }
    }

    @Step("Tap 'Add new group'")
    public void tapAddNewGroup() {
        tap(ADD_NEW_GROUP_BTN);
    }

    public boolean isLoaded() {
        return isPresent(GROUP_QATTA_OPTION, 30);
    }
}
