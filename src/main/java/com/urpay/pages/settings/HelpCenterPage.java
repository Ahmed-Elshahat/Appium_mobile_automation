package com.urpay.pages.settings;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.qameta.allure.Step;

/** Help Center navigation and support-ticket actions. */
public class HelpCenterPage extends BasePage {

    private static final By MORE_NAV =
            AppiumBy.accessibilityId("testID-MORENAV");

        private static final By LATER_BUTTON = AppiumBy.xpath(
            "//*[starts-with(@content-desc,'testID-secondary-') and @clickable='true']");

        private static final By HELP_CENTER = AppiumBy.xpath(
            "//*[@content-desc='testID-viewElemenHelpCenter']/parent::*[@clickable='true']");

    private static final By WALLET_MANAGEMENT = AppiumBy.xpath(
            "//*[@text='Wallet Management' or @text='Wallet management'"
                    + " or contains(@content-desc,'WalletManagement')"
                    + " or contains(@content-desc,'Wallet Management')]");

        private static final By FIRST_ISSUE =
            AppiumBy.accessibilityId("testID-radio-item-0");

        private static final By OTHER_SUBCATEGORY =
            AppiumBy.accessibilityId("testID-radio-item-1");

        private static final By PRIMARY_BUTTON =
            AppiumBy.accessibilityId("testID-primary--main");

        private static final By DESCRIPTION =
            AppiumBy.accessibilityId("testID-input-direct-description");

        private static final By CREATE_TICKET_TITLE =
            AppiumBy.xpath("//*[@text='Create Ticket']");

            private static final By TICKET_SUCCESS = AppiumBy.xpath(
                "//*[@text='We have received your ticket and will address it as a high priority']");

    @Step("Open Wallet Management from Help Center")
    public void openWalletManagement() {
        if (isPresent(LATER_BUTTON, 8)) {
            tap(LATER_BUTTON);
        }
        tap(MORE_NAV);
        tap(HELP_CENTER);
        tap(WALLET_MANAGEMENT);
    }

    @Step("Select the Wallet Management issue and Other subcategory")
    public void selectGeneralWalletIssue() {
        tap(FIRST_ISSUE);
        tap(PRIMARY_BUTTON);
        waitUtils.waitForVisible(OTHER_SUBCATEGORY, 15);
        tap(OTHER_SUBCATEGORY);
        tap(PRIMARY_BUTTON);
        waitUtils.waitForVisible(CREATE_TICKET_TITLE, 15);
    }

    @Step("Describe and submit the support ticket")
    public void submitTicket(String description) {
        type(DESCRIPTION, description);
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.ENTER));
        tap(PRIMARY_BUTTON);
    }

    public boolean isTicketOpenedSuccessfully(long timeoutSec) {
        return isPresent(TICKET_SUCCESS, timeoutSec);
    }
}