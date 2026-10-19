package com.template.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for the Installments Dashboard.
 */
public class DashboardPage extends BasePage {

    @FindBy(id = "logoutButton")
    private WebElement logoutButton;

    @FindBy(id = "newPlanId")
    private WebElement planIdInput;

    @FindBy(id = "newCustomer")
    private WebElement customerInput;

    @FindBy(id = "newTotalAmount")
    private WebElement totalAmountInput;

    @FindBy(id = "newInstallments")
    private WebElement installmentsInput;

    @FindBy(id = "createPlanButton")
    private WebElement createPlanButton;

    @FindBy(id = "installmentsTable")
    private WebElement installmentsTable;

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDashboardLoaded() {
        return isDisplayed(installmentsTable);
    }

    public List<String> getAllPlanIds() {
        By planIdsLocator = By.xpath("//table[@id='installmentsTable']//td[contains(@class, 'plan-id')]");
        return getTexts(planIdsLocator);
    }

    public int getPlansCount() {
        By planRowsLocator = By.xpath("//table[@id='installmentsTable']//tbody//tr");
        return getCount(planRowsLocator);
    }

    public void createNewPlan(String planId, String customer, String totalAmount, String installments) {
        scrollToElement(createPlanButton);
        type(planIdInput, planId);
        type(customerInput, customer);
        type(totalAmountInput, totalAmount);
        type(installmentsInput, installments);
        click(createPlanButton);
        waitForPlanPresent(planId);
    }

    private By getPlanRowLocator(String planId) {
        return By.xpath("//table[@id='installmentsTable']//td[contains(@class, 'plan-id') and normalize-space()='" + planId + "']");
    }

    public boolean isPlanPresent(String planId) {
        try {
            return waitForPlanPresent(planId).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public WebElement waitForPlanPresent(String planId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(getPlanRowLocator(planId)));
    }

    public boolean waitForPlanToDisappear(String planId) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(getPlanRowLocator(planId)));
    }

    public void clickPayPlan(String planId) {
        waitForPlanPresent(planId);
        By payBtnLocator = By.id("pay-" + planId);
        WebElement payBtn = waitForClickable(payBtnLocator);
        scrollToElement(payBtn);
        jsClick(payBtn);
    }

    public void clickDeletePlan(String planId) {
        waitForPlanPresent(planId);
        By deleteBtnLocator = By.id("delete-" + planId);
        WebElement deleteBtn = waitForClickable(deleteBtnLocator);
        scrollToElement(deleteBtn);
        jsClick(deleteBtn);
    }

    public String getPlanStatus(String planId) {
        By statusLocator = By.xpath("//tr[td[normalize-space()='" + planId + "']]//span[contains(@class, 'badge')]");
        return getText(statusLocator);
    }

    public void logout() {
        scrollToElement(logoutButton);
        jsClick(logoutButton);
    }
}
