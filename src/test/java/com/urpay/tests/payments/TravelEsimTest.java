package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.TravelEsimFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.TravelEsimPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Travel E-SIM Test Suite — mirrors the Katalon flow exactly:
 *   1. Login
 *   2. Search "Travel E-Sim" on dashboard → tap first result
 *   3. Tap "New E-Sim" on My Orders page
 *   4. Select "Global" tab
 *   5. Select first global option
 *   6. Tap "Next"
 *   7. Verify confirmation page: Country/Region, Package Name, Package Validity not empty
 *
 * Katalon source: Scripts/PaymntAndCards/Travel Esim/
 * Suite: TravelEsim_Cases.ts (Setup → Login → Purchase verification)
 *
 * Test Data (from sit-cards.properties, travelEsim.* prefix):
 *   User: 0533411565 / 1096436819
 */
@Epic("Payments & Cards")
@Feature("Travel E-SIM")
public class TravelEsimTest extends BaseTest {

    @Test(groups = {"payments", "travel-esim", "smoke"}, priority = 1)
    @Story("Travel E-SIM Login")
    @Description("Login with Travel E-SIM user and verify dashboard is loaded")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginForTravelEsim() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("travelEsim.mobileNumber"),
                c.get("travelEsim.id"),
                c.get("travelEsim.verificationCode", "1234"),
                c.get("travelEsim.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
    }

    @Test(groups = {"payments", "travel-esim", "smoke"}, priority = 2,
            dependsOnMethods = "testLoginForTravelEsim")
    @Story("Global E-SIM Purchase")
    @Description("Search 'Travel E-Sim' → New E-Sim → Global → first plan → Next → "
            + "verify confirmation page shows Country/Region, Package Name, Package Validity")
    @Severity(SeverityLevel.CRITICAL)
    public void testPurchaseNewGlobalEsim() {
        // Step 1: Search "Travel E-Sim" and navigate
        TravelEsimFlow flow = new TravelEsimFlow();
        TravelEsimPage page = flow.navigateToTravelEsim();

        // Step 2: If "My Orders" page → tap "New E-Sim", otherwise already on selection
        if (page.isMyOrdersPageLoaded()) {
            log.info("My Orders page detected — tapping New E-Sim");
            page.tapNewEsim();
        } else {
            log.info("Already on plan selection page (fresh user)");
        }

        // Step 3: Select "Global" → first package (7 Days) → "Next"
        page.selectGlobalTab();
        page.selectFirstPackage();
        page.tapNext();

        // Step 4: Verify confirmation page data (migrated from Katalon assertions)
        Assert.assertTrue(page.isConfirmationPageLoaded(),
                "Confirmation page should be loaded");

        String countryRegion = page.getCountryRegionValue();
        Assert.assertNotNull(countryRegion, "Country/Region value should not be null");
        Assert.assertFalse(countryRegion.trim().isEmpty(),
                "Country/Region value should not be empty");

        String packageName = page.getPackageName();
        Assert.assertNotNull(packageName, "Package Name should not be null");
        Assert.assertFalse(packageName.trim().isEmpty(),
                "Package Name should not be empty");

        String packageValidity = page.getPackageValidity();
        Assert.assertNotNull(packageValidity, "Package Validity should not be null");
        Assert.assertFalse(packageValidity.trim().isEmpty(),
                "Package Validity should not be empty");

        log.info("Country/Region: {}", countryRegion);
        log.info("Package Name: {}", packageName);
        log.info("Package Validity: {}", packageValidity);
    }

    @Test(groups = {"payments", "travel-esim"}, priority = 3,
            dependsOnMethods = "testPurchaseNewGlobalEsim")
    @Story("Global E-SIM Purchase")
    @Description("Confirm E-SIM purchase from confirmation page and complete OTP verification")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfirmEsimPurchase() {
        // Step 1: Tap Confirm on the confirmation page (session continues from previous test)
        TravelEsimFlow flow = new TravelEsimFlow();
        TravelEsimPage page = new TravelEsimPage();
        flow.confirmPurchase(page);

        captureScreenshot("After E-SIM Purchase Confirmation");
        log.info("E-SIM purchase confirmation completed");
    }
}
