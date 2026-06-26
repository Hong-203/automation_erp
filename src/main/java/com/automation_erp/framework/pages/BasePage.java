package com.automation_erp.framework.pages;

import com.automation_erp.framework.config.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getTimeout()));
    }

    // =====================================================================
    // Wait helpers
    // =====================================================================

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Chờ đến khi element biến mất (VD: loading spinner ẩn đi) */
    protected void waitForInvisibility(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Chờ đến khi text của element bằng đúng giá trị mong đợi */
    protected void waitForTextToBe(By locator, String expectedText) {
        wait.until(ExpectedConditions.textToBe(locator, expectedText));
    }

    // =====================================================================
    // Click & Input
    // =====================================================================

    protected void click(By locator) {
        waitForClickability(locator).click();
    }

    /** Click bằng JavaScript (dùng khi element bị che khuất) */
    protected void jsClick(By locator) {
        WebElement element = waitForVisibility(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void writeText(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    /** Xóa trắng field và gõ text, dùng khi clear() không hiệu quả */
    protected void clearAndWrite(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.sendKeys(Keys.CONTROL + "a");
        element.sendKeys(Keys.DELETE);
        element.sendKeys(text);
    }

    // =====================================================================
    // Read
    // =====================================================================

    protected String readText(By locator) {
        return waitForVisibility(locator).getText();
    }

    /** Lấy giá trị attribute của element (VD: value, href, class, placeholder...) */
    protected String getAttributeValue(By locator, String attribute) {
        return waitForVisibility(locator).getAttribute(attribute);
    }

    /** Lấy giá trị của input field (dùng getAttribute("value")) */
    protected String getInputValue(By locator) {
        return getAttributeValue(locator, "value");
    }

    // =====================================================================
    // Dropdown
    // =====================================================================

    /** Chọn option trong thẻ <select> theo visible text */
    protected void selectByVisibleText(By locator, String visibleText) {
        Select select = new Select(waitForVisibility(locator));
        select.selectByVisibleText(visibleText);
    }

    /** Chọn option trong thẻ <select> theo value attribute */
    protected void selectByValue(By locator, String value) {
        Select select = new Select(waitForVisibility(locator));
        select.selectByValue(value);
    }

    // =====================================================================
    // Scroll
    // =====================================================================

    /** Cuộn trang đến element bằng JavaScript */
    protected void scrollToElement(By locator) {
        WebElement element = waitForVisibility(locator);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    /** Cuộn đến bottom của trang */
    protected void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // =====================================================================
    // Visibility check
    // =====================================================================

    protected boolean isElementDisplayed(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Kiểm tra element có tồn tại trong DOM không (không cần visible) */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // =====================================================================
    // Alert handling
    // =====================================================================

    /** Chấp nhận (OK) alert/confirm dialog */
    protected void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }

    /** Hủy (Cancel) alert/confirm dialog */
    protected void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }

    /** Lấy text của alert */
    protected String getAlertText() {
        return wait.until(ExpectedConditions.alertIsPresent()).getText();
    }

    // =====================================================================
    // Frame handling
    // =====================================================================

    /** Switch vào iframe theo locator */
    protected void switchToFrame(By locator) {
        WebElement frame = waitForVisibility(locator);
        driver.switchTo().frame(frame);
    }

    /** Thoát khỏi iframe, trở về frame mặc định */
    protected void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }
}

