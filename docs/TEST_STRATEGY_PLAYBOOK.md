# 📖 Master Test Strategy Playbook (Interview Preparation Guide)

> **Role Focus**: Senior / Lead QA Automation Engineer / SDET  
> **Target Round**: Test Strategy & System Architecture (60 minutes)  
> **Key Tools**: VS Code, Postman / Bruno, SQL, Java 21, CI/CD

---

## ⏱️ 1. Interview Structure & Time Management

| Time Range | Focus Area | Candidate Objective |
| :--- | :--- | :--- |
| **00 - 15 min** | **RCA & Debugging** | Analyze documentation (`SPEC.md`), source code (`PlanService.java` / `plan.js`), and database tables to diagnose why the feature fails. |
| **15 - 25 min** | **Explain Root Cause & Fix** | Walk through the exact defects, present clean code fixes, and show how to validate them. |
| **25 - 45 min** | **Product Flow Test Design** | Design an end-to-end testing strategy (Test Pyramid, Edge Cases, Concurrency, Idempotency). |
| **45 - 55 min** | **SQL & Database Auditing** | Write and explain reconciliation queries, data integrity constraints, and anomaly detection scripts. |
| **55 - 60 min** | **CI/CD & Defect Prevention** | Explain the GitHub Actions pipeline, quality gates, and branch protection policies. |

---

## 🔍 2. Root Cause Analysis: The Key Defects

### Defect A: Penny Rounding Loss (Backend)
- **File**: `PlanService.java` -> `buildSchedule(...)`
- **Defective Code**:
  ```java
  BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(installments), 2, RoundingMode.DOWN);
  ```
- **Contract Violation (`SPEC.md`)**:
  > *"The sum of all installment amounts must equal the total exactly."*
- **Impact**:
  For a \$100.00 plan split into 3 installments (`PLAN-1001`), the service produces 3 installments of \$33.33 each. The sum is **\$99.99**. Exactly \$0.01 is lost, and the initial `remainingBalance` is out of balance by one cent.
- **Solution / Fix**:
  Calculate the remainder and add it to the final installment:
  ```java
  BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(installments), 2, RoundingMode.DOWN);
  BigDecimal remainder = totalAmount.subtract(baseAmount.multiply(BigDecimal.valueOf(installments)));
  // The final installment absorbs the remainder: baseAmount.add(remainder) -> 33.34
  ```

---

### Defect B: Client-Side State Tampering (Frontend UI)
- **File**: `/static/plan.js` -> `buildViewModel(...)`
- **Defective Code**:
  ```javascript
  var status = installment.paidAmount > 0 ? 'PAID' : installment.status;
  ```
- **Impact on `PLAN-1002`**:
  Installment #1 has `amount = 100.00` and `paidAmount = 40.00` (still owes \$60.00). The backend correctly returns `status = "PARTIAL"`. However, the frontend script overrides the status to **`PAID`** as soon as `paidAmount > 0`.
- **Solution / Fix**:
  Trust the state directly returned by the backend API (`status: installment.status`) or validate threshold:
  ```javascript
  var status = installment.paidAmount >= installment.amount ? 'PAID' : (installment.paidAmount > 0 ? 'PARTIAL' : installment.status);
  ```

---

## 🗄️ 3. SQL Essentials: Diagnostic & Reconciliation Queries

### 1. Detecting Mismatches Between Installment Sum and Total Plan Amount
```sql
SELECT p.id, p.total_amount, SUM(i.amount) AS installment_sum, (p.total_amount - SUM(i.amount)) AS difference
FROM plans p
JOIN installments i ON p.id = i.plan_id
GROUP BY p.id, p.total_amount
HAVING p.total_amount <> SUM(i.amount);
```

### 2. Detecting Inconsistent Status Records
```sql
SELECT * FROM installments
WHERE (paid_amount = 0 AND status <> 'PENDING')
   OR (paid_amount > 0 AND paid_amount < amount AND status <> 'PARTIAL')
   OR (paid_amount >= amount AND status <> 'PAID');
```

### 3. Reconciling Remaining Balance
```sql
SELECT p.id, p.remaining_balance, SUM(i.amount - i.paid_amount) AS calculated_real_balance
FROM plans p
JOIN installments i ON p.id = i.plan_id
GROUP BY p.id, p.remaining_balance
HAVING p.remaining_balance <> SUM(i.amount - i.paid_amount);
```

---

## 🏗️ 4. Test Design Across Layers

When asked to design comprehensive test suites for the product flow (detailed reference in [TESTING_CORE_CONCEPTS.md](file:///Users/user/Local/projects/selenium-java-template-bdd/docs/TESTING_CORE_CONCEPTS.md)):

```text
               ▲
              / \     UI / E2E (10%)
             /   \    - Selenium WebDriver / Playwright
            /-----\   - User flow: Login -> Plan Search -> Payment -> Status Display
           /       \  
          /---------\ API / Contract (30%)
         /           \- REST Assured / Bruno / OpenAPI
        /             \- HTTP status codes, schema validation, RBAC, CRUD endpoints
       /---------------\
      /                 \ Unit & Component (60%)
     /                   \- JUnit 5 / TestNG
    /---------------------\- Rounding logic, boundary math, state transition rules
```

### Test Case Matrix:
1. **Happy Path**: Standard plan creation, exact division amounts (\$300 / 3), on-time full card payments.
2. **Negative Cases**: Negative amounts, zero installments, invalid dates (`400 Bad Request`), non-existent IDs (`404 Not Found`), expired auth tokens (`401 Unauthorized`).
3. **Edge Cases**:
   - Inexact division amounts (\$100 / 3, \$50 / 6).
   - Micro-payments of \$0.01 cent.
   - Overpayments exceeding the remaining plan balance.
4. **Non-Functional Tests**: Concurrency / race conditions, Idempotency, and payment gateway downtime resilience.

---

## 🔒 5. Answers to Advanced Technical Interview Questions

### How is Idempotency Implemented and Tested?
- **Concept**: Ensure that sending the identical charge request multiple times produces the exact same outcome without charging the customer more than once.
- **Implementation**:
  1. The client sends a header: `Idempotency-Key: <UUID>`.
  2. The database stores keys in an `idempotency_keys` table with a `UNIQUE` constraint.
  3. A server-side filter intercepts incoming requests: if the key exists and succeeded previously, it immediately returns the cached response without re-executing the payment.
- **Testing**: Send 2+ identical POST requests with the same key; verify response payload match and that the database contains only one payment record.

### How are Race Conditions (Concurrency) Resolved?
- **Concept**: Two payment requests arriving within the exact same millisecond attempting to mutate the balance of the same installment.
- **Implementation**:
  - **Pessimistic Locking**: `SELECT * FROM installments WHERE id = ? FOR UPDATE` (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
  - **Optimistic Locking**: Add a `@Version` column to the entity. If the read version changed before transaction commit, Spring/Hibernate throws `OptimisticLockException` and triggers a rollback.
- **Testing**: Multi-threaded parallel test executions (via Java `ExecutorService` or k6) targeting the same entity.

### How are Payments Applied in Waterfall Fashion?
- According to `SPEC.md`: *"Payments apply to the oldest unpaid installment first"*.
- The payment service queries installments ordered by `due_date ASC`.
- If the customer pays \$150 and each installment is \$100:
  - Installment 1 receives \$100 ➔ status updated to `PAID`.
  - Installment 2 receives remaining \$50 ➔ status updated to `PARTIAL`.
  - Total plan remaining balance decreases by \$150 within a single `@Transactional` boundary.

### How is Nightly Batch Reconciliation Performed?
- A scheduled cron job (`@Scheduled(cron = "0 0 2 * * ?")`) runs accounting audit queries in the background.
- Compares `SUM(installments.paid_amount) == SUM(payments.amount)` and `SUM(installments.amount) == plan.total_amount`.
- If discrepancies are found, it triggers instant alerts (Slack / PagerDuty / Email) for manual financial review prior to bank settlement.

---

## 🚀 6. CI/CD & Prevention in the Pipeline (GitHub Actions)

To prevent bugs from resurfacing in production (*Shift-Left Prevention*):
1. **Shift-Left**: Automated tests executed on every Pull Request before merge.
2. **Pipeline Stages**:
   - `build-and-lint`: Syntax checks, dependency audit, and compilation.
   - `unit-tests`: Millisecond-level math and state machine tests.
   - `contract-tests`: REST Assured API tests validating status codes and OpenAPI schemas.
   - `ui-tests`: Headless Chrome E2E flows.
3. **Branch Protection Rules**:
   - Disallow direct pushes to `main`.
   - Require 100% passing status checks on the CI pipeline.
   - Require at least one peer code review approval.
