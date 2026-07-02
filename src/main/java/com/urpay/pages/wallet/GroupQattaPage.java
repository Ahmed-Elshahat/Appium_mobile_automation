package com.urpay.pages.wallet;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Group Qatta operations: add a qatta (expense) inside a group, validate group/qatta details,
 * and pay multiple qattas within a group.
 *
 * Migrated from Katalon:
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/ToaddNewFirstQatta
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/ToaddNewSecondQatta
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/ToValidateMultiGroupQattaDetails
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/ToPayMultiQattaWithinGroup
 *   Object Repository/android/WalletVas/QattaModule/* and .../editQatta/*
 *
 * Locator notes (FLAG for live verification — many reuse the generic ReactText testID
 * and are disambiguated by text/index):
 *   - latestGPQatta uses a positional locator on the generic testID-data-undefined.
 *   - amount/buttons keep their dedicated action testIDs.
 *   - success/done/tabs reuse the generic ReactText testID, disambiguated by text.
 */
public class GroupQattaPage extends BasePage {

    // ── Open latest group ────────────────────────────
    private static final By LATEST_GROUP = AppiumBy.xpath(
            "(//*[@content-desc='testID-data-undefined'])[3]"
            + " | (//*[@name='testID-data-undefined' or @label='testID-data-undefined'])[3]");

    // ── Add qatta (expense) wizard ───────────────────
    private static final By ADD_NEW_QATTA_BTN = AppiumBy.xpath(
            "//android.widget.TextView[starts-with(@text,'Add new')]"
            + " | //XCUIElementTypeStaticText[starts-with(@label,'Add new')"
            + " or starts-with(@value,'Add new') or starts-with(@name,'Add new')]");

    private static final By QATTA_NAME_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-QattaN.NewGroup");

    private static final By NEXT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-buttonAction-main']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[@text='Next' or @text='Continue']]");

    private static final By AMOUNT_100 = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.a0f96d2e-8083-4a83-bcd2-b867ff6144bd' and @text='100']"
            + " | //*[(@name='testID-Text.a0f96d2e-8083-4a83-bcd2-b867ff6144bd'"
            + " or @label='testID-Text.a0f96d2e-8083-4a83-bcd2-b867ff6144bd')"
            + " and (@label='100' or @value='100' or @name='100')]");

    private static final By NEXT_AMOUNT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-action-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    private static final By NEXT_EQUAL_AMOUNT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onPressNext-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    private static final By SEND_QATTA_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onCreateExpense-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

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

    // ── Validate multi-group qatta details ───────────
    private static final By GROUP_STATUS = AppiumBy.xpath(
            "(//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'])[4]"
            + " | (//*[@name='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " or @label='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'])[4]");

    private static final By GROUP_AMOUNT =
            AppiumBy.accessibilityId("testID-master-amount-1");

    // ── Pay multi qatta (member) ─────────────────────
    private static final By RECEIVED_TAB = AppiumBy.xpath(
            "//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Total qatta to pay']"
            + " | //*[(@name='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " or @label='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42')"
            + " and (@label='Total qatta to pay' or @value='Total qatta to pay')]");

    private static final By PAY_MULTI_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Pay multiple requests ']"
            + " | //*[(@name='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " or @label='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42')"
            + " and (@label='Pay multiple requests ' or @value='Pay multiple requests ')]");

    private static final By FIRST_CHECKBOX = AppiumBy.xpath(
            "(//*[@content-desc='testID-check-box-main'])[1]"
            + " | (//*[@name='testID-check-box-main' or @label='testID-check-box-main'])[1]");

    private static final By SECOND_CHECKBOX = AppiumBy.xpath(
            "(//*[@content-desc='testID-check-box-main'])[2]"
            + " | (//*[@name='testID-check-box-main' or @label='testID-check-box-main'])[2]");

    private static final By NEXT_MULTI_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onNext-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    private static final By CONFIRM_PAY_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onConfirm-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    private static final By PAY_DONE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onSubmit-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    private static final By BACK_BTN =
            AppiumBy.accessibilityId("testID-right-icon-item");

    // ── Reject (member) ──────────────────────────
    // Topmost unpaid qatta row (rows carry no dedicated testID): locate by its "Unpaid" status
    // label and tap the row container. On the GROUP screen the row is a React-Native card that is
    // NOT flagged clickable="true" (unlike the quick-qatta screen), so a plain clickable-ancestor
    // lookup finds nothing. Fall back to the nearest ancestor card that also holds the qatta amount
    // — its centre sits inside the card, so the RN touch handler still opens the qatta. Keeping the
    // clickable-ancestor variant first preserves behaviour on any build where the row IS clickable.
    private static final By UNPAID_QATTA_ROW = AppiumBy.xpath(
            "(//android.widget.TextView[@text='Unpaid'])[1]/ancestor::*[@clickable='true'][1]"
            + " | (//android.widget.TextView[@text='Unpaid'])[1]"
            + "/ancestor::android.view.ViewGroup[.//*[contains(@content-desc,'amount')]][1]"
            + " | (//XCUIElementTypeStaticText[@label='Unpaid' or @value='Unpaid'])[1]"
            + "/ancestor::XCUIElementTypeCell[1]");

    // "Reject" text is not clickable; its clickable parent button carries the secondary-action testID.
    private static final By REJECT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-secondary-action-main']"
            + " | //*[starts-with(@content-desc,'testID-secondary') and substring(@content-desc,string-length(@content-desc)-4)='-main']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[contains(@text,'Reject')]]");

    // Confirm-reject button on the reject confirmation sheet (Katalon confirmRejectQatta).
    // Keep exact id first, then the sheet's "Reject Qatta" label — NOT a broad primary-*-main match,
    // which would grab the detail screen's "Pay Qatta" primary button sitting behind the sheet.
    private static final By CONFIRM_REJECT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-action-main']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[@text='Reject Qatta']]"
            + " | //android.widget.TextView[@text='Reject Qatta']");

    // After confirming, the app lands on the qatta detail screen where the
    // participant's status persists as "Rejected" (the success toast is transient).
    private static final By REJECT_SUCCESS_MSG = AppiumBy.xpath(
            "//*[@text='Rejected' or contains(@text,'Rejected')"
            + " or contains(@text,'rejected')]"
            + " | //*[@label='Rejected' or @value='Rejected'"
            + " or contains(@label,'Rejected') or contains(@label,'rejected')"
            + " or contains(@value,'Rejected') or contains(@value,'rejected')]");

    // Dashboard-UNIQUE presence marker (the wallet info icon only exists on the home screen;
    // master-amount-main also appears on some stacked qatta screens, so it must NOT be used here).
    // Cloud may hash the middle of the id, so also match by the stable 'testID-dashboard' prefix —
    // this stops the back-navigation loop AT the dashboard instead of overshooting into the
    // launcher (which backgrounds the app).
    private static final By DASHBOARD_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-dashboard#InfoIcon-Wallet']"
            + " | //*[starts-with(@content-desc,'testID-dashboard')]"
            + " | //*[starts-with(@name,'testID-dashboard') or starts-with(@label,'testID-dashboard')]");

    // ═════════════════════════════════════════
    //  OPEN GROUP
    // ═════════════════════════════════════════

    @Step("Open the latest qatta group")
    public void openLatestGroup() {
        tap(LATEST_GROUP);
    }

    @Step("Return to the dashboard from a stacked Qatta screen")
    public void returnToDashboard() {
        // Use the Android system back button (driver.navigate().back()). It reliably pops every
        // stacked Qatta screen — including the "New Qatta"/"New Group" wizards that have NO header
        // back button — whereas tapping the React-Native header icon is unreliable across the wizard
        // screens left in the back stack by the create-group / add-qatta flow.
        for (int i = 0; i < 10 && !isPresent(DASHBOARD_MARKER, 2); i++) {
            pressBack();
        }
    }

    // ══════════════════════════════════════════════════
    //  ADD QATTA (EXPENSE) WIZARD
    // ══════════════════════════════════════════════════

    @Step("Add a qatta named '{qattaName}' of amount 100")
    public void addQatta(String qattaName) {
        tap(ADD_NEW_QATTA_BTN);
        type(QATTA_NAME_INPUT, qattaName);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);
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
    //  VALIDATE DETAILS
    // ══════════════════════════════════════════════════

    public String getGroupStatus() {
        return getText(GROUP_STATUS);
    }

    public boolean isGroupAmountDisplayed() {
        return isPresent(GROUP_AMOUNT, 15);
    }

    // ══════════════════════════════════════════════════
    //  PAY MULTI QATTA
    // ══════════════════════════════════════════════════

    @Step("Open the received (Total qatta to pay) tab")
    public void tapReceivedTab() {
        tap(RECEIVED_TAB);
    }

    @Step("Tap 'Pay multiple requests'")
    public void tapPayMultiple() {
        tap(PAY_MULTI_BTN);
    }

    @Step("Select both qattas to pay")
    public void selectBothQattas() {
        tapQattaRow(FIRST_CHECKBOX);
        tapQattaRow(SECOND_CHECKBOX);
    }

    // The qatta checkbox (testID-check-box-main) is a clickable ViewGroup with no press handler;
    // only the parent row (a full-width React-Native TouchableOpacity) toggles selection.
    // Coordinate-tap the row centre using the checkbox's vertical position.
    private void tapQattaRow(By checkbox) {
        isPresent(checkbox, 10);
        WebElement cb = driver.findElement(checkbox);
        Rectangle r = cb.getRect();
        int rowCenterX = driver.manage().window().getSize().getWidth() / 2;
        int rowCenterY = r.getY() + r.getHeight() / 2;
        tapAtCoordinates(rowCenterX, rowCenterY);
    }

    @Step("Proceed to confirmation")
    public void tapNextMulti() {
        tap(NEXT_MULTI_BTN);
    }

    @Step("Confirm payment")
    public void tapConfirmPay() {
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

    @Step("Tap back")
    public void tapBack() {
        tap(BACK_BTN);
    }

    // ═════════════════════════════════════════════
    //  REJECT QATTA (member)
    // ═════════════════════════════════════════════

    @Step("Open the first unpaid qatta in the group")
    public void openFirstUnpaidQatta() {
        tap(UNPAID_QATTA_ROW);
    }

    @Step("Tap Reject on the qatta detail")
    public void tapReject() {
        // The Reject action sits at the bottom of the qatta-detail ScrollView. On a qatta with
        // several participants it renders below the fold, and React Native omits off-screen nodes
        // from the accessibility tree — so the locator resolves nothing until we scroll it into
        // view. Reveal it with a few bounded upward scrolls before tapping.
        for (int i = 0; i < 3 && waitUtils.findQuick(REJECT_BTN, 1).isEmpty(); i++) {
            swipeUp();
        }
        tap(REJECT_BTN);
    }

    @Step("Confirm reject")
    public void tapConfirmReject() {
        tap(CONFIRM_REJECT_BTN);
    }

    public boolean isRejectSuccess() {
        return isPresent(REJECT_SUCCESS_MSG, 30);
    }
}
