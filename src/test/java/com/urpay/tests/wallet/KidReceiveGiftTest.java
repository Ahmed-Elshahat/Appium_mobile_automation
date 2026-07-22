package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.SendGiftFlow;
import com.urpay.helpers.DeviceContactsHelper;
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
 * Kid Receive Gift from Parent — single LambdaTest session, two phases.
 *
 * <p>Migrated from the Katalon "SendingGifts Collection" (parent-send suite + kid-receive suite):
 * <ul>
 *   <li>{@code Wallet_VAS/SendGifts/ParentValidationTCs/*} — parent sends a Marriage gift.</li>
 *   <li>{@code Wallet_VAS/SendGifts/KidValidationTCs/*} — kid opens the received gift.</li>
 * </ul>
 *
 * <p>Both phases run in ONE session (both {@code @Test}s live in this class, chained via
 * {@code dependsOnMethods}; the driver is torn down once at {@code @AfterClass}):
 * <ol>
 *   <li>Parent logs in → sends a Marriage gift to the kid (seeds the UNOPENED gift) → <b>logs out</b>.</li>
 *   <li>Kid logs in → opens the Received tab → verifies "Not opened" → opens it → verifies "Opened".</li>
 * </ol>
 */
@Epic("Wallet & VAS")
@Feature("Send Gift")
public class KidReceiveGiftTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  1) PARENT: SEND GIFT TO KID + LOGOUT
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "send-gift", "family"}, priority = 1)
    @Story("Parent sends a Marriage gift to the kid")
    @Description("Parent sends a Marriage gift to the kid (seeding the unopened received gift), "
            + "then logs out so the kid can log in on the same session")
    @Severity(SeverityLevel.CRITICAL)
    public void testParentSendsGiftToKid() {
        ConfigManager c = ConfigManager.getInstance();
        String appPackage = c.get("appPackage", "com.urpay.consumer.sit");

        // The "Cash" gift picks the recipient from the device's phone contacts, and the app caches an
        // empty contact list at first launch. So seed the kid as a device contact BEFORE login, then
        // restart the app so its launch-time contacts read includes it.
        DeviceContactsHelper.ensureContact(getDriver(), appPackage,
                c.get("sendGift.kidName", "GiftKid"),
                c.get("sendGift.kidContactNumber", "+" + c.get("sendGift.kidSearchMobile")));
        DeviceContactsHelper.restartApp(getDriver(), appPackage);

        DashboardPage dashboard = loginAsParent();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent login");

        SendGiftFlow flow = new SendGiftFlow();
        flow.navigateToGifts();

        SendGiftPage page = flow.sendMarriageGift(
                c.get("sendGift.kidName", "GiftKid"),
                c.get("sendGift.message", "Congratulations"),
                c.get("sendGift.amount", "5"),
                c.get("sendGift.parent.passCode", "2233"),
                c.get("sendGift.parent.verificationCode", "1234"));

        Assert.assertTrue(page.isThankYouShown(), "Gift 'Thank You' screen should be displayed");
        page.tapDone();

        // Log out (same session) so the kid can log in next.
        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  2) KID: LOGIN + OPEN THE RECEIVED GIFT
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "send-gift", "family"}, priority = 2,
            dependsOnMethods = "testParentSendsGiftToKid")
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

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login as the gift-sending parent")
    private DashboardPage loginAsParent() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("sendGift.parent.mobileNumber"),
                c.get("sendGift.parent.id"),
                c.get("sendGift.parent.verificationCode", "1234"),
                c.get("sendGift.parent.passCode", "2233"));
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
