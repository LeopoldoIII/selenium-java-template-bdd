package com.template.steps.ui;

import com.template.driver.DriverManager;
import com.template.pages.PlansViewPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class PlansViewSteps {

    private PlansViewPage plansViewPage;

    private PlansViewPage getPlansViewPage() {
        if (plansViewPage == null) {
            plansViewPage = new PlansViewPage(DriverManager.getDriver());
        }
        return plansViewPage;
    }

    @When("the user searches for plan {string}")
    public void userSearchesForPlan(String planId) {
        getPlansViewPage().searchPlan(planId);
    }

    @When("the user enters plan ID {string} and clicks load plan")
    public void userEntersPlanIdAndClicksLoadPlan(String planId) {
        getPlansViewPage().enterPlanId(planId).clickLoadPlan();
    }

    @Then("the plan details for {string} should be displayed")
    public void planDetailsForShouldBeDisplayed(String expectedPlanId) {
        Assert.assertTrue(getPlansViewPage().isPlanDetailDisplayed(), "Plan detail section should be displayed");
        Assert.assertEquals(getPlansViewPage().getDisplayedPlanId(), expectedPlanId, "Displayed Plan ID mismatch");
    }

    @Then("the plan status should be {string}")
    public void planStatusShouldBe(String expectedStatus) {
        Assert.assertEquals(getPlansViewPage().getDisplayedPlanStatus(), expectedStatus, "Displayed Plan Status mismatch");
    }

    @Then("the installments table should contain {int} installments")
    public void installmentsTableShouldContainInstallments(int expectedCount) {
        int actualCount = getPlansViewPage().getInstallmentsCount();
        Assert.assertEquals(actualCount, expectedCount, "Installments count mismatch in table");
    }

    @Then("the installments table should contain at least {int} installment(s)")
    public void installmentsTableShouldContainAtLeastInstallments(int minCount) {
        int actualCount = getPlansViewPage().getInstallmentsCount();
        Assert.assertTrue(actualCount >= minCount, "Expected at least " + minCount + " installments, but found " + actualCount);
    }

    @Then("the installment {int} status in UI should be {string}")
    @Then("the installment {int} status in UI is {string}")
    public void installmentStatusInUiShouldBe(int installmentNumber, String expectedStatus) {
        String actualStatus = getPlansViewPage().getInstallmentStatus(installmentNumber);
        Assert.assertEquals(actualStatus, expectedStatus, "Installment " + installmentNumber + " UI status mismatch");
    }

    @Then("the installment {int} paid amount in UI should be {string}")
    public void installmentPaidAmountInUiShouldBe(int installmentNumber, String expectedPaidAmount) {
        String actualPaid = getPlansViewPage().getInstallmentPaidAmount(installmentNumber);
        Assert.assertEquals(actualPaid, expectedPaidAmount, "Installment " + installmentNumber + " UI paid amount mismatch");
    }
}
