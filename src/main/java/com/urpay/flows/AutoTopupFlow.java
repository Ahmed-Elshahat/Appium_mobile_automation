package com.urpay.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.DriverFactory;
import com.urpay.pages.wallet.AutoTopupPage;
import com.urpay.pages.wallet.FamilyWalletPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Auto Top-up flow — orchestrates kid auto top-up plan creation.
 *
 * Journey: Dashboard → Family Wallet → Kid Profile → Settings → Auto Top-up
 *          → create Daily / Weekly / Monthly plan → verification code → done.
 *
 * Katalon source: Test Suites/WMVSuites/AutoTopupPlan-CreationFlow
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopup-NavigateToAutoTopupScreen
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopUp-ToValidatethatDailyAutoTopupCreatedSuccessfully
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopUp-ToValidatethatWeeklyAutoTopupCreatedSuccessfully
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopUp-ToValidatethatMonthlyAutoTopupCreatedSuccessfully
 *   Scripts/Wallet_VAS/FamilyWallet/Auto-Topup/AutoTopUp-ToValidateUserAbleToDeleteAutoTopupPlan
 */
public class AutoTopupFlow {

    private static final Logger log = LoggerFactory.getLogger(AutoTopupFlow.class);

    private final MobilePlatformActions platformActions;

    public AutoTopupFlow() {
        AppiumDriver driver = DriverFactory.getInstance().getDriver();
        this.platformActions = PlatformActionsFactory.create(driver);
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Auto Top-up: Family Wallet → Kid Profile → Settings → Auto Top-up")
    public AutoTopupPage navigateToAutoTopupScreen() {
        FamilyWalletPage familyWallet = new FamilyWalletPage();
        familyWallet.tapFamilyWallet();
        familyWallet.waitUntilLoaded();
        return reachAutoTopupScreen();
    }

    @Step("Re-open Auto Top-up")
    public AutoTopupPage reopenAutoTopupScreen() {
        return reachAutoTopupScreen();
    }

    /**
     * Reach the Auto Top-up screen from wherever we are. After creating a plan the app
     * returns to the Family Wallets list (kid card visible); after deleting a plan it stays
     * on the kid profile (settings gear visible). This handles both: open the kid profile
     * first only when the family list is showing, then Settings → Auto Top-up.
     */
    private AutoTopupPage reachAutoTopupScreen() {
        AutoTopupPage page = new AutoTopupPage();
        if (page.isAutoTopupScreenShown()) {
            return page;
        }
        if (page.isFamilyListShown()) {
            new FamilyWalletPage().tapFirstFamilyMember();
            page = new AutoTopupPage();
            log.info("Kid profile opened");
        }
        page.tapSettings();
        page.tapAutoTopup();
        log.info("Auto Top-up screen opened");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  PLAN CREATION
    // ══════════════════════════════════════════════════

    @Step("Create Daily auto top-up plan: amount={amount}")
    public AutoTopupPage createDailyPlan(AutoTopupPage page, String amount, String verificationCode) {
        page.enterAmount(amount);
        page.tapNext();

        page.tapFrequencyList();
        page.tapFirstOption();        // Daily

        page.tapNext();
        confirmPlan(page, verificationCode);
        log.info("Daily auto top-up plan created");
        return page;
    }

    @Step("Create Weekly auto top-up plan: amount={amount}")
    public AutoTopupPage createWeeklyPlan(AutoTopupPage page, String amount, String verificationCode) {
        page.enterAmount(amount);
        page.tapNext();

        page.tapFrequencyList();
        page.tapSecondOption();       // Weekly

        page.tapDayList();
        page.tapFirstOption();        // recurring day

        page.tapTimeList();
        page.tapThirdOption();        // time slot

        page.tapNext();
        confirmPlan(page, verificationCode);
        log.info("Weekly auto top-up plan created");
        return page;
    }

    @Step("Create Monthly auto top-up plan: amount={amount}")
    public AutoTopupPage createMonthlyPlan(AutoTopupPage page, String amount, String verificationCode) {
        page.enterAmount(amount);
        page.tapNext();

        page.tapFrequencyList();
        page.tapThirdOption();        // Monthly

        page.tapDateList();
        page.tapSecondOption();       // recurring date

        page.tapTimeList();
        page.tapThirdOption();        // time slot

        page.tapNext();
        confirmPlan(page, verificationCode);
        log.info("Monthly auto top-up plan created");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════

    @Step("Delete the existing auto top-up plan")
    public AutoTopupPage deletePlan(AutoTopupPage page) {
        page.tapDeleteTopup();
        page.tapConfirmDeleteTopup();
        log.info("Auto top-up plan deleted");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  EDIT PLAN
    // ══════════════════════════════════════════════════

    /**
     * Edit the plan frequency to Weekly.
     * Mirrors AutoTopUp-ToValidateUserAbleToEditPlanFrequent.
     *
     * @return the expected frequency label captured from the dropdown, for the test to assert
     */
    @Step("Edit plan frequency")
    public String editFrequency() {
        AutoTopupPage page = returnToAutoTopupPlan();

        page.tapEditFrequency();
        page.tapFrequencyList();
        String expected = page.getSecondOptionText();
        page.tapSecondOption();      // Weekly

        page.tapDayList();
        page.tapThirdOption();

        page.tapNext();
        page.tapConfirm();
        page.tapDone();

        returnToAutoTopupPlan();
        log.info("Plan frequency edited to: {}", expected);
        return expected;
    }

    /**
     * Edit the plan amount (re-confirms the wizard without changing the value).
     * Mirrors AutoTopUp-ToValidateUserAbleToEditthePlanAmount.
     */
    @Step("Edit plan amount")
    public void editAmount() {
        AutoTopupPage page = new AutoTopupPage();
        page.tapEditAmount();
        page.tapNext();
        page.tapConfirm();
        page.tapDone();
        log.info("Plan amount edit confirmed");
    }

    /**
     * Edit the plan recurring day.
     * Mirrors AutoTopUp-ToValidateUserAbleToEditPlanDay.
     *
     * @return the expected day label captured from the dropdown, for the test to assert
     */
    @Step("Edit plan day")
    public String editDay() {
        AutoTopupPage page = returnToAutoTopupPlan();

        page.tapEditDay();
        page.tapDayList();
        String expected = page.getThirdOptionText();
        page.tapThirdOption();

        page.tapNext();
        page.tapConfirm();
        page.tapDone();

        returnToAutoTopupPlan();
        log.info("Plan day edited to: {}", expected);
        return expected;
    }

    /**
     * Edit the plan time.
     * Mirrors AutoTopUp-ToValidateUserAbleToEditthePlanTime.
     *
     * @return the expected time label captured from the dropdown, for the test to assert
     */
    @Step("Edit plan time")
    public String editTime() {
        AutoTopupPage page = new AutoTopupPage();

        page.tapEditTime();
        page.tapTimeList();
        String expected = page.getSecondOptionText();
        page.tapSecondOption();

        page.tapNext();
        page.tapConfirm();
        page.tapDone();

        returnToAutoTopupPlan();
        log.info("Plan time edited to: {}", expected);
        return expected;
    }

    // ══════════════════════════════════════════════════
    //  ENABLE / DISABLE PLAN
    // ══════════════════════════════════════════════════

    @Step("Disable the auto top-up plan")
    public AutoTopupPage disablePlan() {
        AutoTopupPage page = new AutoTopupPage();
        page.tapStatusToggle();
        page.tapDisable();
        log.info("Auto top-up plan disabled");
        return page;
    }

    @Step("Enable the auto top-up plan")
    public AutoTopupPage enablePlan() {
        AutoTopupPage page = new AutoTopupPage();
        page.tapStatusToggle();
        log.info("Auto top-up plan enabled");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Return to auto top-up plan: Kid Profile → Settings → Auto Top-up")
    private AutoTopupPage returnToAutoTopupPlan() {
        return reachAutoTopupScreen();
    }

    @Step("Confirm plan and enter verification code")
    private void confirmPlan(AutoTopupPage page, String verificationCode) {
        page.tapConfirm();
        platformActions.enterDigits(verificationCode);
        page.tapDone();
    }
}