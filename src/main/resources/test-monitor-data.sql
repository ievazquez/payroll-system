-- Script para crear datos de prueba para el endpoint /api/monitor/{periodId}/progress
-- Resultado esperado:
-- {
--   "periodId": 123,
--   "total": 10000,
--   "processed": 4500,
--   "percentage": 45,
--   "status": "PROCESSING"
-- }

-- 1. Insertar PayrollPeriod con ID 123
INSERT INTO payroll_periods (id, start_date, end_date, status, period_identifier, total_expected)
VALUES (123, '2024-01-01', '2024-01-31', 'PROCESSING', 'PERIOD-2024-01', 10000);

-- 2. Crear empleados de prueba (necesitamos al menos 4500)
-- Para no saturar la BD, creamos solo 100 empleados y luego insertamos
-- 4500 resultados reutilizando esos empleados

-- Insertar 100 empleados de prueba
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 1..100 LOOP
        INSERT INTO employees (employee_number, full_name, hire_date)
        VALUES (
            'EMP-TEST-' || LPAD(i::TEXT, 6, '0'),
            'Empleado Prueba ' || i,
            '2023-01-01'
        );
    END LOOP;
END $$;

-- 3. Insertar 4500 PayrollResults para simular procesamiento
-- Reutilizamos los 100 empleados (cada uno tendrá 45 resultados)
DO $$
DECLARE
    emp_id BIGINT;
    emp_cursor CURSOR FOR SELECT id FROM employees WHERE employee_number LIKE 'EMP-TEST-%' LIMIT 100;
    i INTEGER;
    result_count INTEGER := 0;
BEGIN
    -- Para cada empleado, crear 45 resultados
    OPEN emp_cursor;
    WHILE result_count < 4500 LOOP
        FETCH emp_cursor INTO emp_id;
        IF NOT FOUND THEN
            -- Reiniciar cursor si llegamos al final
            CLOSE emp_cursor;
            OPEN emp_cursor;
            FETCH emp_cursor INTO emp_id;
        END IF;

        INSERT INTO payroll_results (employee_id, period_id, total_earnings, total_deductions, net_pay)
        VALUES (
            emp_id,
            'PERIOD-2024-01',
            5000.00 + (result_count % 1000),
            1000.00 + (result_count % 200),
            4000.00 + (result_count % 800)
        );

        -- Insertar algunos detalles para cada resultado
        INSERT INTO payroll_result_details (payroll_result_id, concept_code, calculated_amount, calculation_log)
        VALUES (
            (SELECT currval('payroll_results_id_seq')),
            'P001',
            5000.00,
            'Sueldo Base'
        );

        result_count := result_count + 1;
    END LOOP;

    CLOSE emp_cursor;
END $$;

-- Verificación
SELECT
    pp.id as period_id,
    pp.total_expected as total,
    COUNT(pr.id) as processed,
    ROUND((COUNT(pr.id)::DECIMAL / pp.total_expected) * 100) as percentage,
    pp.status
FROM payroll_periods pp
LEFT JOIN payroll_results pr ON pr.period_id = pp.period_identifier
WHERE pp.id = 123
GROUP BY pp.id, pp.total_expected, pp.status;
