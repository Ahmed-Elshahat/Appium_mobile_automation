package com.urpay.model;

import com.urpay.core.ConfigManager;

/**
 * Data model for an international (MTO) money transfer.
 *
 * This is the COMMON abstraction shared by every Money Transfer Operator (MoneyGram,
 * Western Union, Tahweel AlRajhi, Transfast, H2H): the transfer flow is identical across MTOs,
 * only this data (provider, delivery option, beneficiary, currency, country, amount) differs.
 *
 * Replaces the Katalon GlobalVariable maps:
 *   GlobalVariable.newBeneficiaryUser[fullName / nickName / currency / deliveryOption]
 *   GlobalVariable.transferDetails[serviceProvider / serviceProviderIndex / receiverCountry /
 *                                  transferAmountInSAR / transactionStatus]
 *
 * Build via {@link #builder()} or {@link #fromConfig(ConfigManager, String)}.
 */
public class InternationalTransferData {

    private final String serviceProvider;
    private final int serviceProviderIndex;
    private final String serviceProviderMarker;
    private final String beneficiaryName;
    private final String beneficiaryNickname;
    private final String deliveryOption;
    private final String currency;
    private final String receiverCountry;
    private final String amountSar;
    private final String transactionStatus;
    // ── Add-beneficiary fields ──
    private final String beneficiaryType;
    private final String bankName;
    private final String branch;
    private final String accountNumber;
    private final String routingNumber;
    private final String city;
    private final String purposeOfFunds;
    private final String firstName;
    private final String middleName;
    private final String lastName;

    private InternationalTransferData(Builder b) {
        this.serviceProvider = b.serviceProvider;
        this.serviceProviderIndex = b.serviceProviderIndex;
        this.serviceProviderMarker = b.serviceProviderMarker;
        this.beneficiaryName = b.beneficiaryName;
        this.beneficiaryNickname = b.beneficiaryNickname;
        this.deliveryOption = b.deliveryOption;
        this.currency = b.currency;
        this.receiverCountry = b.receiverCountry;
        this.amountSar = b.amountSar;
        this.transactionStatus = b.transactionStatus;
        this.beneficiaryType = b.beneficiaryType;
        this.bankName = b.bankName;
        this.branch = b.branch;
        this.accountNumber = b.accountNumber;
        this.routingNumber = b.routingNumber;
        this.city = b.city;
        this.purposeOfFunds = b.purposeOfFunds;
        this.firstName = b.firstName;
        this.middleName = b.middleName;
        this.lastName = b.lastName;
    }

    public String getServiceProvider() { return serviceProvider; }
    public int getServiceProviderIndex() { return serviceProviderIndex; }
    /** Brand text that uniquely identifies the provider's card (e.g. MoneyGram shows
     *  "Easy to track"). Empty = select by name/index instead. */
    public String getServiceProviderMarker() { return serviceProviderMarker; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public String getBeneficiaryNickname() { return beneficiaryNickname; }
    public String getDeliveryOption() { return deliveryOption; }
    public String getCurrency() { return currency; }
    public String getReceiverCountry() { return receiverCountry; }
    public String getAmountSar() { return amountSar; }
    public String getTransactionStatus() { return transactionStatus; }
    /** Beneficiary type when adding: "Others" (default) or "Myself". */
    public String getBeneficiaryType() { return beneficiaryType; }
    public String getBankName() { return bankName; }
    public String getBranch() { return branch; }
    public String getAccountNumber() { return accountNumber; }
    public String getRoutingNumber() { return routingNumber; }
    public String getCity() { return city; }
    public String getPurposeOfFunds() { return purposeOfFunds; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }

    /**
     * Build from a config prefix, e.g. prefix {@code moneygram.cashPickup} reads keys:
     * {@code moneygram.cashPickup.serviceProvider}, {@code .serviceProviderIndex},
     * {@code .beneficiaryName}, {@code .beneficiaryNickname}, {@code .deliveryOption},
     * {@code .currency}, {@code .receiverCountry}, {@code .amount}, {@code .status}.
     */
    public static InternationalTransferData fromConfig(ConfigManager c, String prefix) {
        return builder()
                .serviceProvider(c.get(prefix + ".serviceProvider", "MoneyGram"))
                .serviceProviderIndex(c.getInt(prefix + ".serviceProviderIndex", 1))
                .serviceProviderMarker(c.get(prefix + ".serviceProviderMarker", ""))
                .beneficiaryName(c.get(prefix + ".beneficiaryName"))
                .beneficiaryNickname(c.get(prefix + ".beneficiaryNickname", ""))
                .deliveryOption(c.get(prefix + ".deliveryOption"))
                .currency(c.get(prefix + ".currency", ""))
                .receiverCountry(c.get(prefix + ".receiverCountry", ""))
                .amountSar(c.get(prefix + ".amount", ""))
                .transactionStatus(c.get(prefix + ".status", "Pending"))
                .beneficiaryType(c.get(prefix + ".beneficiaryType", "Others"))
                .bankName(c.get(prefix + ".bankName", ""))
                .branch(c.get(prefix + ".branch", ""))
                .accountNumber(c.get(prefix + ".accountNumber", ""))
                .routingNumber(c.get(prefix + ".routingNumber", ""))
                .city(c.get(prefix + ".city", ""))
                .purposeOfFunds(c.get(prefix + ".purposeOfFunds", ""))
                .firstName(c.get(prefix + ".firstName", ""))
                .middleName(c.get(prefix + ".middleName", ""))
                .lastName(c.get(prefix + ".lastName", ""))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "InternationalTransferData{provider='" + serviceProvider + "', delivery='"
                + deliveryOption + "', beneficiary='" + beneficiaryName + "', country='"
                + receiverCountry + "', amount=" + amountSar + " SAR}";
    }

    /** Fluent builder — the flow/tests read a data object, never scattered parameters. */
    public static class Builder {
        private String serviceProvider = "MoneyGram";
        private int serviceProviderIndex = 1;
        private String serviceProviderMarker = "";
        private String beneficiaryName;
        private String beneficiaryNickname = "";
        private String deliveryOption;
        private String currency = "";
        private String receiverCountry = "";
        private String amountSar;
        private String transactionStatus = "Pending";
        private String beneficiaryType = "Others";
        private String bankName = "";
        private String branch = "";
        private String accountNumber = "";
        private String routingNumber = "";
        private String city = "";
        private String purposeOfFunds = "";
        private String firstName = "";
        private String middleName = "";
        private String lastName = "";

        public Builder serviceProvider(String v) { this.serviceProvider = v; return this; }
        public Builder serviceProviderIndex(int v) { this.serviceProviderIndex = v; return this; }
        public Builder serviceProviderMarker(String v) { this.serviceProviderMarker = v; return this; }
        public Builder beneficiaryName(String v) { this.beneficiaryName = v; return this; }
        public Builder beneficiaryNickname(String v) { this.beneficiaryNickname = v; return this; }
        public Builder deliveryOption(String v) { this.deliveryOption = v; return this; }
        public Builder currency(String v) { this.currency = v; return this; }
        public Builder receiverCountry(String v) { this.receiverCountry = v; return this; }
        public Builder amountSar(String v) { this.amountSar = v; return this; }
        public Builder transactionStatus(String v) { this.transactionStatus = v; return this; }
        public Builder beneficiaryType(String v) { this.beneficiaryType = v; return this; }
        public Builder bankName(String v) { this.bankName = v; return this; }
        public Builder branch(String v) { this.branch = v; return this; }
        public Builder accountNumber(String v) { this.accountNumber = v; return this; }
        public Builder routingNumber(String v) { this.routingNumber = v; return this; }
        public Builder city(String v) { this.city = v; return this; }
        public Builder purposeOfFunds(String v) { this.purposeOfFunds = v; return this; }
        public Builder firstName(String v) { this.firstName = v; return this; }
        public Builder middleName(String v) { this.middleName = v; return this; }
        public Builder lastName(String v) { this.lastName = v; return this; }

        public InternationalTransferData build() {
            return new InternationalTransferData(this);
        }
    }
}
