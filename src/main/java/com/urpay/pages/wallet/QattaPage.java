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
    // Cloud obfuscates testID-viewElemenSplitArrow, so also match the services tile by its "Qatta"
    // label (text is not obfuscated). Local keeps the testID as the first option.
    private static final By QATTA_TILE = AppiumBy.xpath(
            "//*[@content-desc='testID-viewElemenSplitArrow']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[@text='Qatta']]"
            + " | //*[@name='Qatta' or @label='Qatta']");

    // ── Any dashboard service tile (to locate the Services grid for carousel paging) ──
    private static final By ANY_SERVICE_TILE = AppiumBy.xpath(
            "//*[starts-with(@content-desc,'testID-viewElemen')]"
            + " | //*[starts-with(@name,'testID-viewElemen')]");

    // ── Header back button of a full-screen interstitial (e.g. cashback/rewards) some accounts
    //    land on after login; tapping it returns to the wallet dashboard. ──
    private static final By BACK_ICON = AppiumBy.xpath(
            "//*[@content-desc='testID-left-icon-back']"
            + " | //*[@name='testID-left-icon-back' or @label='testID-left-icon-back']");

    // ── Qatta landing ────────────────────────────────
    // Tabs: "Single Qatta" (…3.0) and "Group Qatta" (…3.1). The group tab MUST be
    // selected before "Add new Qatta", otherwise a single qatta is created. testID first, text fallback.
    private static final By GROUP_QATTA_OPTION = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.e7ce227b-5372-4836-b541-bb62889725d3.1']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[@text='Group Qatta']]"
            + " | //android.widget.TextView[@text='Group Qatta']");

    // "Single Qatta" tab (default selection); select it explicitly before "Add new Qatta"
    // to guarantee a single qatta is created rather than a group. testID first, text fallback.
    private static final By SINGLE_QATTA_OPTION = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.e7ce227b-5372-4836-b541-bb62889725d3.0']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[@text='Single Qatta']]"
            + " | //android.widget.TextView[@text='Single Qatta']");

    // "Add new …" entry — label is state-dependent: "Add new Qatta" when the group list is
    // empty, "Add new group" once groups exist. Match the common "Add new" prefix.
    // Platform-aware: Android TextView@text | iOS XCUIElementTypeStaticText@label/value/name.
    private static final By ADD_NEW_GROUP_BTN = AppiumBy.xpath(
            "//android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[starts-with(@text,'Add new')]]"
            + " | //android.widget.TextView[starts-with(@text,'Add new')]"
            + " | //XCUIElementTypeStaticText[starts-with(@label,'Add new')"
            + " or starts-with(@value,'Add new') or starts-with(@name,'Add new')]");

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
    /**
     * Open Qatta from the dashboard Services menu.
     *
     * Scrolls to the tile by its stable content-desc (UiScrollable stops exactly on the tile, no
     * overshoot), then taps and verifies the landing opened. The member dashboard's recent
     * "Qatta payments" rows carry no exact "Qatta" text, so a text/short-scroll fallback slid past
     * Services down into the bottom transactions list — hence we scroll by the tile's accessibility id.
     */
    /**
     * Open Qatta from the dashboard Services menu.
     *
     * Mirrors FamilyWalletPage.tapFamilyWallet: the Services menu is a HORIZONTAL carousel of
     * tile-grid pages and React Native does NOT render off-page tiles into the accessibility tree,
     * so vertical scrolling alone never surfaces the Qatta tile. Sequence: (1) vertical-scroll the
     * Services grid into the upper half of the screen, then (2) swipe the grid LEFT page-by-page
     * until the Qatta tile renders, then tap + verify the landing opened.
     */
    @Step("Open Qatta from dashboard services")
    public void openQatta() {
        ensureDashboardServicesVisible();
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

        // 2. Page the carousel horizontally LEFT until the Qatta tile renders, then tap + verify.
        for (int attempt = 0; attempt < 8; attempt++) {
            WebElement tile = findQattaTile();
            if (tile != null) {
                try {
                    tile.click();
                } catch (Exception ignored) {}
                if (isQattaLandingShown()) {
                    return;
                }
            }
            swipeServicesLeft();
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Could not find Qatta tile after scrolling services");
    }

    /**
     * Some accounts open a full-screen cashback/rewards interstitial right after login (it carries a
     * header back button and renders NO dashboard service tiles). Tapping back returns to the wallet
     * dashboard so the services-carousel scroll below does not drift down into the Transactions list.
     * No-op on the normal path: when the dashboard service tiles are already showing it returns at
     * once, and it never taps back unless a back button is actually present (the dashboard has none).
     */
    private void ensureDashboardServicesVisible() {
        for (int i = 0; i < 3; i++) {
            if (!waitUtils.findQuick(ANY_SERVICE_TILE, 2).isEmpty()) {
                return;
            }
            List<WebElement> back = waitUtils.findQuick(BACK_ICON, 1);
            if (back.isEmpty()) {
                return;
            }
            try {
                back.get(0).click();
            } catch (Exception ignored) {}
        }
    }

    /** Locate the Qatta tile (by id, then by "Qatta" label); null if not currently in the tree. */
    private WebElement findQattaTile() {
        List<WebElement> els = waitUtils.findQuick(QATTA_TILE, 1);
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

    /** A short (~15% of screen) upward scroll to reveal the Services grid without overshooting. */
    private void shortScrollDown() {
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.60);
        int endY = (int) (size.getHeight() * 0.45);
        swipeUtils.performSwipe(x, startY, x, endY);
    }

    /** True once the Qatta landing screen (its tabs / "Add new" entry) has opened. */
    private boolean isQattaLandingShown() {
        return !waitUtils.findQuick(ADD_NEW_GROUP_BTN, 4).isEmpty()
                || !waitUtils.findQuick(GROUP_QATTA_OPTION, 1).isEmpty();
    }

    @Step("Open the group qatta option")
    public void tapGroupQattaOption() {
        // Once a group exists the Group tab is pre-selected and may not render as a separate
        // clickable element; tap only if present, otherwise it is already active.
        List<WebElement> tabs = waitUtils.findQuick(GROUP_QATTA_OPTION, 3);
        if (!tabs.isEmpty()) {
            tabs.get(0).click();
        } else {
            log.info("Group Qatta tab not present (already selected) — proceeding");
        }
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
