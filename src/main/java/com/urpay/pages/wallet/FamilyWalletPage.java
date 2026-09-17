package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

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

        private static final By FAMILY_WALLET_SCREEN = AppiumBy.xpath(
            "//*[@text='Family Wallets' or @text='Family Wallet' or @text='Get Started!' "
            + "or @content-desc='testID-data-0']");

        private static final By GET_STARTED_BTN = AppiumBy.xpath(
            "//*[@text='Get Started!' or @text='Get Started' or @text='Add child' "
            + "or @text='Add Kid' or @text='Add family member']"
            + "/ancestor-or-self::*[@clickable='true'][1]");

        private static final By KID_POI_INPUT = AppiumBy.xpath(
            "//android.widget.EditText[@content-desc='testID-input-direct-childIdNumber' "
            + "or @content-desc='testID-input-direct-idNumber' "
            + "or contains(@content-desc,'childIdNumber')]");

        private static final By PRIMARY_ACTION_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-action-main' or @content-desc='testID-primary--main' "
            + "or @text='Next' or @text='Send Request' or @text='Send request' or @text='Continue']"
            + "/ancestor-or-self::*[@clickable='true'][1]");

        private static final By PRIMARY_ACTION_EXACT = AppiumBy.accessibilityId("testID-primary--main");

        private static final By REQUEST_SENT_MARKER = AppiumBy.xpath(
            "//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'request') "
            + "and (contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sent') "
            + "or contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success'))]");

        private static final By DASHBOARD_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-DashboardHome' or @content-desc='testID-master-amount-main']");

        private static final By PENDING_FAMILY_MEMBER = AppiumBy.xpath(
            "//*[@text='Pending' and ancestor::*[.//android.widget.TextView[contains(@text,'Kid') "
            + "or contains(@text,'mobile') or contains(@text,'Request')]]]");

        private static final By FAMILY_WIZARD_STEP_2_OR_3 = AppiumBy.xpath(
            "//*[@text='Step 2/3' or @text='Step 3/3']");

        private static final By FAMILY_WIZARD_STEP_2 = AppiumBy.xpath(
            "//*[@text='Step 2/3' or @text=\"Kid’s mobile number\" or @text=\"Kid's mobile number\"]");

        private static final By FAMILY_WIZARD_STEP_3 = AppiumBy.xpath(
            "//*[@text='Step 3/3' or @text=\"Kid’s date of birth\" or @text=\"Kid's date of birth\"]");

        private static final By KID_MOBILE_INPUT = AppiumBy.xpath(
            "//android.widget.EditText[@content-desc='testID-input-direct-undefined' "
            + "or contains(@content-desc,'mobile') or contains(@text,'05')]");

        private static final By KID_HIJRI_DOB_INPUT = AppiumBy.xpath(
            "//android.widget.EditText[@content-desc='testID-input-direct-undefined']");

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
                if (isFamilyWalletScreenShown() || isFamilyWalletEntryShown()) {
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

    private boolean isFamilyWalletEntryShown() {
        return !waitUtils.findQuick(FAMILY_WALLET_SCREEN, 4).isEmpty();
    }

    @Step("Send family link request to kid POI {kidPoi}")
    public boolean sendFamilyRequestToKid(String kidPoi, String kidMobile, String kidHijriDob) {
        if (isPresent(PENDING_FAMILY_MEMBER, 5)) {
            log.info("Family request is already Pending for the configured kid");
            return true;
        }
        if (isPresent(GET_STARTED_BTN, 8)) {
            tap(GET_STARTED_BTN);
        }
        for (int step = 0; step < 3 && !isPresent(KID_POI_INPUT, 6); step++) {
            if (isPrimaryActionEnabled(4)) {
                tapPrimaryActionCenter();
            }
        }
        if (!isPresent(KID_POI_INPUT, 10)) {
            dumpPageSource("family-wallet-send-request-no-poi-input");
            return false;
        }
        enterKidPoiDigits(kidPoi);
        log.info("Entered kid POI {} on Create Family Wallet step 1", kidPoi);
        platformActions.dismissKeyboard();
        for (int attempt = 1; attempt <= 3 && isPresent(KID_POI_INPUT, 2); attempt++) {
            log.info("Create Family Wallet Step 1 Next attempt {}", attempt);
            attemptPrimaryAction("element click", this::clickPrimaryActionElement);
            if (isPresent(KID_POI_INPUT, 2)) {
                attemptPrimaryAction("center tap", this::tapPrimaryActionCenter);
            }
            if (isPresent(KID_POI_INPUT, 2)) {
                attemptPrimaryAction("upper-center tap", this::tapPrimaryActionUpperCenter);
            }
            if (isPresent(KID_POI_INPUT, 2)) {
                attemptPrimaryAction("mobile clickGesture", this::clickPrimaryActionMobileGesture);
            }
            if (isPresent(KID_POI_INPUT, 2)) {
                attemptPrimaryAction("element-id clickGesture", this::clickPrimaryActionByElementIdGesture);
            }
            if (isPresent(KID_POI_INPUT, 2)) {
                attemptPrimaryAction("element-id longClickGesture", this::longClickPrimaryActionByElementIdGesture);
            }
        }
        if (isPresent(FAMILY_WIZARD_STEP_2, 30)) {
            enterKidMobileDigitsWithRetry(kidMobile);
            log.info("Entered kid mobile {} on Create Family Wallet step 2", kidMobile);
            platformActions.dismissKeyboard();
            tapPrimaryActionWhenEnabled();
        }
        if (isPresent(FAMILY_WIZARD_STEP_3, 8)) {
            enterKidHijriDob(kidHijriDob);
            log.info("Entered kid Hijri DOB {} on Create Family Wallet step 3", kidHijriDob);
            platformActions.dismissKeyboard();
            tapPrimaryActionWhenEnabled();
        }
        for (int step = 0; step < 4 && !isFamilyRequestSubmissionComplete(); step++) {
            if (isPresent(FAMILY_WIZARD_STEP_2, 5) && isPresent(KID_MOBILE_INPUT, 5)) {
                enterKidMobileDigitsWithRetry(kidMobile);
                platformActions.dismissKeyboard();
                tapPrimaryActionWhenEnabled();
            }
            if (isPresent(FAMILY_WIZARD_STEP_2_OR_3, 2) || isPrimaryActionEnabled(2)) {
                if (isPresent(FAMILY_WIZARD_STEP_3, 1) && isPresent(KID_HIJRI_DOB_INPUT, 1)) {
                    enterKidHijriDob(kidHijriDob);
                    platformActions.dismissKeyboard();
                }
                tapPrimaryActionCenter();
            }
        }
        boolean sent = isFamilyRequestSubmissionComplete();
        if (!sent) {
            dumpPageSource("family-wallet-send-request-after-submit");
        }
        return sent;
    }

    private boolean isFamilyRequestSubmissionComplete() {
        return isPresent(REQUEST_SENT_MARKER, 2)
                || isPresent(DASHBOARD_MARKER, 2)
                || isPresent(PENDING_FAMILY_MEMBER, 2);
    }

    private void clickPrimaryActionElement() {
        WebElement button = waitForPrimaryAction();
        button.click();
        log.info("Clicked primary action element");
    }

    private void attemptPrimaryAction(String strategy, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.warn("Primary action {} failed; trying next fallback: {}", strategy, e.getMessage());
        }
    }

    private void enterKidPoiDigits(String kidPoi) {
        WebElement input = waitUtils.waitForClickable(KID_POI_INPUT, 15);
        input.click();
        input.clear();
        platformActions.clearDigits(kidPoi.length() + 2);
        platformActions.enterDigits(kidPoi);
    }

    private void enterKidMobileDigits(String kidMobile) {
        WebElement input = waitUtils.waitForClickable(KID_MOBILE_INPUT, 15);
        input.click();
        input.clear();
        platformActions.clearDigits(kidMobile.length() + 2);
        platformActions.enterDigits(kidMobile);
        String visibleValue = input.getAttribute("text");
        if (visibleValue == null || !visibleValue.replaceAll("[^0-9]", "").endsWith(kidMobile)) {
            input.clear();
            input.sendKeys(kidMobile);
        }
        log.info("Kid mobile input value after entry: {}", input.getAttribute("text"));
    }

    private void enterKidMobileDigitsWithRetry(String kidMobile) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            enterKidMobileDigits(kidMobile);
            WebElement input = waitUtils.waitForVisible(KID_MOBILE_INPUT, 5);
            String visibleValue = input.getAttribute("text");
            if (visibleValue != null && visibleValue.replaceAll("[^0-9]", "").endsWith(kidMobile)) {
                log.info("Kid mobile committed in Step 2 on attempt {}", attempt);
                return;
            }
            log.warn("Kid mobile was not committed on Step 2 attempt {}: {}", attempt, visibleValue);
            platformActions.dismissKeyboard();
        }
        throw new IllegalStateException("Kid mobile was not committed in Create Family Wallet Step 2");
    }

    private void enterKidHijriDob(String kidHijriDob) {
        String[] parts = kidHijriDob.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Hijri DOB must use DD.MM.YYYY format");
        }
        WebElement input = waitUtils.waitForClickable(KID_HIJRI_DOB_INPUT, 15);
        input.click();
        input.clear();
        input.sendKeys(parts[0]);
        input.sendKeys(parts[1]);
        input.sendKeys(parts[2]);
    }

    private boolean isPrimaryActionEnabled(long timeoutSec) {
        try {
            WebElement button = waitUtils.waitForVisible(PRIMARY_ACTION_BTN, timeoutSec);
            return button.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    private void tapPrimaryActionWhenEnabled() {
        if (isPrimaryActionEnabled(10)) {
            tapPrimaryActionCenter();
        }
    }

    private void tapPrimaryActionCenter() {
        WebElement button = waitForPrimaryAction();
        var rect = button.getRect();
        tapAtCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
        log.info("Tapped primary action center fallback");
    }

    private void tapPrimaryActionUpperCenter() {
        WebElement button = waitForPrimaryAction();
        var rect = button.getRect();
        tapAtCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + Math.max(20, rect.getHeight() / 3));
        log.info("Tapped primary action upper-center fallback");
    }

    private void clickPrimaryActionMobileGesture() {
        WebElement button = waitForPrimaryAction();
        var rect = button.getRect();
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("x", rect.getX() + rect.getWidth() / 2);
        args.put("y", rect.getY() + rect.getHeight() / 2);
        driver.executeScript("mobile: clickGesture", args);
        log.info("Clicked primary action via mobile: clickGesture fallback");
    }

    private void clickPrimaryActionByElementIdGesture() {
        WebElement button = waitForPrimaryAction();
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("elementId", ((RemoteWebElement) button).getId());
        driver.executeScript("mobile: clickGesture", args);
        log.info("Clicked primary action via elementId clickGesture fallback");
    }

    private void longClickPrimaryActionByElementIdGesture() {
        WebElement button = waitForPrimaryAction();
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("elementId", ((RemoteWebElement) button).getId());
        args.put("duration", 500);
        driver.executeScript("mobile: longClickGesture", args);
        log.info("Long-clicked primary action via elementId fallback");
    }

    private WebElement waitForPrimaryAction() {
        if (isPresent(PRIMARY_ACTION_EXACT, 2)) {
            return waitUtils.waitForClickable(PRIMARY_ACTION_EXACT, 15);
        }
        return waitUtils.waitForClickable(PRIMARY_ACTION_BTN, 15);
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
