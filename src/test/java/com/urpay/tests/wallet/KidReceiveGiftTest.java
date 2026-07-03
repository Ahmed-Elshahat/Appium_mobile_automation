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
 * Kid Receive Gift from Parent.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/SendGift/Family Wallet Kid Receive Gift from Parent
 *   ({@code Wallet_VAS/SendGifts/KidValidationTCs/*}).
 *
 * <p>Login as the kid → open the Gifts (Eidya) service → open the Received tab → verify the latest
 * gift is "Not opened" → open it → verify the status becomes "Opened".
 *
 * <p>Depends on a pending (unopened) received gift for the kid account — in Katalon this was
 * DB-seeded before the run; here it relies on an existing received gift on that account.
 */
@Epic("Wallet & VAS")
@Feature("Send Gift")
public class KidReceiveGiftTest extends BaseTest {

    @Test(groups = {"wallet", "send-gift", "family"}, priority = 1)
    @Story("Kid opens a received gift")
    @Description("Kid opens the latest received gift and verifies the status changes from "
            + "'Not opened' to 'Opened'")
    @Severity(SeverityLevel.CRITICAL)
    public void testKidOpensReceivedGift() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsKid();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after kid login");

        SendGiftFlow flow = new SendGiftFlow();
        flow.navigateToGifts();

        SendGiftPage page = flow.openReceivedGifts();
        Assert.assertEquals(page.getGiftStatus().trim(),
                c.get("sendGift.notOpenedStatus", "Not opened"),
                "The received gift should be 'Not opened' before opening");

        page.openReceivedGift();

        Assert.assertEquals(page.getOpenedGiftStatus().trim(),
                c.get("sendGift.openedStatus", "Opened"),
                "The received gift status should be 'Opened' after opening");
    }

    @Step("Login as the gift-receiving kid")
    private DashboardPage loginAsKid() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("sendGift.kid.mobileNumber"),
                c.get("sendGift.kid.id"),
                c.get("sendGift.kid.verificationCode", "1234"),
                c.get("sendGift.kid.passCode", "2233"));
    }
}
