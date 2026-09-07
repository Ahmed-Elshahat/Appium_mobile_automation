package com.urpay.pages.wallet;

import org.openqa.selenium.By;

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

    private static final By PENDING_LINK_REQUEST_CARD = AppiumBy.xpath(
            "//android.widget.TextView[contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'wallet linking request')"
            + " or (contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'link')"
            + " and contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'family'))]");

    private static final By VIEW_DETAILS_BTN = AppiumBy.xpath(
            "//*[@text='View Details']/ancestor-or-self::*[@clickable='true'][1]");

    private static final By APPROVE_BTN = AppiumBy.xpath(
            "//*[@text='Approve' or @text='Accept' or @content-desc='testID-primary--main']");

    @Step("Check for a pending family-link (\"Wallet Linking Request\") card on screen")
    public boolean isPendingLinkRequestVisible(long timeoutSec) {
        return isPresent(PENDING_LINK_REQUEST_CARD, timeoutSec);
    }

    @Step("Open the pending family-link request (View Details)")
    public void openPendingLinkRequest() {
        tap(VIEW_DETAILS_BTN);
    }

    @Step("Tap Approve on the family-link request")
    public void tapApprove() {
        tap(APPROVE_BTN);
    }

    /** Save the current screen's page source for locating the real approval UI later. */
    @Step("Dump current screen for family-link approval investigation")
    public void dumpForInvestigation() {
        dumpPageSource("family-link-approval-investigation");
    }
}
