package com.urpay.tests.dmp;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.DmpOrderConfirmationPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Re-Order.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Re-Order/
 *     To Validate Reordering the order agian from view history
 *   → setupTestData → login → NavigateToDMP → My Orders → View History →
 *     select order → Reorder All → checkout → place order → OTP → Order Placed.
 *
 * Requires the shared DMP user to have at least one previous order in history.
 */
@Epic("DMP")
@Feature("Re-Order")
public class ReorderFromHistoryTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "regression"})
    @Description("Re-order the most recent order from order history and verify the order is placed")
    @Severity(SeverityLevel.CRITICAL)
    public void testReorderFromHistory() {
        new LoginFlow().loginWith(
                config.get("dmp.mobileNumber"),
                config.get("dmp.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpOrderConfirmationPage confirmation =
                new DmpFlow().reorderFromHistory(config.get("dmp.verificationCode", "1234"));

        Assert.assertTrue(confirmation.isOrderPlaced(),
                "The re-order should be placed successfully (Order Placed! screen shown)");
    }
}
