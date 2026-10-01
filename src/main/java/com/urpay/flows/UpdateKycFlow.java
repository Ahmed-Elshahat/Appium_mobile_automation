package com.urpay.flows;

import java.util.concurrent.ThreadLocalRandom;

import com.urpay.model.KycDetails;
import com.urpay.pages.settings.UpdateKycPage;

import io.qameta.allure.Step;

/**
 * Update KYC flow: Dashboard → profile avatar → Personal Information → change every KYC
 * field to a random different value → Save.
 *
 * <p>Migrated from Katalon Scripts/Wallet_VAS/UpdateKYC/* (NavigateToProfileAndPersonalInfo,
 * VerifyThatJobSectorUpdatedSuccessfully, VerifythatIncomeSourceisUpdatedSuccessfully,
 * VerifythatJobCategoryUpdatedSuccessfully, VerifythatIncomeSalaryisUpdatedSuccessfully).
 */
public class UpdateKycFlow {

    private final UpdateKycPage page;

    public UpdateKycFlow() {
        this.page = new UpdateKycPage();
    }

    @Step("Navigate Dashboard → Profile → Personal Information")
    public UpdateKycPage openPersonalInformation() {
        page.tapProfileAvatar();
        page.tapPersonalInformation();
        if (!page.isKycFormDisplayed(20)) {
            page.dumpScreen("form-not-shown");
        }
        return page;
    }

    @Step("Update KYC with random values and Save")
    public KycDetails updateWithRandomValues() {
        String jobSector = page.selectRandomJobSector();
        String employer = "URPAY AUTO " + ThreadLocalRandom.current().nextInt(100, 999);
        if (!page.enterEmployerIfShown(employer)) {
            employer = "";
        }
        String incomeSource = page.selectRandomIncomeSource();
        String jobCategory = page.selectRandomJobCategory();
        String incomeRange = page.selectRandomIncomeRange();
        page.tapSave();
        return new KycDetails(jobSector, employer, incomeSource, jobCategory, incomeRange);
    }

    @Step("Close success screen and reopen Personal Information")
    public UpdateKycPage reopenPersonalInformation() {
        // Success CTA returns to Home on current builds (older builds returned to Profile).
        page.tapDone();
        if (!page.isPersonalInfoEntryDisplayed(5)) {
            page.tapProfileAvatar();
        }
        page.tapPersonalInformation();
        if (!page.isKycFormDisplayed(20)) {
            page.dumpScreen("reopen-form-not-shown");
        }
        return page;
    }

    @Step("Read current KYC values from the form and return to Dashboard")
    public KycDetails readCurrentValues() {
        KycDetails values = new KycDetails(page.getJobSector(), page.getEmployer(), page.getIncomeSource(),
                page.getJobCategory(), page.getIncomeRange());
        page.backToDashboard();
        return values;
    }
}
