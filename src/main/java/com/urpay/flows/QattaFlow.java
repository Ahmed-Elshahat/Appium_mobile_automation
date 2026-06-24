package com.urpay.flows;

import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.QattaGroupPage;
import com.urpay.pages.wallet.QattaPage;

import io.qameta.allure.Step;

/**
 * Qatta group flow.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/QattaSuites/EditGroupQattaDetails
 *
 * Composes page actions into reusable journeys (no Thread.sleep, no assertions):
 *   - navigate Dashboard → services → Qatta → group option
 *   - create a new group with two members
 *   - open the first group, edit its name, save
 *   - validate the updated name on the group details tab
 */
public class QattaFlow {

    private final QattaPage qattaPage = new QattaPage();
    private final QattaGroupPage groupPage = new QattaGroupPage();

    @Step("Navigate Dashboard → Qatta group option")
    public QattaPage navigateToQattaGroups() {
        new DashboardPage().dismissPopups();
        qattaPage.openQatta();
        qattaPage.tapGroupQattaOption();
        return qattaPage;
    }

    @Step("Navigate Dashboard → Qatta single option")
    public QattaPage navigateToSingleQatta() {
        new DashboardPage().dismissPopups();
        qattaPage.openQatta();
        qattaPage.tapSingleQattaOption();
        return qattaPage;
    }

    /**
     * Create a new group with two members.
     * Mirrors Katalon ToCreateNewQattaGroup step order exactly.
     *
     * @return the group page, on the group list after creation
     */
    @Step("Create qatta group '{groupName}' with two members")
    public QattaGroupPage createGroup(String groupName,
                                      String firstMemberMobile, String firstMemberName,
                                      String secondMemberMobile, String secondMemberName) {
        // Ensure the Group Qatta tab is active before adding (single vs group tabs).
        qattaPage.tapGroupQattaOption();
        qattaPage.tapAddNewGroup();

        groupPage.enterGroupName(groupName);
        groupPage.tapNext();

        // First member
        groupPage.tapAddNewNumber();
        groupPage.enterMemberMobile(firstMemberMobile);
        groupPage.tapNextContact();
        groupPage.enterContactName(firstMemberName);
        groupPage.tapAddContact();

        // Second member
        groupPage.tapAddNewNumber();
        groupPage.enterMemberMobile(secondMemberMobile);
        groupPage.tapNextContact();
        groupPage.enterContactName(secondMemberName);
        groupPage.tapAddContact();

        groupPage.tapCreateGroup();
        // Creation returns to the main Qatta view; switch back to the Group tab to see the group.
        qattaPage.tapGroupQattaOption();
        return groupPage;
    }

    /**
     * Open the first group and rename it.
     * Mirrors Katalon editFirstQattaDetails.
     *
     * @return the group page, ready for success-message verification
     */
    @Step("Edit first group's name to '{newName}'")
    public QattaGroupPage editFirstGroupName(String newName) {
        groupPage.openFirstGroup();
        groupPage.tapEdit();
        groupPage.replaceGroupName(newName);
        groupPage.tapSave();
        return groupPage;
    }
}
