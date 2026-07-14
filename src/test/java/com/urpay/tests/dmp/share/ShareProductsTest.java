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
 *   login → NavigateToDMP → open a product's details → tap Share → verify the share sheet.
 *
 * The Katalon deep link {@code urpay://MarketPlace/ProductDetails?sku=<sku>} crashes the SIT app
 * on some product details screens ({@code IllegalStateException: HorizontalScrollView can host
 * only one direct child}), so the test opens a normal Store product from the catalogue instead —
 * the Share behaviour under test is identical.
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

        DmpShareProductPage share = new DmpShareFlow().shareFirstStoreProduct();

        Assert.assertTrue(share.isShareSheetDisplayed(),
                "The share sheet should be displayed after tapping Share");
    }
}
