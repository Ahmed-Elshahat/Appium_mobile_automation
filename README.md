# URPay Mobile Tests — Appium + Java + TestNG

Automated mobile test suite for the **URPay Consumer** Android app (SIT environment), migrated from Katalon to Appium.

## Tech Stack

| Component | Version |
|-----------|---------|
| Java | 17 |
| Appium Java Client | 9.1.0 |
| Selenium | 4.18.1 |
| TestNG | 7.10.2 |
| Allure Reporting | 2.29.1 |
| AspectJ (Allure weaving) | 1.9.22.1 |
| Cloud Execution | LambdaTest |

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- LambdaTest account (for cloud execution) or local Appium 2.x setup

### Environment Variables (LambdaTest)
```bash
export LT_USERNAME=your_username
export LT_ACCESS_KEY=your_access_key
```

### Run Tests

```bash
# Smoke test (local)
mvn clean test -Dsuite=suites/smoke.xml -Dprofile=default

# Telecom Recharge — all providers in parallel (3 threads)
mvn clean test -Dsuite=suites/telecom-recharge.xml -Dprofile=sit-cards

# Single provider
mvn clean test -Dsuite=suites/zain-only.xml -Dprofile=sit-cards
mvn clean test -Dsuite=suites/mobily-only.xml -Dprofile=sit-cards
mvn clean test -Dsuite=suites/stc-only.xml -Dprofile=sit-cards
```

### Generate Allure Report

```bash
# Install Allure CLI first, then:
allure generate allure-results --single-file -o target/allure-single-report --clean
```

#### Trend-aware report (recommended)

The one-liner above produces a single run with no history. To keep the **trend graph,
flaky detection, and retries** across runs, use the helper script — it persists Allure
history in a git-ignored `.allure-history/` and feeds it back on every generate:

```powershell
powershell -ExecutionPolicy Bypass -File tools/gen-allure-report.ps1
```

Outputs `target/allure-report` (full, with trend) and `target/allure-single-report/index.html`
(shareable). Run it after each `mvn test` to accumulate history.

## Project Structure

```
src/main/java/com/urpay/
├── core/           ← DriverFactory, BasePage, BaseTest, ConfigManager
│                     RetryAnalyzer, TestExecutionListener, CloudSessionManager
├── pages/          ← Page Objects (one class per screen)
│   ├── auth/       ← LandingPage, LoginPage, OtpPage, PasscodePage
│   ├── dashboard/  ← DashboardPage, SearchPage
│   ├── payments/   ← TelecomRechargePage (+ future: Cards, SADAD, Gov)
│   ├── wallet/     ← (planned)
│   ├── remittance/ ← (planned)
│   └── dmp/        ← (planned)
├── flows/          ← Multi-page business workflows
│   ├── LoginFlow   ← Onboarding → credentials → OTP → passcode → dashboard
│   └── TelecomRechargeFlow ← Zain / Mobily / STC recharge + order history + re-order
├── helpers/        ← AdbHelper
├── utils/          ← WaitUtils, SwipeUtils, ScreenshotUtils
├── reporting/      ← ReportManager (Allure wrapper)
├── model/          ← UserData
└── data/           ← TestDataProvider

src/test/java/com/urpay/tests/
├── auth/           ← SmokeTest (login verification)
└── payments/       ← TelecomRechargeTest (Zain/Mobily/STC — 9 test cases)

src/test/resources/
├── config/         ← Profile-based .properties (default, sit-cards, sit-wmv, sit-remittance)
├── suites/         ← TestNG XML suites (smoke, telecom-recharge, provider-specific)
└── testdata/       ← (planned: Excel/CSV data files)
```

## Test Modules

### Telecom Recharge (Complete)

| Test | Provider | Status |
|------|----------|--------|
| Recharge | Zain | ❌ SIT bug (Service Unavailable) |
| Order History | Zain | ⏭️ Skipped (depends on recharge) |
| Re-order | Zain | ⏭️ Skipped (depends on recharge) |
| Recharge | Mobily | ✅ Passing |
| Order History | Mobily | ✅ Passing |
| Re-order | Mobily | ✅ Passing |
| Sawa Recharge | STC | ✅ Passing |
| Sawa Re-order | STC | ✅ Passing |
| QuickNet Recharge | STC | ❌ SIT bug (Transaction Declined) |

### Planned Modules
- Cards (Mada card management)
- SADAD Bills
- Government Services
- Wallet / Family Wallet
- Remittance (Local / International)
- DMP Marketplace

## Architecture Principles

- **Page Object Model** — one class per screen, `@AndroidFindBy` locators
- **Flow Pattern** — multi-page business workflows (LoginFlow, TelecomRechargeFlow)
- **Zero Thread.sleep()** — all waits via explicit `WaitUtils`
- **SOLID** — Single Responsibility, Open/Closed, Dependency Inversion
- **Allure `@Step` weaving** — AspectJ for automatic step tracking
- **Parallel execution** — TestNG `parallel="tests"` with thread-safe DriverFactory
- **Profile-based config** — `ConfigManager` loads environment-specific `.properties`

## Copilot Skills

This project includes AI coding assistant skills in `.github/skills/`:

| Skill | Purpose |
|-------|---------|
| `urpay-appium-migration` | Generate Appium code from Katalon migration — locator extraction, golden templates, translation table |
| `jiraplanning` | Transform Jira tickets into QA test strategies with testable requirements |

## Copilot Instructions

Framework coding rules are defined in `.github/copilot-instructions.md` — enforces zero Thread.sleep, SOLID principles, locator priority, keyboard handling patterns, etc.
