---
name: import-reference-feature
description: >-
  Use this skill when the user asks to import, port, or adapt capabilities, utilities,
  or architectural patterns from the advanced reference project at /Users/usermone/local/projects/seleniumBDD
  into the current repository.
---

# Import Reference Feature Workflow

Use this procedural runbook to inspect the advanced reference project at `/Users/usermone/local/projects/seleniumBDD`, extract desired modules or features (e.g., Database Validations, Retry Listeners, Test Data Generators, Mobile Helpers, etc.), and adapt them cleanly into the current `selenium-java-template-bdd` project on demand.

---

## 1. Reference Project Context & Location

* **Reference Project Path**: `/Users/usermone/local/projects/seleniumBDD`
* **Current Project Path**: Current workspace (`/Users/usermone/local/projects/selenium-java-template-bdd`)

### Key Mapping Rules:
1. **Package Namespace Translation**:
   * Reference uses: `com.bdd.*` (e.g. `com.bdd.core.*`, `com.bdd.steps.*`)
   * Current project uses: `com.template.*` (e.g. `com.template.database.*`, `com.template.steps.database.*`)
2. **Configuration Translation**:
   * Reference uses `src/main/resources/config.json` via `ConfigReader.java`.
   * Current project uses `src/main/resources/config.properties` via `ConfigManager.java` (with `-Dkey=val` System properties and environment variable overrides).
3. **Driver & Context Management**:
   * Use current project's `DriverManager.getDriver()` and `ScenarioContext` (ThreadLocal).

---

## 2. Step-by-Step Porting Procedure

When asked to port or implement a capability from the reference project:

### Step 1: Locate Relevant Files in Reference Project
Use `run_command` with `find` or `grep` to inspect what the reference project contains:
```bash
find /Users/usermone/local/projects/seleniumBDD/src -iname "*<Keyword>*"
grep -rn "<Keyword>" /Users/usermone/local/projects/seleniumBDD/src/
```

### Step 2: Audit Dependencies in `pom.xml`
Check if the feature requires external Maven dependencies:
```bash
grep -A 10 -B 2 -i "<dependency-keyword>" /Users/usermone/local/projects/seleniumBDD/pom.xml
```
If needed, add the corresponding `<dependency>` to `pom.xml` in the current project.

### Step 3: Inspect & Adapt Core Classes
Read the reference file:
```bash
cat /Users/usermone/local/projects/seleniumBDD/src/main/java/com/bdd/.../<Class>.java
```
Adapt:
- Change package header to `com.template.<subpackage>`.
- Replace `ConfigReader.get(...)` with `ConfigManager.getProperty(...)` or dedicated typed getters.
- Ensure logger and exception handling adhere to template conventions.

### Step 4: Inspect & Adapt Step Definitions & Features
If the feature includes Cucumber steps or feature scenarios:
- Copy/adapt step definitions into `src/test/java/com/template/steps/<category>/<Name>Steps.java`.
- Adapt feature files into `src/test/resources/features/<Category>/<name>.feature`.

### Step 5: Verify Build & Test Compilation
Always run:
```bash
mvn test-compile
```
And verify that existing tests continue to pass without regression:
```bash
mvn test -Dtest=TestRunner -Dheadless=true
```

---

## 3. Catalog of Port-Ready Modules in `seleniumBDD`

| Module | Reference Files in `seleniumBDD` | Target in `selenium-java-template-bdd` |
| :--- | :--- | :--- |
| **Database Validations** | `com/bdd/core/database/DbManager.java`<br>`com/bdd/steps/database/DbSteps.java` | `com.template.database.DbManager`<br>`com.template.steps.database.DbSteps` |
| **Test Retry Analyzer** | `com/bdd/utils/RetryAnalyzer.java`<br>`com/bdd/utils/RetryListener.java`<br>`com/bdd/runners/FailedUiTestRunner.java` | `com.template.utils.RetryAnalyzer`<br>`com.template.utils.RetryListener` |
| **Test Data Utilities** | `com/bdd/utils/TestDataUtils.java` | `com.template.utils.TestDataUtils` |
| **File Management** | `com/bdd/utils/FileManager.java` | `com.template.utils.FileManager` |
| **Wait Helpers** | `com/bdd/utils/WaitHelper.java` | Integrated into `com.template.pages.BasePage` |
