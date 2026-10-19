# Selenium & REST Assured BDD Automation Template

> **Optimized for Live Coding Technical Interviews (60 minutes)**  
> Built with **Java 21**, **Cucumber BDD 7**, **TestNG**, **Selenium WebDriver 4**, and **REST Assured**.

---

## 🚀 1. Preparing for the Live Coding Interview

### Step 1: Launch the Interview Application
```bash
# In a separate terminal tab:
java -jar installments.jar
```
*(Or if practicing locally with Docker: `docker compose up -d`)*

### Step 2: Configure Endpoints (if needed)
`src/main/resources/config.properties`:
```properties
baseUrl=http://localhost:8080
apiBaseUrl=http://localhost:8080/api
headless=false
```

---

## 🏃 2. How to Run Tests

### Run Everything (UI + API)
```bash
# Headless mode (fastest, ~13s):
mvn test -Dtest=TestRunner -Dheadless=true

# Real Chrome browser:
mvn test -Dtest=TestRunner
```

### Run by Category (UI vs API)
```bash
# Run UI Scenarios only (Chrome opens):
mvn test -Dtest=UiTestRunner

# Run API Scenarios only (No browser launched, ~2s):
mvn test -Dtest=ApiTestRunner
```

### Run a Single Feature File
```bash
# Run Login feature only:
mvn test -Dtest=UiTestRunner -Dcucumber.features="src/test/resources/features/UI/login.feature"

# Run Dashboard feature only:
mvn test -Dtest=UiTestRunner -Dcucumber.features="src/test/resources/features/UI/dashboard.feature"

# Run API feature only:
mvn test -Dtest=ApiTestRunner -Dcucumber.features="src/test/resources/features/API/installments_api.feature"

# Debug by tag 
mvn test -Dmaven.surefire.debug -Dcucumber.filter.tags="@debug"

``` 


### Run by Tag
```bash
# Run @smoke scenarios:
mvn test -Dcucumber.filter.tags="@smoke" -Dheadless=true

# Run @login scenarios:
mvn test -Dcucumber.filter.tags="@login"

# Run @plans scenarios:
mvn test -Dcucumber.filter.tags="@plans"
```

---

## 📊 3. Cucumber HTML Reports

After execution, open the generated HTML report in your browser:
- **Full Suite Report**: `open target/cucumber-reports.html`
- **UI Report**: `open target/cucumber-ui-reports.html`
- **API Report**: `open target/cucumber-api-reports.html`

*Note: In case of UI failures, full-page screenshots are automatically captured and embedded in the report.*

---

## 💡 4. How to Use ScenarioContext (Inspired by Python Behave)

To share values (e.g. auth tokens, response objects, plan IDs) across steps:

```java
// In Step 1: Save state
ScenarioContext.set("AUTH_TOKEN", token);

// In Step 2: Retrieve state
String token = ScenarioContext.get("AUTH_TOKEN");
Response response = ApiClient.get("/plans", token);
```

---

## 🎙️ 5. How to Defend Your Code ("Walk us through your code and decisions")

Use this 2-minute pitch:

1. **Clean Separation of Concerns**:
   - *"I organized the framework into 3 layers: Feature files define business behavior, Step Definitions handle glue code and state orchestration (`ScenarioContext`), and Page Objects (`BasePage`) encapsulate locators and web interactions."*

2. **Decoupled Hooks (`@UI` vs `@API`)**:
   - *"To prevent opening unnecessary Chrome browser instances during API tests, Selenium setup and teardown are bound exclusively to the `@UI` tag in `Hooks.java`. API tests execute headlessly in milliseconds."*

3. **Resilience & Zero Flakiness**:
   - *"All web interactions in `BasePage` rely on `WebDriverWait` explicit conditions. We completely avoid `Thread.sleep()`."*

4. **Failure Diagnostics**:
   - *"If any UI scenario fails, the `@After("@UI")` hook automatically captures a screenshot and embeds it directly into the Cucumber HTML report for rapid triaging."*

5. **Flexible Step Granularity**:
   - *"We support both composite business workflows (`When the user creates an installment plan with ID 'X', customer 'Y'...`) and granular field-by-field interactions (`When the user enters plan ID...`), giving flexibility based on scenario complexity."*
