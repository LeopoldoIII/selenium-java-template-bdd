@UI @login
Feature: User Authentication Web UI

  @smoke
  Scenario: Successful login with valid credentials
    Given the user navigates to the login page
    When the user logs in with email "testuser@example.com" and password "testpassword"
    Then the user should be redirected to the dashboard

  Scenario: Unsuccessful login with invalid credentials
    Given the user navigates to the login page
    When the user logs in with email "wronguser@example.com" and password "wrongpassword"
    Then an error message containing "Invalid username or password!" should be displayed
