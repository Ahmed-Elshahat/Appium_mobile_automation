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
import com.urpay.pages.payments.TravelEsimPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

/**
 * Travel E-SIM flow — orchestrates the full eSIM purchase journey.
 *
 * Navigation: Dashboard → Search "Travel E-Sim" → My Orders → New E-Sim
 *             → Select Country (Global) → Select Plan → Confirmation
 *
 * Migrated from Katalon: Scripts/PaymntAndCards/Travel Esim/
 *
 * Rules:
 *   - ZERO Thread.sleep()
 *   - NO assertions (returns page objects/values for test to verify)
 *   - NO hardcoded credentials (reads from ConfigManager)
 */
public class TravelEsimFlow {

    private static final Logger log = LoggerFactory.getLogger(TravelEsimFlow.class);
    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final CommonComponentsPage common;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;

    public TravelEsimFlow() {
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

    @Step("Navigate to Travel E-Sim via search")
    public TravelEsimPage navigateToTravelEsim() {
        // Ensure we're at a screen with search icon visible
        for (int i = 0; i < 3; i++) {
            if (dashboardPage.isSearchIconVisible(2)) {
                break;
            }
            driver.navigate().back();
        }

        SearchPage search = new SearchPage();
        String serviceName = ConfigManager.getInstance().get("travelEsim.serviceName", "Travel E-Sim");
        search.searchAndSelect(serviceName);

        TravelEsimPage page = new TravelEsimPage();
        // Handle two scenarios:
        // 1. User has existing orders → "My Orders" page with "New E-Sim" button
        // 2. Fresh user → directly shows plan selection page (Global/Local/Regional)
        waits.waitForVisible(
                AppiumBy.xpath("//*[@text='New E-Sim' or @text='Global' or @text='Local' or @text='Regional']"), 15);

        if (waits.isPresent(AppiumBy.xpath("//*[@class=\"android.view.ViewGroup\" and ./*[@text=\"New E-Sim\"]]"), 2)) {
            log.info("Travel E-Sim My Orders page loaded (user has existing orders)");
        } else {
            log.info("Travel E-Sim plan selection page loaded directly (fresh user)");
        }
        return page;
    }

    // ══════════════════════════════════════════════════
    //  PURCHASE GLOBAL E-SIM
    // ══════════════════════════════════════════════════

    @Step("Purchase new Global E-SIM (first available plan)")
    public TravelEsimPage purchaseGlobalEsim() {
        TravelEsimPage page = navigateToTravelEsim();
        page.tapNewEsim();
        page.selectGlobalTab();
        page.selectFirstPackage();
        page.tapNext();

        // Wait for confirmation page to load
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
        log.info("Global E-SIM confirmation page loaded");
        return page;
    }

    @Step("Confirm E-SIM purchase and complete payment")
    public TravelEsimPage confirmPurchase(TravelEsimPage page) {
        page.tapConfirm();
        enterVerificationCode();
        captureAfterOtp("Travel E-SIM Purchase - Post OTP");
        waitAfterOtp();
        log.info("E-SIM purchase confirmed");
        return page;
    }

    @Step("Full Global E-SIM purchase flow (navigate → select → confirm)")
    public TravelEsimPage purchaseAndConfirmGlobalEsim() {
        TravelEsimPage page = purchaseGlobalEsim();
        return confirmPurchase(page);
    }

    // ══════════════════════════════════════════════════
    //  PURCHASE LOCAL E-SIM
    // ══════════════════════════════════════════════════

    @Step("Purchase new Local E-SIM (first available plan)")
    public TravelEsimPage purchaseLocalEsim() {
        TravelEsimPage page = navigateToTravelEsim();
        page.tapNewEsim();
        page.selectLocalTab();
        page.selectFirstPackage();
        page.tapNext();

        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
        log.info("Local E-SIM confirmation page loaded");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  PURCHASE REGIONAL E-SIM
    // ══════════════════════════════════════════════════

    @Step("Purchase new Regional E-SIM (first available plan)")
    public TravelEsimPage purchaseRegionalEsim() {
        TravelEsimPage page = navigateToTravelEsim();
        page.tapNewEsim();
        page.selectRegionalTab();
        page.selectFirstPackage();
        page.tapNext();

        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
        log.info("Regional E-SIM confirmation page loaded");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Enter verification code")
    private void enterVerificationCode() {
        String code = ConfigManager.getInstance().get("urpayUser.verificationCode", "1234");
        otpPage.enterOtp(code);
    }

    /** Wait for result screen after OTP — success or error */
    private void waitAfterOtp() {
        common.waitForResultAfterOtp(30);
    }

    @Step("Capture screen after OTP: {name}")
    private void captureAfterOtp(String name) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            if (screenshot.length > 0) {
                Allure.addAttachment(name, "image/png",
                        new ByteArrayInputStream(screenshot), ".png");
            }
            log.info("Post-OTP screenshot captured: {}", name);
        } catch (Exception e) {
            log.warn("Post-OTP screenshot failed: {}", e.getMessage());
        }
    }
}
