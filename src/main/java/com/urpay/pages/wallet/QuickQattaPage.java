package com.urpay.pages.wallet;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Quick (single) Qatta operations: create a single qatta to one contact (sender),
 * and pay a received single qatta (receiver).
 *
 * Migrated from Katalon:
 *   Test Cases/Wallet_VAS/Qatta/Quickqatta/ToValidateCreatenewQuickQatta
 *   Test Cases/Wallet_VAS/Qatta/Quickqatta/ToPayQuickQattaInReceivedTab
 *   Object Repository/android/WalletVas/QattaModule/* and .../MoneyRequestnew/*
 *
 * Locator notes (mapped from the Katalon Object Repository .rs files):
 *   - Most action buttons keep their dedicated testIDs; tabs / labels / Done reuse the generic
 *     ReactText testID and are disambiguated by text.
 *   - latestReceivedQatta uses a positional locator on the generic testID-data-undefined row.
 */
public class QuickQattaPage extends BasePage {

    // ── Create single qatta wizard ───────────────────
    // "Add new …" label is state-dependent ("Add new Qatta"); match the common "Add new" prefix.
    private static final By ADD_NEW_QATTA_BTN = AppiumBy.xpath(
            "//android.widget.TextView[starts-with(@text,'Add new')]"
            + " | //XCUIElementTypeStaticText[starts-with(@label,'Add new')"
            + " or starts-with(@value,'Add new') or starts-with(@name,'Add new')]");

    private static final By QATTA_NAME_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-QattaN.NewGroup");

    private static final By NEXT_BTN =
            AppiumBy.accessibilityId("testID-primary-buttonAction-main");

    private static final By ADD_NEW_NUMBER_BTN =
            AppiumBy.accessibilityId("testID-secondary-action-main");

    // Member mobile-number field — no stable testID, mirrors the group flow.
    private static final By MEMBER_MOBILE_INPUT =
            AppiumBy.xpath("//android.widget.EditText | //XCUIElementTypeTextField");

    private static final By NEXT_CONTACT_BTN =
            AppiumBy.accessibilityId("testID-primary-onCheckContactNumber-main");

    private static final By CONTACT_NAME_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-undefined");

    private static final By ADD_CONTACT_BTN =
            AppiumBy.accessibilityId("testID-primary-onEnterContactName-main");

    // "Next" on the contacts page (after adding the single contact).
    private static final By NEXT_CONTACT_PAGE_BTN =
            AppiumBy.accessibilityId("testID-primary-onAddContacts-main");

    private static final By AMOUNT_100 = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.a0f96d2e-8083-4a83-bcd2-b867ff6144bd' and @text='100']"
            + " | //*[(@name='testID-Text.a0f96d2e-8083-4a83-bcd2-b867ff6144bd'"
            + " or @label='testID-Text.a0f96d2e-8083-4a83-bcd2-b867ff6144bd')"
            + " and (@label='100' or @value='100' or @name='100')]");

    private static final By NEXT_AMOUNT_BTN =
            AppiumBy.accessibilityId("testID-primary-action-main");

    private static final By NEXT_EQUAL_AMOUNT_BTN =
            AppiumBy.accessibilityId("testID-primary-onPressNext-main");

    private static final By SEND_QATTA_BTN =
            AppiumBy.accessibilityId("testID-primary-onCreateQatta-main");

    private static final By SUCCESS_MSG = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5'"
            + " or contains(@text,'Success')]"
            + " | //*[@name='testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5'"
            + " or @label='testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5'"
            + " or contains(@label,'Success') or contains(@value,'Success')]");

    private static final By DONE_BTN = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc="
            + "'testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Done']"
            + " | //XCUIElementTypeStaticText[(@name='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " or @label='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42')"
            + " and (@label='Done' or @value='Done' or @name='Done')]");

    // ── Pay received single qatta ────────────────────
    private static final By RECEIVED_TAB = AppiumBy.xpath(
            "//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Total qatta to pay']"
            + " | //*[(@name='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " or @label='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42')"
            + " and (@label='Total qatta to pay' or @value='Total qatta to pay')]");

    // Latest unpaid qatta row. Rows carry no dedicated testID, so locate the topmost row by its
    // "Unpaid" status label and tap its nearest clickable ancestor (the row container itself).
    private static final By LATEST_RECEIVED_QATTA = AppiumBy.xpath(
            "(//android.widget.TextView[@text='Unpaid'])[1]/ancestor::*[@clickable='true'][1]"
            + " | (//XCUIElementTypeStaticText[@label='Unpaid' or @value='Unpaid'])[1]"
            + "/ancestor::XCUIElementTypeCell[1]");

    // The "Pay Qatta" TextView is not clickable; its clickable parent button carries this testID.
    private static final By PAY_QATTA_BTN =
            AppiumBy.accessibilityId("testID-primary-payQatta-main");

    private static final By CONFIRM_PAY_BTN =
            AppiumBy.accessibilityId("testID-primary-onConfirm-main");

    private static final By PAY_DONE_BTN =
            AppiumBy.accessibilityId("testID-primary-onSubmit-main");

    private static final By BACK_BTN =
            AppiumBy.accessibilityId("testID-right-icon-item");

    // ── Reject received single qatta (receiver) ─────
    // "Reject" text is not clickable; its clickable parent button carries the secondary-action testID.
    private static final By REJECT_BTN =
            AppiumBy.accessibilityId("testID-secondary-action-main");

    // Confirm-reject button on the reject confirmation sheet (Katalon confirmRejectQatta).
    private static final By CONFIRM_REJECT_BTN =
            AppiumBy.accessibilityId("testID-primary-action-main");

    // After confirming, the app lands on the qatta detail screen where the
    // participant's status persists as "Rejected" (the success toast is transient).
    private static final By REJECT_SUCCESS_MSG = AppiumBy.xpath(
            "//*[@text='Rejected' or contains(@text,'Rejected')"
            + " or contains(@text,'rejected')]"
            + " | //*[@label='Rejected' or @value='Rejected'"
            + " or contains(@label,'Rejected') or contains(@label,'rejected')"
            + " or contains(@value,'Rejected') or contains(@value,'rejected')]");

    // Dashboard-UNIQUE presence marker (wallet info icon only exists on the home screen).
    private static final By DASHBOARD_MARKER =
            AppiumBy.accessibilityId("testID-dashboard#InfoIcon-Wallet");

    // ══════════════════════════════════════════════════
    //  CREATE SINGLE QATTA (sender)
    // ══════════════════════════════════════════════════

    @Step("Create quick qatta '{qattaName}' to {mobile} ({contactName})")
    public void createQuickQatta(String qattaName, String mobile, String contactName) {
        tap(ADD_NEW_QATTA_BTN);
        type(QATTA_NAME_INPUT, qattaName);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);

        tap(ADD_NEW_NUMBER_BTN);
        type(MEMBER_MOBILE_INPUT, mobile);
        tap(NEXT_CONTACT_BTN);
        type(CONTACT_NAME_INPUT, contactName);
        platformActions.dismissKeyboard();
        tap(ADD_CONTACT_BTN);
        tap(NEXT_CONTACT_PAGE_BTN);

        tap(AMOUNT_100);
        tap(NEXT_AMOUNT_BTN);
        tap(NEXT_EQUAL_AMOUNT_BTN);
        tap(SEND_QATTA_BTN);
    }

    public boolean isQattaCreatedSuccess() {
        return isPresent(SUCCESS_MSG, 30);
    }

    @Step("Tap Done")
    public void tapDone() {
        tap(DONE_BTN);
    }

    // ══════════════════════════════════════════════════
    //  PAY RECEIVED SINGLE QATTA (receiver)
    // ══════════════════════════════════════════════════

    @Step("Pay the latest received qatta")
    public void payLatestReceivedQatta() {
        tap(RECEIVED_TAB);
        tap(LATEST_RECEIVED_QATTA);
        tap(PAY_QATTA_BTN);
        tap(CONFIRM_PAY_BTN);
    }

    @Step("Enter verification code")
    public void enterVerificationCode(String code) {
        platformActions.enterDigits(code);
    }

    public boolean isPaymentSuccess() {
        return isPresent(SUCCESS_MSG, 30);
    }

    @Step("Tap Done (payment)")
    public void tapPayDone() {
        tap(PAY_DONE_BTN);
    }

    // ═════════════════════════════════════════════
    //  REJECT RECEIVED SINGLE QATTA (receiver)
    // ═════════════════════════════════════════════

    @Step("Reject the latest received qatta")
    public void rejectLatestReceivedQatta() {
        tap(RECEIVED_TAB);
        tap(LATEST_RECEIVED_QATTA);
        tap(REJECT_BTN);
        tap(CONFIRM_REJECT_BTN);
    }

    public boolean isRejectSuccess() {
        return isPresent(REJECT_SUCCESS_MSG, 30);
    }

    @Step("Tap back")
    public void tapBack() {
        tap(BACK_BTN);
    }

    // ══════════════════════════════════════════════════
    //  RETURN TO DASHBOARD
    // ══════════════════════════════════════════════════

    @Step("Return to the dashboard from a stacked Qatta screen")
    public void returnToDashboard() {
        // Android system back reliably pops every stacked Qatta screen (including wizard screens
        // that lack a header back button), matching the verified PayGroupQatta logout approach.
        for (int i = 0; i < 10 && !isPresent(DASHBOARD_MARKER, 2); i++) {
            pressBack();
        }
    }
}
