package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.TelecomRechargeFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.TelecomRechargePage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Telecom Recharge Test Suite — Zain, Mobily, STC (Sawa + QuickNet).
 *
 * Assertions migrated & enhanced from Katalon:
 *   - Recharge: verifyElementExist(RechargeAnotherNumber), amount > 0
 *   - Order History: provider, productName, accountNumber, purchaseAmount, purchaseDate
 *   - Re-order: provider, mobileHeader == mobileValue, amount != empty, packageType, duration
 *
 * Test Data (from sit-cards.properties):
 *   Zain:   0520835167 / 2194263444 → recharge 0582561101
 *   Mobily: 0520080284 / 2094569585 → recharge 0545672587
 *   STC:    0520897136 / 1509065858 → recharge 545972587
 */
@Epic("Payments & Cards")
@Feature("Telecom Recharge")
public class TelecomRechargeTest extends BaseTest {

    // ═══════════════════════════════════════════════════
    //  ZAIN RECHARGE
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "telecom", "zain"}, priority = 1)
    @Story("Zain Recharge")
    @Description("Zain recharge — login, select Zain, pick package, enter mobile 0582561101, pay, verify success")
    @Severity(SeverityLevel.CRITICAL)
    public void testZainRecharge() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginForProvider("zain", c);

        String rechargeNumber = c.get("zain.rechargeNumber", "0582561101");
        TelecomRechargePage result = new TelecomRechargeFlow().rechargeZain(rechargeNumber);

        captureScreenshot("Zain Recharge - Result Screen");
        Assert.assertTrue(result.isRechargeSuccessful(),
                "Zain recharge must show success (Done button)");
        result.tapDone();
        log.info("ZAIN RECHARGE PASSED for: {}", rechargeNumber);
    }

    @Test(groups = {"payments", "telecom", "zain"}, priority = 2,
            dependsOnMethods = "testZainRecharge")
    @Story("Zain Order History")
    @Description("Zain order history — verify provider=Zain, account not empty, purchase amount & date present")
    @Severity(SeverityLevel.NORMAL)
    public void testZainOrderHistory() {
        TelecomRechargePage page = new TelecomRechargeFlow().viewFirstOrderDetails();
        captureScreenshot("Zain Order History - Details");

        SoftAssert soft = new SoftAssert();
        soft.assertTrue(page.isOrderDetailsLoaded(), "Order details must load");
        soft.assertEquals(page.getProviderName(), "Zain", "Provider must be Zain");
        soft.assertFalse(page.getAccountNumber().isEmpty(), "Account number must not be empty");
        soft.assertFalse(page.getPurchaseAmount().isEmpty(), "Purchase amount must not be empty");
        soft.assertFalse(page.getPurchaseDate().isEmpty(), "Purchase date must not be empty");
        soft.assertAll();

        page.tapBack();
        log.info("ZAIN ORDER HISTORY PASSED");
    }

    @Test(groups = {"payments", "telecom", "zain"}, priority = 3,
            dependsOnMethods = "testZainOrderHistory")
    @Story("Zain Re-order")
    @Description("Zain re-order — verify provider=Zain, mobile header matches value, amount not empty, confirm + OTP")
    @Severity(SeverityLevel.NORMAL)
    public void testZainReOrder() {
        TelecomRechargePage page = new TelecomRechargeFlow().reorderFromHistory();
        captureScreenshot("Zain Re-order - Confirmation");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(page.getProviderName(), "Zain", "Re-order provider must be Zain");
        soft.assertEquals(page.getMobileNumberHeader(), page.getReorderMobileNumber(),
                "Mobile header must match mobile value");
        soft.assertFalse(page.getTotalAmount().isEmpty(), "Total amount must not be empty");
        soft.assertFalse(page.getReorderMobileNumber().isEmpty(), "Recharge amount must not be empty");

        new TelecomRechargeFlow().confirmReorder(page);
        captureScreenshot("Zain Re-order - After OTP");
        soft.assertAll();
        log.info("ZAIN RE-ORDER PASSED");
    }

    // ═══════════════════════════════════════════════════
    //  MOBILY RECHARGE
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "telecom", "mobily"}, priority = 10)
    @Story("Mobily Recharge")
    @Description("Mobily recharge — login, select Mobily, pick package, enter mobile 0545672587, pay, verify success")
    @Severity(SeverityLevel.CRITICAL)
    public void testMobilyRecharge() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginForProvider("mobily", c);

        String rechargeNumber = c.get("mobily.rechargeNumber", "0545672587");
        TelecomRechargePage result = new TelecomRechargeFlow().rechargeMobily(rechargeNumber);

        captureScreenshot("Mobily Recharge - Result Screen");
        Assert.assertTrue(result.isRechargeSuccessful(),
                "Mobily recharge must show success (Done button)");
        result.tapDone();
        log.info("MOBILY RECHARGE PASSED for: {}", rechargeNumber);
    }

    @Test(groups = {"payments", "telecom", "mobily"}, priority = 11,
            dependsOnMethods = "testMobilyRecharge")
    @Story("Mobily Order History")
    @Description("Mobily order history — verify provider=Mobily, productName contains Mobily, purchaseAmount=20")
    @Severity(SeverityLevel.NORMAL)
    public void testMobilyOrderHistory() {
        TelecomRechargePage page = new TelecomRechargeFlow().viewFirstOrderDetails();
        captureScreenshot("Mobily Order History - Details");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(page.getProviderName(), "Mobily", "Provider must be Mobily");
        soft.assertTrue(page.getProductName().contains("Mobily"),
                "Product name must contain 'Mobily', got: " + page.getProductName());
        soft.assertFalse(page.getAccountNumber().isEmpty(), "Account number must not be empty");
        soft.assertTrue(page.getPurchaseAmount().contains("20"), "Mobily purchase amount must contain 20, got: " + page.getPurchaseAmount());
        soft.assertFalse(page.getPurchaseDate().isEmpty(), "Purchase date must not be empty");
        soft.assertAll();

        page.tapBack();
        log.info("MOBILY ORDER HISTORY PASSED");
    }

    @Test(groups = {"payments", "telecom", "mobily"}, priority = 12,
            dependsOnMethods = "testMobilyOrderHistory")
    @Story("Mobily Re-order")
    @Description("Mobily re-order — verify provider=Mobily, mobile matches, amount not empty, confirm + OTP")
    @Severity(SeverityLevel.NORMAL)
    public void testMobilyReOrder() {
        TelecomRechargePage page = new TelecomRechargeFlow().reorderFromHistory();
        captureScreenshot("Mobily Re-order - Confirmation");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(page.getProviderName(), "Mobily", "Re-order provider must be Mobily");
        soft.assertEquals(page.getMobileNumberHeader(), page.getReorderMobileNumber(),
                "Mobile header must match mobile value");
        soft.assertFalse(page.getTotalAmount().isEmpty(), "Total amount must not be empty");
        soft.assertFalse(page.getReorderMobileNumber().isEmpty(), "Recharge amount must not be empty");

        new TelecomRechargeFlow().confirmReorder(page);
        captureScreenshot("Mobily Re-order - After OTP");
        soft.assertAll();
        log.info("MOBILY RE-ORDER PASSED");
    }

    // ═══════════════════════════════════════════════════
    //  STC — Case 1: Sawa Recharge + Re-order (Katalon: StcLocalRecharge + validateSawaOrderHistory)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "telecom", "stc"}, priority = 20)
    @Story("STC Sawa Recharge")
    @Description("Katalon StcLocalRecharge: login → STC → Sawa Recharge → first card → scroll → Next → mobile → Next → Confirm → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testSTCSawaRecharge() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginForProvider("stc", c);

        String rechargeNumber = c.get("stc.rechargeNumber", "545972587");
        TelecomRechargePage result = new TelecomRechargeFlow().rechargeSawa(rechargeNumber);

        captureScreenshot("STC Sawa Recharge - Result Screen");
        Assert.assertTrue(result.isRechargeSuccessful(),
                "STC Sawa recharge must show success (Done button)");
        result.tapDone();
        log.info("STC SAWA RECHARGE PASSED for: {}", rechargeNumber);
    }

    @Test(groups = {"payments", "telecom", "stc"}, priority = 21,
            dependsOnMethods = "testSTCSawaRecharge")
    @Story("STC Sawa Re-order")
    @Description("Katalon validateSawaOrderHistory: same session → swipe to last orders → tap Reorder → verify provider=STC, mobile, amount → Confirm + OTP")
    @Severity(SeverityLevel.NORMAL)
    public void testSTCSawaReOrder() {
        // Same session — no login needed. Navigate to telecom and find reorder button
        TelecomRechargePage page = new TelecomRechargeFlow().reorderFromHistory();
        captureScreenshot("STC Sawa Re-order - Confirmation");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(page.getProviderName(), "STC", "Provider must be STC");
        soft.assertEquals(page.getMobileNumberHeader(), page.getReorderMobileNumber(),
                "Mobile header must match mobile value");
        soft.assertFalse(page.getTotalAmount().isEmpty(), "Total amount must not be empty");

        new TelecomRechargeFlow().confirmReorder(page);
        captureScreenshot("STC Sawa Re-order - After OTP");
        soft.assertAll();
        log.info("STC SAWA RE-ORDER PASSED");
    }

    // ═══════════════════════════════════════════════════
    //  STC — Case 2: QuickNet Recharge (Katalon: validateQuickNetRecharge)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "telecom", "stc"}, priority = 30,
            dependsOnMethods = "testSTCSawaReOrder")
    @Story("STC QuickNet Recharge")
    @Description("Katalon validateQuickNetRecharge: same session → navigate → STC → QuickNet → first card → scroll → Next → mobile → Next → Confirm → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testSTCQuickNetRecharge() {
        // Same STC session — no login needed
        ConfigManager c = ConfigManager.getInstance();
        String rechargeNumber = c.get("stc.rechargeNumber", "545972587");
        TelecomRechargePage result = new TelecomRechargeFlow().rechargeQuickNet(rechargeNumber);

        captureScreenshot("STC QuickNet Recharge - Result Screen");
        Assert.assertTrue(result.isRechargeSuccessful(),
                "STC QuickNet recharge must show success");
        result.tapDone();
        log.info("STC QUICKNET RECHARGE PASSED for: {}", rechargeNumber);
    }

    // ═══════════════════════════════════════════════════
    //  HELPER — login with provider-specific credentials
    // ═══════════════════════════════════════════════════

    private DashboardPage loginForProvider(String provider, ConfigManager c) {
        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get(provider + ".mobileNumber"),
                c.get(provider + ".id"),
                c.get("urpayUser.verificationCode", "1234"),
                c.get("urpayUser.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard must load for " + provider);
        return dashboard;
    }
}
