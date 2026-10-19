package com.template.steps.api;

import com.template.api.ApiClient;
import com.template.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

public class InstallmentsApiSteps {

    private static final String CONTEXT_TOKEN = "AUTH_TOKEN";
    private static final String CONTEXT_RESPONSE = "LAST_RESPONSE";

    @When("the API user authenticates with email {string} and password {string}")
    public void apiUserAuthenticates(String email, String password) {
        Map<String, String> credentials = Map.of("username", email, "password", password);
        Response response = ApiClient.post("/auth/login", credentials);
        ScenarioContext.set(CONTEXT_RESPONSE, response);

        if (response.statusCode() == 200) {
            String token = response.jsonPath().getString("token");
            ScenarioContext.set(CONTEXT_TOKEN, token);
        }
    }

    @Given("the API user is authenticated")
    public void ensureApiUserIsAuthenticated() {
        if (!ScenarioContext.contains(CONTEXT_TOKEN)) {
            apiUserAuthenticates("testuser@example.com", "testpassword");
        }
    }

    @When("the API user requests all installment plans")
    public void apiUserRequestsAllPlans() {
        String token = ScenarioContext.get(CONTEXT_TOKEN);
        Response response = ApiClient.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/plans")
                .then()
                .extract().response();

        ScenarioContext.set(CONTEXT_RESPONSE, response);
    }

    @When("the API user creates a plan with ID {string}, customer {string}, amount {double} and {int} installments")
    public void apiUserCreatesPlan(String id, String customer, Double amount, Integer installments) {
        String token = ScenarioContext.get(CONTEXT_TOKEN);
        Map<String, Object> body = Map.of(
                "id", id,
                "customer", customer,
                "totalAmount", amount,
                "installments", installments,
                "status", "ACTIVE"
        );

        Response response = ApiClient.given()
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post("/plans")
                .then()
                .extract().response();

        ScenarioContext.set(CONTEXT_RESPONSE, response);
    }

    @When("the API user deletes the plan {string}")
    public void apiUserDeletesPlan(String planId) {
        String token = ScenarioContext.get(CONTEXT_TOKEN);
        Response response = ApiClient.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/plans/" + planId)
                .then()
                .extract().response();

        ScenarioContext.set(CONTEXT_RESPONSE, response);
    }

    @Then("the response status code should be {int}")
    public void responseStatusCodeShouldBe(int expectedStatusCode) {
        Response response = ScenarioContext.get(CONTEXT_RESPONSE);
        Assert.assertNotNull(response, "No API response found in ScenarioContext");
        Assert.assertEquals(response.statusCode(), expectedStatusCode, "Status code mismatch");
    }

    @Then("the response should contain a valid JWT token")
    public void responseShouldContainValidJwtToken() {
        String token = ScenarioContext.get(CONTEXT_TOKEN);
        Assert.assertNotNull(token, "JWT Token should not be null");
        Assert.assertFalse(token.isBlank(), "JWT Token should not be blank");
    }

    @Then("the plans list should contain {string}")
    public void plansListShouldContain(String expectedPlanId) {
        Response response = ScenarioContext.get(CONTEXT_RESPONSE);
        List<String> planIds = response.jsonPath().getList("id");
        Assert.assertTrue(planIds.contains(expectedPlanId), "Plans list should contain: " + expectedPlanId);
    }
}
