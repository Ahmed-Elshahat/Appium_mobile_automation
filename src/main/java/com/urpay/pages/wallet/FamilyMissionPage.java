package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.utils.AppGuard;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Family Mission page — handles the Missions screen for both parent and kid.
 *
 * Parent actions: Add new mission, fill details, confirm, send reward.
 * Kid actions: View mission, mark as done, ask for reward.
 *
 * Katalon source: Object Repository/android/WalletVas/FamilyMission/
 */
public class FamilyMissionPage extends BasePage {

    // ══════════════════════════════════════════════════
    //  NAVIGATION & ENTRY
    // ══════════════════════════════════════════════════

    // ── Missions button on kid profile ────────────────
    // Its testID is build-specific (e.g. testID-secondary-action-1 / testID-secondary-qTf-1),
    // so match the clickable button by its visible "Missions" label with the testIDs as fallback.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-action-1' "
            + "or @content-desc='testID-secondary-qTf-1'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Missions']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-secondary-action-1' or @name='testID-secondary-qTf-1'"
            + " or @label='Missions' or @name='Missions']")
    private WebElement missionsButton;

    // ── Get Started button (shown on first visit) ─────
    @AndroidFindBy(accessibility = "testID-primary-action-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-action-main")
    private WebElement getStartedButton;

    // ── Add New Mission button ────────────────────────
    // testID is build-specific (e.g. testID-primary-onAddNewMission-main / testID-primary-PTf-main),
    // so match the clickable button by its visible "Add new mission" label with testID as fallback.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onAddNewMission-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Add new mission' or @text='Add New Mission']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onAddNewMission-main'"
            + " or @label='Add new mission' or @label='Add New Mission'"
            + " or @name='Add new mission' or @name='Add New Mission']")
    private WebElement addNewMissionButton;

    // ── Dashboard-UNIQUE presence marker (wallet info icon only exists on the home screen) ──
    private static final By DASHBOARD_MARKER =
            AppiumBy.accessibilityId("testID-dashboard#InfoIcon-Wallet");

    // ══════════════════════════════════════════════════
    //  MISSION CREATION — Step 1: Category & Name
    // ══════════════════════════════════════════════════

    // ── Entertainment category option ─────────────────
    // Platform-aware: Android @text | iOS @label/@value/@name.
    private static final By ENTERTAINMENT_OPTION = AppiumBy.xpath(
            "//*[@text='Entertainment']"
            + " | //*[@label='Entertainment' or @value='Entertainment' or @name='Entertainment']");

    // ── Mission Name field ────────────────────────────
    @AndroidFindBy(accessibility = "testID-input-direct-missionNameValue")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-missionNameValue")
    private WebElement missionNameField;

    // ── Mission Target view (tap to reveal text field) ──
    @AndroidFindBy(accessibility = "testID-View.657af242-9a1e-497f-b5a2-a558ba68dcc6")
    @iOSXCUITFindBy(accessibility = "testID-View.657af242-9a1e-497f-b5a2-a558ba68dcc6")
    private WebElement missionTargetView;

    // ── Target text input ─────────────────────────────
    @AndroidFindBy(accessibility = "testID-input-direct-FW.NewMission.PlaceHolder2")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-FW.NewMission.PlaceHolder2")
    private WebElement targetTextField;

    // ── Next button (Step 1 → Step 2) ─────────────────
    // testID is build-specific (hashed on the LT build); match by the visible "Next" label with
    // the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onSubmit-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Next']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onSubmit-main'"
            + " or @label='Next' or @name='Next']")
    private WebElement nextButton;

    // ══════════════════════════════════════════════════
    //  MISSION CREATION — Step 2: Amount
    // ══════════════════════════════════════════════════

    // ── Amount text input ─────────────────────────────
    @AndroidFindBy(accessibility = "testID-TextInput.76377517-6a8a-4208-85e9-ba24b1430bcf")
    @iOSXCUITFindBy(accessibility = "testID-TextInput.76377517-6a8a-4208-85e9-ba24b1430bcf")
    private WebElement amountField;

    // ── Second Next button (Step 2 → Step 3) ──────────
    // testID is build-specific (hashed on the LT build); match by the visible "Next" label with
    // the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-action-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Next']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-action-main'"
            + " or @label='Next' or @name='Next']")
    private WebElement secondNextButton;

    // ══════════════════════════════════════════════════
    //  MISSION CREATION — Step 3: End Date
    // ══════════════════════════════════════════════════

    // ── End Date picker ───────────────────────────────
    @AndroidFindBy(accessibility = "testID-DatePicker.ac4105c9-a4ad-40c4-b093-3566376fee10")
    @iOSXCUITFindBy(accessibility = "testID-DatePicker.ac4105c9-a4ad-40c4-b093-3566376fee10")
    private WebElement endDatePicker;

    // ── Date dialog OK button (Android system dialog) ──
    @AndroidFindBy(id = "android:id/button1")
    private WebElement dateDialogOk;

    // ── Primary button (generic — used for final Next) ──
    // testID is build-specific (hashed on the LT build); match by the visible "Next" label with
    // the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary--main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Next']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary--main'"
            + " or @label='Next' or @name='Next']")
    private WebElement primaryButton;

    // ══════════════════════════════════════════════════
    //  MISSION CREATION — Step 4: Confirmation
    // ══════════════════════════════════════════════════

    // ── Confirmation header ───────────────────────────
    // Platform-aware: Android TextView@text | iOS XCUIElementTypeStaticText@label/@value/@name.
    private static final By CONFIRMATION_TEXT = AppiumBy.xpath(
            "//android.widget.TextView[@text='Confirmation']"
            + " | //XCUIElementTypeStaticText[@label='Confirmation'"
            + " or @value='Confirmation' or @name='Confirmation']");

    // ── Confirm button ────────────────────────────────
    // testID is build-specific (hashed on the LT build); match by the visible "Confirm" label with
    // the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onConfirm-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Confirm']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onConfirm-main'"
            + " or @label='Confirm' or @name='Confirm']")
    private WebElement confirmButton;

    // By mirror of confirmButton, used to scroll the review screen until the button is on-screen
    // (PageFactory WebElements cannot be polled with findElements for a presence check).
    private static final By CONFIRM_BUTTON = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onConfirm-main']"
            + " | //android.view.ViewGroup[@clickable='true'"
            + " and .//android.widget.TextView[@text='Confirm']]"
            + " | //XCUIElementTypeButton[@name='testID-primary-onConfirm-main']");

    // ── Thank you text (mission created success) ──────
    @AndroidFindBy(accessibility = "testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5")
    @iOSXCUITFindBy(accessibility = "testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5")
    private WebElement thankYouText;

    // ── Done button (after creation) ──────────────────
    // testID is build-specific (hashed on the LT build); match by the visible "Done" label with
    // the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-action-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Done']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-action-main'"
            + " or @label='Done' or @name='Done']")
    private WebElement doneButton;

    // ══════════════════════════════════════════════════
    //  MISSION STATUS & REWARD (Parent view)
    // ══════════════════════════════════════════════════

    // ── Mission name text in list ─────────────────────
    @AndroidFindBy(accessibility = "testID-Text.4edeb055-7926-4561-b097-1b48a6e2d7f9.0")
    @iOSXCUITFindBy(accessibility = "testID-Text.4edeb055-7926-4561-b097-1b48a6e2d7f9.0")
    private WebElement missionNameText;

    // ── Mission status text ───────────────────────────
    @AndroidFindBy(accessibility = "testID-Text.c7e65c21-4694-460a-90f4-ff23a9a59f42.0")
    @iOSXCUITFindBy(accessibility = "testID-Text.c7e65c21-4694-460a-90f4-ff23a9a59f42.0")
    private WebElement missionStatusText;

    // ── Send Reward button ────────────────────────────
    // testID is build-specific (hashed on the LT build); match by the visible "Send" label with the
    // testID as fallback so the locator works on BOTH the semantic and hashed builds.
    // (The label renders as "Send " with a trailing space, so normalize-space is used.)
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-onSendReward-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[normalize-space(@text)='Send']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-secondary-onSendReward-main'"
            + " or @label='Send' or @name='Send']")
    private WebElement sendRewardButton;

    // ── Validate Reward button ────────────────────────
    // testID is build-specific (hashed on the LT build); match by the visible "Send Reward" label
    // with the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onValidateReward-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Send Reward']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onValidateReward-main'"
            + " or @label='Send Reward' or @name='Send Reward']")
    private WebElement validateRewardButton;

    // ── Done Reward button (after sending reward) ─────
    // testID is build-specific (hashed on the LT build); match by the visible "Done" label with
    // the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onSuccessButtonPress-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Done']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onSuccessButtonPress-main'"
            + " or @label='Done' or @name='Done']")
    private WebElement doneRewardButton;

    // ── Closed tab ────────────────────────────────────
    @AndroidFindBy(accessibility = "testID-Tabs.1")
    @iOSXCUITFindBy(accessibility = "testID-Tabs.1")
    private WebElement closedTab;

    // ── Back button ───────────────────────────────────
    @AndroidFindBy(accessibility = "testID-left-icon-back")
    @iOSXCUITFindBy(accessibility = "testID-left-icon-back")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  KID SCREEN ELEMENTS
    // ══════════════════════════════════════════════════

    // ── Kid's mission home (on kid dashboard services) ──
    private static final By KID_MISSIONS_HOME =
            AppiumBy.accessibilityId("testID-viewElemenMissionsLogo");

    // ── Any dashboard service tile (platform-aware: Android @content-desc | iOS @name) ──
    private static final By ANY_SERVICE_TILE = AppiumBy.xpath(
            "//*[starts-with(@content-desc,'testID-viewElemen')]"
            + " | //*[starts-with(@name,'testID-viewElemen')]");

    // ── Kid's "I'm Done" button ───────────────────────
    // testID is build-specific (hashed on the LT build); match by the visible "I'm Done!" label with
    // the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    // (The label uses a curly apostrophe, so contains(...,'Done') avoids any encoding issues.)
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onSubmitted-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[contains(@text,'Done')]]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onSubmitted-main'"
            + " or contains(@label,'Done') or contains(@name,'Done')]")
    private WebElement imDoneButton;

    // ── Kid's "Ask Reward" button ─────────────────────
    // Same accessibilityId as I'm Done — it's a state-dependent button.
    // After "I'm Done" the button changes to ask reward.
    // testID is build-specific (hashed on the LT build); match by the visible "Ask for reward" label
    // with the testID as fallback so the locator works on BOTH the semantic and hashed builds.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onSubmitted-main'] "
            + "| //android.view.ViewGroup[@clickable='true' "
            + "and .//android.widget.TextView[@text='Ask for reward']]")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onSubmitted-main'"
            + " or @label='Ask for reward' or @name='Ask for reward']")
    private WebElement askRewardButton;

    // ══════════════════════════════════════════════════
    //  PAGE ACTIONS — Navigation
    // ══════════════════════════════════════════════════

    @Step("Tap Missions button on kid profile")
    public void tapMissions() {
        tap(missionsButton);
    }

    @Step("Tap 'Get Started' if first-time visit")
    public boolean tapGetStartedIfPresent() {
        if (isDisplayed(getStartedButton, 5)) {
            tap(getStartedButton);
            return true;
        }
        return false;
    }

    @Step("Tap 'Add New Mission' button")
    public void tapAddNewMission() {
        tap(addNewMissionButton);
    }

    // ══════════════════════════════════════════════════
    //  PAGE ACTIONS — Mission Creation
    // ══════════════════════════════════════════════════

    @Step("Select Entertainment category")
    public void selectEntertainment() {
        waitUtils.waitForClickable(ENTERTAINMENT_OPTION).click();
    }

    @Step("Enter mission name: {name}")
    public void enterMissionName(String name) {
        tap(missionNameField);
        type(missionNameField, name);
    }

    @Step("Enter mission target: {target}")
    public void enterMissionTarget(String target) {
        tap(missionTargetView);
        hideKeyboard();
        type(targetTextField, target);
        hideKeyboard();
    }

    @Step("Tap Next button (step 1)")
    public void tapNext() {
        tap(nextButton);
    }

    @Step("Enter reward amount: {amount}")
    public void enterAmount(String amount) {
        type(amountField, amount);
    }

    @Step("Tap second Next button (step 2)")
    public void tapSecondNext() {
        tap(secondNextButton);
    }

    @Step("Select end date (accept default)")
    public void selectEndDate() {
        tap(endDatePicker);
        waitUtils.waitForVisible(dateDialogOk, 10);
        tap(dateDialogOk);
    }

    @Step("Tap Next (step 3 → confirmation)")
    public void tapPrimaryNext() {
        tap(primaryButton);
    }

    @Step("Scroll to Confirm button and tap")
    public void scrollAndConfirm() {
        waitUtils.waitForVisible(CONFIRMATION_TEXT, 15);
        // The Confirm button sits below the fold on the review screen; how far down it is varies
        // with device resolution across the LT device pool, and an occasional swipe can no-op, so
        // scroll until the button is actually present rather than relying on a fixed number of
        // blind swipes. It anchors at the bottom of the screen, so extra swipes cannot overshoot.
        for (int i = 0; i < 6 && !isPresent(CONFIRM_BUTTON, 1); i++) {
            swipeUp();
        }
        tap(confirmButton);
    }

    @Step("Verify 'Thank you' screen")
    public boolean isThankYouVisible() {
        return isDisplayed(thankYouText, 20);
    }

    @Step("Tap Done after mission creation")
    public void tapDone() {
        tap(doneButton);
    }

    // ══════════════════════════════════════════════════
    //  PAGE ACTIONS — Mission Status (Parent)
    // ══════════════════════════════════════════════════

    @Step("Wait for missions list to load")
    public void waitForMissionsList() {
        waitUtils.waitForVisible(missionNameText, 30);
    }

    public String getMissionName() {
        return getText(missionNameText);
    }

    public String getMissionStatus() {
        return getText(missionStatusText);
    }

    @Step("Tap on mission status to open details")
    public void tapMissionStatus() {
        tap(missionStatusText);
    }

    @Step("Tap 'Send Reward' button")
    public void tapSendReward() {
        tap(sendRewardButton);
    }

    @Step("Tap 'Validate Reward' button")
    public void tapValidateReward() {
        tap(validateRewardButton);
    }

    @Step("Tap 'Done' after reward sent")
    public void tapDoneReward() {
        tap(doneRewardButton);
    }

    @Step("Tap back button")
    public void tapBack() {
        tap(backButton);
    }

    @Step("Tap 'Closed' tab to view closed missions")
    public void tapClosedTab() {
        tap(closedTab);
    }

    public String getRewardedStatus() {
        return getText(missionStatusText);
    }

    // ══════════════════════════════════════════════════
    //  PAGE ACTIONS — Kid Screen
    // ══════════════════════════════════════════════════

    @Step("Navigate to Missions from Kid dashboard")
    public void navigateToMissionsFromKidDashboard() {
        // The Missions tile (testID-viewElemenMissionsLogo) lives on a later HORIZONTAL page of the
        // dashboard Services carousel, and React Native does NOT render off-page tiles into the
        // accessibility tree. The Services grid is the dashboard's bottom section, so it is usually
        // already on screen — we must NOT drag it into the upper half (that just scrolls the page to
        // the very bottom). Confirmed-on-device sequence: (1) make sure the Services row is visible,
        // then (2) swipe the grid LEFT page-by-page until the Missions tile renders, then tap.

        // 1. Ensure the Services row is on screen (only scroll down if it isn't rendered yet).
        for (int i = 0; i < 8 && firstServiceTile() == null; i++) {
            shortScrollDown();
        }

        // 2. Page the carousel horizontally LEFT until the Missions tile is rendered, then tap.
        for (int attempt = 0; attempt < 8; attempt++) {
            List<WebElement> tiles = waitUtils.findQuick(KID_MISSIONS_HOME, 1);
            if (!tiles.isEmpty()) {
                tiles.get(0).click();
                return;
            }
            swipeServicesLeft();
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Could not find the Missions tile on the kid dashboard after scrolling");
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
        int startX = (int) (size.getWidth() * 0.85);
        int endX = (int) (size.getWidth() * 0.15);
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

    @Step("Return to the dashboard from a stacked Family Wallet / Missions screen")
    public void returnToDashboard() {
        // Pop the stacked Family Wallet / Missions screens with Android system Back, stopping the
        // moment we reach the dashboard. The dashboard testID marker is build-specific (hashed on
        // the LambdaTest build), so gating ONLY on it makes every Back fire — overshooting the
        // dashboard and ejecting the app to the launcher (App State: background). To stay
        // build-agnostic we also treat "a Back press pushed the app out of the foreground" as proof
        // we were on the dashboard: re-activate the app (which resumes on the dashboard) and stop.
        for (int i = 0; i < 10; i++) {
            if (isPresent(DASHBOARD_MARKER, 2) || AppGuard.isOnDashboard(driver)) {
                break;
            }
            pressBack();
            if (!AppGuard.isInForeground(driver)) {
                break; // Back popped past the root → we were on the dashboard
            }
        }
        AppGuard.ensureForeground(driver); // guarantee the dashboard is foreground for the next step
    }

    @Step("Tap on mission name to open details (Kid)")
    public void tapMissionNameKid() {
        tap(missionNameText);
    }

    @Step("Scroll to 'I'm Done' and tap")
    public void tapImDone() {
        scrollToText("Comments");
        tap(imDoneButton);
    }

    @Step("Tap 'Ask Reward' button (Kid)")
    public void tapAskReward() {
        tap(askRewardButton);
    }

    // ══════════════════════════════════════════════════
    //  PAGE STATE QUERIES
    // ══════════════════════════════════════════════════

    public boolean isMissionsPageLoaded() {
        return isDisplayed(addNewMissionButton, 15)
                || isDisplayed(missionNameText, 15);
    }

    public boolean isAddNewMissionVisible() {
        return isDisplayed(addNewMissionButton, 10);
    }
}
