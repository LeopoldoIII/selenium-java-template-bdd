package com.template.pages;

import com.template.config.ConfigManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * BasePage containing reusable element interactions and explicit waits.
 */
public abstract class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitTimeout()));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public boolean waitForUrlContains(String partialUrl) {
        return wait.until(ExpectedConditions.urlContains(partialUrl));
    }

    public WebElement waitForVisibility(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public List<WebElement> waitForVisibilityOfAll(List<WebElement> elements) {
        return wait.until(ExpectedConditions.visibilityOfAllElements(elements));
    }

    public List<WebElement> waitForVisibilityOfAll(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public WebElement waitForClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void click(WebElement element) {
        waitForClickable(element).click();
    }

    public void click(By locator) {
        waitForClickable(locator).click();
    }

    public void type(WebElement element, String text) {
        WebElement el = waitForVisibility(element);
        el.clear();
        el.sendKeys(text);
    }

    public void type(By locator, String text) {
        WebElement el = waitForVisibility(locator);
        el.clear();
        el.sendKeys(text);
    }

    public String getText(WebElement element) {
        return waitForVisibility(element).getText().trim();
    }

    public String getText(By locator) {
        return waitForVisibility(locator).getText().trim();
    }

    public List<String> getTexts(List<WebElement> elements) {
        return waitForVisibilityOfAll(elements).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }

    public List<String> getTexts(By locator) {
        return waitForVisibilityOfAll(locator).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }

    public int getCount(By locator) {
        return driver.findElements(locator).size();
    }

    public int getCount(List<WebElement> elements) {
        return elements.size();
    }

    public void clickElementByText(List<WebElement> elements, String text) {
        WebElement target = waitForVisibilityOfAll(elements).stream()
                .filter(el -> el.getText().trim().equalsIgnoreCase(text.trim()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Element with text '" + text + "' not found in list"));
        scrollToElement(target);
        click(target);
    }

    public void clickElementByText(By locator, String text) {
        WebElement target = waitForVisibilityOfAll(locator).stream()
                .filter(el -> el.getText().trim().equalsIgnoreCase(text.trim()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Element with text '" + text + "' not found with locator " + locator));
        scrollToElement(target);
        click(target);
    }

    public boolean isDisplayed(WebElement element) {
        try {
            return waitForVisibility(element).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    public boolean isDisplayed(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    public void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
}
