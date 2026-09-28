package com.urpay.model;

/** Personal Information (KYC) values shown on the Profile → Personal Information form. */
public record KycDetails(String jobSector, String employer, String incomeSource,
                         String jobCategory, String incomeRange) {
}
