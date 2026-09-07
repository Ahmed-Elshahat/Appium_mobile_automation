package com.urpay.pages.wallet;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Parent-side approval of an in-app "link to family" request sent by a kid.
 *
 * <p><b>EXPLORATORY:</b> the exact screen (dashboard banner vs. Notifications vs. a dedicated
 * "Family requests" list) has not been captured on-device yet. Locators below are best-effort
 * keyword matches; {@link #dumpForInvestigation()} saves the current screen so the real testIDs
 * can be added once we see the flow render.
 */
public class FamilyLinkApprovalPage extends BasePage {

    private static final By PENDING_LINK_REQUEST_CARD = AppiumBy.xpath(
            "//*[contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'link')"
            + " and contains(translate(@text,"
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'family')]");

    private static final By APPROVE_BTN = AppiumBy.xpath(
            "//*[@text='Approve' or @text='Accept' or @content-desc='testID-primary--main']");

    @Step("Check for a pending family-link request on screen")
    public boolean isPendingLinkRequestVisible(long timeoutSec) {
        return isPresent(PENDING_LINK_REQUEST_CARD, timeoutSec);
    }

    @Step("Open the pending family-link request")
    public void openPendingLinkRequest() {
        tap(PENDING_LINK_REQUEST_CARD);
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
