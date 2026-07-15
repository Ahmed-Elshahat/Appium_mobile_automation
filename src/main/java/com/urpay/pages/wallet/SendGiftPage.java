package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.pages.auth.OtpPage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Send Gift (Eidya) operations — parent sends a gift to a kid, and the kid opens it.
 *
 * <p>Migrated from Katalon suites:
 * <ul>
 *   <li>Test Suites/.../WMVSuites/SendGift/Family Wallet Send Gift From Parent to Kid Test Suite
 *       ({@code Wallet_VAS/SendGifts/ParentValidationTCs/*})</li>
 *   <li>Test Suites/.../WMVSuites/SendGift/Family Wallet Kid Receive Gift from Parent
 *       ({@code Wallet_VAS/SendGifts/KidValidationTCs/*})</li>
 * </ul>
 *
 * <p>The Katalon {@code SelectGiftDetails.selectRandomGiftDetails} keyword library (random message +
 * amount) is not portable, so the migrated flow uses a fixed gift message and amount supplied via
 * config. The gift type is fixed to "Marriage" (the WMV suite's chosen type).
 */
public class SendGiftPage extends BasePage {

    // ── Navigation: the "Gifts" dashboard service tile ──
    // The correct entry is the plain "Gifts" service — NOT "Eidya Gift" (testID-viewElemenEidyaGiftMain,
    // which now opens the "Gift Hunting" AR game) and NOT any game gift. React Native does not render
    // off-page carousel tiles into the a11y tree, so we page the Services carousel horizontally and
    // match the tile by its visible "Gifts" label (exact @text, so it never matches "Eidya Gift" /
    // "Gift Hunting"), with the testID variants as fallbacks.
    private static final By GIFTS_TILE = AppiumBy.xpath(
            "//*[@content-desc='testID-viewElemenGiftMain'"
            + " or @content-desc='testID-viewElemenGifts'"
            + " or @content-desc='testID-viewElemendashboard#Gifts']"
            + " | //android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Gifts']]");
    private static final By ANY_SERVICE_TILE =
            AppiumBy.xpath("//*[starts-with(@content-desc,'testID-viewElemen')]");
    // The Gifts landing header (marks a successful navigation).
    private static final By GIFTS_HEADER = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Gifts']");
    // First-visit "Send Gifts" intro screen — its "Try it Now" button must be tapped before the
    // send wizard is reachable. testID is build-specific, so match by the visible label.
    private static final By TRY_IT_NOW_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onSubmit-main' or @content-desc='testID-primary-action-main']"
            + " | //android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Try it Now']]");
    // ── Return-to-dashboard-root helpers (kid/parent login can leave a pushed sub-screen) ──
    private static final By HOME_TAB =
            AppiumBy.accessibilityId("testID-dashboard");
    private static final By INAPP_BACK_BUTTON =
            AppiumBy.accessibilityId("testID-left-icon-back");

    // ── Send wizard ──
    // The "Send new gift" entry — testID may be hashed on the LT build, so match the testID or the
    // visible label. Skipped when the app lands straight on the contact search.
    private static final By SEND_NEW_GIFT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-action-main']"
            + " | //android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Send new gift' or @text='Send a gift'"
            + " or @text='Send Gift' or @text='New gift']]");
    // New build inserts a "Gift Type" step (Cash vs Store Item) before contact search — pick Cash
    // for a cash gift. Match by the visible "Cash" card label.
    private static final By CASH_GIFT_TYPE = AppiumBy.xpath(
            "//android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Cash']]"
            + " | //android.widget.TextView[@text='Cash']");
    // The contact search box is a CONTAINER wrapper — typing must target its child EditText
    // (mirrors InternationalTransferPage.SEARCH_INPUT), otherwise the wrapper only focuses and the
    // text never registers.
    private static final By SEARCH_BAR = AppiumBy.xpath(
            "//*[@content-desc='testID-Search-Input-Container']//android.widget.EditText"
            + " | //*[@content-desc='testID-Search-Input']//android.widget.EditText"
            + " | //android.widget.EditText");
    private static final By FIRST_CONTACT = AppiumBy.xpath(
            "//*[@content-desc='testID-contacts-number-0']"
            + " | //*[contains(@content-desc,'testID-contacts-number')]"
            + " | //*[contains(@content-desc,'testID-search-item-0')]");
    private static final By MARRIAGE_GIFT_TYPE = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-Text.7f1afd0e-5c80-40d6-bc3e-85300c52bf3e'"
            + " and @text='Marriage']");
    private static final By GIFT_MESSAGE_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-message");
    // testID-primary-onSubmit-main is reused for Next / Done / Open on their respective screens.
    // The middle action segment is hashed on the LT build, and the previous screen's "Next" button
    // lingers in the RN tree (both the Gift-Types and Enter-amount screens have "Next"). The current
    // screen mounts LAST in document order, so target the LAST visible "Next" button.
    private static final By NEXT_BTN = AppiumBy.xpath(
            "(//android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Next']])[last()]");
    private static final By GIFT_AMOUNT_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-TextInput.d81d3b37-7d22-410e-bb9b-ac500e5b5a67']");
    // The app caps a sender's outstanding (unopened) gifts. At the cap, tapping Next on the amount
    // screen shows this inline error and stays on the amount screen (no Confirm button).
    private static final By GIFT_LIMIT_ERROR = AppiumBy.xpath(
            "//*[contains(@text,'unopened gifts')]");
    // testID-primary-onConfirm-main is hashed on the LT build, and a "|" union combined with a
    // relative ".//" predicate throws in the UiAutomator2 xpath engine (swallowed -> 15s timeout).
    // Mirror the proven NEXT_BTN pattern instead: match the CLICKABLE ViewGroup by its visible
    // "Confirm" label, taking the LAST match (the current screen mounts last in document order).
    private static final By CONFIRM_BTN = AppiumBy.xpath(
            "(//android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Confirm']])[last()]");
    private static final By THANK_YOU_TEXT =
            AppiumBy.accessibilityId("testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5");
    private static final By DONE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onSubmit-main']"
            + " | //android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Done']]");

    // ── Gift lists (Sent / Received tabs) ──
    // The tab testID is build-specific/hashed, so match the tab by its visible label.
    private static final By SENT_TAB = AppiumBy.xpath(
            "//*[@content-desc='testID-Tabs.6f2c265b-839c-4595-9a08-309f15055da0.1']"
            + " | //android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Sent']]"
            + " | //android.widget.TextView[@text='Sent']");
    private static final By RECEIVED_TAB = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Received']"
            + " | //android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Received']]"
            + " | //android.widget.TextView[@text='Received']");
    private static final By NOT_OPENED_STATUS =
            AppiumBy.accessibilityId("testID-Text.3825beaf-7c7e-4740-9920-c9a65f76dc79.0");
    private static final By OPENED_STATUS =
            AppiumBy.accessibilityId("testID-Text.cf451cef-4ea3-43de-b402-c5a68d73a30e.0");
    private static final By GIFT_TYPE_LABEL =
            AppiumBy.accessibilityId("testID-Text.5f5d82a4-8b8f-4656-b8f9-aa0e7972d668.0");
    private static final By LATEST_GIFT =
            AppiumBy.accessibilityId("testID-data-0");
    private static final By OPEN_GIFT_BTN =
            AppiumBy.accessibilityId("testID-primary-onSubmit-main");

    // ══════════════════════════════════════════════════
    //  NAVIGATION — open the Gifts (Eidya) service
    // ══════════════════════════════════════════════════

    @Step("Open the Gifts service from the dashboard")
    public void navigateToGifts() {
        // The kid/parent login can leave the app on a pushed sub-screen (e.g. Transactions) whose
        // list would be scrolled instead of the Services carousel — return to the dashboard root
        // first (mirrors FamilyMissionPage.navigateToMissionsFromKidDashboard).
        returnToDashboardRoot();

        // 1. Scroll VERTICALLY until the Services grid renders in the upper half of the screen.
        final int screenHeight = driver.manage().window().getSize().getHeight();
        final int upperBand = (int) (screenHeight * 0.50);

        for (int i = 0; i < 12; i++) {
            WebElement svc = firstServiceTile();
            int y = (svc == null) ? -1 : safeY(svc);
            if (y >= 0 && y <= upperBand) {
                break;
            }
            shortScrollUp();
        }

        // 2. Page the carousel HORIZONTALLY (left) until the "Gifts" tile renders, then tap it.
        for (int attempt = 0; attempt < 10; attempt++) {
            List<WebElement> tiles = waitUtils.findQuick(GIFTS_TILE, 1);
            if (!tiles.isEmpty()) {
                try {
                    tiles.get(0).click();
                } catch (Exception ignored) {
                    // stale tile — re-page and retry
                }
                if (isPresent(GIFTS_HEADER, 6) || isPresent(SEND_NEW_GIFT_BTN, 3)) {
                    dismissGiftsIntroIfPresent();
                    return;
                }
            }
            swipeServicesLeft();
        }
        dumpPageSource("sendGift-noGiftsTile");
        throw new org.openqa.selenium.NoSuchElementException(
                "Could not find the 'Gifts' tile on the dashboard after scrolling"
                + " (not 'Eidya Gift' / not a game gift)");
    }

    /**
     * The first visit to the Gifts service shows a "Send Gifts" intro with a "Try it Now" button;
     * tap it to reach the send wizard. A no-op on subsequent visits (intro not shown).
     */
    private void dismissGiftsIntroIfPresent() {
        if (isPresent(TRY_IT_NOW_BTN, 3)) {
            tap(TRY_IT_NOW_BTN);
        }
    }

    /**
     * Ensure the dashboard ROOT is showing before searching the Services carousel. Pops any pushed
     * sub-screen (e.g. Transactions) via its in-app back button, then taps the Home bottom-nav tab.
     * Idempotent — a no-op when already on the dashboard root.
     */
    private void returnToDashboardRoot() {
        for (int i = 0; i < 3 && isPresent(INAPP_BACK_BUTTON, 2); i++) {
            tap(INAPP_BACK_BUTTON);
        }
        if (isPresent(HOME_TAB, 3)) {
            tap(HOME_TAB);
        }
    }

    private WebElement firstServiceTile() {
        List<WebElement> tiles = waitUtils.findQuick(ANY_SERVICE_TILE, 1);
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    private int safeY(WebElement el) {
        try {
            return el.getRect().getY();
        } catch (Exception e) {
            return -1;
        }
    }

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

    private void shortScrollUp() {
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.60);
        int endY = (int) (size.getHeight() * 0.45);
        swipeUtils.performSwipe(x, startY, x, endY);
    }

    // ══════════════════════════════════════════════════
    //  PARENT — send a Marriage gift to a kid
    // ══════════════════════════════════════════════════

    @Step("Send a Marriage gift of {amount} to {kidSearchMobile} (message: {message})")
    public void sendMarriageGift(String kidSearchMobile, String message, String amount,
            String passcode, String otp) {
        // Gifts landing = a list (Received/Sent tabs, Cash/Store filter chips) with a bottom
        // "Send new gift" button. Start a new gift from there. (Must tap this BEFORE selecting Cash,
        // otherwise the "Cash" filter chip on the list is matched instead of the Gift Type card.)
        if (isPresent(SEND_NEW_GIFT_BTN, 10)) {
            tap(SEND_NEW_GIFT_BTN);
        }
        // Gift Type screen (Cash vs Store Item cards) → choose Cash for a cash gift.
        if (isPresent(CASH_GIFT_TYPE, 8)) {
            tap(CASH_GIFT_TYPE);
        }
        type(SEARCH_BAR, kidSearchMobile);
        tap(FIRST_CONTACT);

        tap(MARRIAGE_GIFT_TYPE);
        type(GIFT_MESSAGE_INPUT, message);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);

        type(GIFT_AMOUNT_INPUT, amount);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);

        // If the sender is at the unopened-gifts cap, the app blocks the send here with an inline
        // error and never renders the Confirm screen. Surface it as a clear, actionable failure
        // instead of a confusing 15s Confirm-button timeout.
        List<WebElement> limitError = waitUtils.findQuick(GIFT_LIMIT_ERROR, 3);
        if (!limitError.isEmpty()) {
            throw new IllegalStateException("Gift could not be sent — app returned: \""
                    + limitError.get(0).getText() + "\". Clear the sender's unopened gifts (have the"
                    + " recipient open them) or use a sender under the limit, then re-run.");
        }
        tap(CONFIRM_BTN);
        platformActions.enterDigits(passcode);   // fillVerificationCode

        // After the passcode, the gift is verified with an OTP sent to the parent's mobile. Gated so
        // a build that skips the OTP step still proceeds to the Thank You screen.
        OtpPage otpPage = new OtpPage();
        if (otpPage.isVisible(20)) {
            otpPage.enterOtp(otp);
        }
    }

    /** True once the "Thank You" success screen has rendered. */
    public boolean isThankYouShown() {
        return isPresent(THANK_YOU_TEXT, 30);
    }

    @Step("Tap Done on the gift success screen")
    public void tapDone() {
        tap(DONE_BTN);
    }

    // ══════════════════════════════════════════════════
    //  SENT / RECEIVED gift verification
    // ══════════════════════════════════════════════════

    @Step("Open the Sent gifts tab")
    public void openSentTab() {
        tap(SENT_TAB);
    }

    @Step("Open the Received gifts tab")
    public void openReceivedTab() {
        tap(RECEIVED_TAB);
    }

    @Step("Read the latest gift status")
    public String getGiftStatus() {
        return getText(NOT_OPENED_STATUS);
    }

    @Step("Read the latest gift type")
    public String getGiftType() {
        return getText(GIFT_TYPE_LABEL);
    }

    @Step("Open the latest gift")
    public void openLatestGift() {
        tap(LATEST_GIFT);
    }

    // ══════════════════════════════════════════════════
    //  KID — open a received gift
    // ══════════════════════════════════════════════════

    @Step("Open the received gift (open → done)")
    public void openReceivedGift() {
        tap(LATEST_GIFT);
        tap(OPEN_GIFT_BTN);
        tap(DONE_BTN);
    }

    @Step("Read the opened-gift status")
    public String getOpenedGiftStatus() {
        return getText(OPENED_STATUS);
    }
}
