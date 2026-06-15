package com.urpay.model;

/**
 * URPay user data model.
 *
 * Replaces: Katalon GlobalVariable.urpayUser map
 *           [('id') : '', ('mobileNumber') : '', ('verificationCode') : '', ('passCode') : '2233']
 */
public class UserData {

    private String id;
    private String mobileNumber;
    private String verificationCode;
    private String passCode;
    private String userType;
    private double balance;

    public UserData() {}

    public UserData(String mobileNumber, String id, String passCode, String verificationCode) {
        this.mobileNumber = mobileNumber;
        this.id = id;
        this.passCode = passCode;
        this.verificationCode = verificationCode;
    }

    /**
     * Build a UserData from ConfigManager properties.
     * e.g. config keys: urpayUser.id, urpayUser.mobileNumber, etc.
     */
    public static UserData fromConfig(com.urpay.core.ConfigManager config, String prefix) {
        UserData user = new UserData();
        user.id = config.get(prefix + ".id", "");
        user.mobileNumber = config.get(prefix + ".mobileNumber", "");
        user.verificationCode = config.get(prefix + ".verificationCode", "1234");
        user.passCode = config.get(prefix + ".passCode", "2233");
        user.userType = config.get(prefix + ".userType", "");
        user.balance = config.getDouble(prefix + ".balance", 0);
        return user;
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public String getPassCode() { return passCode; }
    public void setPassCode(String passCode) { this.passCode = passCode; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return "UserData{mobile='" + mobileNumber + "', id='" + id + "'}";
    }
}
