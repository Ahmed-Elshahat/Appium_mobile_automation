package com.urpay.pages.remittance;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Local Transaction (history) details — reached from Dashboard → Transactions →
 * tap the most recent transaction.
 *
 * Migrated from Katalon: Scripts/Remittance/LocalTran/ValidateSenderLocalTransactionHistory.
 *
 * A local (bank/IBAN) transfer is recorded as:
 *   - Type     = "Outgoing transfer"
 *   - Category = "Expense"
 *   - Beneficiary Name + the transferred amount among the label/value rows.
 *
 * The label/value rows are testID-label-value-main-N, but the index of a given field
 * varies by transaction type, so value assertions scan the rows (index-independent) and
 * compare digits-only to survive formatting like "60.00 SAR".
 */
public class LocalTransactionDetailsPage extends BasePage {

    /** Any label/value row on the details screen. */
    private static final By LABEL_VALUE_ROWS =
            AppiumBy.xpath("//*[contains(@content-desc,'testID-label-value')]");

    @AndroidFindBy(accessibility = "testID-main-firstRow-0")
    @iOSXCUITFindBy(accessibility = "testID-main-firstRow-0")
    private WebElement firstTransactionRow;

    @AndroidFindBy(accessibility = "testID-label-value-main-0")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-0")
    private WebElement firstDetailValue;

    @AndroidFindBy(accessibility = "testID-left-icon-item")
    @iOSXCUITFindBy(accessibility = "testID-left-icon-item")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  TRANSACTION LIST
    // ══════════════════════════════════════════════════

    @Step("Tap the most recent transaction")
    public void tapFirstTransaction() {
        tap(firstTransactionRow);
    }

    public boolean isTransactionListLoaded(long timeoutSec) {
        return isDisplayed(firstTransactionRow, timeoutSec);
    }

    // ══════════════════════════════════════════════════
    //  DETAILS — STATE QUERIES (no assertions)
    // ══════════════════════════════════════════════════

    public boolean isLoaded() {
        return isDisplayed(firstDetailValue, 15);
    }

    /** First detail row text (typically the Type) — for logging/diagnostics. */
    public String getFirstDetail() {
        return getText(firstDetailValue);
    }

    /** True if the details screen records a local transfer — labelled "Outgoing transfer". */
    public boolean isLocalTransferTransaction(long timeoutSec) {
        return hasTextAnywhere("Outgoing transfer", timeoutSec);
    }

    /** True if the transaction is categorised as an Expense. */
    public boolean isExpenseCategory(long timeoutSec) {
        return hasTextAnywhere("Expense", timeoutSec);
    }

    public boolean showsType(String type) {
        return hasTextAnywhere(type, 5);
    }

    public boolean showsCategory(String category) {
        return hasTextAnywhere(category, 5);
    }

    /** True if the beneficiary name appears (digit-insensitive contiguous match). */
    public boolean showsBeneficiaryName(String name) {
        return hasTextAnywhere(name, 5);
    }

    public boolean showsAmount(String amount) {
        return hasTextAnywhere(amount, 5) || showsDigits(amount);
    }

    /** True if any detail row contains the given digit sequence, ignoring spaces/formatting. */
    public boolean showsDigits(String digits) {
        String wanted = digits.replaceAll("\\D", "");
        if (wanted.isEmpty()) {
            return false;
        }
        List<WebElement> rows = driver.findElements(LABEL_VALUE_ROWS);
        for (WebElement row : rows) {
            try {
                String text = row.getText();
                if (text != null && text.replaceAll("\\D", "").contains(wanted)) {
                    return true;
                }
            } catch (Exception ignored) {
                // row went stale — skip
            }
        }
        return false;
    }

    /** True if ANY element's text/content-desc/label contains the substring (contiguous match). */
    private boolean hasTextAnywhere(String text, long timeoutSec) {
        return isPresent(AppiumBy.xpath(
                "//*[contains(@text,'" + text + "') or contains(@content-desc,'" + text + "') "
                + "or contains(@label,'" + text + "')]"), timeoutSec);
    }

    @Step("Tap back from transaction details")
    public void tapBack() {
        tap(backButton);
    }
}
