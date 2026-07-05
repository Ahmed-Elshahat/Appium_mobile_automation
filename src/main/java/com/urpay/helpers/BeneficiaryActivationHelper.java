package com.urpay.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;

import io.qameta.allure.Step;

/**
 * Local-beneficiary IVR-skip activation.
 *
 * Adding a new local beneficiary triggers an IVR phone-verification call that cannot be driven on
 * LambdaTest, so the beneficiary is activated directly in the wallet DB — exactly as Katalon does
 * in {@code com.uspace.activateBeneficiaryKeyword.BeneficiaryKeyword.activateBeneficiary}:
 *
 * <pre>
 *   SELECT ID FROM EPAY_BENEFICIARY.UNIFIED_BENEFICIARY
 *     WHERE ACCOUNT_HOLDER_NAME = ? AND STATUS = 'PENDING';
 *   UPDATE EPAY_BENEFICIARY.UNIFIED_BENEFICIARY
 *     SET STATUS = 'ACTIVE' WHERE ID = ?;
 * </pre>
 *
 * Implemented as a single UPDATE by (name, PENDING) so the just-created beneficiary is activated.
 *
 * <p>DB connection (Katalon OracleConnectionKeyword default):
 * {@code jdbc:oracle:thin:@//192.168.102.37:1521/wltdbsit.neoleap.com.sa} (user EPAY_TESTING).
 * Requires network access to the SIT wallet DB — run in an environment that can reach it.
 */
public final class BeneficiaryActivationHelper {

    private static final Logger log = LoggerFactory.getLogger(BeneficiaryActivationHelper.class);

    private BeneficiaryActivationHelper() {
    }

    /**
     * Activate the PENDING local beneficiary with the given account-holder name.
     *
     * @param fullName the account-holder name entered when adding the beneficiary
     * @return number of rows activated (&gt;0 = success), 0 if none matched, -1 on DB error
     */
    @Step("Activate local beneficiary in DB (IVR skip): {fullName}")
    public static int activate(String fullName) {
        ConfigManager c = ConfigManager.getInstance();
        String url = c.get("beneficiary.db.url",
                "jdbc:oracle:thin:@//192.168.102.37:1521/wltdbsit.neoleap.com.sa");
        String user = c.get("beneficiary.db.user", "EPAY_TESTING");
        String password = c.get("beneficiary.db.password", "JLSK#2936gkldw#Hjls");

        String sql = "UPDATE EPAY_BENEFICIARY.UNIFIED_BENEFICIARY "
                + "SET STATUS = 'ACTIVE' WHERE ACCOUNT_HOLDER_NAME = ? AND STATUS = 'PENDING'";

        log.info("Activating local beneficiary '{}' via DB (IVR skip)...", fullName);
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            int rows = ps.executeUpdate();
            log.info("Beneficiary '{}' activation — rows affected: {}", fullName, rows);
            return rows;
        } catch (SQLException e) {
            log.error("DB activation failed for '{}': {}", fullName, e.getMessage());
            return -1;
        }
    }
}
