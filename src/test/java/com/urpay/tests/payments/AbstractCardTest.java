package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.CardsFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.CardBenefitsPage;
import com.urpay.pages.payments.CardInfoPage;
import com.urpay.pages.payments.CardSettingsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Abstract card management test — contains ALL card test cases parameterized by card type.
 *
 * Subclasses only define:
 *   - getCardPrefix() → "madaCard", "alahliCard", "platinumCard", "signatureCard"
 *   - Allure @Epic/@Feature annotations
 *
 * ALL test logic lives here — DRY, no duplication across card types.
 * Each test reads expected values from config using the card prefix.
 */
public abstract class AbstractCardTest extends BaseTest {

    /** Config prefix for this card type (e.g., "madaCard", "alahliCard") */
    protected abstract String getCardPrefix();

    /** Helper: get config value for this card type */
    protected String cardConfig(String key) {
        return ConfigManager.getInstance().get(getCardPrefix() + "." + key);
    }

    /** Helper: get config with default */
    protected String cardConfig(String key, String defaultValue) {
        return ConfigManager.getInstance().get(getCardPrefix() + "." + key, defaultValue);
    }

    /** Login with this card type's user */
    protected DashboardPage loginForCard() {
        return new LoginFlow().loginWith(
                cardConfig("mobileNumber"),
                cardConfig("id"),
                cardConfig("verificationCode", "1234"),
                cardConfig("passCode", "2233"));
    }

    // ═══════════════════════════════════════════════════
    //  1. ISSUE DIGITAL CARD
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 1)
    @Story("Issue Digital Card")
    @Description("Login → navigate to Cards → issue new digital card")
    @Severity(SeverityLevel.BLOCKER)
    public void testIssueDigitalCard() {
        loginForCard();
        captureScreenshot("After Login");

        CardsFlow flow = new CardsFlow();
        flow.issueNewDigitalCard(getCardPrefix());
        captureScreenshot("Card Issued");
    }

    // ═══════════════════════════════════════════════════
    //  2. TOGGLE ONLINE TRANSACTIONS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 2,
            dependsOnMethods = "testIssueDigitalCard")
    @Story("Online Transactions Toggle")
    @Description("Disable online → verify → Enable → verify")
    @Severity(SeverityLevel.NORMAL)
    public void testToggleOnlineTransactions() {
        CardsFlow flow = new CardsFlow();
        String expected = cardConfig("expectedChangesApplied");

        CardSettingsPage settings = flow.disableOnlineTransactions();
        captureScreenshot("Disable Online");
        Assert.assertEquals(settings.getNotificationMessage(), expected);

        settings = flow.enableOnlineTransactions();
        captureScreenshot("Enable Online");
        Assert.assertEquals(settings.getNotificationMessage(), expected);
    }

    // ═══════════════════════════════════════════════════
    //  3. TOGGLE ATM TRANSACTIONS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 3,
            dependsOnMethods = "testToggleOnlineTransactions")
    @Story("ATM Transactions Toggle")
    @Description("Disable ATM → verify → Enable → verify")
    @Severity(SeverityLevel.NORMAL)
    public void testToggleAtmTransactions() {
        CardsFlow flow = new CardsFlow();
        String expected = cardConfig("expectedChangesApplied");

        CardSettingsPage settings = flow.disableAtmTransactions();
        captureScreenshot("Disable ATM");
        Assert.assertEquals(settings.getNotificationMessage(), expected);

        settings = flow.enableAtmTransactions();
        captureScreenshot("Enable ATM");
        Assert.assertEquals(settings.getNotificationMessage(), expected);
    }

    // ═══════════════════════════════════════════════════
    //  4. LOCK / UNLOCK CARD
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 4,
            dependsOnMethods = "testToggleAtmTransactions")
    @Story("Card Lock / Unlock")
    @Description("Lock → verify message → Unlock → verify message")
    @Severity(SeverityLevel.CRITICAL)
    public void testLockUnlockCard() {
        CardsFlow flow = new CardsFlow();

        CardSettingsPage settings = flow.lockCard();
        captureScreenshot("Card Locked");
        Assert.assertEquals(settings.getNotificationMessage(),
                cardConfig("expectedLockMsg"));

        settings = flow.unlockCard();
        captureScreenshot("Card Unlocked");
        Assert.assertEquals(settings.getNotificationMessage(),
                cardConfig("expectedUnlockMsg"));
    }

    // ═══════════════════════════════════════════════════
    //  5. CHANGE CARD PIN
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 5,
            dependsOnMethods = "testLockUnlockCard")
    @Story("Change Card PIN")
    @Description("Change PIN → enter new PIN × 2 → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testChangeCardPin() {
        CardsFlow flow = new CardsFlow();
        flow.goFromSettingsToFirstCard();
        flow.changeCardPin();
        captureScreenshot("PIN Changed");
    }

    // ═══════════════════════════════════════════════════
    //  6. VALIDATE CARD INFO
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 6,
            dependsOnMethods = "testIssueDigitalCard")
    @Story("Card Information")
    @Description("Open card info → verify card number, holder, expiry, CVV → copy each")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateCardInfo() {
        CardInfoPage info = new CardsFlow().openCardInfo();
        captureScreenshot("Card Info");

        SoftAssert soft = new SoftAssert();
        soft.assertFalse(info.getCardNumber().isEmpty(), "Card number must not be empty");
        soft.assertFalse(info.getCardHolderName().isEmpty(), "Card holder must not be empty");
        soft.assertFalse(info.getExpiryDate().isEmpty(), "Expiry date must not be empty");
        soft.assertFalse(info.getCvv().isEmpty(), "CVV must not be empty");

        info.tapCopyCardNumber();
        soft.assertEquals(info.getNotificationMessage(), cardConfig("expectedCopyCardNumber"));
        info.tapCopyCardHolder();
        soft.assertEquals(info.getNotificationMessage(), cardConfig("expectedCopyCardHolder"));
        info.tapCopyExpiry();
        soft.assertEquals(info.getNotificationMessage(), cardConfig("expectedCopyExpiry"));
        info.tapCopyCvv();
        soft.assertEquals(info.getNotificationMessage(), cardConfig("expectedCopyCvv"));

        soft.assertAll();
        getDriver().navigate().back();
    }

    // ═══════════════════════════════════════════════════
    //  7. VALIDATE CARD BENEFITS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 7,
            dependsOnMethods = "testIssueDigitalCard")
    @Story("Card Benefits")
    @Description("Open benefits → verify card name, cashback desc, fees desc")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateCardBenefits() {
        CardBenefitsPage benefits = new CardsFlow().openCardBenefits();
        captureScreenshot("Card Benefits");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(benefits.getCardName(), cardConfig("expectedPhysicalCardName"));
        soft.assertEquals(benefits.getCashbackDescription(), cardConfig("expectedMultipayDesc"));
        soft.assertEquals(benefits.getFeesDescription(), cardConfig("expectedFeesDesc"));
        soft.assertAll();

        benefits.goBack();
    }

    // ═══════════════════════════════════════════════════
    //  8. CANCEL CARD (runs last — destructive)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 8,
            dependsOnMethods = "testIssueDigitalCard")
    @Story("Cancel Card")
    @Description("Cancel card → select reason → confirm → passcode → no thanks")
    @Severity(SeverityLevel.CRITICAL)
    public void testCancelCard() {
        new CardsFlow().cancelCard();
        captureScreenshot("Card Cancelled");
    }
}
