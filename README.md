# Appium Mobile Automation Project Handbook

## 1. Project Overview
This project is a sophisticated **Mobile Test Automation Framework** built using **Appium 2.x** and **Java 17**. It is designed to be scalable, maintainable, and thread-safe for parallel execution.

The framework implements the **Page Object Model (POM)** design pattern, separating test logic from UI interaction logic, ensuring that tests are readable and easy to maintain.

### Key Features
*   **Cross-Platform Ready**: Supports **Android** (UiAutomator2) and **iOS** (XCUITest).
*   **Auto-Server Management**: Automatically starts/stops the Appium Server via `AppiumServiceBuilder`.
*   **Deep Linking**: Utilities to navigate directly to app screens on both Android (`mobile: deepLink`) and iOS.
*   **CI/CD Ready**: Includes **GitHub Actions** pipeline for automated testing.
*   **Data-Driven**: Uses external **JSON** files to drive test scenarios.
*   **Rich Reporting**: Integrated **Allure Report** with **Video Recording** and screenshot-on-failure.
*   **Thread-Safe**: Uses `ThreadLocal` for driver management.
*   **Configuration Management**: Uses **Owner API** for type-safe property management (`config.properties`).

---

## 2. Technology Stack
*   **Language**: Java 17
*   **Core Engine**: Appium Java Client 8.5.1
*   **WebDriver**: Selenium 4.9.1
*   **Test Runner**: TestNG 7.8.0
*   **Reporting**: Allure Framework 2.24.0
*   **Data Parsing**: Jackson Databind 2.15.2 (JSON)
*   **Logging**: Log4j2
*   **Assertions**: AssertJ (Fluent Assertions)
*   **CI/CD**: GitHub Actions

---

## 3. Architecture & End-to-End Structure

The project follows a **Layered Architecture**:

### Layer 1: Configuration & Data
*   **Config**: `src/main/resources/config.properties`.
    *   **Android**: `platform.name=ANDROID`, `app.package`, `app.activity`.
    *   **iOS**: `platform.name=IOS`, `app.bundleId`, `platform.version` (e.g., 18.0).
*   **Test Data**: `src/test/resources/testdata/gmail_data.json`.
*   **Code**: `FrameworkConfig.java` (Interface) and `JsonUtils.java` (Parser).

### Layer 2: Core Engine (Drivers & Server)
*   **Location**: `com.appium.drivers`
*   **`DriverFactory`**:
    *   **Server**: Uses `AppiumServiceBuilder` to auto-start Appium on any free port.
    *   **Drivers**: Initializes `AndroidDriver` or `IOSDriver` based on config.
*   **`DriverManager`**: Uses `ThreadLocal<WebDriver>` for thread safety.

### Layer 3: Utilities
*   **Location**: `com.appium.utils`
*   **`DeepLinkUtils`**: Handles platform-specific deep linking commands.
*   **`WaitUtils`**: Wraps `WebDriverWait` for stability.
*   **`GestureUtils`**: Handles complex gestures (Scroll, Swipe).

### Layer 4: Page Objects (Business Logic)
*   **Location**: `com.appium.pages`
*   **`BasePage`**: Wrapper for standard interactions.
*   **`GmailWelcomePage`**: Setup Wizard logic.
*   **`GmailInboxPage`**: Inbox logic.

### Layer 5: Tests (Execution)
*   **Location**: `src/test/java/com/appium/tests`
*   **`BaseTest`**: Handles Setup (Driver+Video) and Teardown (Video Attachment).
*   **`GmailTests`**: Data-Driven tests.
*   **`IosSmokeTest`**: iOS Verification.

---

## 4. End-to-End Execution Flow

When you run `mvn clean test`:

1.  **Server Start**: `DriverFactory` checks if Appium is running. If not, it builds and starts a local service.
2.  **Driver Init**: `AndroidDriver` or `IOSDriver` is created using the local server URL.
3.  **Video Start**: Screen recording begins directly on the device.
4.  **Test Execution**:
    *   Tests read data from JSON (via DataProvider).
    *   Page Objects interact with the app.
    *   `DeepLinkUtils` may be used to jump to specific screens.
5.  **Teardown**:
    *   Video is stopped and attached to Allure.
    *   Driver is quit.
    *   **Server Stop**: Appium Service is gracefully shut down.

---

## 5. CI/CD & GitHub Actions

The project includes a pipeline in `.github/workflows/maven.yml`:
*   **Triggers**: Push/Pull Request to `main`.
*   **Environment**: `macos-latest` (supports iOS/Android).
*   **Steps**:
    1.  Checkout Code.
    2.  Setup JDK 17.
    3.  Install Appium & Drivers (npm).
    4.  Build Project.
    5.  Run Tests (Note: Fails gracefully if Emulators are missing on the runner).

---

## 6. How to Run

### 1. Configure Platform
Open `src/main/resources/config.properties`:
*   **Android**: Uncomment `platform.name=ANDROID`.
*   **iOS**: Uncomment `platform.name=IOS`.

### 2. Run Tests
```bash
# No need to start appium manually!
mvn clean test
```

### 3. Generate Report
```bash
mvn allure:report
./.allure/allure-2.24.0/bin/allure generate allure-results --clean --single-file -o target/allure-single-report
```

---

## 7. Known Limitations
*   **Deep Link**: iOS Deep Linking behavior varies by app implementation.
*   **Linting**: Automated linting (`Spotless`) is currently disabled due to incompatibility with **JDK 25**. Please use IDE formatting.
