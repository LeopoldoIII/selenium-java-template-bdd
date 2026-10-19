---
name: add-bdd-scenario
description: >-
  Use this skill when analyzing requirements to create BDD Cucumber scenarios or feature files for UI or API, automatically deriving positive, negative, and edge test cases, and optionally scaffolding full code (Page Objects, Step Definitions, ApiClient methods) or generating only the feature file based on user intent.
---

# Add BDD Scenario Workflow

Use this skill to convert requirements, user stories, acceptance criteria, or API/UI specifications into robust BDD Cucumber tests adhering to framework standards.

---

## 1. Intent & Mode Detection

Before generating anything, determine the **Target Mode** and **Target Layer** based on the user's prompt:

### A. Execution Mode
* **Feature Only (`feature-only`)**: The user explicitly asks for scenarios or feature file only (e.g., *"solo créame el feature file"*, *"solo los escenarios"*, *"draft Gherkin only"*).
  * *Action*: Generate only the `.feature` file (or append scenarios to an existing one). Do not create/modify Java step definitions or Page Objects.
* **Full Code (`full-code`)**: The user asks for complete implementation (e.g., *"crea todo el código"*, *"implement all steps and page objects"*, *"full automation"*) or asks to automate the requirement end-to-end.
  * *Action*: Create the feature file + Step Definitions + Page Objects (for UI) + ApiClient calls (for API) + verify test execution.

### B. Target Layer & File Location
* **UI Tests (`@UI`)**:
  * Feature files: `src/test/resources/features/UI/<feature_name>.feature`
  * Step Definitions: `src/test/java/com/template/steps/ui/<Name>Steps.java`
  * Page Objects: `src/main/java/com/template/pages/<Name>Page.java`
* **API Tests (`@API`)**:
  * Feature files: `src/test/resources/features/API/<feature_name>.feature`
  * Step Definitions: `src/test/java/com/template/steps/api/<Name>ApiSteps.java`
  * Client: `src/main/java/com/template/api/ApiClient.java`

### C. File Scope (Existing vs New Feature)
* Check if a feature file for this domain already exists (e.g., `login.feature`, `dashboard.feature`, `installments_api.feature`).
* If it exists, append new scenarios cleanly under existing features.
* If it is a new domain (e.g., `checkout.feature`, `profile.feature`, `customers_api.feature`), create a brand new `.feature` file with proper tags.

---

## 2. Requirement Analysis & Test Case Derivation Matrix

Analyze the requirement and automatically categorize test cases across three essential pillars:

| Test Pillar | UI Focus | API Focus | Typical Tags |
| :--- | :--- | :--- | :--- |
| **1. Positive (Happy Path)** | Valid input combinations, successful transitions, expected data rendering, dashboard counters updating. | Valid payloads, expected HTTP status (`200 OK`, `201 Created`, `204 No Content`), valid JSON schema and response headers. | `@smoke`, `@happy-path` |
| **2. Negative (Validations & Errors)** | Empty required fields, malformed input formats (e.g., invalid email, text in numeric field), unauthorized access, descriptive alert banner validation. | Missing required keys, invalid types, unauthorized calls (`401 Unauthorized`), forbidden roles (`403 Forbidden`), resource not found (`404 Not Found`), duplicate entities (`409 Conflict`). | `@negative` |
| **3. Edge Cases & Boundaries** | Boundary value extremes (0, negative, min/max length), special Unicode characters, rapid double-clicks, long strings, empty table states. | Boundary query params (page=0, negative limits), payload size boundaries, SQL/XSS injection attempts in strings, concurrent operations. | `@edge`, `@boundary` |

---

## 3. Step Reuse Audit (Always Check Before Writing Steps)

To maintain consistency and avoid duplication, always audit existing steps in:
* `src/test/java/com/template/steps/ui/`
* `src/test/java/com/template/steps/api/`

### Standard Reusable Catalog:
* **UI Navigation & Auth:**
  * `Given the user navigates to the login page`
  * `When the user logs in with email {string} and password {string}`
  * `Then the user should be redirected to the dashboard`
  * `Then an error message containing {string} should be displayed`
* **UI Table & Forms:**
  * `Then the installment plan {string} should be displayed in the list`
  * `When the user fills the installment form with:` (DataTable)
* **API General:**
  * `Given the API user is authenticated`
  * `Then the response status code should be {int}`
  * `And the response should contain {string}`
  * `And the response field {string} should be {string}`

---

## 4. Gherkin Standards & Best Practices

1. **Language**: **Strictly English** for all feature files, scenario names, and step texts.
2. **Tags**:
   * Top-level tags on Feature: `@UI` or `@API`, plus feature identifier (e.g. `@login`, `@plans`).
   * Scenario tags: `@smoke`, `@negative`, `@edge`, `@regression`.
3. **Declarative Business Language**: Focus on *intent* rather than UI clicks (prefer *"When the user submits valid registration details"* over *"When the user clicks id=btn-1 and enters id=input-2"*).
4. **Data Tables / Scenario Outlines**: Use `Scenario Outline` with `Examples:` for repetitive validation checks (e.g., negative input validation).

---

## 5. Code Scaffolding Rules (When `full-code` Mode Is Active)

When full code generation is required:

### A. Page Objects (`src/main/java/com/template/pages/`)
* Must extend `BasePage`.
* Use `@FindBy` annotations for element locators with robust CSS or ID selectors:
  ```java
  @FindBy(id = "customer-name")
  private WebElement customerNameInput;
  ```
* Rely on inherited `BasePage` methods (`click()`, `type()`, `getText()`, `waitForVisibility()`).
* **Never use `Thread.sleep()`**.

### B. Step Definitions (`src/test/java/com/template/steps/`)
* **UI Steps**: Instantiate the Page Object using `DriverManager.getDriver()`.
* **API Steps**: Use `ApiClient` methods directly (`ApiClient.get()`, `ApiClient.post()`).
* **State Management**: Use `ScenarioContext.set(key, value)` and `ScenarioContext.get(key)` to share objects, IDs, or responses across steps.
* **Assertions**: Use TestNG `Assert` or REST Assured `validatableResponse`.

---

## 6. Verification & Execution Commands

Always verify the newly generated scenario using the appropriate runner:

* Run specific feature:
  ```bash
  mvn test -Dtest=UiTestRunner -Dcucumber.features="src/test/resources/features/UI/<file>.feature"
  ```
  *(or for API: `-Dtest=ApiTestRunner -Dcucumber.features="src/test/resources/features/API/<file>.feature"`)*
* Run by tag:
  ```bash
  mvn test -Dcucumber.filter.tags="@<tag>" -Dheadless=true
  ```
