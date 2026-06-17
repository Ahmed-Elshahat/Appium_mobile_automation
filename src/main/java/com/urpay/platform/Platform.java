package com.urpay.platform;

/**
 * Supported mobile platforms.
 */
public enum Platform {
    ANDROID,
    IOS;

    public static Platform fromString(String value) {
        if (value == null || value.isEmpty()) return ANDROID;
        return "ios".equalsIgnoreCase(value.trim()) ? IOS : ANDROID;
    }

    public boolean isAndroid() {
        return this == ANDROID;
    }

    public boolean isIOS() {
        return this == IOS;
    }
}
