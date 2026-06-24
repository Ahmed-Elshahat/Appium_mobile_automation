package com.urpay.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration manager — reads .properties files per profile.
 * Supports nested map-style properties (e.g. urpayUser.id, urpayUser.mobileNumber).
 *
 * Replaces: Katalon GlobalVariable + 16 .glbl execution profiles + SharedProfile.groovy
 *
 * Usage:
 *   ConfigManager config = ConfigManager.getInstance();
 *   String mobile = config.get("urpayUser.mobileNumber");
 *   Map<String,String> user = config.getMap("urpayUser");
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final Properties properties = new Properties();
    private final String profileName;

    private ConfigManager() {
        this.profileName = System.getProperty("profile", "default");
        loadDefaults();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Reload with a different profile.
     */
    public static synchronized void reloadProfile(String profileName) {
        getInstance().properties.clear();
        getInstance().loadFile("default");
        if (!"default".equals(profileName)) {
            getInstance().loadFile(profileName);
        }
    }

    private void loadDefaults() {
        loadFile("default");
        if (!"default".equals(profileName)) {
            loadFile(profileName);
        }
    }

    private void loadFile(String name) {
        String path = "src/test/resources/config/" + name + ".properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Config file not found: " + path + " (skipping)");
        }
    }

    // ── Getters ────────────────────────────────────────────────────

    /**
     * Get a configuration value with precedence:
     * 1. System property (e.g. -Durpay.platform=ios)
     * 2. Environment variable (e.g. URPAY_PLATFORM=ios, dots→underscores, uppercase)
     * 3. Properties file value
     * 4. Empty string
     */
    public String get(String key) {
        return resolveWithPrecedence(key, "");
    }

    public String get(String key, String defaultValue) {
        return resolveWithPrecedence(key, defaultValue);
    }

    private String resolveWithPrecedence(String key, String defaultValue) {
        // 1. System property (urpay.xxx or exact key)
        String sysProp = System.getProperty("urpay." + key);
        if (sysProp == null) sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isEmpty()) return sysProp;

        // 2. Environment variable (URPAY_XXX_YYY, dots→underscores, uppercase)
        String envKey = "URPAY_" + key.replace(".", "_").toUpperCase();
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isEmpty()) return envVal;

        // 3. Properties file (resolve ${ENV_VAR} placeholders)
        String raw = properties.getProperty(key, defaultValue);
        return resolveEnvPlaceholders(raw);
    }

    private String resolveEnvPlaceholders(String value) {
        if (value == null || !value.contains("${")) return value;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}").matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String envName = m.group(1);
            String resolved = System.getenv(envName);
            m.appendReplacement(sb, resolved != null ? java.util.regex.Matcher.quoteReplacement(resolved) : "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public int getInt(String key, int defaultValue) {
        String val = resolveWithPrecedence(key, null);
        if (val == null || val.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        String val = resolveWithPrecedence(key, null);
        if (val == null || val.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = resolveWithPrecedence(key, null);
        if (val == null || val.isEmpty()) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    /**
     * Get a group of properties as a Map.
     * e.g. getMap("urpayUser") returns {id=..., mobileNumber=..., passCode=...}
     * from properties like urpayUser.id=xxx, urpayUser.mobileNumber=yyy
     */
    public Map<String, String> getMap(String prefix) {
        Map<String, String> map = new HashMap<>();
        String dotPrefix = prefix.endsWith(".") ? prefix : prefix + ".";
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(dotPrefix)) {
                String subKey = key.substring(dotPrefix.length());
                map.put(subKey, properties.getProperty(key));
            }
        }
        return map;
    }

    // ── Setters (for runtime state like balance) ───────────────────

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void set(String key, double value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public String getProfileName() {
        return profileName;
    }

    /**
     * Get the target environment (SIT, UAT).
     * Set via: -Denv=uat  or  env property in default.properties.
     */
    public String getEnv() {
        String env = resolveWithPrecedence("env", "SIT");
        return env.toUpperCase();
    }
}
