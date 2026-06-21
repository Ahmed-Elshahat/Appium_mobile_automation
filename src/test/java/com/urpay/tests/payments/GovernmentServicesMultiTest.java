package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.GovernmentServicesFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.GovernmentServicesPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Multiple Traffic Violations Payment — Gov Services (Sadad MOI).
 *
 * Login → Gov Services → query → select 2 violations → pay → OTP → Done
 *
 * Run independently on its own device for parallel execution.
 */
@Epic("Payments & Cards")
@Feature("Government Services")
public class GovernmentServicesMultiTest extends BaseTest {

    @Test(groups = {"payments", "gov-services"})
    @Story("Multiple Traffic Violations Payment")
    @Description("Login → Gov Services → query → select 2 violations → pay → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testMultipleTrafficViolationsPayment() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("gov.multi.mobileNumber"),
                c.get("gov.multi.id"),
                c.get("gov.multi.verificationCode", "1234"),
                c.get("gov.multi.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        GovernmentServicesFlow flow = new GovernmentServicesFlow();
        GovernmentServicesPage page = flow.payMultipleViolations(
                c.get("gov.multi.violatorId", "1131236050"));

        Assert.assertTrue(page.isDoneButtonVisible(30),
                "Done button should be visible after multi-violation payment");
    }
}
