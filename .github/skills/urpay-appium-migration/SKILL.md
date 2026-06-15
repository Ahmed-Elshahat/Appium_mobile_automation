---
name: urpay-appium-migration
description: Generate Appium test automation code for URPay mobile app migration from Katalon. Use this skill whenever asked to create page objects, flows, test classes, or migrate Katalon test cases to Appium. Enforces zero Thread.sleep, SOLID principles, Allure reporting, and the project's established patterns. Always apply before generating any Java code in the urpay-mobile-tests project.
---

# urpay-appium-migration

Expert Appium test automation engineer skill for URPay mobile app. Generates clean, production-grade Java code following all established framework patterns.

## When to Use

- Creating new Page Objects for URPay screens
- Creating Flow classes (multi-page business workflows like LoginFlow, TransferFlow)
- Writing test classes for any URPay module
- Migrating Katalon test cases to Appium
- Adding helpers, utilities, or framework extensions
- Extracting locators from Katalon `.rs` Object Repository files

## Tech Stack (PINNED — never change versions without explicit approval)

- Java 17
- Appium Java Client 9.1.0
- Selenium 4.18.1 (pinned via dependencyManagement for Allure compatibility)
- TestNG 7.10.2
- Allure 2.29.1 + AspectJ 1.9.22.1
- Maven
- LambdaTest for cloud execution

---

## ABSOLUTE RULES (violating any of these fails review)

### Rule 1: ZERO Thread.sleep()
- **NEVER** use `Thread.sleep()` anywhere in any file — not in tests, not in pages, not in helpers, not "just temporarily"
- ALL waits go through `WaitUtils` (explicit waits via WebDriverWait + ExpectedConditions)
- For instant element checks without waiting, use `quickTap()` pattern: set `implicitlyWait(Duration.ZERO)`, call `driver.findElements()`, restore implicit wait
- For screen transitions, wait for the NEXT screen's element to appear — not an arbitrary delay

### Rule 2: No Assertions in Page Objects or Flows
- Assertions (`Assert.*`, `assertThat`, `assertTrue`) belong ONLY in test classes under `src/test/java/`
- Page objects return values (boolean, String) — the test decides what to assert
- Flow classes return page objects — the test asserts on the returned page

### Rule 3: No Hardcoded Values
- ALL credentials, device info, app paths, timeouts, and test data come from `.properties` files via `ConfigManager`
- Locators are defined as `private static final By` fields or `@AndroidFindBy` annotations — never inline strings in methods

### Rule 4: Private WebElement Fields
- ALL `@AndroidFindBy` / `@iOSXCUITFindBy` fields are `private` — never `public` or `protected`
- Exposed via public action methods with `@Step` annotation

### Rule 5: Allure Annotations Required
- Every Page Object action method: `@Step("description")`
- Every test method: `@Description("...")` + `@Severity(SeverityLevel.XXX)` + `groups`

### Rule 6: Locator Priority
1. `@AndroidFindBy(accessibility = "testID-xxx")` — first choice
2. `@AndroidFindBy(id = "com.urpay:id/xxx")` — second choice
3. `@AndroidFindBy(uiAutomator = "new UiSelector()...")` — third choice
4. `@AndroidFindBy(xpath = "...")` — LAST RESORT, always flag for testID ticket

### Rule 7: Keyboard Handling
- On LambdaTest Samsung devices: `((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.ENTER))` — taps "Done"
- `AndroidKey.ESCAPE` does NOT work on LambdaTest
- `driver.navigate().back()` navigates AWAY from forms — never use for keyboard dismiss
- `mobile: shell` commands are BLOCKED on LambdaTest

### Rule 8: Fast Element Discovery
- During skip/onboarding phases, set `driver.manage().timeouts().implicitlyWait(Duration.ZERO)` and use `driver.findElements()` for instant checks
- Always restore implicit wait after: `driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))`
- Never use `waitForClickable(loc, 1)` in loops — 1-second timeout × N locators × M iterations = slow

---

## PROJECT STRUCTURE (generate code ONLY inside these packages)

```
src/main/java/com/urpay/
├── core/           ← Framework engine (DO NOT MODIFY existing classes)
├── pages/          ← Page Objects (one class per screen)
│   ├── auth/       ← Login, OTP, Passcode, Landing
│   ├── dashboard/  ← Dashboard, MoreMenu, Settings
│   ├── wallet/     ← Wallet balance, Family wallet, Qatta
│   ├── remittance/ ← Local/International/Wallet transfer, Beneficiary
│   ├── payments/   ← Cards, SADAD, Telecom, Government, Takaful
│   └── dmp/        ← Products, Cart, Checkout, Orders
├── flows/          ← Multi-page business workflows (LoginFlow, TransferFlow)
├── helpers/        ← Device helpers (AdbHelper)
├── utils/          ← WaitUtils, SwipeUtils, ScreenshotUtils
├── reporting/      ← ReportManager (Allure wrapper)
├── model/          ← Data POJOs (UserData)
└── data/           ← TestDataProvider

src/test/java/com/urpay/tests/
├── auth/           ← Login, registration tests
├── dashboard/      ← Dashboard navigation tests
├── wallet/         ← Wallet/VAS tests
├── remittance/     ← Transfer tests
├── payments/       ← Cards, SADAD, Telecom tests
└── dmp/            ← Marketplace tests
```

---

## GOLDEN TEMPLATES (imitate these exactly)

### Golden Page Object

```java
package com.urpay.pages.payments;

import com.urpay.core.BasePage;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

public class CardsPage extends BasePage {

    @AndroidFindBy(accessibility = "testID-cards-list")
    private WebElement cardsList;

    @AndroidFindBy(accessibility = "testID-request-card-btn")
    private WebElement requestCardButton;

    @Step("Tap Request Card button")
    public void clickRequestCard() {
        tap(requestCardButton);
    }

    public boolean isLoaded() {
        return isDisplayed(cardsList, 10);
    }

    public String getCardCount() {
        return getText(cardsList);
    }
}
```

### Golden Flow (multi-page workflow)

```java
package com.urpay.flows;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class LoginFlow {

    private final AndroidDriver driver;
    private final WaitUtils waits;

    public LoginFlow() {
        this.driver = (AndroidDriver) DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
    }

    @Step("Full login: skip → credentials → OTP → passcode → dashboard")
    public DashboardPage loginWith(String mobile, String id, String otp, String passcode) {
        skipOnboarding();
        enterCredentials(mobile, id);
        enterOtp(otp);
        enterPasscode(passcode);
        return new DashboardPage();
    }
}
```

### Golden Test Class

```java
package com.urpay.tests.payments;

import com.urpay.core.BaseTest;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.CardsPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CardsTest extends BaseTest {

    @Test(groups = {"smoke", "payments"})
    @Description("Verify Mada card is displayed after login")
    @Severity(SeverityLevel.CRITICAL)
    public void testMadaCardVisible() {
        DashboardPage dashboard = new LoginFlow().loginDefault();
        dashboard.navigateToPayments();
        CardsPage cards = new CardsPage();
        Assert.assertTrue(cards.isLoaded(), "Cards list should be visible");
    }
}
```

---

## KATALON MIGRATION — STRICT FLOW EXTRACTION PROCESS

When migrating a Katalon test case to Appium, follow these steps **in exact order**. Do NOT skip or reorder.

### Step 1: Read the Katalon Groovy Script
- Find the test script under `Scripts/` in the Katalon project (e.g., `Scripts/PaymntAndCards/Telecom Recharge/validateZainRecharge/`)
- Read the `.groovy` file line by line
- Identify every `findTestObject()` call — each is a UI interaction step
- Identify `callTestCase()` calls — these are shared dependencies (login, navigation, setup)
- Note the **exact order** of steps — the Flow class must replicate this sequence

### Step 2: Read Every .rs File Referenced
- For each `findTestObject('Object Repository/android/...')`, locate the corresponding `.rs` file
- Extract the `content-desc` or `accessibilityID` value from `<webElementProperties>`
- If `content-desc` contains a `testID-` prefix, use `@AndroidFindBy(accessibility = "testID-xxx")`
- If no accessibility ID exists, check the `<locator>` tag for XPath (use as last resort)
- **Record every locator** — do not guess or assume element IDs

### Step 3: Read Shared/Dependency Scripts
- If the Katalon script calls `callTestCase("SharedSteps/ScrollToTelecomRechargeSection")`, read that script too
- Understand the full navigation path: how does Katalon get from Dashboard to the target screen?
- Common shared steps: login → deep link to home → search → tap service card → target page

### Step 4: Build the Flow — Match Katalon Step Order Exactly
- The Flow class method must call page object methods in the **same order** as the Katalon script
- Every `tap()` in Katalon → a page action method call in the Flow
- Every `sendKeys()` in Katalon → a page input method call in the Flow
- Every `swipe()` or `scrollToText()` in Katalon → a scroll/swipe call in the Flow
- Every `WebUI.delay()` or `Thread.sleep()` in Katalon → replace with `WaitUtils` wait for the NEXT element
- Every `verifyElementExist()` in Katalon → a boolean check method in the Page Object (assertion stays in Test)

### Step 5: Build the Page Object — One Locator Per .rs File
- Create `@AndroidFindBy` field for each `.rs` locator found in Step 2
- Create one action method per UI interaction (tap, type, getText)
- Group related locators with comments referencing the Katalon `.rs` file name

### Step 6: Build the Test — Assertions Only Here
- Call the Flow method (arrange + act)
- Assert on the result using values from the Katalon `verifyEqual` / `verifyElementExist` calls
- Use `@Description` annotation describing what the Katalon test case validates

### CRITICAL: Never Deviate from Katalon Flow Order
- If Katalon taps A → B → C, the Appium Flow must tap A → B → C
- If Katalon scrolls twice before tapping Next, the Flow must scroll twice before tapping Next
- If Katalon searches "Telecom Services" then taps a specific card, do exactly that — not a shortcut
- If Katalon uses `refreshThePage()` before re-order, add a swipe-down refresh in the Flow
- **Never add extra steps** that Katalon doesn't do (no extra scrolls, no extra waits, no "improvements")
- **Never remove steps** that Katalon does (even if they seem redundant)

---

## KATALON MIGRATION TRANSLATION TABLE

| Katalon Code | Appium Replacement |
|---|---|
| `Mobile.tap(findTestObject('path'), timeout)` | `tap(element)` in Page Object |
| `Mobile.setText(findTestObject('path'), text, timeout)` | `type(element, text)` in Page Object |
| `Mobile.getText(findTestObject('path'), timeout)` | `getText(element)` in Page Object |
| `Mobile.verifyElementExist(findTestObject('path'), timeout)` | `isDisplayed(element, timeout)` returns boolean |
| `Mobile.scrollToText(text)` | `scrollToText(text)` in BasePage |
| `Mobile.pressBack()` | `pressBack()` in BasePage |
| `Mobile.hideKeyboard()` | `dismissKeyboard()` via `pressKey(AndroidKey.ENTER)` |
| `Mobile.swipe(x1,y1,x2,y2)` | `swipeUtils.performSwipe(x1,y1,x2,y2)` |
| `WebUI.delay(seconds)` | FORBIDDEN — use WaitUtils to wait for next element |
| `CustomKeywords.'com.uspace.xxx'()` | Create a Flow class or helper method |
| `GlobalVariable.xxx` | `ConfigManager.getInstance().get("xxx")` |
| `findTestObject('Object Repository/android/Dev/...')` | `@AndroidFindBy(accessibility = "testID-xxx")` |
| `FailureHandling.STOP_ON_FAILURE` | Default behavior (test fails on exception) |
| `FailureHandling.CONTINUE_ON_FAILURE` | Wrap in try/catch, log warning |
| `FailureHandling.OPTIONAL` | Use `quickTap()` or `isDisplayed()` |

## LOCATOR EXTRACTION FROM KATALON .rs FILES

When reading Katalon Object Repository `.rs` XML files:

1. Look for `<webElementProperties>` where `<name>` is `accessibilityID` or `content-desc` — use as `@AndroidFindBy(accessibility = "value")`
2. Look for `<locator>` tag with `<locatorStrategy>XPATH</locatorStrategy>` — use as fallback only
3. **NEVER** copy XPath 1:1 from Katalon — the auto-generated absolute XPaths are fragile
4. If the `.rs` file's `accessibilityID` value is like `testID-SkipButton` but the actual device element has a different `content-desc`, use `xpath("//*[@text='Skip']")` as fallback

## COMMON PITFALLS TO AVOID

1. **Samsung keyboard won't dismiss** → Use `pressKey(AndroidKey.ENTER)`, never `ESCAPE` or `navigate().back()`
2. **Element not found after typing** → Dismiss keyboard BEFORE looking for next element
3. **Onboarding skip is slow** → Use `implicitlyWait(ZERO)` + `findElements()` pattern for instant checks
4. **OTP/Passcode entry** → Use `pressKey(AndroidKey.DIGIT_X)` for custom keypads, not `sendKeys()`
5. **Stale element after screen transition** → Re-find the element after navigation, don't reuse old references
6. **LambdaTest device allocation adds ~60-90s** → This is infrastructure overhead, not a code issue
