package com.template.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for Plans View Screen (/plans-view).
 * Generated following dom-to-page-object guidelines.
 */
public class PlansViewPage extends BasePage {

    @FindBy(id = "plan-id-input")
    private WebElement planIdInput;

    @FindBy(id = "load-plan-button")
    private WebElement loadPlanButton;

    @FindBy(id = "plan-loading")
    private WebElement planLoading;

    @FindBy(id = "plan-error")
    private WebElement planError;

    @FindBy(id = "plan-detail")
    private WebElement planDetailSection;

    @FindBy(id = "plan-id")
    private WebElement planIdDisplay;

    @FindBy(id = "plan-status")
    private WebElement planStatusDisplay;

    @FindBy(id = "plan-total")
    private WebElement planTotalDisplay;

    @FindBy(id = "plan-remaining")
    private WebElement planRemainingDisplay;

    @FindBy(id = "installments-table")
    private WebElement installmentsTable;

    @FindBy(id = "logout-link")
    private WebElement logoutLink;

    public PlansViewPage(WebDriver driver) {
        super(driver);
    }

    public PlansViewPage enterPlanId(String planId) {
        type(planIdInput, planId);
        return this;
    }

    public PlansViewPage clickLoadPlan() {
        click(loadPlanButton);
        return this;
    }

    /**
     * Composite action to search for a plan and wait for the async load to complete.
     */
    public void searchPlan(String planId) {
        enterPlanId(planId);
        clickLoadPlan();
        waitForPlanDetailToLoad();
    }

    public void waitForPlanDetailToLoad() {
        // Wait for detail section to become visible
        waitForVisibility(planDetailSection);
    }

    public boolean isPlanDetailDisplayed() {
        return isDisplayed(planDetailSection);
    }

    public String getDisplayedPlanId() {
        return getText(planIdDisplay);
    }

    public String getDisplayedPlanStatus() {
        return getText(planStatusDisplay);
    }

    public String getDisplayedTotal() {
        return getText(planTotalDisplay);
    }

    public String getDisplayedRemainingBalance() {
        return getText(planRemainingDisplay);
    }

    public int getInstallmentsCount() {
        By rowLocator = By.cssSelector("#installments-table tbody tr");
        return getCount(rowLocator);
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(planError);
    }

    public String getErrorMessage() {
        return getText(planError);
    }

    public String getInstallmentStatus(int installmentNumber) {
        By locator = By.xpath("//table[@id='installments-table']//tr[@data-number='" + installmentNumber + "']//td[contains(@class, 'status')]");
        return getText(locator);
    }

    public String getInstallmentAmount(int installmentNumber) {
        By locator = By.xpath("//table[@id='installments-table']//tr[@data-number='" + installmentNumber + "']//td[contains(@class, 'amount') and not(contains(@class, 'paid-amount'))]");
        return getText(locator);
    }

    public String getInstallmentPaidAmount(int installmentNumber) {
        By locator = By.xpath("//table[@id='installments-table']//tr[@data-number='" + installmentNumber + "']//td[contains(@class, 'paid-amount')]");
        return getText(locator);
    }

    public void logout() {
        click(logoutLink);
    }
}
