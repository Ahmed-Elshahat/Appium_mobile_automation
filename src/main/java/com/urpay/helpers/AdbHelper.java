package com.urpay.helpers;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * SRP: ADB command execution helper.
 * Encapsulates all adb shell interactions — no other class should call Runtime.exec("adb ...").
 *
 * Replaces: ADBCommands.groovy (450 LOC) + CMDExecutor.groovy
 */
public final class AdbHelper {

    private static final Logger log = LoggerFactory.getLogger(AdbHelper.class);
    private static final int CMD_TIMEOUT_SECONDS = 10;

    private AdbHelper() {}

    // ── Text Input ─────────────────────────────────────────────────

    /**
     * Type text via adb shell (bypasses custom keypads).
     * Used for passcode entry, OTP, etc.
     */
    public static void inputText(String text) {
        executeAdb("input", "text", text);
    }

    /**
     * Send a keyevent via adb shell.
     * Common: 111 = ESCAPE, 66 = ENTER, 4 = BACK
     */
    public static void sendKeyEvent(int keyCode) {
        executeAdb("input", "keyevent", String.valueOf(keyCode));
    }

    // ── App Management ─────────────────────────────────────────────

    public static void forceStop(String packageName) {
        executeAdb("am", "force-stop", packageName);
    }

    public static void clearAppData(String packageName) {
        executeAdb("pm", "clear", packageName);
    }

    public static void launchApp(String packageName, String activity) {
        executeAdb("am", "start", "-n", packageName + "/" + activity);
    }

    // ── Device Control ─────────────────────────────────────────────

    public static void enableWifi() {
        executeAdb("svc", "wifi", "enable");
    }

    public static void disableWifi() {
        executeAdb("svc", "wifi", "disable");
    }

    public static void enableMobileData() {
        executeAdb("svc", "data", "enable");
    }

    public static void disableMobileData() {
        executeAdb("svc", "data", "disable");
    }

    public static void unlockDevice(String pin) {
        sendKeyEvent(26); // POWER to wake
        executeAdb("input", "swipe", "540", "1800", "540", "800");
        if (pin != null && !pin.isEmpty()) {
            inputText(pin);
            sendKeyEvent(66); // ENTER
        }
    }

    // ── Call Handling ──────────────────────────────────────────────

    public static void acceptCall() {
        sendKeyEvent(5); // CALL
    }

    public static void rejectCall() {
        sendKeyEvent(6); // ENDCALL
    }

    // ── Screen Recording ──────────────────────────────────────────

    public static Process startScreenRecord(String outputPath) {
        try {
            return new ProcessBuilder("adb", "shell", "screenrecord", outputPath)
                    .redirectErrorStream(true)
                    .start();
        } catch (Exception e) {
            log.warn("Failed to start screen recording: {}", e.getMessage());
            return null;
        }
    }

    public static void pullFile(String remotePath, String localPath) {
        executeCommand("adb", "pull", remotePath, localPath);
    }

    // ── Core Execution ─────────────────────────────────────────────

    /**
     * Execute an adb shell command with timeout.
     * @return command output
     */
    public static String executeAdb(String... args) {
        String[] cmd = new String[args.length + 2];
        cmd[0] = "adb";
        cmd[1] = "shell";
        System.arraycopy(args, 0, cmd, 2, args.length);
        return executeCommand(cmd);
    }

    /**
     * Execute any command with timeout.
     */
    public static String executeCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(CMD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("Command timed out: {}", String.join(" ", command));
            }

            return output.toString().trim();
        } catch (Exception e) {
            log.error("Command failed [{}]: {}", String.join(" ", command), e.getMessage());
            return "";
        }
    }
}
