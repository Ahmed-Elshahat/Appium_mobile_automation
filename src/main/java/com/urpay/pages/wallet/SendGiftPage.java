package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

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

    // ── Navigation: the Eidya / Gift dashboard service tile ──
    private static final By EIDYA_TILE =
            AppiumBy.accessibilityId("testID-viewElemenEidyaGiftMain");
    private static final By ANY_SERVICE_TILE =
            AppiumBy.xpath("//*[starts-with(@content-desc,'testID-viewElemen')]");
    // The Gifts landing header (marks a successful navigation).
    private static final By GIFTS_HEADER = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Gifts']");

    // ── Send wizard ──
    private static final By SEND_NEW_GIFT_BTN =
            AppiumBy.accessibilityId("testID-primary-action-main");
    private static final By SEARCH_BAR =
            AppiumBy.accessibilityId("testID-Search-Input");
    private static final By FIRST_CONTACT =
            AppiumBy.accessibilityId("testID-contacts-number-0");
    private static final By MARRIAGE_GIFT_TYPE = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-Text.7f1afd0e-5c80-40d6-bc3e-85300c52bf3e'"
            + " and @text='Marriage']");
    private static final By GIFT_MESSAGE_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-message");
    // testID-primary-onSubmit-main is reused for Next / Done / Open on their respective screens.
    private static final By NEXT_BTN =
            AppiumBy.accessibilityId("testID-primary-onSubmit-main");
    private static final By GIFT_AMOUNT_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-TextInput.d81d3b37-7d22-410e-bb9b-ac500e5b5a67']");
    private static final By CONFIRM_BTN =
            AppiumBy.accessibilityId("testID-primary-onConfirm-main");
    private static final By THANK_YOU_TEXT =
            AppiumBy.accessibilityId("testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5");
    private static final By DONE_BTN =
            AppiumBy.accessibilityId("testID-primary-onSubmit-main");

    // ── Gift lists (Sent / Received tabs) ──
    private static final By SENT_TAB =
            AppiumBy.accessibilityId("testID-Tabs.6f2c265b-839c-4595-9a08-309f15055da0.1");
    private static final By RECEIVED_TAB = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Received']");
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

    @Step("Open the Gifts (Eidya) service from the dashboard")
    public void navigateToGifts() {
        // The Eidya gift tile lives on the dashboard Services carousel (same off-page rendering
        // behaviour as Money Request): scroll the Services grid into the upper half, then page the
        // carousel left until the tile renders. Mirrors MoneyRequestPage.openMoneyRequestService().
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

        for (int attempt = 0; attempt < 8; attempt++) {
            List<WebElement> tiles = waitUtils.findQuick(EIDYA_TILE, 1);
            if (!tiles.isEmpty()) {
                try {
                    tiles.get(0).click();
                } catch (Exception ignored) {
                    // stale tile — re-page and retry
                }
                if (isPresent(GIFTS_HEADER, 6) || isPresent(SEND_NEW_GIFT_BTN, 3)) {
                    return;
                }
            }
            swipeServicesLeft();
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Could not find the Eidya gift tile on the dashboard after scrolling");
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
    public void sendMarriageGift(String kidSearchMobile, String message, String amount, String passcode) {
        tap(SEND_NEW_GIFT_BTN);
        type(SEARCH_BAR, kidSearchMobile);
        tap(FIRST_CONTACT);

        tap(MARRIAGE_GIFT_TYPE);
        type(GIFT_MESSAGE_INPUT, message);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);

        type(GIFT_AMOUNT_INPUT, amount);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);

        tap(CONFIRM_BTN);
        platformActions.enterDigits(passcode);   // fillVerificationCode
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
