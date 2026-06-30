package com.urpay.tests.provisioning;

import static org.testng.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.urpay.helpers.FamilyRegistrationApiHelper;
import com.urpay.helpers.RegistrationApiHelper;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Standalone, driverless provisioning utility.
 *
 * <p>Unlike the rest of the suite it does <b>not</b> extend {@code BaseTest}: there is no
 * Appium driver, device or app launch. Every method talks only to the backend / simulator
 * / Oracle DB to mint fresh URPay accounts, and each created account's credentials
 * (mobile, POI, passcode, party id) are printed to the console by the helpers.
 *
 * <p>Requires the neoleap VPN — the SIT API host, backend simulator and Oracle DB are all
 * on the internal network, so this will not run from a developer laptop.
 *
 * <p>Run a single flow, e.g. just the family link:
 * <pre>
 *   mvn test -Dprofile=sit-wmv -Dsurefire.suiteXmlFiles=src/test/resources/suites/provision-users.xml
 * </pre>
 */
@Feature("User Provisioning")
public class UserProvisioningTest {

    private static final Logger log = LoggerFactory.getLogger(UserProvisioningTest.class);

    @Test
    @Story("Provision a national (NAT) consumer")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Registers and activates a brand-new national URPay consumer via the backend.")
    public void provisionNationalUser() {
        log.info("########## PROVISIONING: national (NAT) consumer ##########");
        assertTrue(RegistrationApiHelper.registerNationalConsumer(),
                "Failed to provision a national consumer");
    }

    @Test
    @Story("Provision a resident (IQA) consumer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Registers and activates a brand-new resident (Iqama) URPay consumer via the backend.")
    public void provisionResidentUser() {
        log.info("########## PROVISIONING: resident (IQA) consumer ##########");
        assertTrue(RegistrationApiHelper.registerResidentConsumer(),
                "Failed to provision a resident consumer");
    }

    @Test
    @Story("Provision a visitor (BOR) consumer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Registers and activates a brand-new visitor (Border) URPay consumer via the backend.")
    public void provisionVisitorUser() {
        log.info("########## PROVISIONING: visitor (BOR) consumer ##########");
        assertTrue(RegistrationApiHelper.registerVisitorConsumer(),
                "Failed to provision a visitor consumer");
    }

    @Test
    @Story("Provision a linked parent + kid (<18) family")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Registers a parent and a kid (under 18) and links them into a family entirely "
            + "through the backend (kid sends the link request, parent approves it).")
    public void provisionLinkedParentAndKid() {
        log.info("########## PROVISIONING: parent + kid family link ##########");
        assertTrue(FamilyRegistrationApiHelper.registerAndLinkParentChild(),
                "Failed to provision and link a parent + kid family");
    }
}
