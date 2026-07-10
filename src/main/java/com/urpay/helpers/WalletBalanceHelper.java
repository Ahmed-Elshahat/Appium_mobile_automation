package com.urpay.helpers;

import com.urpay.core.ConfigManager;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Wallet Balance Helper — tops up a user's wallet balance directly in the DB.
 * Used after registration to ensure the account has sufficient funds for card issuance,
 * transfers, and other payment operations.
 */
public class WalletBalanceHelper {

    private static final Logger log = LoggerFactory.getLogger(WalletBalanceHelper.class);
    private static final String DEFAULT_BALANCE = "20000";

    /**
     * Set wallet balance to 20,000 SAR for the given wallet number.
     *
     * @param walletNumber the wallet number from registration (Provisioned.walletNumber)
     * @return true if the update succeeded (rows affected > 0)
     */
    @Step("Top up wallet balance to 20,000 SAR — wallet: {walletNumber}")
    public static boolean topUp(String walletNumber) {
        return topUp(walletNumber, DEFAULT_BALANCE);
    }

    /**
     * Set wallet balance to a specific amount.
     *
     * @param walletNumber the wallet number
     * @param amount       the balance amount (e.g. "20000")
     * @return true if the update succeeded
     */
    @Step("Set wallet balance to {amount} SAR — wallet: {walletNumber}")
    public static boolean topUp(String walletNumber, String amount) {
        ConfigManager c = ConfigManager.getInstance();
        String url = c.get("beneficiary.db.url",
                "jdbc:oracle:thin:@//192.168.102.37:1521/wltdbsit.neoleap.com.sa");
        String user = c.get("beneficiary.db.user", "EPAY_TESTING");
        String password = c.get("beneficiary.db.password", "JLSK#2936gkldw#Hjls");

        String sql = "UPDATE EPAY_WALLET.WALLET SET AVAILABLE_BALANCE = ?, CURRENT_BALANCE = ? "
                + "WHERE WALLET_NUMBER = ?";

        log.info("========== WALLET BALANCE TOP-UP ==========");
        log.info("Wallet Number: {}", walletNumber);
        log.info("Amount: {} SAR", amount);
        log.info("DB URL: {}", url);
        log.info("============================================");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, amount);
            ps.setString(2, amount);
            ps.setString(3, walletNumber);
            int rows = ps.executeUpdate();
            log.info("Wallet balance updated — rows affected: {}", rows);
            return rows > 0;
        } catch (SQLException e) {
            log.error("Wallet balance update failed for wallet {}: {}", walletNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Top up wallet by mobile number (for old/existing users).
     * Looks up the wallet number from EPAY_PARTY.PARTY by mobile, then updates balance.
     *
     * @param mobileNumber the mobile (format: 05XXXXXXXX or +96605XXXXXXXX)
     * @return true if balance was updated
     */
    @Step("Top up wallet balance by mobile number: {mobileNumber}")
    public static boolean topUpByMobile(String mobileNumber) {
        ConfigManager c = ConfigManager.getInstance();
        String url = c.get("beneficiary.db.url",
                "jdbc:oracle:thin:@//192.168.102.37:1521/wltdbsit.neoleap.com.sa");
        String user = c.get("beneficiary.db.user", "EPAY_TESTING");
        String password = c.get("beneficiary.db.password", "JLSK#2936gkldw#Hjls");

        // Normalize: remove +966 prefix, ensure starts with 0
        String mobile = mobileNumber;
        if (mobile.startsWith("+966")) {
            mobile = "0" + mobile.substring(4);
        }
        if (!mobile.startsWith("0")) {
            mobile = "0" + mobile;
        }

        // Direct update using mobile number in WALLET table (joined via PARTY)
        String sql = "UPDATE EPAY_WALLET.WALLET SET AVAILABLE_BALANCE = ?, CURRENT_BALANCE = ? "
                + "WHERE WALLET_NUMBER IN ("
                + "  SELECT w.WALLET_NUMBER FROM EPAY_WALLET.WALLET w "
                + "  JOIN EPAY_PARTY.PARTY p ON w.PARTY_ID = p.PARTY_ID "
                + "  WHERE p.MOBILE = ?)";

        log.info("========== WALLET TOP-UP BY MOBILE ==========");
        log.info("Mobile: {}", mobile);
        log.info("==============================================");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DEFAULT_BALANCE);
            ps.setString(2, DEFAULT_BALANCE);
            ps.setString(3, mobile);
            int rows = ps.executeUpdate();
            log.info("Wallet balance updated by mobile — rows affected: {}", rows);
            return rows > 0;
        } catch (SQLException e) {
            log.error("Wallet balance update failed for mobile {}: {}", mobile, e.getMessage());
            return false;
        }
    }
}
