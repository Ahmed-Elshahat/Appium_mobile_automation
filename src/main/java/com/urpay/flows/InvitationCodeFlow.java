package com.urpay.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.LandingPage;
import com.urpay.pages.auth.RegistrationPage;
import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.InviteFriendsPage;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;

/**
 * Flow for the Registration Using Invitation Code test suite.
 * Orchestrates: navigate to Invite Friends → get codes → logout → register with code.
 *
 * Katalon source: Test Suites/WMVSuites/RegisterationUsingInvitationCode
 */
public class InvitationCodeFlow {

    private static final Logger log = LoggerFactory.getLogger(InvitationCodeFlow.class);

    private final AndroidDriver driver;
    private final WaitUtils waits;
    private final InviteFriendsPage inviteFriendsPage;
    private final LandingPage landingPage;
    private final RegistrationPage registrationPage;
    private final SettingsPage settingsPage;

    public InvitationCodeFlow() {
        this.driver = (AndroidDriver) DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.inviteFriendsPage = new InviteFriendsPage();
        this.landingPage = new LandingPage();
        this.registrationPage = new RegistrationPage();
        this.settingsPage = new SettingsPage();
    }

    @Step("Navigate to Invite Friends page and get referral code")
    public String navigateAndGetReferralCode() {
        inviteFriendsPage.navigateToInviteFriends();
        String referralCode = inviteFriendsPage.getReferralCodeText();
        log.info("Referral code: {}", referralCode);
        return referralCode;
    }

    @Step("Switch to Invite Friends tab and get invitation code")
    public String getInvitationCode() {
        inviteFriendsPage.tapInviteFriendsTab();
        String invitationCode = inviteFriendsPage.getInvitationCodeText();
        log.info("Invitation code: {}", invitationCode);
        return invitationCode;
    }

    // Onboarding walkthrough shown after logout: a 'Skip' link (a TextView inside a clickable
    // TouchableOpacity) returns to the Login/Register landing page.
    private static final By SKIP_ONBOARDING = AppiumBy.xpath(
            "//*[@text='Skip' or @text='SKIP' or @label='Skip']");

    // Landing page is ready once its Login or Register button renders (register id is stripped to
    // testID-primary--main on the obfuscated build; the visible 'Register' label covers both).
    private static final By LANDING_READY = AppiumBy.xpath(
            "//*[@content-desc='testID-secondary-login-main'"
            + " or @content-desc='testID-primary-register-main'"
            + " or @content-desc='testID-primary--main' or @text='Register']");

    @Step("Register new user with invitation code: {invitationCode}")
    public RegistrationPage registerWithInvitationCode(String invitationCode) {
        logout();
        skipOnboardingToLanding();
        landingPage.clickRegister();
        registrationPage.enableInvitationCodeToggle();
        registrationPage.enterInvitationCode(invitationCode);
        log.info("Entered invitation code: {}", invitationCode);
        return registrationPage;
    }

    @Step("Logout via Settings deep link to return to the landing page")
    public void logout() {
        settingsPage.openViaDeepLink();
        settingsPage.tapLogout();
        settingsPage.confirmLogout();
    }

    @Step("Dismiss the onboarding walkthrough shown after logout, back to the landing page")
    private void skipOnboardingToLanding() {
        // Logout drops the app back onto the feature walkthrough (Payment Management / Language /
        // Skip), not straight to the landing page. Tap 'Skip' until the landing buttons render.
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        for (int i = 0; i < 8; i++) {
            if (!waits.findQuick(LANDING_READY, 2).isEmpty()) {
                return;
            }
            List<WebElement> skip = waits.findQuick(SKIP_ONBOARDING, 2);
            if (!skip.isEmpty()) {
                skip.get(0).click();
            }
        }
    }

    public InviteFriendsPage getInviteFriendsPage() {
        return inviteFriendsPage;
    }
}
