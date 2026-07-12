package com.urpay.helpers;

import com.urpay.core.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Passcode Reset API Helper — resets a consumer's passcode to the known starting value
 * via the wallet backend, so the UI login at the start of a Change-Passcode suite is
 * deterministic (the account is guaranteed to be on {@code 2233} regardless of any state
 * left by a previous run or manual testing).
 *
 * Replicates Katalon's pre-test API chain (ChangePasscodeFor&lt;Tier&gt;User/setUpTestData):
 *   1. preLogin                      → consumers/pre-login
 *   2. generateOTP                   → otp/generate          (returns otpReference + X-OTP-Token)
 *   3. verifyOTP                     → otp/verify            (returns X-OTP-Token)
 *   4. getResetRules                 → password/reset/rules  (returns X-OTP-Token)
 *   5. resetPasswordAuthorization    → password/reset/authorization (returns X-OTP-Token)
 *   6. resetPasscodeWithOldOtpToken  → consumers/passcode    (PUT — commits the new passcode)
 *
 * Mirrors the keywords:
 *   com.uspace.Register.urpayuser.LoginWithAPI
 *   com.uspace.Register.urpayuser.ResetPasswordWithAPI
 *
 * Best-effort: every call is guarded; on any failure the helper logs a warning and returns
 * {@code false} so the caller can continue with the UI flow ("skip on failure").
 */
public final class PasscodeResetApiHelper {

    private static final Logger log = LoggerFactory.getLogger(PasscodeResetApiHelper.class);

    private static final String OTP_TOKEN_HEADER = "X-OTP-Token";

    private PasscodeResetApiHelper() {
    }

    /**
     * Run the full reset chain to set the account passcode back to its known starting value.
     *
     * @param mobileNumber raw mobile number from config (e.g. {@code 0520111131}); converted to
     *                     the {@code +966XXXXXXXXX} form expected by the API
     * @param poi          proof-of-identity number (national/border id)
     * @param poiType      proof-of-identity type ({@code NAT} or {@code BOR})
     * @param otp          the OTP the SIT backend accepts for this account (e.g. {@code 1234})
     * @param dob          the account's registered date of birth ({@code yyyy-MM-dd}); validated by
     *                     the reset-authorization step and therefore per-user, not global
     * @return {@code true} only if the final reset call returned a 2xx status
     */
    @Step("Reset passcode via API for {mobileNumber} (poiType {poiType})")
    public static boolean resetToDefaultPasscode(String mobileNumber, String poi, String poiType, String otp,
                                                 String dob) {
        ConfigManager config = ConfigManager.getInstance();
        String baseUrl = config.get("passcodeReset.baseUrl", "https://192.168.100.71:14301/walletapp/v1");
        String deviceId = config.get("passcodeReset.deviceId", "3f5996472f8f6c3d");
        String resolvedDob = (dob == null || dob.trim().isEmpty())
                ? config.get("passcodeReset.dob", "1990-01-01")
                : dob.trim();
        String passcodeBlob = config.get("passcodeReset.passcode2233Blob", DEFAULT_PASSCODE_2233_BLOB);
        String mobileWithExtension = toInternationalFormat(mobileNumber);

        try {
            RestAssured.useRelaxedHTTPSValidation();

            log.info("Passcode reset via API starting for {} ({})", mobileWithExtension, poiType);

            preLogin(baseUrl, mobileWithExtension, poi, poiType, deviceId);

            Response generateOtp = generateOtp(baseUrl, mobileWithExtension, poi, poiType, deviceId);
            String otpReference = generateOtp.jsonPath().getString("body.otpReference");
            String generateOtpToken = generateOtp.getHeader(OTP_TOKEN_HEADER);
            if (otpReference == null || generateOtpToken == null) {
                log.warn("Passcode reset aborted: missing otpReference/token from generate-OTP response");
                return false;
            }

            Response verify = verifyOtp(baseUrl, mobileWithExtension, otpReference, otp, generateOtpToken, deviceId);
            String verifyOtpToken = verify.getHeader(OTP_TOKEN_HEADER);
            if (verifyOtpToken == null) {
                log.warn("Passcode reset aborted: OTP verification did not return a token (status {})",
                        verify.getStatusCode());
                return false;
            }

            Response rules = getResetRules(baseUrl, mobileWithExtension, poi, poiType, verifyOtpToken, deviceId);
            String resetRulesToken = rules.getHeader(OTP_TOKEN_HEADER);
            if (resetRulesToken == null) {
                log.warn("Passcode reset aborted: reset-rules call did not return a token (status {})",
                        rules.getStatusCode());
                return false;
            }

            Response auth = resetPasswordAuthorization(baseUrl, mobileWithExtension, resolvedDob, resetRulesToken, deviceId);
            String authToken = auth.getHeader(OTP_TOKEN_HEADER);
            if (authToken == null) {
                log.warn("Passcode reset aborted: authorization call did not return a token (status {})",
                        auth.getStatusCode());
                return false;
            }

            Response reset = resetPasscode(baseUrl, passcodeBlob, authToken, deviceId);
            int status = reset.getStatusCode();
            if (status >= 200 && status < 300) {
                log.info("Passcode reset via API succeeded for {} (status {})", mobileWithExtension, status);
                return true;
            }
            log.warn("Passcode reset final call failed for {} (status {}): {}",
                    mobileWithExtension, status, reset.getBody().asString());
            return false;
        } catch (Exception e) {
            log.warn("Passcode reset via API failed for {} — continuing with UI flow: {}",
                    mobileWithExtension, e.getMessage());
            return false;
        }
    }

    // ── API steps (mirror the Katalon keywords) ────────────────────

    @Step("API pre-login")
    private static Response preLogin(String baseUrl, String mobile, String poi, String poiType, String deviceId) {
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"poi\":{\"poiNumber\":\"" + poi
                + "\",\"poiType\":\"" + poiType + "\"}}";
        return commonHeaders(deviceId, null)
                .header("Content-Type", "application/json")
                .body(body)
                .post(baseUrl + "/authentication/consumers/pre-login");
    }

    @Step("API generate OTP")
    private static Response generateOtp(String baseUrl, String mobile, String poi, String poiType, String deviceId) {
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"purpose\":\"002\",\"poi\":{\"poiNumber\":\"" + poi
                + "\",\"poiType\":\"" + poiType + "\"}}";
        return commonHeaders(deviceId, null)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(body)
                .post(baseUrl + "/otp/generate");
    }

    @Step("API verify OTP")
    private static Response verifyOtp(String baseUrl, String mobile, String otpReference, String otp,
                                      String otpToken, String deviceId) {
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"otp\":\"" + otp + "\",\"otpReference\":\""
                + otpReference + "\",\"parameters\":{},\"purpose\":\"002\"}";
        return commonHeaders(deviceId, otpToken)
                .header("Content-Type", "application/json")
                .body(body)
                .post(baseUrl + "/otp/verify");
    }

    @Step("API get reset rules")
    private static Response getResetRules(String baseUrl, String mobile, String poi, String poiType,
                                          String otpToken, String deviceId) {
        return commonHeaders(deviceId, otpToken)
                .queryParam("mobileNumber", mobile)
                .queryParam("poi.number", poi)
                .queryParam("poi.type", poiType)
                .get(baseUrl + "/consumers/password/reset/rules");
    }

    @Step("API reset-password authorization")
    private static Response resetPasswordAuthorization(String baseUrl, String mobile, String dob,
                                                       String otpToken, String deviceId) {
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"validationFields\":{\"DATE_OF_BIRTH\":\""
                + dob + "\"},\"action\":\"reset\"}";
        return commonHeaders(deviceId, otpToken)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(body)
                .post(baseUrl + "/consumers/password/reset/authorization");
    }

    @Step("API reset passcode")
    private static Response resetPasscode(String baseUrl, String newPasscodeBlob, String otpToken, String deviceId) {
        String body = "{\"action\":\"reset\",\"newPasscode\":\"" + newPasscodeBlob + "\"}";
        return commonHeaders(deviceId, otpToken)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(body)
                .put(baseUrl + "/authentication/consumers/passcode");
    }

    // ── Shared header set (matches the Katalon TestObjectProperty list) ──

    private static RequestSpecification commonHeaders(String deviceId, String otpToken) {
        RequestSpecification spec = RestAssured.given()
                .header("Accept", "application/json, text/plain, */*")
                .header("Strapi_token", "")
                .header("User-Agent", "okhttp/4.12.0")
                .header("X-App-Version", "5.10.0.1596")
                .header("X-Client-Id", "1278490422")
                .header("X-Client-Secret", "64")
                .header("X-Device-Id", deviceId)
                .header("X-Device-Name", "android v28")
                .header("X-Device-Platform", "Android")
                .header("X-Fp-Latitude", "24.7111")
                .header("X-Fp-Longitude", "46.6753")
                .header("X-Latitude", "24.7111")
                .header("X-Longitude", "46.6753")
                .header("X-Request-Id", UUID.randomUUID().toString())
                .header("X-Session-Language", "EN");
        if (otpToken != null) {
            spec = spec.header(OTP_TOKEN_HEADER, otpToken);
        }
        return spec;
    }

    /** Convert a local mobile ({@code 05XXXXXXXX}) to the {@code +966XXXXXXXXX} form the API expects. */
    private static String toInternationalFormat(String mobileNumber) {
        String trimmed = mobileNumber == null ? "" : mobileNumber.trim();
        if (trimmed.startsWith("+")) {
            return trimmed;
        }
        if (trimmed.startsWith("0")) {
            return "+966" + trimmed.substring(1);
        }
        return "+966" + trimmed;
    }

    /** Encrypted payload representing passcode {@code 2233} (same across tiers in Katalon). */
    private static final String DEFAULT_PASSCODE_2233_BLOB =
            "FzStfcMyZ2ze9ssLh3zz6S2vb4eD8RTskOAAYrigrnVKFaR1se1hLvvRJ2EV5iYb5HWGUCixRp9r5ttc6D7YWno5Zwn"
            + "paZ0oX3KKX6uXFarvOEAMwUKCAajO4KrEgn+4G0QfolzlFKoC4AMoTbF0VNOiSJ1Kn9s7VrVrN4MhkcbH3xDyS85mP"
            + "wwqKcRgEVZ4tg7SWqspBHrwzegnLbu5kTW+KFedvkRqPtZv1MXdMqWUOswX7VyQbYRQ367GlUr0L0eExE2aGrVqgui"
            + "YkefPkNRAvpJR1pmC92voGoDIQb9ovfEYLJ6JEmL+E817laG8jsvz/8ckkAIhEuTK/+9P5Q==";
}
