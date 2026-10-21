@UI @login
Feature: User Authentication Web UI

  @planTest
  Scenario: Successful login and list installment plan details
    Given the user navigates to the login page
    When the user logs in with email "candidate" and password "Aplazo2026!"
    Then the user should be redirected to the dashboard
    When the user searches for plan "PLAN-1001"
    Then the plan details for "PLAN-1001" should be displayed
    And the plan status should be "ACTIVE"

  Scenario: Unsuccessful login with invalid credentials
    Given the user navigates to the login page
    When the user logs in with email "wronguser@example.com" and password "wrongpassword"
    Then an error message containing "Invalid username or password." should be displayed

  @discrepancy @bug
  Scenario: Verify UI behavior for partially paid installment in PLAN-1002
    Given the user navigates to the login page
    When the user logs in with email "candidate" and password "Aplazo2026!"
    Then the user should be redirected to the dashboard
    When the user searches for plan "PLAN-1002"
    Then the plan details for "PLAN-1002" should be displayed
    And the installment 1 paid amount in UI should be "40.00"
    # Discrepancy Note: API returns status "PARTIAL", but UI displays "PAID" due to bug in plan.js (paidAmount > 0 ? 'PAID' : status)
    And the installment 1 status in UI is "PAID"

    
