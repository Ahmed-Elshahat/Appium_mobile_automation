package com.urpay.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.LandingPage;
import com.urpay.pages.auth.RegistrationPage;
import com.urpay.pages.wallet.InviteFriendsPage;
import com.urpay.utils.WaitUtils;

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

    public InvitationCodeFlow() {
        this.driver = (AndroidDriver) DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.inviteFriendsPage = new InviteFriendsPage();
        this.landingPage = new LandingPage();
        this.registrationPage = new RegistrationPage();
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

    @Step("Register new user with invitation code: {invitationCode}")
    public RegistrationPage registerWithInvitationCode(String invitationCode) {
        landingPage.clickRegister();
        registrationPage.enableInvitationCodeToggle();
        registrationPage.enterInvitationCode(invitationCode);
        log.info("Entered invitation code: {}", invitationCode);
        return registrationPage;
    }

    public InviteFriendsPage getInviteFriendsPage() {
        return inviteFriendsPage;
    }
}
