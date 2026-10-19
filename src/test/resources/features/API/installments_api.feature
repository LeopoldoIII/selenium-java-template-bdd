@API @plans
Feature: Installments Backend REST API

  @smoke
  Scenario: User authentication via API
    When the API user authenticates with email "testuser@example.com" and password "testpassword"
    Then the response status code should be 200
    And the response should contain a valid JWT token

  Scenario: Fetch seeded installment plans
    Given the API user is authenticated
    When the API user requests all installment plans
    Then the response status code should be 200
    And the plans list should contain "PLAN-101"

  Scenario: Create and delete a plan via API
    Given the API user is authenticated
    When the API user creates a plan with ID "API-PLAN-999", customer "Bruce Wayne", amount 4500.00 and 9 installments
    Then the response status code should be 201
    When the API user deletes the plan "API-PLAN-999"
    Then the response status code should be 200
