package com.urpay.pages.wallet;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Qatta Group create / edit / validate page.
 *
 * Migrated from Katalon:
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/ToCreateNewQattaGroup
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/editFirstQattaDetails
 *   Test Cases/Wallet_VAS/Qatta/QattaGroup/ValidateGroupQattaNameFromGroupDetailsTab
 *   Object Repository/android/WalletVas/QattaModule/* and .../editQatta/*
 *
 * Locator notes:
 *   - Form fields/buttons keep their dedicated action testIDs.
 *   - The member mobile-number field has no testID in the app → xpath EditText (flag for testID ticket).
 *   - Group-details header / Save reuse the generic ReactText testID → disambiguated by text.
 *   - The group name shown in details is matched via a runtime text xpath (groupNameByText).
 */
public class QattaGroupPage extends BasePage {

    private static final long SLOW_TIMEOUT = 50;

    // ── Create group wizard ──────────────────────────
    private static final By GROUP_NAME_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-QattaN.NewGroup");

    private static final By NEXT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-buttonAction-main']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[@text='Next' or @text='Continue']]");

    private static final By ADD_NEW_NUMBER_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-secondary-action-main']"
            + " | //*[starts-with(@content-desc,'testID-secondary') and substring(@content-desc,string-length(@content-desc)-4)='-main']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[contains(@text,'Add new')]]");

    // Member mobile-number field — no stable testID, flag for dedicated testID.
    private static final By MEMBER_MOBILE_INPUT =
            AppiumBy.xpath("//android.widget.EditText");

    private static final By NEXT_CONTACT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onCheckContactNumber-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    private static final By CONTACT_NAME_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-undefined");

    private static final By ADD_CONTACT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onEnterContactName-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    private static final By CREATE_GROUP_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onAddContacts-main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']");

    // ── Group list / edit details ────────────────────
    // First qatta group row. Each group row is a clickable ViewGroup whose content-desc is the
    // MERGED descs of its two children (group name + member-count/role), so it STARTS WITH the
    // generic ReactText id 'testID-ReactText.c8f08fb6...'. The summary CARDS start with
    // 'testID-View.83414e9e', the Group/Single TABS with 'testID-TouchableOpacity.68ac7862', and the
    // back/CTAs with other ids — so a starts-with(content-desc,'testID-ReactText.c8f08fb6') match
    // uniquely isolates the group rows. (starts-with is resolved reliably/fast by the UiAutomator2
    // XPath engine, unlike substring-after/count, which timed out on this 12-node list.)
    private static final By FIRST_QATTA_GROUP = AppiumBy.xpath(
            "(//android.view.ViewGroup[@clickable='true' and starts-with(@content-desc,"
            + "'testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42')])[1]"
            + " | (//XCUIElementTypeCell[.//XCUIElementTypeStaticText["
            + "@name='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " or @label='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42']])[1]");

    private static final By GROUP_DETAILS_HEADER = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc="
            + "'testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Group details']");

    private static final By EDIT_BTN =
            AppiumBy.accessibilityId("testID-right-icon-0");

    private static final By GROUP_NAME_TEXTBOX =
            AppiumBy.accessibilityId("testID-input-direct-undefined");

    private static final By SAVE_BTN = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc="
            + "'testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Save']");

    private static final By SUCCESS_MSG =
            AppiumBy.accessibilityId("testID-notification-message");

    // ══════════════════════════════════════════════════
    //  CREATE GROUP WIZARD
    // ══════════════════════════════════════════════════

    @Step("Enter group name: {name}")
    public void enterGroupName(String name) {
        type(GROUP_NAME_INPUT, name);
        platformActions.dismissKeyboard();
    }

    @Step("Tap Next (group name)")
    public void tapNext() {
        tap(NEXT_BTN);
    }

    @Step("Tap 'Add new number'")
    public void tapAddNewNumber() {
        tap(ADD_NEW_NUMBER_BTN);
    }

    @Step("Enter member mobile number: {mobile}")
    public void enterMemberMobile(String mobile) {
        type(MEMBER_MOBILE_INPUT, mobile);
    }

    @Step("Tap Next (contact number)")
    public void tapNextContact() {
        tap(NEXT_CONTACT_BTN);
    }

    @Step("Enter contact name: {name}")
    public void enterContactName(String name) {
        type(CONTACT_NAME_INPUT, name);
        platformActions.dismissKeyboard();
    }

    @Step("Tap 'Add contact'")
    public void tapAddContact() {
        tap(ADD_CONTACT_BTN);
    }

    @Step("Tap 'Create group'")
    public void tapCreateGroup() {
        tap(CREATE_GROUP_BTN);
    }

    // ══════════════════════════════════════════════════
    //  EDIT GROUP DETAILS
    // ══════════════════════════════════════════════════

    @Step("Open the first qatta group")
    public void openFirstGroup() {
        waitUtils.waitForClickable(FIRST_QATTA_GROUP, SLOW_TIMEOUT).click();
    }

    public boolean isGroupDetailsHeaderDisplayed() {
        return isPresent(GROUP_DETAILS_HEADER, 30);
    }

    @Step("Tap edit (group details)")
    public void tapEdit() {
        tap(EDIT_BTN);
    }

    @Step("Replace group name with: {name}")
    public void replaceGroupName(String name) {
        type(GROUP_NAME_TEXTBOX, name);
        // Dismiss the keyboard (ENTER only blurs the field here, it does NOT commit the rename),
        // so the inline Save button is unobstructed and the tap registers.
        platformActions.dismissKeyboard();
    }

    @Step("Tap Save")
    public void tapSave() {
        // "Save" is a non-clickable React Native TextView; Appium element.click() does not fire
        // its onPress. Tap its centre coordinates instead (mirrors a real finger tap).
        WebElement save = waitUtils.waitForVisible(SAVE_BTN);
        Rectangle r = save.getRect();
        tapAtCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
    }

    public boolean isNameUpdatedMessageDisplayed() {
        return isPresent(SUCCESS_MSG, 15);
    }

    public String getUpdatedMessage() {
        return getText(SUCCESS_MSG);
    }

    /**
     * Toggle out of edit mode back to the read-only "Group details" view (the edit icon acts
     * as a toggle). Mirrors Katalon editFirstQattaDetails' final "tap editBtn again" step.
     */
    @Step("Return to Group details view")
    public void returnToGroupDetails() {
        tap(EDIT_BTN);
    }

    // ══════════════════════════════════════════════════
    //  VALIDATE GROUP NAME (details tab)
    // ══════════════════════════════════════════════════

    public boolean isGroupNameDisplayed(String name) {
        return isPresent(groupNameByText(name), 30);
    }

    public String getDisplayedGroupName(String name) {
        return getText(groupNameByText(name));
    }

    /** Runtime text-qualified locator for the group name in the details tab. */
    private By groupNameByText(String name) {
        return AppiumBy.xpath(
                "//android.widget.TextView[@content-desc="
                + "'testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='" + name + "']");
    }
}
