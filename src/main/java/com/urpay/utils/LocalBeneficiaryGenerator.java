package com.urpay.utils;

import java.util.List;
import java.util.Random;

/**
 * Generates random local-beneficiary test data (account-holder name).
 *
 * Mirrors Katalon {@code com.uspace.localBeneficiaryGenerator.LocalBeneficiaryGenerator}: the
 * account-holder name is random per run so each AddLocalBeneficiary creates a fresh PENDING
 * beneficiary that the DB activation (IVR skip) can then activate by name. The IBAN is fixed in
 * the Katalon AddLocalBeneficiary script, so only the name varies here.
 */
public final class LocalBeneficiaryGenerator {

    private static final List<String> FIRST_NAMES = List.of(
            "Ahmed", "Mohammed", "Khalid", "Omar", "Youssef", "Sara", "Noura",
            "Fatima", "Layla", "Rana", "Hassan", "Karim", "Aziz", "Mansour", "Salem");

    private static final List<String> LAST_NAMES = List.of(
            "Habib", "Sarkis", "Faisal", "Saleh", "Nasser", "Hassan", "Karim",
            "Aziz", "Mansour", "Rashid", "Qassim", "Tamimi", "Harbi", "Otaibi", "Ghamdi");

    private static final Random RANDOM = new Random();

    private LocalBeneficiaryGenerator() {
    }

    /** A random "First Last" account-holder name (e.g. "Khalid Faisal"). */
    public static String randomFullName() {
        String first = FIRST_NAMES.get(RANDOM.nextInt(FIRST_NAMES.size()));
        String last = LAST_NAMES.get(RANDOM.nextInt(LAST_NAMES.size()));
        return first + " " + last;
    }
}
