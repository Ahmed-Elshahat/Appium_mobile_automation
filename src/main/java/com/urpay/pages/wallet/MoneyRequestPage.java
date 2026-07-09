package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Money Request operations (send → verify in Sent tab → approve).
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/RequestMoney/Send Money Request And Approve Test
 *   Suite, using the {@code Wallet_VAS/MoneyRequest/SendMoneyRequest/*} test cases.
 *
 * <p>Locator notes (from the Object Repository {@code .rs} files):
 * <ul>
 *   <li>The wizard / list buttons share the generic ReactText testID
 *       {@code testID-ReactText.c8f08fb6-...} and are disambiguated by their visible {@code text}
 *       (New Number / Next / Add / Confirm / Done / Request List / Money requests / Sent /
 *       Received).</li>
 *   <li>The mobile-number and amount fields keep their own TextInput testIDs.</li>
 *   <li>The approval screen (Katalon {@code verifyUserApproveRequest}) reuses the older
 *       {@code WalletVas/MoneyRequest/*} testIDs.</li>
 * </ul>
 */
public class MoneyRequestPage extends BasePage {

    /** Generic React-Native text testID shared by most wizard / list buttons. */
    private static final String REACT_TEXT = "testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42";

    // ── Dashboard services → Money Request tile ──────
    private static final By MONEY_REQUEST_TILE =
            AppiumBy.accessibilityId("testID-viewElemendashboard#MoneyRequest");

    // Any dashboard service tile — anchors the horizontal carousel swipe (vertical position of the
    // Services row), mirroring the Family wallets / Missions navigation.
    private static final By ANY_SERVICE_TILE =
            AppiumBy.xpath("//*[starts-with(@content-desc,'testID-viewElemen')]");

    // ── Send wizard ──────────────────────────────────
    private static final By ADD_NEW_NUMBER_BTN =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='New Number']");

    private static final By RECIPIENT_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-TextInput.9e7x5090-8l93-4kea-b278-28wf09097013']");

    // All four wizard "Next" buttons share this exact locator (one per page).
    private static final By NEXT_BTN =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='Next']");

    private static final By ADD_BTN =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='Add']");

    private static final By AMOUNT_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-TextInput.da2d517d-cfb5-425c-8b20-8c992de62dd0']");

    // The visible "Confirm" label is a non-clickable ReactText node; the real submit button is the
    // clickable wrapper testID-primary-requestMoneyFromContact-main. Target the wrapper so the RN
    // onPress fires (clicking the inner text node is a no-op).
    private static final By CONFIRM_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-requestMoneyFromContact-main']"
            + " | //*[@content-desc='" + REACT_TEXT + "' and @text='Confirm']");

    // ── Success screen ───────────────────────────────
    private static final By THANK_YOU_TEXT =
            AppiumBy.accessibilityId("testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5");

    private static final By SUCCESS_SUBTITLE = AppiumBy.xpath(
            "//*[@content-desc='" + REACT_TEXT
            + "' and contains(@text,'Money request has been sent successfully')]");

    private static final By DONE_BTN =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='Done']");

    // ── Request list (new ReactText tabs) ────────────
    private static final By MORE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-MORENAV' or @text='More']");

    private static final By REQUEST_LIST_OPTION =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='Request List']");

    private static final By MONEY_REQUESTS_OPTION =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='Money requests']");

    private static final By SENT_TAB =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='Sent']");

    private static final By RECEIVED_TAB =
            AppiumBy.xpath("//*[@content-desc='" + REACT_TEXT + "' and @text='Received']");

    private static final By SENT_AMOUNT = AppiumBy.xpath(
            "(//*[@content-desc='testID-master-amount-main'])[1]");

    private static final By LATEST_STATUS =
            AppiumBy.xpath("//*[@content-desc='testID-second-secRow-0']");

    // ── Approve (older WalletVas/MoneyRequest testIDs) ──
    private static final By REQUEST_LIST_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-viewElemenRequestList']");

    private static final By MONEY_REQ_LIST = AppiumBy.xpath(
            "(//*[@content-desc='testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995."
            + "dashboard#MoneyRequest'])[1]");

    private static final By APPROVE_CARD_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-ActionCard-card-item-0']");

    // Reject action card — the second action item (Katalon android/WalletVas/MoneyRequest/rejectBtn).
    private static final By REJECT_CARD_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-ActionCard-card-item-1']");

    private static final By APPROVE_CONFIRM_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary--main']");

    private static final By NOTIFICATION_MESSAGE = AppiumBy.xpath(
            "//*[@content-desc='testID-notification-message']");

    // Dashboard-unique presence marker (wallet balance only renders on the home screen).
    private static final By DASHBOARD_MARKER =
            AppiumBy.accessibilityId("testID-master-amount-main");

    // ══════════════════════════════════════════════════
    //  NAVIGATION — open the Money Request service
    // ══════════════════════════════════════════════════

    @Step("Open Money Request from dashboard services")
    public void openMoneyRequestService() {
        // The dashboard Services menu is a HORIZONTAL carousel of tile-grid pages. The Money Request
        // tile (testID-viewElemendashboard#MoneyRequest) lives on a later page, and React Native does
        // NOT render off-page tiles into the accessibility tree — so vertical scrolling alone never
        // surfaces it. Mirrors the proven FamilyWalletPage.tapFamilyWallet() sequence:
        //   1. scroll DOWN until the Services grid sits in the UPPER HALF of the screen (a clean
        //      horizontal-swipe zone, clear of the bottom nav), then
        //   2. swipe the grid LEFT page-by-page until the Money Request tile renders, then tap+verify.
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

        // 2. Page the carousel horizontally LEFT until the Money Request tile renders, then tap+verify.
        for (int attempt = 0; attempt < 8; attempt++) {
            List<WebElement> tiles = waitUtils.findQuick(MONEY_REQUEST_TILE, 1);
            if (!tiles.isEmpty()) {
                try {
                    tiles.get(0).click();
                } catch (Exception ignored) {}
                if (isPresent(ADD_NEW_NUMBER_BTN, 6)) {
                    return;
                }
            }
            swipeServicesLeft();
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Could not find the Money Request tile on the dashboard after scrolling");
    }

    /** First dashboard service tile currently rendered, or null if the Services grid isn't in view. */
    private WebElement firstServiceTile() {
        List<WebElement> tiles = waitUtils.findQuick(ANY_SERVICE_TILE, 1);
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    /** Y coordinate of an element, or -1 if it cannot be read. */
    private int safeY(WebElement el) {
        try {
            return el.getRect().getY();
        } catch (Exception e) {
            return -1;
        }
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

    /** A short (~15% of screen) upward scroll to reveal the Services grid without overshooting. */
    private void shortScrollDown() {
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.60);
        int endY = (int) (size.getHeight() * 0.45);
        swipeUtils.performSwipe(x, startY, x, endY);
    }

    // ══════════════════════════════════════════════════
    //  SEND MONEY REQUEST (requester)
    // ══════════════════════════════════════════════════

    @Step("Send a money request of {amount} to {recipientMobile}")
    public void sendMoneyRequest(String recipientMobile, String amount) {
        tap(ADD_NEW_NUMBER_BTN);
        type(RECIPIENT_INPUT, recipientMobile);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);          // addMobileNoNextBtn

        tap(ADD_BTN);           // addRecieverNameScreenAddBtn
        tap(NEXT_BTN);          // contactTabNextBtn

        type(AMOUNT_INPUT, amount);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);          // nextBtnFromEnterMoneyRequestTab
        tap(NEXT_BTN);          // addNoteScreenEditbtn

        tap(CONFIRM_BTN);       // confirmationScreenConfirmBtn
    }

    /** True once the "Thank You!" success screen has rendered. */
    public boolean isSuccessScreenShown() {
        return isPresent(THANK_YOU_TEXT, 30);
    }

    @Step("Read the success-screen title")
    public String getThankYouText() {
        return getText(THANK_YOU_TEXT);
    }

    @Step("Read the success-screen subtitle")
    public String getSuccessSubtitle() {
        return getText(SUCCESS_SUBTITLE);
    }

    @Step("Tap Done on the success screen")
    public void tapDone() {
        tap(DONE_BTN);
    }

    // ══════════════════════════════════════════════════
    //  SENT TAB (requester verifies the request)
    // ══════════════════════════════════════════════════

    @Step("Open the Sent tab (More → Request List → Money requests → Sent)")
    public void openSentTab() {
        tap(MORE_BTN);
        tap(REQUEST_LIST_OPTION);
        tap(MONEY_REQUESTS_OPTION);
        tap(SENT_TAB);
    }

    @Step("Read the latest sent amount")
    public String getSentAmount() {
        return getText(SENT_AMOUNT);
    }

    @Step("Read the latest request status")
    public String getLatestStatus() {
        return getText(LATEST_STATUS);
    }

    // ══════════════════════════════════════════════════
    //  APPROVE (approver)
    // ══════════════════════════════════════════════════

    @Step("Open the Money Request list (More → Request List → Money Request)")
    public void openRequestList() {
        tap(MORE_BTN);
        tap(REQUEST_LIST_BTN);
        tap(MONEY_REQ_LIST);
    }

    @Step("Open the kid's Sent money-request list (More → Request List → Money Request → Sent)")
    public void openSentRequestList() {
        // A kid's own request appears in the SENT tab of the Money Request list (Katalon
        // KidValidationTCs/ToValidatetheSentRequestintheList taps sentTab before reading the status).
        openRequestList();
        tap(SENT_TAB);
    }

    @Step("Open the Received tab")
    public void openReceivedTab() {
        tap(RECEIVED_TAB);
    }

    @Step("Open the latest request and approve it")
    public void approveLatestRequest() {
        tap(LATEST_STATUS);
        tap(APPROVE_CARD_BTN);
        tap(APPROVE_CONFIRM_BTN);
    }

    @Step("Open the latest request and reject it")
    public void rejectLatestRequest() {
        tap(LATEST_STATUS);
        tap(REJECT_CARD_BTN);
        tap(APPROVE_CONFIRM_BTN);
    }

    // ══════════════════════════════════════════════════
    //  KID → PARENT (family wallet request money)
    // ══════════════════════════════════════════════════

    // Kid money-request entry (current build, user-confirmed): tap "Top Up" on the dashboard, then
    // enter the amount — for a linked kid, Top Up IS the request-from-parent flow.
    private static final By KID_TOP_UP_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-PlusWithCircle-Top Up' or @text='Top Up']");
    // The primary wizard button (Next / Confirm / Done). Its testID middle segment is OBFUSCATED on
    // the LambdaTest build (e.g. testID-primary-Yb0-main), so match the exact id OR any clickable
    // testID-primary-…-main wrapper structurally (there is a single primary button per wizard screen).
    private static final By PRIMARY_ACTION_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-action-main'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc, string-length(@content-desc) - 4) = '-main')]");

    // Home bottom-nav tab + in-app back button (mirrors FamilyMissionPage). The kid login can leave
    // the app on a pushed sub-screen (e.g. Transactions) because the passcode-retry focus tap lands
    // on the freshly-loaded dashboard; these pop back to the dashboard ROOT before navigating.
    private static final By HOME_TAB =
            AppiumBy.accessibilityId("testID-dashboard");
    private static final By INAPP_BACK_BUTTON =
            AppiumBy.accessibilityId("testID-left-icon-back");

    /**
     * Kid requests money from the linked parent. Current-build navigation (user-confirmed):
     * tap "Top Up" on the dashboard, then enter the amount on the keypad and complete the wizard
     * (Next → Confirm → Done). For a linked kid, Top Up IS the request-from-parent flow — there is no
     * recipient selection.
     */
    @Step("Kid requests {amount} from the linked parent")
    public void kidRequestFromParent(String amount) {
        // The kid login can land on a pushed sub-screen (e.g. Transactions); clear any popup and
        // return to the dashboard root first (mirrors FamilyMissionPage's kid navigation).
        new com.urpay.pages.dashboard.DashboardPage().dismissPopups();
        returnToDashboardRoot();

        // Current build: a kid requests money from the parent by tapping "Top Up" on the dashboard.
        tap(KID_TOP_UP_BTN);

        // Enter the amount on the keypad, then Next → Confirm → Done.
        for (char ch : amount.toCharArray()) {
            if (Character.isDigit(ch)) {
                tap(AppiumBy.accessibilityId("testID-keyboard-element-" + ch));
            }
        }
        tap(PRIMARY_ACTION_BTN);   // Next
        tap(PRIMARY_ACTION_BTN);   // Confirm
        tap(PRIMARY_ACTION_BTN);   // Done
    }

    /**
     * Ensure the kid dashboard ROOT is showing before looking for the Request Money quick-action.
     * Pops any pushed sub-screen (e.g. Transactions) via its in-app back button, then taps the Home
     * bottom-nav tab so the dashboard root is selected. Idempotent — a no-op when already on the root.
     */
    private void returnToDashboardRoot() {
        for (int i = 0; i < 3 && isPresent(INAPP_BACK_BUTTON, 2); i++) {
            tap(INAPP_BACK_BUTTON);
        }
        if (isPresent(HOME_TAB, 3)) {
            tap(HOME_TAB);
        }
    }

    @Step("Enter the verification code")
    public void enterVerificationCode(String code) {
        platformActions.enterDigits(code);
    }

    /**
     * Read the post-approval notification popup text. Mirrors Katalon
     * {@code SystemPopupHandler.getMessageFromNotificationPopup}: poll for
     * {@code testID-notification-message} and read its {@code text} attribute. Best-effort: returns
     * "" if the transient popup never appears.
     */
    @Step("Read the notification popup message")
    public String getNotificationMessage(long timeoutSec) {
        List<WebElement> els = waitUtils.findQuick(NOTIFICATION_MESSAGE, timeoutSec);
        if (els.isEmpty()) {
            log.warn("Notification message popup did not appear within {}s", timeoutSec);
            return "";
        }
        WebElement el = els.get(0);
        String text = el.getAttribute("text");
        if (text == null || text.isEmpty()) {
            text = el.getText();
        }
        log.info("Notification popup message: {}", text);
        return text == null ? "" : text;
    }

    // ══════════════════════════════════════════════════
    //  RETURN TO DASHBOARD
    // ══════════════════════════════════════════════════

    @Step("Return to the dashboard from a stacked Money Request screen")
    public void returnToDashboard() {
        for (int i = 0; i < 10 && !isPresent(DASHBOARD_MARKER, 2); i++) {
            pressBack();
        }
    }
}
