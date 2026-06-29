package com.urpay.tests.auth;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.pages.auth.ForgotPasscodePage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Forgot Passcode — full negative journey (Date-of-Birth negatives + new-passcode negatives).
 *
 * Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/{Family,Visitor}Tier/
 *       ForgotPasscode{Family,Visitor}Tier-Na/egativeCases
 *
 * Extends the Date-of-Birth negative cases ({@link AbstractForgotPasscodeDobNegativeTest}) and,
 * after returning to the passcode screen, re-enters the User Verification screen, supplies the
 * VALID date of birth, then exercises the Enter-New-Passcode validations:
 *   consecutive/repeated digits → rejected, existing passcode → allowed to confirm,
 *   mismatching confirm passcode → rejected.
 */
public abstract class AbstractForgotPasscodeNegativeJourneyTest extends AbstractForgotPasscodeDobNegativeTest {

    private String consecutivePasscode() {
        return cfg().get(keyPrefix() + ".consecutiveNumber", "1111");
    }

    private String existingPasscode() {
        return cfg().get(keyPrefix() + ".existingPasscode", "2233");
    }

    private String differentPasscode() {
        return cfg().get(keyPrefix() + ".differentPasscode", "3233");
    }

    private String dobMonth() {
        return cfg().get(keyPrefix() + ".dob.month", "May");
    }

    private String dobDay() {
        return cfg().get(keyPrefix() + ".dob.day", "30");
    }

    private String dobYear() {
        return cfg().get(keyPrefix() + ".dob.year", "1994");
    }

    // ── 8) RETURN TO USER VERIFICATION (tap Forgot Passcode again) ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 8,
            dependsOnMethods = "testDragDownReturnsToPasscodeScreen")
    @Story("Return To User Verification")
    @Description("Tap 'Forgot your passcode?' again to return to the User Verification screen")
    @Severity(SeverityLevel.NORMAL)
    public void testReturnToUserVerification() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.tapForgotPasscode();

        Assert.assertTrue(page.isUserVerificationScreenDisplayed(),
                "User Verification screen should be displayed again");
    }

    // ── 9) ENTER VALID DOB → ENTER NEW PASSCODE SCREEN ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 9,
            dependsOnMethods = "testReturnToUserVerification")
    @Story("Enter Valid Date Of Birth")
    @Description("Enter the valid date of birth and verify navigation to the Enter New Passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterValidDobNavigatesToNewPasscode() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterDateOfBirth(dobMonth(), dobDay(), dobYear());

        Assert.assertTrue(page.isEnterNewPasscodeScreenDisplayed(),
                "Enter New Passcode screen should be shown after entering a valid date of birth");
    }

    // ── 10) CONSECUTIVE/REPEATED PASSCODE → ALERT ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 10,
            dependsOnMethods = "testEnterValidDobNavigatesToNewPasscode")
    @Story("Consecutive Passcode")
    @Description("Enter a consecutive/repeated passcode and verify the rejection alert")
    @Severity(SeverityLevel.NORMAL)
    public void testConsecutivePasscodeShowsAlert() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterPasscodeOnKeypad(consecutivePasscode());

        String message = page.getNotificationMessage(20);
        Assert.assertEquals(message, "The passcode should not be in sequence or consecutive",
                "Consecutive passcode alert should be displayed");
    }

    // ── 11) EXISTING PASSCODE AS NEW → NAVIGATES TO CONFIRM ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 11,
            dependsOnMethods = "testConsecutivePasscodeShowsAlert")
    @Story("Existing Passcode As New")
    @Description("Enter the existing passcode as the new passcode and verify navigation to the Confirm screen")
    @Severity(SeverityLevel.NORMAL)
    public void testEnterExistingPasscodeNavigatesToConfirm() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterPasscodeOnKeypad(existingPasscode());

        Assert.assertTrue(page.isConfirmPasscodeScreenDisplayed(),
                "Confirm passcode screen should be shown after entering the existing passcode");
    }

    // ── 12) DIFFERENT CONFIRM PASSCODE → MISMATCH ALERT ──

    @Test(groups = {"auth", "passcode", "negative"}, priority = 12,
            dependsOnMethods = "testEnterExistingPasscodeNavigatesToConfirm")
    @Story("Mismatching Confirm Passcode")
    @Description("Enter a different passcode on the Confirm screen and verify the mismatch alert")
    @Severity(SeverityLevel.NORMAL)
    public void testDifferentConfirmPasscodeShowsMismatch() {
        ForgotPasscodePage page = new ForgotPasscodePage();
        page.enterPasscodeOnKeypad(differentPasscode());

        String message = page.getNotificationMessage(20);
        Assert.assertEquals(message, "The passcode is not matched",
                "Passcode mismatch alert should be displayed");
    }
}
