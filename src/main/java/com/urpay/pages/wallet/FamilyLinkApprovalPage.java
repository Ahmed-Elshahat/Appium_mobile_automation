package com.urpay.pages.wallet;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Parent-side approval of an in-app "link to family" request sent by a kid.
 *
 * <p>The pending request renders as a "Wallet Linking Request" card (confirmed on-device from the
 * kid's own "Waiting For Parent Approval" screen, which shows the same card) with a
 * "View Details" CTA. The details screen's Approve/Reject controls are still EXPLORATORY —
 * {@link #dumpForInvestigation()} saves the current screen so real testIDs can be added once seen.
 */
public class FamilyLinkApprovalPage extends BasePage {

    private static final By MORE_BTN = AppiumBy.xpath(
        "//*[@content-desc='testID-MORENAV' or @text='More']");

    private static final By REQUESTS_OPTION = AppiumBy.xpath(
        "//*[contains(@content-desc,'ReactText') and (@text='Requests' or @text='Request')]"
            + " | //*[@text='Requests' or @text='Request']");

    private static final By FAMILY_REQUESTS_OPTION = AppiumBy.xpath(
        "//*[contains(@content-desc,'ReactText') and translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='family requests']"
            + " | //*[(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')="
            + "'family requests')]");

    private static final By FAMILY_REQUESTS_BACK_BTN =
            AppiumBy.accessibilityId("testID-left-icon-back");

        private static final By DASHBOARD_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-DashboardHome' or @content-desc='testID-master-amount-main']");

    private static final By PENDING_LINK_REQUEST_CARD = AppiumBy.xpath(
            "//android.widget.TextView[contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'wallet linking request')"
            + " or (contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'link')"
            + " and contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'family'))]");

    private static final By VERIFIED_LINK_REQUEST = AppiumBy.xpath(
            "//*[@text='Wallet Linking Request']/ancestor::*[.//*[@text='Verified']][1]");

    private static final By VIEW_DETAILS_BTN = AppiumBy.xpath(
            "//*[@text='View Details']/ancestor-or-self::*[@clickable='true'][1]");

        private static final By APPROVE_BTN = AppiumBy.xpath(
            "//*[@text='Approve' or @text='Accept']"
            + "/ancestor-or-self::*[@clickable='true'][1]"
            + " | //*[@content-desc='testID-primary--main']");

            private static final By YES_TAKE_ME_THERE_BTN = AppiumBy.xpath(
                "//*[@text='Yes, take me there' or @text='Yes, take me there!']"
                + "/ancestor::*[@clickable='true'][1]");

                private static final By NOTIFICATIONS_LATER_BTN = AppiumBy.xpath(
                    "//*[@text='Later']/ancestor-or-self::*[@clickable='true'][1]");

                    private static final By KID_HIJRI_DOB_INPUT = AppiumBy.xpath(
                        "//android.widget.EditText[contains(@text,'DD') or contains(@text,'MM') "
                        + "or contains(@text,'YYYY') or contains(@content-desc,'dateOfBirth') "
                        + "or contains(@content-desc,'date-of-birth')]");

                    private static final By DOB_SUBMIT_BTN = AppiumBy.accessibilityId("testID-primary--main");

    // Kid family-member KYC form shown after Approve + Hijri DOB (same testIDs as registration KYC).
    private static final By KID_KYC_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-multi-select-basicIncomeSource' "
            + "or @content-desc='testID-multi-select-incomeRange' "
            + "or @content-desc='testID-input-container-employmentStatus']");

    private static final By KYC_INCOME_SOURCE_DROPDOWN =
            AppiumBy.accessibilityId("testID-multi-select-basicIncomeSource");

    private static final By KYC_INCOME_RANGE_DROPDOWN =
            AppiumBy.accessibilityId("testID-multi-select-incomeRange");

    private static final By KYC_FIRST_OPTION = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-search-item-') "
            + "or contains(@content-desc,'testID-radio-item-')][1]");

    private static final By KYC_OPTION_ITEMS = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-search-item-') "
            + "or contains(@content-desc,'testID-radio-item-')]");

    private static final By KYC_SAVE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary--main' or @text='Save']");

        private static final By KYC_SCROLL_VIEW = AppiumBy.xpath(
            "//android.widget.ScrollView[@content-desc='testID-ScrollView.cdd67778-e118-4702-a69d-5f59165c8ad1']");

    @Step("Open Family requests (More -> Requests -> Family requests)")
    public void openFamilyRequests() {
        tap(MORE_BTN);
        tap(REQUESTS_OPTION);
        tap(FAMILY_REQUESTS_OPTION);
    }

    public boolean isFamilyRequestsScreenDisplayed(long timeoutSec) {
        return isPresent(FAMILY_REQUESTS_OPTION, timeoutSec)
                || isPresent(PENDING_LINK_REQUEST_CARD, timeoutSec);
    }

    @Step("Check for a pending family-link (\"Wallet Linking Request\") card on screen")
    public boolean isPendingLinkRequestVisible(long timeoutSec) {
        return isPresent(PENDING_LINK_REQUEST_CARD, timeoutSec);
    }

    @Step("Confirm the family-link request is marked Verified")
    public boolean isLinkRequestVerified(long timeoutSec) {
        return isPresent(VERIFIED_LINK_REQUEST, timeoutSec);
    }

    @Step("Return from Family Requests to the parent dashboard")
    public void returnToDashboard() {
        for (int attempt = 0; attempt < 3 && !isPresent(DASHBOARD_MARKER, 2); attempt++) {
            tap(FAMILY_REQUESTS_BACK_BTN);
        }
    }

    @Step("Check for the direct approval prompt: 'Yes, take me there'")
    public boolean isYesTakeMeTherePromptVisible(long timeoutSec) {
        return isPresent(YES_TAKE_ME_THERE_BTN, timeoutSec);
    }

    @Step("Tap the direct family request approval prompt: Yes, take me there")
    public void tapYesTakeMeThere() {
        tap(YES_TAKE_ME_THERE_BTN);
        log.info("Tapped 'Yes, take me there' on the Family requests prompt");
    }

    @Step("Wait for the pending Wallet Linking Request after the family prompt")
    public boolean isPendingLinkRequestVisibleAfterPrompt(long timeoutSec) {
        return isPresent(PENDING_LINK_REQUEST_CARD, timeoutSec);
    }

    @Step("Dismiss the Notifications preference popup with Later when shown")
    public void dismissNotificationsPopupIfDisplayed() {
        if (isPresent(NOTIFICATIONS_LATER_BTN, 10)) {
            tap(NOTIFICATIONS_LATER_BTN);
            log.info("Dismissed the Notifications preference popup with Later");
        }
    }

    @Step("Open the pending family-link request (View Details)")
    public void openPendingLinkRequest() {
        tap(VIEW_DETAILS_BTN);
    }

    @Step("Tap Approve on the family-link request")
    public void tapApprove() {
        tap(APPROVE_BTN);
        log.info("Tapped Approve on the pending Wallet Linking Request");
    }

    @Step("Enter kid Hijri date of birth {hijriDob} to confirm approval")
    public void enterKidHijriDateOfBirthAndConfirm(String hijriDob) {
        String[] parts = hijriDob.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Hijri DOB must use DD.MM.YYYY format");
        }
        WebElement input = waitUtils.waitForClickable(KID_HIJRI_DOB_INPUT, 15);
        input.clear();
        input.sendKeys(parts[0]);
        input.sendKeys(parts[1]);
        input.sendKeys(parts[2]);
        hideKeyboard();
        tap(DOB_SUBMIT_BTN);
        log.info("Submitted kid Hijri DOB {} to confirm family-link approval", hijriDob);
    }

    @Step("Check the kid family-member KYC form is displayed")
    public boolean isKidKycFormDisplayed(long timeoutSec) {
        return isPresent(KID_KYC_MARKER, timeoutSec);
    }

    @Step("Complete the kid family-member KYC (income source + range) and Save to finish approval")
    public boolean completeKidFamilyMemberKyc(long timeoutSec) {
        if (!isPresent(KID_KYC_MARKER, timeoutSec)) {
            return false;
        }
        log.info("Kid family-member KYC detected \u2014 filling required fields");
        selectRandomKycOption(KYC_INCOME_SOURCE_DROPDOWN, "Income Source");
        selectRandomKycOption(KYC_INCOME_RANGE_DROPDOWN, "Income Range");
        for (int attempt = 1; attempt <= 3 && isPresent(KID_KYC_MARKER, 4); attempt++) {
            log.info("Kid KYC Save attempt {}", attempt);
            try {
                WebElement save = revealKycSaveButton();
                var rect = save.getRect();
                tapAtCoordinates(rect.getX() + rect.getWidth() / 2,
                        rect.getY() + rect.getHeight() / 2);
            } catch (Exception e) {
                log.warn("Kid KYC Save tap failed on attempt {}: {}", attempt, e.getMessage());
            }
        }
        boolean completed = !isPresent(KID_KYC_MARKER, 8);
        if (completed) {
            log.info("Kid family-member KYC completed \u2014 family-link approval finished");
        } else {
            log.warn("Kid family-member KYC form still visible after save attempts");
        }
        return completed;
    }

    private WebElement revealKycSaveButton() {
        for (int scroll = 0; scroll < 5; scroll++) {
            List<WebElement> buttons = waitUtils.findQuick(KYC_SAVE_BTN, 3);
            if (!buttons.isEmpty()) {
                WebElement save = buttons.get(0);
                var rect = save.getRect();
                List<WebElement> scrollViews = waitUtils.findQuick(KYC_SCROLL_VIEW, 2);
                int formBottom = scrollViews.isEmpty()
                        ? driver.manage().window().getSize().getHeight()
                        : scrollViews.get(0).getRect().getY() + scrollViews.get(0).getRect().getHeight();
                if (rect.getY() >= 0 && rect.getY() + rect.getHeight() <= formBottom - 40) {
                    return save;
                }
            }
            swipeInsideKycForm();
        }
        return waitUtils.waitForClickable(KYC_SAVE_BTN, 5);
    }

    private void swipeInsideKycForm() {
        List<WebElement> scrollViews = waitUtils.findQuick(KYC_SCROLL_VIEW, 2);
        if (scrollViews.isEmpty()) {
            swipeUp();
            return;
        }
        var rect = scrollViews.get(0).getRect();
        int x = rect.getX() + rect.getWidth() / 2;
        int startY = rect.getY() + (int) (rect.getHeight() * 0.72);
        int endY = rect.getY() + (int) (rect.getHeight() * 0.28);
        swipeUtils.performSwipe(x, startY, x, endY);
        log.info("Scrolled inside the kid KYC form to reveal Save");
    }

    private void selectRandomKycOption(By dropdown, String label) {
        try {
            tap(dropdown, 10);
            List<WebElement> options = waitUtils.findQuick(KYC_OPTION_ITEMS, 6);
            if (!options.isEmpty()) {
                WebElement chosen = options.get(ThreadLocalRandom.current().nextInt(options.size()));
                chosen.click();
                log.info("Selected random option for {} ({} options)", label, options.size());
            } else {
                tap(KYC_FIRST_OPTION, 6);
                log.info("Selected first option for {} (options not enumerable)", label);
            }
        } catch (Exception e) {
            log.warn("Failed to select {} option: {}", label, e.getMessage());
        }
    }

    @Step("Dismiss approval success/confirmation screens after the kid link is approved")
    public void dismissApprovalSuccessScreens() {
        By confirm = AppiumBy.xpath(
                "//*[@text='Done' or @text='OK' or @text='Ok' or @text='Continue' "
                + "or @text='Great' or @text='Got it' or @content-desc='testID-primary--main']"
                + "/ancestor-or-self::*[@clickable='true'][1]");
        for (int i = 0; i < 3; i++) {
            dismissNotificationsPopupIfDisplayed();
            if (!isPresent(confirm, 4)) {
                break;
            }
            try {
                tap(confirm);
                log.info("Dismissed an approval success/confirmation screen");
            } catch (Exception e) {
                break;
            }
        }
    }

    /** Save the current screen's page source for locating the real approval UI later. */
    @Step("Dump current screen for family-link approval investigation")
    public void dumpForInvestigation() {
        dumpPageSource("family-link-approval-investigation");
    }
}
