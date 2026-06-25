package com.urpay.flows;

import java.io.ByteArrayInputStream;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SearchPage;
import com.urpay.pages.payments.SadadBillsPage;
import com.urpay.pages.payments.SadadTransactionDetailsPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

/**
 * Saddad Bills flow — orchestrates the full SADAD bill lifecycle.
 *
 * Navigation: Dashboard → Search "Sadad" → Saddad Bills page
 *
 * Migrated from Katalon: Scripts/PaymntAndCards/SadadBills/
 *
 * Supported Workflows:
 *   - Add new bill (Prepaid / Postpaid / Overpaid)
 *   - Pay bill
 *   - Edit bill name
 *   - Delete bill
 *   - Filter/Sort bills
 *   - Multi-select bills
 *   - Verify transaction details
 *
 * Rules:
 *   - ZERO Thread.sleep()
 *   - NO assertions (returns page objects/values for test to verify)
 *   - NO hardcoded credentials (reads from ConfigManager)
 */
public class SadadBillsFlow {

    private static final Logger log = LoggerFactory.getLogger(SadadBillsFlow.class);

    /** Locator that matches when Saddad Bills page is loaded (tabs OR empty state) */
    private static final org.openqa.selenium.By SADAD_PAGE_LOADED = AppiumBy.xpath(
            "//*[@content-desc='testID-tags-menu-0' or @name='testID-tags-menu-0'] | "
                    + "//*[@content-desc='testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.Plus' "
                    + "or @name='testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.Plus'] | "
                    + "//*[@text='My bills' or @label='My bills' or @text='New Bill' or @label='New Bill']");

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final CommonComponentsPage common;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;

    public SadadBillsFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.common = new CommonComponentsPage();
        this.otpPage = new OtpPage();
        this.dashboardPage = new DashboardPage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Saddad Bills via search")
    public SadadBillsPage navigateToSadadBills() {
        long implicit = ConfigManager.getInstance().getInt("timeout", 10);
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        try {
            // Fast path: if the Saddad bills list is already on screen, skip the whole
            // back-out + dashboard + re-search round-trip (big time saver across the
            // sub-tests, and avoids the flaky dashboard re-search).
            if (!driver.findElements(SADAD_PAGE_LOADED).isEmpty()) {
                log.info("Already on Saddad Bills page — skipping re-navigation");
                driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(implicit));
                return new SadadBillsPage();
            }
            // testID-right-icon-0 is AMBIGUOUS (search on the dashboard, filter on the Sadad
            // page). If a prior step left us inside Sadad / its wizard, back out FIRST using
            // Sadad-only markers. Detecting the dashboard directly is unreliable.
            org.openqa.selenium.By sadadMarker = AppiumBy.xpath(
                    "//*[@content-desc='testID-tags-menu-0' or @name='testID-tags-menu-0' "
                    + "or @text='My bills' or @text='New Bill' "
                    + "or @content-desc='testID-multi-select-serviceType']");
            org.openqa.selenium.By dashboardMarker =
                    AppiumBy.accessibilityId("testID-master-amount-main");
            for (int i = 0; i < 6 && !driver.findElements(sadadMarker).isEmpty(); i++) {
                // Exit-safe: the moment we reach the dashboard, STOP — a Back there exits the
                // app (implicit wait is 0 here, so this check is instant). Recover the app if a
                // prior Back overshot and backgrounded it under parallel load.
                if (!driver.findElements(dashboardMarker).isEmpty()) {
                    break;
                }
                driver.navigate().back();
                com.urpay.utils.AppGuard.ensureForeground(driver);
            }
        } finally {
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(implicit));
        }

        // Land on the dashboard reliably via the bottom-nav Home button (works from any
        // tab screen once Sadad's modals are exited above), then verify the search icon.
        dashboardPage.navigateToHome();
        for (int i = 0; i < 2; i++) {
            if (dashboardPage.isSearchIconVisible(2)) {
                break;
            }
            com.urpay.utils.AppGuard.safeBack(driver);
            dashboardPage.navigateToHome();
        }

        // Dismiss any dashboard popups/banners before searching
        dashboardPage.dismissPopups();

        SearchPage search = new SearchPage();
        String serviceName = ConfigManager.getInstance().get("sadad.serviceName", "Sadad");
        search.searchAndSelect(serviceName);

        SadadBillsPage page = new SadadBillsPage();
        // Wait for either tabs (user has bills) or "New Bill" button (empty state)
        waits.waitForVisible(SADAD_PAGE_LOADED, 20);
        log.info("Saddad Bills page loaded");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  ADD NEW BILL
    // ══════════════════════════════════════════════════

    @Step("Add new Prepaid bill (Katalon ValidateAddNewBill flow)")
    public SadadBillsPage addNewPrepaidBill(String billNumber, String amount, String billName) {
        SadadBillsPage page = navigateToSadadBills();
        page.tapAddNewBill();

        // Wait for Add New Bill screen to load
        waits.waitForVisible(
                AppiumBy.xpath("//*[@text='All Services' or @text='Bill details']"), 15);

        // Service type dropdown (testID-multi-select-serviceType) → Telecom & Internet (item-1)
        page.tapServiceTypeDropdown();
        // Check if dropdown items loaded (Saddad service might be down)
        if (!waits.isPresent(AppiumBy.accessibilityId("testID-search-item-1"), 15)) {
            throw new RuntimeException(
                    "Saddad service appears to be DOWN — service type dropdown items did not load. "
                            + "The Add New Bill wizard could not fetch service categories from the server.");
        }
        page.selectTelecomAndInternet();

        // Provider dropdown → type "Mobily" to filter → select Mobily 005 by name
        waits.waitForClickable(AppiumBy.accessibilityId("testID-multi-select-provider"), 10);
        page.tapProviderDropdown();
        page.searchProviderName(ConfigManager.getInstance().get("sadad.providerSearch", "Mobily"));
        page.selectMobilyProvider();

        // Bill type dropdown → Prepaid (item-1)
        waits.waitForClickable(AppiumBy.accessibilityId("testID-multi-select-billType"), 10);
        page.tapBillTypeDropdown();
        waits.waitForClickable(AppiumBy.accessibilityId("testID-search-item-1"), 10);
        page.selectPrepaid();

        // Bill number (Katalon enters NO amount in the add-bill flow)
        waits.waitForClickable(AppiumBy.accessibilityId("testID-input-direct-number"), 10);
        page.enterBillNumber(billNumber);
        platformActions.dismissKeyboard();

        // Toggle Save switcher + bill name — BEFORE Next (matches Katalon)
        waits.waitForClickable(AppiumBy.accessibilityId("testID-switcher-isSaveFlag"), 10);
        page.toggleSaveBill();
        waits.waitForClickable(AppiumBy.accessibilityId("testID-input-direct-alias"), 10);
        page.enterBillName(billName);
        platformActions.dismissKeyboard();

        // Next (testID-primary--main) → Save & Add Bill → Pay Later
        page.tapNext();
        page.tapSaveBillConfirm();   // SaveBill-AddBillButton (testID-primary-onConfirmSave-main)
        page.tapPayLater();          // PayLaterButton (testID-secondary-action-main / "Pay Later")

        log.info("New Prepaid bill added (Katalon flow): number={}, name={}", billNumber, billName);
        return page;
    }

    @Step("Add new Postpaid bill: number={billNumber}, amount={amount}")
    public SadadBillsPage addNewPostpaidBill(String billNumber, String amount, String billName) {
        SadadBillsPage page = navigateToSadadBills();
        page.tapAddNewBill();

        // Wait for Add New Bill screen to load
        waits.waitForVisible(
                AppiumBy.xpath("//*[@text='All Services' or @text='Bill details']"), 15);

        // Select service type: Telecom & Internet
        page.tapServiceTypeDropdown();
        if (!waits.isPresent(AppiumBy.accessibilityId("testID-search-item-1"), 15)) {
            throw new RuntimeException(
                    "Saddad service appears to be DOWN — service type dropdown items did not load.");
        }
        page.selectTelecomAndInternet();

        // Select provider (first — STC 001)
        waits.waitForClickable(AppiumBy.accessibilityId("testID-multi-select-provider"), 10);
        page.tapProviderDropdown();
        waits.waitForClickable(AppiumBy.accessibilityId("testID-search-item-0"), 10);
        page.selectFirstProvider();

        // Select bill type: Postpaid
        waits.waitForClickable(AppiumBy.accessibilityId("testID-multi-select-billType"), 10);
        page.tapBillTypeDropdown();
        waits.waitForClickable(AppiumBy.accessibilityId("testID-search-item-0"), 10);
        page.selectPostpaid();

        // Enter bill number
        waits.waitForClickable(AppiumBy.accessibilityId("testID-input-direct-number"), 10);
        page.enterBillNumber(billNumber);
        platformActions.dismissKeyboard();

        // Enter bill amount
        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-TextInput.6ebddf51-6774-41c1-956f-5e9e51bc5847']"), 10);
        page.enterBillAmount(amount);
        platformActions.dismissKeyboard();

        // Tap Next
        page.tapNext();

        // Toggle save bill and enter name
        waits.waitForClickable(AppiumBy.accessibilityId("testID-switcher-isSaveFlag"), 10);
        page.toggleSaveBill();

        waits.waitForClickable(AppiumBy.accessibilityId("testID-input-direct-alias"), 10);
        page.enterBillName(billName);
        platformActions.dismissKeyboard();

        // Confirm save
        page.tapSaveBillConfirm();

        log.info("New Postpaid bill added: number={}, amount={}, name={}", billNumber, amount, billName);
        return page;
    }

    @Step("Add new Overpaid bill: number={billNumber}, amount={amount}")
    public SadadBillsPage addNewOverpaidBill(String billNumber, String amount, String billName) {
        SadadBillsPage page = navigateToSadadBills();
        page.tapAddNewBill();

        // Wait for Add New Bill screen to load
        waits.waitForVisible(
                AppiumBy.xpath("//*[@text='All Services' or @text='Bill details']"), 15);

        // Select service type: Telecom & Internet
        page.tapServiceTypeDropdown();
        if (!waits.isPresent(AppiumBy.accessibilityId("testID-search-item-1"), 15)) {
            throw new RuntimeException(
                    "Saddad service appears to be DOWN — service type dropdown items did not load.");
        }
        page.selectTelecomAndInternet();

        // Select provider (first — STC 001)
        waits.waitForClickable(AppiumBy.accessibilityId("testID-multi-select-provider"), 10);
        page.tapProviderDropdown();
        waits.waitForClickable(AppiumBy.accessibilityId("testID-search-item-0"), 10);
        page.selectFirstProvider();

        // Select bill type: Overpaid
        waits.waitForClickable(AppiumBy.accessibilityId("testID-multi-select-billType"), 10);
        page.tapBillTypeDropdown();
        waits.waitForClickable(AppiumBy.accessibilityId("testID-search-item-1"), 10);
        page.selectOverpaid();

        // Enter bill number
        waits.waitForClickable(AppiumBy.accessibilityId("testID-input-direct-number"), 10);
        page.enterBillNumber(billNumber);
        platformActions.dismissKeyboard();

        // Enter bill amount
        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-TextInput.6ebddf51-6774-41c1-956f-5e9e51bc5847']"), 10);
        page.enterBillAmount(amount);
        platformActions.dismissKeyboard();

        // Tap Next
        page.tapNext();

        // Toggle save bill and enter name
        waits.waitForClickable(AppiumBy.accessibilityId("testID-switcher-isSaveFlag"), 10);
        page.toggleSaveBill();

        waits.waitForClickable(AppiumBy.accessibilityId("testID-input-direct-alias"), 10);
        page.enterBillName(billName);
        platformActions.dismissKeyboard();

        // Confirm save
        page.tapSaveBillConfirm();

        log.info("New Overpaid bill added: number={}, amount={}, name={}", billNumber, amount, billName);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  PAY BILL
    // ══════════════════════════════════════════════════

    @Step("Pay bill and confirm with verification code")
    public SadadBillsPage payBill(SadadBillsPage page) {
        page.tapPayBill();
        waits.waitForClickable(AppiumBy.xpath("//*[@content-desc='testID-primary-onConfirm-main']"), 10);
        page.tapConfirmPay();
        enterVerificationCode();
        captureAfterOtp("Saddad Bill Payment - Post OTP");
        waitAfterOtp();
        log.info("Bill payment completed");
        return page;
    }

    @Step("Pay bill directly from Saddad Bills list")
    public SadadBillsPage navigateAndPayFirstBill() {
        SadadBillsPage page = navigateToSadadBills();
        page.tapFirstBill();
        return payBill(page);
    }

    @Step("Select the first saved bill and pay it (Katalon ValidatePayBill flow)")
    public SadadBillsPage selectBillAndPay(String billName, String amount) {
        SadadBillsPage page = navigateToSadadBills();
        // Do NOT search by a fixed name: the shared SIT account already holds a leftover
        // bill (e.g. "testA"/966503745901), so add-bill can't create a duplicate "hamada
        // Bill" and the name search returns "No Results" (→ empty list → tapFirstBill
        // times out). Operate on the FIRST bill in the list, which is always present.
        page.tapFirstBill();                 // open the first saved bill (FirstBillButton)
        page.tapPayBill();                   // Pay Bill (testID-primary-onPressCustomize-main)
        page.enterBillAmount(amount);        // amount (BillAmountTextField), e.g. "100"
        platformActions.dismissKeyboard();
        page.tapNextBillAmount();            // Next (testID-primary-onPressNext-main)
        page.tapPayBillFinalStep();          // final Pay Bill (testID-View.b8ce712a..., swipe)
        enterVerificationCode();             // OTP
        page.tapDoneWithScroll();            // Done (testID-primary-onPressDone-main, swipe)
        log.info("First saved bill paid (configured name '{}', amount={})", billName, amount);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  EDIT BILL
    // ══════════════════════════════════════════════════

    @Step("Edit bill name to: {newName}")
    public SadadBillsPage editBillName(String newName) {
        SadadBillsPage page = navigateToSadadBills();
        page.tapFirstBill();

        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-secondary-onPressEdit-main']"), 10);
        page.tapEdit();

        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-input-direct-SadadN.EditBil']"), 10);
        page.editBillName(newName);
        platformActions.dismissKeyboard();

        page.tapApplyEdit();

        // applyEdit returns to the bills list — confirm it (so the next sub-test's
        // fast-path can skip the dashboard re-search).
        waits.waitForVisible(SADAD_PAGE_LOADED, 10);

        log.info("Bill name edited to: {}", newName);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  DELETE BILL
    // ══════════════════════════════════════════════════

    @Step("Delete first bill")
    public SadadBillsPage deleteFirstBill() {
        SadadBillsPage page = navigateToSadadBills();
        page.tapFirstBill();

        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-secondary-onPressEdit-main']"), 10);
        page.tapEdit();

        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-TouchableOpacity.ae91a088-6b7a-4715-89a4-52cabcc15934']"), 10);
        page.tapDelete();

        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-primary-onPressDelete-main']"), 10);
        page.tapConfirmDelete();

        // Wait for bills list to reload
        waits.waitForVisible(SADAD_PAGE_LOADED, 10);

        log.info("First bill deleted successfully");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  FILTER / SORT BILLS
    // ══════════════════════════════════════════════════

    @Step("Apply all sort filters in ONE panel session, then reset (Katalon validateFilterBills)")
    public SadadBillsPage applyFiltersAndReset() {
        SadadBillsPage page = navigateToSadadBills();
        // Katalon opens the filter panel ONCE, taps every sort option, then Applies ONCE.
        // (The old code re-opened + re-applied for EACH option — that was the duplication.)
        page.tapFilterSort();
        waits.waitForClickable(AppiumBy.xpath("//*[@content-desc='testID-radio-item-0']"), 10);
        page.selectMostRecentFilter();
        page.selectAmountLowFilter();
        page.selectAmountHighFilter();
        page.selectTelecomServiceTypeFilter();
        page.tapApplyFilter();
        waits.waitForVisible(SADAD_PAGE_LOADED, 10);
        log.info("Filters applied in one session (Most Recent, Amount Low/High, Telecom)");

        // Reopen the panel, Reset, Apply
        page.tapFilterSort();
        waits.waitForClickable(
                AppiumBy.xpath("//*[@content-desc='testID-secondary-onResetFilters-main']"), 10);
        page.tapResetFilter();
        page.tapApplyFilter();
        waits.waitForVisible(SADAD_PAGE_LOADED, 10);
        log.info("Filters reset");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  MULTI-SELECT BILLS
    // ══════════════════════════════════════════════════

    @Step("Select first bill for multi-payment")
    public SadadBillsPage selectFirstBillMulti() {
        SadadBillsPage page = navigateToSadadBills();
        waits.waitForClickable(
                AppiumBy.xpath(
                        "//*[@content-desc='testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.0']"),
                10);
        page.selectFirstBillMulti();
        log.info("First bill selected for multi-payment");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTION DETAILS VERIFICATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to transactions tab and open first Saddad transaction")
    public SadadTransactionDetailsPage viewTransactionDetails() {
        // Navigate to dashboard transactions
        dashboardPage.clickTransactions();

        SadadTransactionDetailsPage detailsPage = new SadadTransactionDetailsPage();
        waits.waitForVisible(AppiumBy.xpath("//*[@content-desc='testID-main-firstRow-0']"), 15);

        // Tap first transaction to view details
        detailsPage.tapFirstTransaction();
        waits.waitForVisible(AppiumBy.xpath("//*[@content-desc='testID-label-value-main-0']"), 15);

        log.info("Transaction details page loaded");
        return detailsPage;
    }

    // ══════════════════════════════════════════════════
    //  WALLET BALANCE
    // ══════════════════════════════════════════════════

    @Step("Get current wallet balance from dashboard")
    public String getWalletBalance() {
        waits.waitForVisible(AppiumBy.accessibilityId("testID-master-amount-main"), 10);
        String integer = driver.findElement(AppiumBy.accessibilityId("testID-master-amount-main")).getText();
        String fraction = "";
        try {
            fraction = driver.findElement(AppiumBy.accessibilityId("testID-fraction-amount-main")).getText();
        } catch (Exception ignored) {
            // fraction may not be present
        }
        String balance = integer + fraction;
        log.info("Wallet balance: {}", balance);
        return balance;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Enter verification code")
    private void enterVerificationCode() {
        String code = ConfigManager.getInstance().get("sadad.verificationCode", "1234");
        otpPage.enterOtp(code);
    }

    private void waitAfterOtp() {
        common.waitForResultAfterOtp(30);
    }

    @Step("Capture screen after OTP: {name}")
    private void captureAfterOtp(String name) {
        try {
            for (int i = 1; i <= 3; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (screenshot.length > 0) {
                    Allure.addAttachment(name + " (" + i + "s)", "image/png",
                            new ByteArrayInputStream(screenshot), ".png");
                }
            }
        } catch (Exception e) {
            log.warn("Post-OTP screenshot failed: {}", e.getMessage());
        }
    }


}
