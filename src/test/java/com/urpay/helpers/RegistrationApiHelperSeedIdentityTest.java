package com.urpay.helpers;

import com.urpay.core.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RegistrationApiHelperSeedIdentityTest {

    @Test
    public void seedIdentityBuildsConsistentNamesAndDob() {
        ConfigManager cfg = ConfigManager.getInstance();
        cfg.set("registration.identity.firstNameAr", "محمد");
        cfg.set("registration.identity.fatherNameAr", "عبدالله");
        cfg.set("registration.identity.grandNameAr", "محمد");
        cfg.set("registration.identity.familyNameAr", "الحمري");
        cfg.set("registration.identity.firstNameEn", "Mohamed");
        cfg.set("registration.identity.fatherNameEn", "Abdullah");
        cfg.set("registration.identity.grandNameEn", "Mohammed");
        cfg.set("registration.identity.familyNameEn", "Al-Ahmari");
        cfg.set("registration.identity.birthDateG", "2001-08-11");
        cfg.set("registration.identity.dateOfBirthH", "1422-05-21");
        cfg.set("registration.identity.nationalityAr", "المملكة العربية السعودية");
        cfg.set("registration.identity.placeOfBirthAr", "الرياض");

        RegistrationApiHelper.SeedIdentity identity = RegistrationApiHelper.buildSeedIdentity(cfg);

        Assert.assertEquals(identity.twoNamesAr, "محمد الحمري");
        Assert.assertEquals(identity.fullNameAr, "محمد عبدالله محمد الحمري");
        Assert.assertEquals(identity.twoNamesEn, "Mohamed Al-Ahmari");
        Assert.assertEquals(identity.fullNameEn, "Mohamed Abdullah Mohammed Al-Ahmari");
        Assert.assertEquals(identity.birthDateG, "2001-08-11");
        Assert.assertEquals(identity.dateOfBirthH, "1422-05-21");
        Assert.assertEquals(identity.dobHAsInt, 14220521);
    }

    @Test
    public void queryEncodingKeepsPlusAndEncodesArabic() {
        String qp = RegistrationApiHelper.encodeQueryParam("MobileNumber", "+966520000000");
        Assert.assertTrue(qp.contains("%2B966520000000"), "Plus sign must be encoded as %2B");

        String encodedArabic = RegistrationApiHelper.urlEncode("الرياض");
        Assert.assertTrue(encodedArabic.contains("%D8"), "Arabic text should be UTF-8 URL encoded");
        Assert.assertFalse(encodedArabic.contains("+"), "Spaces should not use + in encoded output");
    }
}
