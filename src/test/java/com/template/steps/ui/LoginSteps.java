package com.template.steps.ui;

import com.template.driver.DriverManager;
import com.template.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private LoginPage loginPage;

    private LoginPage getLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(DriverManager.getDriver());
        }
        return loginPage;
    }

    @Given("the user navigates to the login page")
    public void userNavigatesToLoginPage() {
        getLoginPage().open();
    }

    @When("the user enters email {string}")
    public void userEntersEmail(String email) {
        getLoginPage().enterUsername(email);
    }

    @When("the user enters password {string}")
    public void userEntersPassword(String password) {
        getLoginPage().enterPassword(password);
    }

    @When("the user clicks the login button")
    public void userClicksLoginButton() {
        getLoginPage().clickLogin();
    }

    @When("the user logs in with email {string} and password {string}")
    public void userLogsInWithCredentials(String email, String password) {
        getLoginPage().login(email, password);
    }

    @Then("the user should be redirected to the dashboard")
    public void userShouldBeRedirectedToDashboard() {
        Assert.assertTrue(getLoginPage().waitForUrlContains("/dashboard"),
                "User should be on dashboard page");
    }

    @Then("an error message containing {string} should be displayed")
    public void errorMessageShouldBeDisplayed(String expectedMessage) {
        Assert.assertTrue(getLoginPage().isErrorMessageDisplayed(), "Error alert should be visible");
        Assert.assertTrue(getLoginPage().getErrorMessage().contains(expectedMessage),
                "Error message should contain: " + expectedMessage);
    }
}
