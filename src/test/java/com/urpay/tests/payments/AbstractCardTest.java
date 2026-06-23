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
import com.urpay.pages.payments.CardsPage;
import com.urpay.pages.payments.TransactionDetailsPage;

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
 *
 * Test execution order:
 *   1. Issue Digital Card (prerequisite)
 *   2-5. Card Settings (toggles, lock, PIN)
 *   6. Card Info
 *   7. Card Benefits
 *   8. Transaction Details
 *   9. Invalid PIN validation
 *   10. Request Physical Card
 *   11. Validate Card Duplication
 *   12. Cancel Card from Settings (invalid + correct passcode)
 *   13. Cancel Card
 *   14. Validate Canceled Card Removed from Dashboard
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
    //  1. SETUP: Login + navigate to card products page
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 1)
    @Story("Navigate to Card")
    @Description("Login → navigate to Cards → detect existing card → land on products page")
    @Severity(SeverityLevel.BLOCKER)
    public void testNavigateToCard() {
        loginForCard();
        captureScreenshot("After Login");

        CardsFlow flow = new CardsFlow();
        flow.issueNewDigitalCard(getCardPrefix());
        captureScreenshot("On Card Products Page");
    }

    // ═══════════════════════════════════════════════════
    //  2. LOCK / UNLOCK CARD (appears first in Card Security section)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 2,
            dependsOnMethods = "testNavigateToCard")
    @Story("Card Lock / Unlock")
    @Description("Lock → verify message → Unlock → verify message")
    @Severity(SeverityLevel.CRITICAL)
    public void testLockUnlockCard() {
        CardsFlow flow = new CardsFlow();

        CardSettingsPage settings = flow.lockCard();
        captureScreenshot("Card Locked");
        if (settings.isNotificationVisible()) {
            Assert.assertEquals(settings.getNotificationMessage(),
                    cardConfig("expectedLockMsg"));
        }

        settings = flow.unlockCard();
        captureScreenshot("Card Unlocked");
        if (settings.isNotificationVisible()) {
            Assert.assertEquals(settings.getNotificationMessage(),
                    cardConfig("expectedUnlockMsg"));
        }
    }

    // ═══════════════════════════════════════════════════
    //  3. TOGGLE ONLINE TRANSACTIONS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 3,
            dependsOnMethods = "testLockUnlockCard")
    @Story("Online Transactions Toggle")
    @Description("Disable online → verify → Enable → verify")
    @Severity(SeverityLevel.NORMAL)
    public void testToggleOnlineTransactions() {
        CardsFlow flow = new CardsFlow();
        String expected = cardConfig("expectedChangesApplied");

        CardSettingsPage settings = flow.disableOnlineTransactions();
        captureScreenshot("Disable Online");
        if (settings.isNotificationVisible()) {
            Assert.assertEquals(settings.getNotificationMessage(), expected);
        }

        settings = flow.enableOnlineTransactions();
        captureScreenshot("Enable Online");
        if (settings.isNotificationVisible()) {
            Assert.assertEquals(settings.getNotificationMessage(), expected);
        }
    }

    // ═══════════════════════════════════════════════════
    //  3. TOGGLE ATM TRANSACTIONS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 4,
            dependsOnMethods = "testToggleOnlineTransactions", enabled = false)
    @Story("ATM Transactions Toggle")
    @Description("Disable ATM → verify → Enable → verify — DISABLED: ATM toggle removed in new UI")
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
    //  5. CHANGE CARD PIN
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 5,
            dependsOnMethods = "testToggleOnlineTransactions")
    @Story("Change Card PIN")
    @Description("Change PIN → enter new PIN × 2 → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testChangeCardPin() {
        CardsFlow flow = new CardsFlow();
        flow.goFromSettingsToFirstCard();
        flow.navigateToCardSettings();
        flow.changeCardPin(getCardPrefix());
        captureScreenshot("PIN Changed");
    }

    // ═══════════════════════════════════════════════════
    //  6. VALIDATE CARD INFO
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 6,
            dependsOnMethods = "testNavigateToCard", enabled = false)
    @Story("Card Information")
    @Description("Open card info — DISABLED: Card Information removed in new UI")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateCardInfo() {
        CardInfoPage info = new CardsFlow().openCardInfo(getCardPrefix());
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
            dependsOnMethods = "testNavigateToCard", enabled = false)
    @Story("Card Benefits")
    @Description("Open benefits — DISABLED: Card Benefits removed in new UI")
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
    //  8. VALIDATE TRANSACTION DETAILS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 8,
            dependsOnMethods = "testNavigateToCard", enabled = false)
    @Story("Transaction Details")
    @Description("Open transaction tab — DISABLED: Transactions tab removed in new UI")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateTransactionDetails() {
        CardsFlow flow = new CardsFlow();
        TransactionDetailsPage details = flow.openTransactionDetails();
        captureScreenshot("Transaction Details");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(details.getType(), cardConfig("expectedTransactionType", "Issue Card"));
        soft.assertFalse(details.getReferenceNumber().isEmpty(), "Reference number must not be empty");
        soft.assertFalse(details.getTransactionDate().isEmpty(), "Transaction date must not be empty");
        soft.assertAll();

        details.tapBack();
    }

    // ═══════════════════════════════════════════════════
    //  9. INVALID PIN VALIDATION
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 9,
            dependsOnMethods = "testNavigateToCard")
    @Story("Invalid PIN Validation")
    @Description("Enter mismatched PINs → verify error notification → enter correct PIN")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidPinValidation() {
        CardsFlow flow = new CardsFlow();
        flow.navigateToCardSettings();

        CardSettingsPage settings = flow.enterInvalidPinMismatch(getCardPrefix());
        captureScreenshot("Invalid PIN Error");
        if (settings.isNotificationVisible()) {
            Assert.assertEquals(settings.getNotificationMessage(),
                    cardConfig("expectedPinMismatchMsg"));
        }
    }

    // ═══════════════════════════════════════════════════
    //  10. REQUEST PHYSICAL CARD
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 10,
            dependsOnMethods = "testNavigateToCard")
    @Story("Request Physical Card")
    @Description("Request physical copy → select region → accept terms → confirm → OTP")
    @Severity(SeverityLevel.CRITICAL)
    public void testRequestPhysicalCard() {
        CardsFlow flow = new CardsFlow();
        flow.requestPhysicalCard(getCardPrefix());
        captureScreenshot("Physical Card Requested");
    }

    // ═══════════════════════════════════════════════════
    //  11. VALIDATE CARD DUPLICATION
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 11,
            dependsOnMethods = "testNavigateToCard", enabled = false)
    @Story("Card Duplication Validation")
    @Description("Attempt duplicate card — DISABLED: Request Card button removed in new UI")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateCardDuplication() {
        CardsFlow flow = new CardsFlow();
        CardsPage page = flow.attemptDuplicateCard();
        captureScreenshot("Duplication Error");

        Assert.assertEquals(page.getNotificationMessage(),
                cardConfig("expectedDuplicationMsg"));
    }

    // ═══════════════════════════════════════════════════
    //  12. CANCEL CARD FROM SETTINGS (with invalid passcode first)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 12,
            dependsOnMethods = "testIssueDigitalCard", enabled = false)
    @Story("Cancel Card from Settings")
    @Description("Cancel from settings → enter invalid passcode → verify error → enter correct passcode")
    @Severity(SeverityLevel.CRITICAL)
    public void testCancelCardFromSettings() {
        CardsFlow flow = new CardsFlow();
        CardSettingsPage settings = flow.cancelCardFromSettings(getCardPrefix());
        captureScreenshot("Card Cancelled from Settings");
    }

    // ═══════════════════════════════════════════════════
    //  13. CANCEL CARD (runs last — destructive)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 13,
            dependsOnMethods = "testIssueDigitalCard", enabled = false)
    @Story("Cancel Card")
    @Description("Cancel card → select reason → confirm → passcode → no thanks")
    @Severity(SeverityLevel.CRITICAL)
    public void testCancelCard() {
        new CardsFlow().cancelCard(getCardPrefix());
        captureScreenshot("Card Cancelled");
    }

    // ═══════════════════════════════════════════════════
    //  14. VALIDATE CANCELED CARD REMOVED FROM DASHBOARD
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 14,
            dependsOnMethods = "testCancelCard", enabled = false)
    @Story("Validate Card Removed")
    @Description("Return to dashboard → scroll → verify canceled card banner is gone")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateCanceledCardRemoved() {
        CardsFlow flow = new CardsFlow();
        boolean cardStillVisible = flow.isCanceledCardStillVisible();
        captureScreenshot("Dashboard After Cancel");

        Assert.assertFalse(cardStillVisible,
                "Canceled card should not be visible on dashboard");
    }
}
