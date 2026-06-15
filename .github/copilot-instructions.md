# Copilot rules for urpay-mobile-tests

## Structure
- Generate code ONLY inside the existing packages. Never create new top-level packages.
- Imitate the golden templates: pages/LoginPage, flows/LoginFlow, tests/SmokeTest.

## Hard rules
- NEVER use Thread.sleep() anywhere. Use WaitUtils for all waits.
- NEVER hardcode credentials, URLs, app paths, timeouts, or test data. Read via ConfigManager.
- NEVER put assertions in page objects or flows. Tests only.
- NEVER put locators inside methods. Define as private fields at class top.
- All WebElement fields must be private with @AndroidFindBy.
- Waits via WaitUtils only. For instant checks use quickTap pattern (implicitlyWait ZERO + findElements).
- Locators: accessibility id first, then id, then uiAutomator; XPath only as last resort.
- Keyboard dismiss: pressKey(AndroidKey.ENTER) on Samsung/LambdaTest. Never ESCAPE or navigate().back().
- OTP/Passcode entry: pressKey(AndroidKey.DIGIT_X) for custom keypads.

## Code patterns
- Tests: arrange (LoginFlow/data) → act (flow/page) → assert (outcome). Max 5 lines of test logic.
- Pages: extend BasePage, private locators, @Step on actions, return values for queries.
- Flows: compose pages into journeys, return the landing page, @Step on public methods.

## Boundaries
- Do NOT add dependencies without approval.
- Do NOT reference or import from the Katalon project.
- Do NOT use DesiredCapabilities — use UiAutomator2Options / XCUITestOptions.
