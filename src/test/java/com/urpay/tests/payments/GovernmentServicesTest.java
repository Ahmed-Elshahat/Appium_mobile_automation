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
 * Single Traffic Violation Payment — Gov Services (Sadad MOI).
 *
 * Login → Gov Services → query → select 1 violation → pay → OTP → Done
 *
 * Run independently on its own device for parallel execution.
 */
@Epic("Payments & Cards")
@Feature("Government Services")
public class GovernmentServicesTest extends BaseTest {

    @Test(groups = {"payments", "gov-services", "smoke"})
    @Story("Single Traffic Violation Payment")
    @Description("Login → Gov Services → query → select 1 violation → pay → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testSingleTrafficViolationPayment() {
        ConfigManager c = ConfigManager.getInstance();

        String mobile = c.get("gov.mobileNumber");
        topUpBalanceByMobile(mobile);
        DashboardPage dashboard = new LoginFlow().loginWith(
                mobile,
                c.get("gov.id"),
                c.get("gov.verificationCode", "1234"),
                c.get("gov.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        GovernmentServicesFlow flow = new GovernmentServicesFlow();
        GovernmentServicesPage page = flow.paySingleViolation(
                c.get("gov.violatorId", "1131236050"));

        Assert.assertTrue(page.isDoneButtonVisible(30),
                "Done button should be visible after single violation payment");
    }
}
