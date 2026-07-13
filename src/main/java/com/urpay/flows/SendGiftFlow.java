package com.urpay.flows;

import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.SendGiftPage;

import io.qameta.allure.Step;

/**
 * Send Gift (Eidya) flow — composes the Send Gift page into parent-send and kid-open journeys.
 *
 * <p>Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/SendGift/* ({@code Wallet_VAS/SendGifts/*}).
 *
 * <p>No Thread.sleep, no assertions — returns the page for the test to assert on.
 */
public class SendGiftFlow {

    private final SendGiftPage giftPage = new SendGiftPage();

    @Step("Navigate Dashboard → Gifts service")
    public SendGiftPage navigateToGifts() {
        new DashboardPage().dismissPopups();
        giftPage.navigateToGifts();
        return giftPage;
    }

    @Step("Send a Marriage gift to {kidSearchMobile}")
    public SendGiftPage sendMarriageGift(String kidSearchMobile, String message, String amount,
            String passcode, String otp) {
        giftPage.sendMarriageGift(kidSearchMobile, message, amount, passcode, otp);
        return giftPage;
    }

    @Step("Open the Sent gifts tab")
    public SendGiftPage openSentGifts() {
        giftPage.openSentTab();
        return giftPage;
    }

    @Step("Open the Received gifts tab")
    public SendGiftPage openReceivedGifts() {
        giftPage.openReceivedTab();
        return giftPage;
    }
}
