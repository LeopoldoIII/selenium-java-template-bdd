-- ==============================================================================
-- SQL Interview Cheat Sheet: Installment Plans Reconciliation & Diagnostic
-- Use these queries in H2 Console (http://localhost:8080/h2-console), DBeaver, or CLI
-- ==============================================================================

-- 1. DETECTAR DESCUADRE DE MONTOS (Suma de cuotas <> Monto total del plan)
-- Falla reportada: Pérdida de centavos por redondeo (e.g. 100/3 = 99.99)
SELECT 
    p.id AS plan_id, 
    p.total_amount, 
    SUM(i.amount) AS suma_cuotas, 
    (p.total_amount - SUM(i.amount)) AS centavos_perdidos
FROM installment_plans p
JOIN installments i ON p.id = i.plan_id
GROUP BY p.id, p.total_amount
HAVING p.total_amount <> SUM(i.amount);

-- 2. DETECTAR ESTADOS INCONSISTENTES DE CUOTAS
-- Falla reportada: Cuotas marcadas como 'PAID' que tienen saldo adeudado
SELECT 
    i.id,
    i.plan_id,
    i.number,
    i.amount,
    i.paid_amount,
    i.status AS estado_actual,
    CASE 
        WHEN i.paid_amount = 0 THEN 'PENDING'
        WHEN i.paid_amount >= i.amount THEN 'PAID'
        ELSE 'PARTIAL'
    END AS estado_esperado
FROM installments i
WHERE (i.paid_amount = 0 AND i.status <> 'PENDING')
   OR (i.paid_amount > 0 AND i.paid_amount < i.amount AND i.status <> 'PARTIAL')
   OR (i.paid_amount >= i.amount AND i.status <> 'PAID');

-- 3. CONCILIACIÓN DE SALDO RESTANTE (Remaining Balance)
-- Verifica que lo que dice el plan que se debe coincida con la suma matemática adeudada
SELECT 
    p.id AS plan_id,
    p.remaining_balance AS saldo_en_plan,
    SUM(i.amount - i.paid_amount) AS saldo_calculado,
    ABS(p.remaining_balance - SUM(i.amount - i.paid_amount)) AS descuadre
FROM installment_plans p
JOIN installments i ON p.id = i.plan_id
GROUP BY p.id, p.remaining_balance
HAVING p.remaining_balance <> SUM(i.amount - i.paid_amount);

-- 4. CONCILIACIÓN DE PAGOS REGISTRADOS VS CUOTAS
-- Verifica que el dinero ingresado por pasarela/transacciones coincida con lo abonado
SELECT 
    p.id AS plan_id,
    COALESCE(SUM(pay.amount), 0) AS total_pagos_recibidos,
    SUM(i.paid_amount) AS total_abonado_a_cuotas
FROM installment_plans p
JOIN installments i ON p.id = i.plan_id
LEFT JOIN payments pay ON p.id = pay.plan_id
GROUP BY p.id
HAVING COALESCE(SUM(pay.amount), 0) <> SUM(i.paid_amount);

-- 5. DETECCIÓN DE REGISTROS HUÉRFANOS (Integridad Referencial)
SELECT i.* 
FROM installments i
LEFT JOIN installment_plans p ON i.plan_id = p.id
WHERE p.id IS NULL;
