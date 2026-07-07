package com.urpay.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Picks the next new-passcode for the Forgot/Change-Passcode journeys so repeated runs never reuse
 * a value the wallet still remembers ("Your new passcode must be different from the ones you used
 * before"). The last-used pool index per account is persisted to a small git-ignored state file
 * under {@code target/}, and each run advances to the next value in the configured pool.
 */
public final class PasscodeRotation {

    private static final Logger log = LoggerFactory.getLogger(PasscodeRotation.class);
    private static final Path STATE_FILE = Paths.get("target", "passcode-rotation.properties");

    private PasscodeRotation() {
    }

    /**
     * Return the next passcode from {@code pool} for {@code accountKey}, advancing and persisting
     * the rotation index. Best-effort: if the state file cannot be read/written the rotation still
     * returns a value (it may repeat), so a passcode is always produced.
     */
    public static synchronized String next(String accountKey, List<String> pool) {
        if (pool == null || pool.isEmpty()) {
            throw new IllegalArgumentException("Passcode pool must not be empty");
        }
        Properties state = load();
        int last = parseIndex(state.getProperty(accountKey));
        int next = (last + 1) % pool.size();
        state.setProperty(accountKey, Integer.toString(next));
        save(state);
        String passcode = pool.get(next);
        log.info("Rotated new passcode for {} -> pool index {}", accountKey, next);
        return passcode;
    }

    private static int parseIndex(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Properties load() {
        Properties props = new Properties();
        if (Files.exists(STATE_FILE)) {
            try (InputStream in = Files.newInputStream(STATE_FILE)) {
                props.load(in);
            } catch (IOException e) {
                log.warn("Could not read passcode-rotation state ({}) — starting fresh", e.getMessage());
            }
        }
        return props;
    }

    private static void save(Properties props) {
        try {
            if (STATE_FILE.getParent() != null) {
                Files.createDirectories(STATE_FILE.getParent());
            }
            try (OutputStream out = Files.newOutputStream(STATE_FILE)) {
                props.store(out, "Forgot/Change passcode rotation state (git-ignored)");
            }
        } catch (IOException e) {
            log.warn("Could not persist passcode-rotation state ({}) — rotation may repeat", e.getMessage());
        }
    }
}
