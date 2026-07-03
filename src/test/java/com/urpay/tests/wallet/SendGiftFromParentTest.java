package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.SendGiftFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.SendGiftPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Send Gift From Parent to Kid.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/SendGift/Family Wallet Send Gift From Parent to Kid Test Suite
 *   ({@code Wallet_VAS/SendGifts/ParentValidationTCs/*}).
 *
 * <p>Login as parent → open the Gifts (Eidya) service → select the kid from contacts → send a
 * Marriage gift (fixed message + amount, replacing the non-portable random-gift keyword) → confirm
 * with the passcode → verify the "Thank You" screen → confirm the gift shows in the Sent tab as
 * "Not opened" with the Marriage type.
 */
@Epic("Wallet & VAS")
@Feature("Send Gift")
public class SendGiftFromParentTest extends BaseTest {

    @Test(groups = {"wallet", "send-gift", "family"}, priority = 1)
    @Story("Parent sends a Marriage gift to a kid")
    @Description("Parent sends a Marriage gift to the kid and verifies it appears as 'Not opened' in "
            + "the Sent tab with the correct gift type")
    @Severity(SeverityLevel.CRITICAL)
    public void testParentSendsGiftToKid() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsParent();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent login");

        SendGiftFlow flow = new SendGiftFlow();
        flow.navigateToGifts();

        SendGiftPage page = flow.sendMarriageGift(
                c.get("sendGift.kidSearchMobile"),
                c.get("sendGift.message", "Congratulations"),
                c.get("sendGift.amount", "5"),
                c.get("sendGift.parent.passCode", "2233"));

        Assert.assertTrue(page.isThankYouShown(), "Gift 'Thank You' screen should be displayed");
        page.tapDone();

        flow.openSentGifts();
        Assert.assertEquals(page.getGiftStatus().trim(),
                c.get("sendGift.notOpenedStatus", "Not opened"),
                "The sent gift status should be 'Not opened'");
        Assert.assertEquals(page.getGiftType(), c.get("sendGift.giftType", "Marriage"),
                "The sent gift type should be 'Marriage'");
    }

    @Step("Login as the gift-sending parent")
    private DashboardPage loginAsParent() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("sendGift.parent.mobileNumber"),
                c.get("sendGift.parent.id"),
                c.get("sendGift.parent.verificationCode", "1234"),
                c.get("sendGift.parent.passCode", "2233"));
    }
}
