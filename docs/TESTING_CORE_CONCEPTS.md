# 🧠 Core Test Strategy Concepts

> **Quick Reference Guide for QA Automation / SDET Technical Interviews**  
> Covers essential test design topics for end-to-end and product flows: **Test Pyramid**, **Edge Cases**, **Concurrency**, and **Idempotency**.

---

## 📐 1. The Test Pyramid

Popularized by Mike Cohn and Martin Fowler, the Test Pyramid defines the optimal proportion of automated tests across layers based on execution speed, maintainability cost, and feedback cycle.

```text
               ▲
              / \     E2E / UI (~10%)
             /   \    - Tools: Selenium, Playwright, Cypress
            /-----\   - Scope: Critical end-to-end user journeys
           /       \  - Characteristics: Slowest, flakiest, highest maintenance cost
          /---------\ 
         /           \ Integration / API (~30%)
        /             \- Tools: REST Assured, Postman, Bruno, MockMvc
       /---------------\- Scope: Inter-service communication, endpoints, contracts, DB state
      /                 \
     /                   \ Unit & Component (~60%)
    /---------------------\- Tools: JUnit 5, TestNG, Mockito
                           \- Scope: Pure business logic, edge math, individual classes/methods
                           \- Characteristics: Blazing fast (milliseconds), isolated, lowest cost
```

### ⚠️ Critical Interview Clarification: Where Does "Regression Testing" Fit?
* **Common Mistake**: Stating that regression testing is the top layer of the pyramid.
* **Accurate Answer**: Regression testing **is not an architectural layer**; it is an **execution purpose or test suite scope**. A regression suite combines unit, integration, and E2E tests executed periodically (or per PR) to prove that recent code changes did not break existing functionality.

---

## 🔍 2. Edge Cases

### Definition
An **edge case** is a test scenario that occurs at the **extreme boundaries** of operating parameters, inputs, or system states (minimums, maximums, precision limits, unusual transitions, or null/empty payloads).

### Why Are Edge Cases Critical?
Developers naturally optimize and write tests for the *Happy Path*. The vast majority of production outages, data corruptions, and financial discrepancies (e.g., penny rounding losses, integer overflows, `NullPointerException`) occur precisely at boundary conditions.

### Practical Examples in Installment / Payment Systems:

| Boundary Type | Business Scenario | Expected System Behavior |
| :--- | :--- | :--- |
| **Inexact Division** | Splitting \$100.00 across 3 installments. | Installments of \$33.33, \$33.33, and \$33.34. The sum must strictly equal \$100.00. |
| **Minimum Amount** | Micro-payment or payment of \$0.01 cent. | Successfully processed without rounding down to zero. |
| **Zero & Empty** | Installments = 0, empty string `""`, empty list `[]`, `null` payload. | Rejected immediately with `400 Bad Request` and clear validation messages. |
| **Overpayment** | Remaining balance is \$50.00; user attempts to pay \$50.01 or \$1,000.00. | Rejected by business rules or allocated to account credit depending on spec. |
| **Calendar Boundaries** | Due date generated on February 29th (leap year) or Daylight Saving Time transition. | Correct `due_date` calculation without date arithmetic exceptions. |

---

## ⚡ 3. Concurrency & Race Conditions

### Definition
A **race condition** occurs when **two or more concurrent requests, threads, or users attempt to read and mutate the same shared resource simultaneously (within the exact same millisecond)**. Without proper synchronization or locking mechanisms, one transaction overwrites the other, causing severe data and financial corruption.

```text
User 1: Reads balance ($100) ------> Withdraws $100 ------> Final balance: $0
                                  ▲
User 2: Reads balance ($100) -----/ (Simultaneously) ------> Withdraws $100 ------> Final balance: $0 ?? ($100 double-spent!)
```

### Prevention Mechanisms (Architecture & Database)

1. **Pessimistic Locking**:
   * The database locks the row during the read query so no other transaction can read or modify it until the first transaction commits or rolls back:
     ```sql
     SELECT * FROM installments WHERE id = ? FOR UPDATE;
     ```
   * In JPA/Hibernate: `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

2. **Optimistic Locking**:
   * The entity table contains a `version INT` column.
   * When committing changes, the DB executes:
     ```sql
     UPDATE installments SET balance = ?, version = version + 1 WHERE id = ? AND version = 1;
     ```
   * If another transaction already incremented the version, the update affects 0 rows, triggering an `OptimisticLockException` and causing an automatic rollback.

### How to Automate & Test Concurrency:
* Launch 10 to 50 parallel requests targeting the exact same resource using Java's multithreaded `ExecutorService`, JMeter, or k6.
* Assert that only one transaction successfully mutates state while all other concurrent requests are safely rejected or queued, preserving balance integrity.

---

## 🔄 4. Idempotency

### Definition
**Idempotency** is the property of an operation whereby **executing it once or executing it multiple times with the exact same parameters yields the identical outcome**, without unintended side effects (such as charging a customer twice).

### In HTTP / REST APIs:
* **Naturally Idempotent**: `GET`, `PUT`, `DELETE`, `HEAD`, `OPTIONS`.
* **Non-Idempotent by Default**: `POST` (creates a new resource on each execution unless explicitly handled).

### Why Is Idempotency Critical?
If a user double-clicks "Submit Payment", or if a mobile client suffers a network drop and automatically retries the payment request, **the customer must never be double-charged**.

```text
[Client] --- POST /payments (Header: Idempotency-Key: abc-123) ---> [Backend API]
                                                                          │
                                                      Does 'abc-123' exist in DB?
                                                      ├── NO: Process charge, save key & response, return 201 Created.
                                                      └── YES: Return cached original response (200 OK) WITHOUT re-charging.
```

### Standard Implementation Pattern:
1. **Request Header**: The client generates a unique UUID and attaches it to the request: `Idempotency-Key: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d`.
2. **Database Table**: The backend records the key in an `idempotency_keys` table with a `UNIQUE` index constraint.
3. **Interceptor / Filter**: If the key already exists and was previously processed, the API returns the cached response immediately, bypassing payment execution.

### How to Automate & Test Idempotency:
* Send the identical payment payload with the same `Idempotency-Key` header two or three times consecutively.
* **Assertions**:
  * First request: Returns `201 Created` (or `200 OK`) with generated transaction ID.
  * Subsequent requests: Return `200 OK` with the exact same transaction ID and response body.
  * Database query: Query `payments` table and verify exactly ONE payment record exists for that key.

---

## 📋 Interview Quick Reference Cheat Sheet

| Concept | One-Sentence Pitch | Key Example | How to Validate in Automation |
| :--- | :--- | :--- | :--- |
| **Test Pyramid** | Balanced distribution of test coverage (60% unit, 30% API, 10% UI). | Regression is an execution scope across layers, not a single tier. | Monitor pipeline stage runtimes and test counts in CI. |
| **Edge Cases** | Tests validating extreme boundary conditions of domain inputs. | Penny rounding when dividing \$100 into 3 installments (\$33.34 on last installment). | Parameterized unit and API tests with boundary value partitions. |
| **Concurrency** | Multiple parallel requests racing to mutate the same resource. | Two simultaneous withdrawals against the same remaining balance. | Multithreaded stress/race tests and DB locks (`FOR UPDATE` / `@Version`). |
| **Idempotency** | Repeating the same operation never duplicates business side effects. | Payment retries due to network timeouts using `Idempotency-Key`. | Consecutive API requests using the same key; verify single DB mutation. |
