package com.urpay.data;

import com.urpay.core.ConfigManager;
import org.testng.annotations.DataProvider;

/**
 * SRP: Centralized TestNG data providers.
 * All test data comes from .properties files via ConfigManager.
 * No hardcoded values — all environment-specific data in config files.
 *
 * Usage in tests:
 *   @Test(dataProvider = "loginData", dataProviderClass = TestDataProvider.class)
 *   public void testLogin(String mobile, String id, String passcode, String otp) { ... }
 */
public class TestDataProvider {

    // ── Login Data ─────────────────────────────────────────────────

    @DataProvider(name = "loginData")
    public static Object[][] loginData() {
        ConfigManager config = ConfigManager.getInstance();
        return new Object[][]{
                {
                        config.get("urpayUser.mobileNumber"),
                        config.get("urpayUser.id"),
                        config.get("urpayUser.passCode", "2233"),
                        config.get("urpayUser.verificationCode", "1234")
                }
        };
    }

    @DataProvider(name = "parentKidLoginData")
    public static Object[][] parentKidLoginData() {
        ConfigManager config = ConfigManager.getInstance();
        return new Object[][]{
                {
                        "parent",
                        config.get("urpayParent.mobileNumber"),
                        config.get("urpayParent.id"),
                        config.get("urpayParent.passCode", "2233"),
                        config.get("urpayParent.verificationCode", "1234")
                },
                {
                        "kid",
                        config.get("urpayKid.mobileNumber"),
                        config.get("urpayKid.id"),
                        config.get("urpayKid.passCode", "2233"),
                        config.get("urpayKid.verificationCode", "1234")
                }
        };
    }

    // ── Remittance Data ────────────────────────────────────────────

    @DataProvider(name = "localTransferData")
    public static Object[][] localTransferData() {
        ConfigManager config = ConfigManager.getInstance();
        return new Object[][]{
                {
                        config.get("urpayUser.mobileNumber"),
                        config.get("urpayUser.id"),
                        config.get("transfer.iban"),
                        config.get("transfer.amount", "60"),
                        config.get("receiver.mobileNumber")
                }
        };
    }

    @DataProvider(name = "internationalTransferProviders")
    public static Object[][] internationalTransferProviders() {
        ConfigManager config = ConfigManager.getInstance();
        return new Object[][]{
                {
                        "MoneyGram",
                        config.get("intl.moneygram.mobileNumber"),
                        config.get("intl.moneygram.id"),
                        config.get("intl.moneygram.amount"),
                        config.get("intl.moneygram.totalAmount")
                },
                {
                        "Tahweel AlRajhi",
                        config.get("intl.tahweel.mobileNumber"),
                        config.get("intl.tahweel.id"),
                        "20.50",
                        config.get("intl.tahweel.totalAmount")
                },
                {
                        "Western Union",
                        config.get("intl.wu.mobileNumber"),
                        config.get("intl.wu.id"),
                        "20.50",
                        config.get("intl.wu.totalAmount")
                }
        };
    }

    // ── Cards Data ─────────────────────────────────────────────────

    @DataProvider(name = "madaCardData")
    public static Object[][] madaCardData() {
        ConfigManager config = ConfigManager.getInstance();
        return new Object[][]{
                {
                        config.get("madaCard.mobileNumber"),
                        config.get("madaCard.id"),
                        config.get("madaCard.pin", "1234"),
                        config.get("madaCard.newPin", "5678")
                }
        };
    }

    @DataProvider(name = "sadadBillData")
    public static Object[][] sadadBillData() {
        ConfigManager config = ConfigManager.getInstance();
        return new Object[][]{
                {
                        config.get("sadad.mobileNumber"),
                        config.get("sadad.id"),
                        config.get("sadad.serviceCategory"),
                        config.get("sadad.serviceProvider"),
                        config.get("sadad.billType"),
                        config.get("sadad.billNumber"),
                        config.get("sadad.billName")
                }
        };
    }

    @DataProvider(name = "telecomRechargeProviders")
    public static Object[][] telecomRechargeProviders() {
        ConfigManager config = ConfigManager.getInstance();
        return new Object[][]{
                {
                        "STC",
                        config.get("stc.mobileNumber"),
                        config.get("stc.id"),
                        config.get("stc.rechargeNumber")
                },
                {
                        "Mobily",
                        config.get("mobily.mobileNumber"),
                        config.get("mobily.id"),
                        config.get("stc.rechargeNumber") // reuse
                },
                {
                        "Zain",
                        config.get("zain.mobileNumber"),
                        config.get("zain.id"),
                        config.get("zain.rechargeNumber")
                }
        };
    }
}
