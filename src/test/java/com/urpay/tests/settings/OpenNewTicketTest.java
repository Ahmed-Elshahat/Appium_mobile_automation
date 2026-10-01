package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.HelpCenterFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.settings.HelpCenterPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Wallet & VAS")
@Feature("Help Center")
public class OpenNewTicketTest extends BaseTest {

    @Test(groups = {"settings", "help-center", "smoke"})
    @Story("Open New Ticket")
    @Description("Login and open a new Wallet Management support ticket successfully")
    @Severity(SeverityLevel.CRITICAL)
    public void testOpenWalletManagementTicket() {
        ConfigManager config = ConfigManager.getInstance();
        Assert.assertTrue(new LoginFlow().loginWith(
                config.get("helpCenter.mobileNumber"), config.get("helpCenter.id"),
                config.get("helpCenter.verificationCode"), config.get("helpCenter.passCode")).isLoaded());
        HelpCenterPage page = new HelpCenterFlow().openWalletManagementTicket(
                config.get("helpCenter.ticketDescription"));
        Assert.assertTrue(page.isTicketOpenedSuccessfully(30), "Ticket should be opened successfully");
    }
}