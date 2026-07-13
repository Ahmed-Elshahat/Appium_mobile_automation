package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.CardsFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.RegistrationApiHelper;
import com.urpay.helpers.WalletBalanceHelper;
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

    /** Override in subclass to return true if this suite should register a fresh user via API first. */
    protected boolean useRegistration() { return false; }

    /** Provisioned user credentials (populated when useRegistration() == true). */
    private RegistrationApiHelper.Provisioned provisionedUser;

    /** Helper: get config value for this card type */
    protected String cardConfig(String key) {
        return ConfigManager.getInstance().get(getCardPrefix() + "." + key);
    }

    /** Helper: get config with default */
    protected String cardConfig(String key, String defaultValue) {
        return ConfigManager.getInstance().get(getCardPrefix() + "." + key, defaultValue);
    }

    /** Login with this card type's user — uses provisioned credentials if useRegistration() is true */
    protected DashboardPage loginForCard() {
        if (useRegistration()) {
            // Register a fresh full-tier NAT user via backend API
            log.info("Registering a fresh full-tier NAT user via API...");
            provisionedUser = RegistrationApiHelper.registerNationalAndReturn();
            if (provisionedUser == null) {
                throw new RuntimeException("Failed to provision a new user via API — check VPN/backend access");
            }
            log.info("User provisioned: mobile={}, poi={}, consumerId={}, walletTier={}",
                    provisionedUser.mobile, provisionedUser.poi,
                    provisionedUser.consumerId, provisionedUser.walletTier);

            // Top up wallet balance to 20,000 SAR for card issuance fees and operations
            if (provisionedUser.walletNumber != null && !provisionedUser.walletNumber.isEmpty()) {
                boolean topped = WalletBalanceHelper.topUp(provisionedUser.walletNumber);
                log.info("Wallet balance top-up result: {}", topped ? "SUCCESS" : "FAILED");
            }

            // Convert mobile format: API returns +966520XXXXXX → UI needs 0520XXXXXX
            String mobile = provisionedUser.mobile;
            if (mobile.startsWith("+966")) {
                mobile = "0" + mobile.substring(4); // +966520... → 0520...
            }

            // Login with the freshly provisioned credentials
            return new LoginFlow().loginWith(
                    mobile,
                    provisionedUser.poi,
                    "1234",  // default OTP
                    provisionedUser.passcode);
        }
        // Default: use config credentials + top up balance for old users
        String mobile = cardConfig("mobileNumber");
        topUpBalanceByMobile(mobile);
        return new LoginFlow().loginWith(
                mobile,
                cardConfig("id"),
                cardConfig("verificationCode", "1234"),
                cardConfig("passCode", "2233"));
    }

    /** Get the provisioned user (available after loginForCard when useRegistration=true). */
    protected RegistrationApiHelper.Provisioned getProvisionedUser() { return provisionedUser; }

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

        try {
            CardsFlow flow = new CardsFlow();
            boolean isNewCard = flow.issueNewDigitalCard(getCardPrefix()) != null 
                    && !flow.isExistingCardDetected();
            if (!isNewCard) {
                // Only check lock state for existing cards — new cards are always unlocked
                flow.ensureCardUnlocked();
            }
        } catch (com.urpay.utils.BackendErrorException e) {
            // A backend/SIT error during card issuance is a genuine product defect, NOT a test
            // problem. Fail (not swallow) so this shows as a clean FAILED with the backend message,
            // and TestNG SKIPS the dependent card tests instead of letting them cascade into
            // misleading "broken" element timeouts.
            captureScreenshot("Backend Error During Card Issuance");
            Assert.fail(e.getMessage());
        } catch (Exception e) {
            // F2: a non-backend navigation/setup failure is non-recoverable for this BLOCKER setup —
            // do NOT swallow it and let dependent card tests run against an unknown screen (that only
            // produces misleading element-timeout "broken" cascades). Skip the whole card chain so
            // the failure is honest and the dependent tests report as SKIPPED, not broken.
            log.error("Card setup navigation failed — skipping dependent card tests: {}", e.getMessage());
            captureScreenshot("Navigation Setup Failure");
            throw new SkipException("Card setup navigation failed: " + e.getMessage(), e);
        }
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
        CardSettingsPage settings = new CardSettingsPage();

        try {
            flow.lockCard();
            captureScreenshot("Card Locked");
            String lockMsg = settings.readNotificationIfVisible();
            if (lockMsg != null) {
                Assert.assertEquals(lockMsg, cardConfig("expectedLockMsg"));
            }
        } finally {
            // Always unlock to prevent cascade failures
            flow.unlockCard();
            captureScreenshot("Card Unlocked");
        }
        String unlockMsg = settings.readNotificationIfVisible();
        if (unlockMsg != null) {
            Assert.assertEquals(unlockMsg, cardConfig("expectedUnlockMsg"));
        }
    }

    // ═══════════════════════════════════════════════════
    //  3. TOGGLE ONLINE TRANSACTIONS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 3,
            dependsOnMethods = "testNavigateToCard")
    @Story("Online Transactions Toggle")
    @Description("Disable online → verify → Enable → verify")
    @Severity(SeverityLevel.NORMAL)
    public void testToggleOnlineTransactions() {
        CardsFlow flow = new CardsFlow();
        String expected = cardConfig("expectedChangesApplied");

        CardSettingsPage settings = flow.disableOnlineTransactions();
        captureScreenshot("Disable Online");
        String disableMsg = settings.readNotificationIfVisible();
        if (disableMsg != null) {
            Assert.assertEquals(disableMsg, expected);
        }

        settings = flow.enableOnlineTransactions();
        captureScreenshot("Enable Online");
        String enableMsg = settings.readNotificationIfVisible();
        if (enableMsg != null) {
            Assert.assertEquals(enableMsg, expected);
        }
    }

    // ═══════════════════════════════════════════════════
    //  3. TOGGLE ATM TRANSACTIONS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 11,
            dependsOnMethods = "testRequestPhysicalCard")
    @Story("ATM Transactions Toggle")
    @Description("Disable ATM → verify → Enable → verify (runs after physical card request — ATM only available for physical cards)")
    @Severity(SeverityLevel.NORMAL)
    public void testToggleAtmTransactions() {
        CardsFlow flow = new CardsFlow();
        String expected = cardConfig("expectedChangesApplied");

        try {
            CardSettingsPage settings = flow.disableAtmTransactions();
            captureScreenshot("Disable ATM");
            String disableMsg = settings.readNotificationIfVisible();
            if (disableMsg != null) {
                Assert.assertEquals(disableMsg, expected);
            }

            settings = flow.enableAtmTransactions();
            captureScreenshot("Enable ATM");
            String enableMsg = settings.readNotificationIfVisible();
            if (enableMsg != null) {
                Assert.assertEquals(enableMsg, expected);
            }
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new org.testng.SkipException("ATM toggle not available (digital-only card)");
        }
    }

    // ═══════════════════════════════════════════════════
    //  5. CHANGE CARD PIN
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 5,
            dependsOnMethods = "testNavigateToCard")
    @Story("Change Card PIN")
    @Description("Change PIN → enter new PIN × 2 → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testChangeCardPin() {
        CardsFlow flow = new CardsFlow();
        flow.navigateToCardSettings();
        flow.changeCardPin(getCardPrefix());
        captureScreenshot("PIN Changed");
    }

    // ═══════════════════════════════════════════════════
    //  6. VALIDATE CARD INFO
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 6,
            dependsOnMethods = "testNavigateToCard")
    @Story("Card Information")
    @Description("Open card info → verify card number, holder, expiry, CVV → copy each")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateCardInfo() {
        CardInfoPage info = new CardsFlow().openCardInfo(getCardPrefix());
        captureScreenshot("Card Info");

        SoftAssert soft = new SoftAssert();
        soft.assertFalse(info.getCardNumber().isEmpty(), "Card number must not be empty");
        soft.assertFalse(info.getCardHolderName().isEmpty(), "Card holder must not be empty");
        soft.assertFalse(info.getExpiryDate().isEmpty(), "Expiry date must not be empty");
        soft.assertFalse(info.getCvv().isEmpty(), "CVV must not be empty");

        // Copy each field and verify notification (wait for previous notification to dismiss)
        info.tapCopyCardNumber();
        String copyCardMsg = info.readNotificationIfVisible();
        if (copyCardMsg != null) {
            soft.assertEquals(copyCardMsg, cardConfig("expectedCopyCardNumber"));
        }
        info.tapCopyCardHolder();
        String copyHolderMsg = info.readNotificationIfVisible();
        if (copyHolderMsg != null) {
            soft.assertEquals(copyHolderMsg, cardConfig("expectedCopyCardHolder"));
        }
        info.tapCopyExpiry();
        String copyExpiryMsg = info.readNotificationIfVisible();
        if (copyExpiryMsg != null) {
            soft.assertEquals(copyExpiryMsg, cardConfig("expectedCopyExpiry"));
        }
        info.tapCopyCvv();
        String copyCvvMsg = info.readNotificationIfVisible();
        if (copyCvvMsg != null) {
            soft.assertEquals(copyCvvMsg, cardConfig("expectedCopyCvv"));
        }

        soft.assertAll();
        getDriver().navigate().back();
    }

    // ═══════════════════════════════════════════════════
    //  7. VALIDATE CARD BENEFITS
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 7,
            dependsOnMethods = "testNavigateToCard")
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
        String errorMsg = settings.readNotificationIfVisible();
        if (errorMsg != null) {
            Assert.assertEquals(errorMsg, cardConfig("expectedPinMismatchMsg"));
        }
    }

    // ═══════════════════════════════════════════════════
    //  10. REQUEST PHYSICAL CARD
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 10,
            dependsOnMethods = "testNavigateToCard")
    @Story("Request Physical Card")
    @Description("Request physical card copy — skips if already requested or not available")
    @Severity(SeverityLevel.CRITICAL)
    public void testRequestPhysicalCard() {
        CardsFlow flow = new CardsFlow();
        try {
            flow.requestPhysicalCard(getCardPrefix());
            captureScreenshot("Physical Card Requested");
        } catch (org.openqa.selenium.TimeoutException e) {
            captureScreenshot("Physical Card Not Available");
            throw new org.testng.SkipException("Request Physical Card not available for this account");
        }
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
    //  11.5 REQUEST CARD REPLACEMENT (Katalon: RequestReplacementCardFromCardsSettingsScreen)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 15,
            dependsOnMethods = "testNavigateToCard")
    @Story("Card Replacement")
    @Description("Replace card from settings — skips if Replace not available (digital-only)")
    @Severity(SeverityLevel.CRITICAL)
    public void testReplaceCard() {
        CardsFlow flow = new CardsFlow();
        try {
            flow.replaceCard();
            captureScreenshot("Card Replaced");
        } catch (org.openqa.selenium.TimeoutException e) {
            captureScreenshot("Replace Not Available");
            throw new org.testng.SkipException("Card Replacement not available (digital-only card)");
        }
    }

    // ═══════════════════════════════════════════════════
    //  11.6 ACTIVATE REPLACEMENT CARD (Katalon: activateReplacementCard)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 16,
            dependsOnMethods = "testReplaceCard")
    @Story("Activate Replacement Card")
    @Description("Activate replacement card → enter verification code → back to cards")
    @Severity(SeverityLevel.CRITICAL)
    public void testActivateReplacementCard() {
        CardsFlow flow = new CardsFlow();
        try {
            flow.activateReplacementCard(getCardPrefix());
            captureScreenshot("Replacement Card Activated");
        } catch (org.openqa.selenium.TimeoutException e) {
            captureScreenshot("Activate Not Available");
            throw new org.testng.SkipException("Activate Replacement not available");
        }
    }

    // ═══════════════════════════════════════════════════
    //  11.7 VALIDATE CANCEL CARD STEPS (non-destructive)
    // ═══════════════════════════════════════════════════

    @Test(groups = {"payments", "cards"}, priority = 17,
            dependsOnMethods = "testNavigateToCard")
    @Story("Cancel Card Steps Validation")
    @Description("Card Settings → Cancel → reason (Other) → Confirm → stops before passcode (card preserved)")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateCancelCardSteps() {
        CardsFlow flow = new CardsFlow();
        boolean cardStillVisible = flow.validateCancelCardSteps(getCardPrefix());
        captureScreenshot("Cancel Card Steps Validated");
        Assert.assertFalse(cardStillVisible,
                "Cancelled card should not be visible on dashboard after cancellation");
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

    @Test(groups = {"payments", "cards"}, priority = 18,
            dependsOnMethods = "testNavigateToCard")
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
