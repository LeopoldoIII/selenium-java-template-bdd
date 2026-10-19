package com.template.steps.ui;

import com.template.driver.DriverManager;
import com.template.pages.DashboardPage;
import com.template.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class DashboardSteps {

    private DashboardPage dashboardPage;

    // Temporal fields for granular step-by-step form filling
    private String tempPlanId;
    private String tempCustomer;
    private String tempAmount;
    private String tempInstallments;

    private DashboardPage getDashboardPage() {
        if (dashboardPage == null) {
            dashboardPage = new DashboardPage(DriverManager.getDriver());
        }
        return dashboardPage;
    }

    @Given("the user is logged into the dashboard")
    public void userIsLoggedIntoDashboard() {
        LoginPage loginPage = new LoginPage(DriverManager.getDriver());
        loginPage.open().login("testuser@example.com", "testpassword");
        Assert.assertTrue(getDashboardPage().isDashboardLoaded(), "Dashboard should be loaded");
    }

    // --- Granular step-by-step actions ---
    @When("the user enters plan ID {string}")
    public void userEntersPlanId(String planId) {
        this.tempPlanId = planId;
    }

    @When("the user enters customer name {string}")
    public void userEntersCustomerName(String customer) {
        this.tempCustomer = customer;
    }

    @When("the user enters total amount {string}")
    public void userEntersTotalAmount(String amount) {
        this.tempAmount = amount;
    }

    @When("the user enters installments {string}")
    public void userEntersInstallments(String installments) {
        this.tempInstallments = installments;
    }

    @When("the user clicks the create plan button")
    public void userClicksCreatePlanButton() {
        getDashboardPage().createNewPlan(tempPlanId, tempCustomer, tempAmount, tempInstallments);
    }

    // --- Composite (all-in-one) action ---
    @When("the user creates an installment plan with ID {string}, customer {string}, amount {string} and {string} installments")
    public void userCreatesPlanComposite(String planId, String customer, String amount, String installments) {
        getDashboardPage().createNewPlan(planId, customer, amount, installments);
    }

    @Then("the plan {string} should be displayed in the installments table")
    public void planShouldBeDisplayedInTable(String planId) {
        Assert.assertTrue(getDashboardPage().isPlanPresent(planId), "Plan " + planId + " should be in the table");
    }

    @When("the user pays the installment plan {string}")
    public void userPaysPlan(String planId) {
        getDashboardPage().clickPayPlan(planId);
    }

    @Then("the status of plan {string} should be {string}")
    public void statusOfPlanShouldBe(String planId, String expectedStatus) {
        String actualStatus = getDashboardPage().getPlanStatus(planId);
        Assert.assertEquals(actualStatus, expectedStatus, "Status of plan " + planId + " mismatch");
    }

    @When("the user deletes the plan {string}")
    public void userDeletesPlan(String planId) {
        getDashboardPage().clickDeletePlan(planId);
    }

    @Then("the plan {string} should disappear from the table")
    public void planShouldDisappearFromTable(String planId) {
        boolean disappeared = getDashboardPage().waitForPlanToDisappear(planId);
        Assert.assertTrue(disappeared, "Plan " + planId + " should be removed from table");
    }

    @When("the user clicks the logout button")
    public void userClicksLogoutButton() {
        getDashboardPage().logout();
    }

    @Then("the user should be redirected to the login page")
    public void userShouldBeRedirectedToLoginPage() {
        Assert.assertTrue(getDashboardPage().waitForUrlContains("/login"), "User should be on login page");
    }
}
