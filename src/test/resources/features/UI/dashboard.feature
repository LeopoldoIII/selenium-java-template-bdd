@UI @dashboard
Feature: Installment Plans Dashboard Management

  Background:
    Given the user is logged into the dashboard

  @smoke
  Scenario: Create a new installment plan using composite step
    When the user creates an installment plan with ID "PLAN-UI-100", customer "Diana Prince", amount "3000.00" and "12" installments
    Then the plan "PLAN-UI-100" should be displayed in the installments table

  Scenario: Create a new installment plan using step-by-step form actions
    When the user enters plan ID "PLAN-UI-200"
    And the user enters customer name "Clark Kent"
    And the user enters total amount "1500.00"
    And the user enters installments "6"
    And the user clicks the create plan button
    Then the plan "PLAN-UI-200" should be displayed in the installments table

  Scenario: Pay an active installment plan
    When the user pays the installment plan "PLAN-101"
    Then the status of plan "PLAN-101" should be "PAID"

  Scenario: Delete an installment plan
    When the user deletes the plan "PLAN-103"
    Then the plan "PLAN-103" should disappear from the table

  Scenario: User sign out
    When the user clicks the logout button
    Then the user should be redirected to the login page
