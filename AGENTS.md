# AGENTS.md - Repository Guidelines for selenium-java-template-bdd

This template is optimized for **speed, clarity, and live coding Cucumber BDD interviews (60 min)**.

## Project Summary
- **Type**: BDD Automation Testing Framework (Web UI & REST API)
- **Language**: Java 21
- **BDD Framework**: Cucumber Java 7 + TestNG
- **UI Automation**: Selenium WebDriver 4 + Page Object Model + WebDriverManager
- **API Automation**: REST Assured (`ApiClient`) + `ScenarioContext`
- **Config**: Multi-level (`-Dkey=value` > Env vars > `config.properties`)

## Key Commands
- Run Full Suite: `mvn test -Dtest=TestRunner -Dheadless=true`
- Run UI Tests Only: `mvn test -Dtest=UiTestRunner`
- Run API Tests Only: `mvn test -Dtest=ApiTestRunner`
- Run Single Feature: `mvn test -Dtest=UiTestRunner -Dcucumber.features="src/test/resources/features/UI/login.feature"`
- Run by Tag: `mvn test -Dcucumber.filter.tags="@smoke"`
