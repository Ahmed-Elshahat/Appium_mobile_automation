package com.urpay.flows;

import com.urpay.pages.settings.UpdateNamePage;

import io.qameta.allure.Step;

/** Update Name flow: Dashboard → profile avatar → Update Name → Update. */
public class UpdateNameFlow {

    private final UpdateNamePage page;

    public UpdateNameFlow() {
        this.page = new UpdateNamePage();
    }

    @Step("Navigate Dashboard → Profile → Update Name")
    public UpdateNamePage openUpdateName() {
        page.tapProfileAvatar();
        page.tapUpdateName();
        return page;
    }

    /** Taps Update and returns the app's result message ("" if none), then returns to the Dashboard. */
    @Step("Submit Update Name")
    public String submitUpdate() {
        String result = page.tapUpdateAndReadResult(20);
        page.backToDashboard();
        return result;
    }
}
