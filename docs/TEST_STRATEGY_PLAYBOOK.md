# 📖 Master Test Strategy Playbook (Interview Preparation Guide)

> **Role Focus**: Senior / Lead QA Automation Engineer  
> **Target Round**: Test Strategy & Architecture (60 minutes)  
> **Key Tools**: VS Code, Postman / Bruno, SQL, Java 21, CI/CD

---

## ⏱️ 1. Estructura de la Entrevista (Time Management)

| Minutos | Foco | Objetivo del Candidato |
| :--- | :--- | :--- |
| **00 - 15** | **RCA & Debugging** | Analizar Docs (`SPEC.md`), Código (`PlanService.java` / `plan.js`) y Tabla DB para identificar por qué falla el feature. |
| **15 - 25** | **Explicar Causa Raíz & Fix** | Demostrar el defecto exacto y dar la solución en código + cómo validarlo. |
| **25 - 45** | **Test Design for Product Flow** | Diseñar la estrategia de pruebas de punta a punta (Pirámide de pruebas, Casos Borde, Concurrencia, Idempotencia). |
| **45 - 55** | **SQL & Base de Datos** | Escribir / explicar consultas de conciliación, detección de inconsistencias y constraints. |
| **55 - 60** | **CI/CD & Prevención** | Explicar el pipeline de GitHub Actions, quality gates y políticas de branch protection. |

---

## 🔍 2. Root Cause Analysis: Los 3 Defectos Clave

### Defecto A: Pérdida de Centavos por Redondeo (Backend)
- **Archivo**: `PlanService.java` -> `buildSchedule(...)`
- **Código con Defecto**:
  ```java
  BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(installments), 2, RoundingMode.DOWN);
  ```
- **Violación del Contrato (`SPEC.md`)**:
  > *"The sum of all installment amounts must equal the total exactly."*
- **Impacto**:
  En un plan de $100.00 en 3 cuotas (`PLAN-1001`), genera 3 cuotas de $33.33. La suma da **$99.99**. Se pierde $0.01 centavo y el `remainingBalance` se inicializa descuadrado.
- **Solución / Fix**:
  Calcular el residuo y asignarlo a la última cuota:
  ```java
  BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(installments), 2, RoundingMode.DOWN);
  BigDecimal remainder = totalAmount.subtract(baseAmount.multiply(BigDecimal.valueOf(installments)));
  // La última cuota suma el residuo: baseAmount.add(remainder) -> 33.34
  ```

---

### Defecto B: Adulteración de Estado en Frontend (UI)
- **Archivo**: `/static/plan.js` -> `buildViewModel(...)`
- **Código con Defecto**:
  ```javascript
  var status = installment.paidAmount > 0 ? 'PAID' : installment.status;
  ```
- **Impacto en `PLAN-1002`**:
  La cuota #1 tiene `amount = 100.00` y `paidAmount = 40.00` (adeuda $60.00). El backend devuelve `status = "PARTIAL"`. Sin embargo, el script del cliente sobreescribe el estado a **`PAID`**.
- **Solución**:
  Confiar directamente en el estado provisto por el backend API (`status: installment.status`) o validar:
  ```javascript
  var status = installment.paidAmount >= installment.amount ? 'PAID' : (installment.paidAmount > 0 ? 'PARTIAL' : installment.status);
  ```

---

## 🗄️ 3. SQL Essentials: Consultas de Diagnóstico

### 1. Detección de Descuadre en Suma de Cuotas vs Total del Plan
```sql
SELECT p.id, p.total_amount, SUM(i.amount) AS suma_cuotas, (p.total_amount - SUM(i.amount)) AS diferencia
FROM plans p
JOIN installments i ON p.id = i.plan_id
GROUP BY p.id, p.total_amount
HAVING p.total_amount <> SUM(i.amount);
```

### 2. Detección de Estados Inconsistentes
```sql
SELECT * FROM installments
WHERE (paid_amount = 0 AND status <> 'PENDING')
   OR (paid_amount > 0 AND paid_amount < amount AND status <> 'PARTIAL')
   OR (paid_amount >= amount AND status <> 'PAID');
```

### 3. Conciliación de Saldo Restante (*Remaining Balance*)
```sql
SELECT p.id, p.remaining_balance, SUM(i.amount - i.paid_amount) AS saldo_real_calculado
FROM plans p
JOIN installments i ON p.id = i.plan_id
GROUP BY p.id, p.remaining_balance
HAVING p.remaining_balance <> SUM(i.amount - i.paid_amount);
```

---

## 🏗️ 4. Test Design Across Layers (Estrategia por Capas)

Cuando te pidan diseñar las pruebas para el flujo de producto:

```text
               ▲
              / \     UI / E2E (10%)
             /   \    - Selenium / Playwright
            /-----\   - Flujo de Login -> Búsqueda -> Pago -> Visualización
           /       \  
          /---------\ API / Contract (30%)
         /           \- REST Assured / Bruno / OpenAPI
        /             \- Status codes, schemas, RBAC, endpoints
       /---------------\
      /                 \ Unit & Component (60%)
     /                   \- JUnit 5 / TestNG
    /---------------------\- Lógica de redondeo, validación de montos, transiciones de estado
```

### Matriz de Casos de Prueba:
1. **Happy Path**: Compra normal, montos divisibles exactos ($300 / 3), pagos a tiempo con tarjeta.
2. **Negative Cases**: Montos negativos, cero cuotas, fechas inválidas (400 Bad Request), IDs inexistentes (404 Not Found), tokens expirados (401 Unauthorized).
3. **Edge Cases**:
   - Montos impares que no dividen exacto ($100 / 3, $50 / 6).
   - Abono de $0.01 centavo.
   - Sobrepago que excede el saldo restante.
4. **Casos No Funcionales**: Concurrencia, Idempotencia, Resiliencia ante caídas de pasarela de pago.

---

## 🔒 5. Respuestas a Preguntas Técnicas Avanzadas

### ¿Cómo se resuelve la Idempotencia?
- **Concepto**: Garantizar que enviar la misma solicitud de cobro múltiples veces produzca el mismo resultado sin cobrar dos veces.
- **Implementación**:
  1. El cliente envía un header `Idempotency-Key: <UUID>`.
  2. En la BD existe una tabla `idempotency_keys` con constraint `UNIQUE`.
  3. Un filtro intercepta la solicitud: si la clave ya existe y fue exitosa, devuelve la respuesta cacheada sin re-ejecutar el cobro.

### ¿Cómo se resuelven las Race Conditions (Concurrencia)?
- **Concepto**: Dos pagos que llegan en el mismo milisegundo intentando modificar la misma cuota.
- **Implementación**:
  - **Pessimistic Locking**: `SELECT * FROM installments WHERE id = ? FOR UPDATE` (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
  - **Optimistic Locking**: Columna `@Version` en la entidad. Si la versión leída cambió al hacer commit, Spring lanza `OptimisticLockException` y realiza rollback.

### ¿Cómo se aplican los pagos en Cascada (*Waterfall*)?
- Según `SPEC.md`: *"Payments apply to the oldest unpaid installment first"*.
- El servicio consulta cuotas ordenadas por `due_date ASC`.
- Si el usuario abona $150 y las cuotas son de $100:
  - Cuota 1 recibe $100 ➔ `PAID`.
  - Cuota 2 recibe $50 sobrantes ➔ `PARTIAL`.
  - Saldo total se reduce en $150. Todo dentro de una transacción `@Transactional`.

### ¿Cómo se hace la Reconciliación Batch (Nocturna)?
- Un job programado con `@Scheduled(cron = "0 0 2 * * ?")` ejecuta queries SQL de auditoría contable.
- Compara que `SUM(installments.paid_amount) == SUM(payments.amount)` y que `SUM(installments.amount) == plan.total_amount`.
- Si detecta inconsistencias, dispara alertas (Slack/PagerDuty/Email) para revisión antes del corte bancario.

---

## 🚀 6. CI/CD & Calidad en el Pipeline (GitHub Actions)

Para evitar que los bugs regresen a producción (*Prevention*):
1. **Shift-Left**: Pruebas automáticas ejecutadas en cada Pull Request.
2. **Etapas del Pipeline**:
   - `build-and-lint`: Chequeo de sintaxis y compilación.
   - `unit-tests`: Ejecución ultra-rápida de pruebas unitarias matemáticas.
   - `contract-tests`: Pruebas de API contra OpenAPI / Swagger.
   - `ui-tests`: Pruebas E2E en Chrome Headless.
3. **Branch Protection Rules**:
   - Bloquear merges directos a `main`.
   - Requerir que el pipeline de CI esté 100% en verde.
   - Requerir aprobación de al menos 1 Peer Reviewer.
