package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.UpdateKycFlow;
import com.urpay.model.KycDetails;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.settings.UpdateKycPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Update KYC — login → Profile → Personal Information → change KYC fields to random values →
 * Save → success → verify the saved values persisted.
 *
 * <p>Migrated from Katalon Scripts/Wallet_VAS/UpdateKYC/*.
 */
@Epic("Wallet & VAS")
@Feature("Update KYC")
public class UpdateKycTest extends BaseTest {

    private KycDetails updated;

    @Test(groups = {"settings", "kyc"}, priority = 1)
    @Story("Open Personal Information")
    @Description("Login and open Profile → Personal Information (Update KYC)")
    @Severity(SeverityLevel.CRITICAL)
    public void testOpenPersonalInformation() {
        Assert.assertTrue(login().isLoaded(), "Dashboard should be visible after login");
        UpdateKycPage page = new UpdateKycFlow().openPersonalInformation();
        Assert.assertTrue(page.isKycFormDisplayed(5), "Personal Information (KYC) form should be displayed");
    }

    @Test(groups = {"settings", "kyc"}, priority = 2, dependsOnMethods = "testOpenPersonalInformation")
    @Story("Update KYC")
    @Description("Change job sector, employer, income source, job category and income range to random values and Save")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdateKycWithRandomValues() {
        updated = new UpdateKycFlow().updateWithRandomValues();
        Assert.assertTrue(new UpdateKycPage().isSuccessDisplayed(40),
                "Success message should be shown after saving KYC " + updated);
    }

    @Test(groups = {"settings", "kyc"}, priority = 3, dependsOnMethods = "testUpdateKycWithRandomValues")
    @Story("Verify KYC persisted")
    @Description("Tap Done, reopen Personal Information and verify the saved values")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdatedKycValuesPersisted() {
        UpdateKycFlow flow = new UpdateKycFlow();
        flow.reopenPersonalInformation();
        Assert.assertEquals(flow.readCurrentValues(), updated, "Saved KYC values should persist");
    }

    @Step("Login with Update KYC test user")
    private DashboardPage login() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("updateKyc.mobileNumber"),
                config.get("updateKyc.id"),
                config.get("updateKyc.verificationCode", "1234"),
                config.get("updateKyc.passCode", "2233"));
    }
}
