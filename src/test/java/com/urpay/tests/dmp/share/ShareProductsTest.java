package com.urpay.tests.dmp.share;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpShareFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.share.DmpShareProductPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Share Products (LANE B).
 *
 * Migrated from Katalon (DMP_1 branch) suite ValidatShareingProductsWithOthers:
 *   login → NavigateToDMP → open a specific product's details by deep link → tap Share →
 *   verify the share sheet is displayed.
 */
@Epic("DMP")
@Feature("Share Products")
public class ShareProductsTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-share", "regression"})
    @Description("Sharing a product opens the share sheet")
    @Severity(SeverityLevel.NORMAL)
    public void testShareProductOpensShareSheet() {
        new LoginFlow().loginWith(
                config.get("dmpb.share.mobileNumber"),
                config.get("dmpb.share.id"),
                config.get("dmpb.share.verificationCode", "1234"),
                config.get("dmpb.share.passCode", "2233"));

        DmpShareProductPage share = new DmpShareFlow()
                .shareProductByDeepLink(config.get("dmpb.share.sku", "6280066009007"));

        Assert.assertTrue(share.isShareSheetDisplayed(),
                "The share sheet should be displayed after tapping Share");
    }
}
