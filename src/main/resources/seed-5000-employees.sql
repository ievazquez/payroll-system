-- Script para generar 5000 empleados de prueba con datos realistas
-- Incluye nombres variados, fechas de contratación y sueldos base

-- Limpiar TODAS las tablas de resultados y empleados ANTES de insertar nuevos seeds
TRUNCATE TABLE payroll_result_details CASCADE;
TRUNCATE TABLE payroll_results CASCADE;
DELETE FROM employee_concept_values WHERE employee_id IN (SELECT id FROM employees WHERE employee_number LIKE 'EMP-%');
DELETE FROM employees WHERE employee_number LIKE 'EMP-%';


-- Generar 5000 empleados
DO $$
DECLARE
    i INTEGER;
    nombres TEXT[] := ARRAY[
        'Juan', 'María', 'Pedro', 'Ana', 'Luis', 'Carmen', 'José', 'Laura', 'Carlos', 'Elena',
        'Miguel', 'Rosa', 'Jorge', 'Patricia', 'Francisco', 'Isabel', 'Antonio', 'Teresa', 'Manuel', 'Cristina',
        'Alejandro', 'Andrea', 'Fernando', 'Beatriz', 'Rafael', 'Mónica', 'Ricardo', 'Silvia', 'Roberto', 'Diana',
        'Diego', 'Gabriela', 'Javier', 'Verónica', 'Pablo', 'Claudia', 'Sergio', 'Mariana', 'Alberto', 'Paola',
        'Andrés', 'Sandra', 'Raúl', 'Adriana', 'Héctor', 'Cecilia', 'Daniel', 'Liliana', 'Arturo', 'Norma'
    ];
    apellidos TEXT[] := ARRAY[
        'García', 'Rodríguez', 'Martínez', 'Hernández', 'López', 'González', 'Pérez', 'Sánchez', 'Ramírez', 'Torres',
        'Flores', 'Rivera', 'Gómez', 'Díaz', 'Cruz', 'Morales', 'Reyes', 'Gutiérrez', 'Ortiz', 'Chávez',
        'Ruiz', 'Jiménez', 'Mendoza', 'Castillo', 'Vargas', 'Romero', 'Herrera', 'Medina', 'Aguilar', 'Vega',
        'Rojas', 'Salazar', 'Cortés', 'Núñez', 'Guerrero', 'Estrada', 'Delgado', 'Campos', 'Contreras', 'Ramos',
        'Valdez', 'Mora', 'Vázquez', 'Carrillo', 'Mendez', 'Mejía', 'Navarro', 'Luna', 'Ríos', 'Domínguez'
    ];
    nombre_completo TEXT;
    fecha_contrato DATE;
    sueldo_base NUMERIC(19,4);
    dias_atras INTEGER;
BEGIN
    FOR i IN 4..5000 LOOP
        -- Generar nombre completo aleatorio
        nombre_completo := nombres[1 + floor(random() * array_length(nombres, 1))::int] || ' ' ||
                          apellidos[1 + floor(random() * array_length(apellidos, 1))::int] || ' ' ||
                          apellidos[1 + floor(random() * array_length(apellidos, 1))::int];

        -- Generar fecha de contratación entre 2015 y 2024 (últimos 10 años)
        dias_atras := floor(random() * 3650)::int; -- 10 años = ~3650 días
        fecha_contrato := CURRENT_DATE - dias_atras;

        -- Insertar empleado
        INSERT INTO employees (employee_number, full_name, hire_date, active)
        VALUES (
            'EMP-' || LPAD(i::TEXT, 6, '0'),
            nombre_completo,
            fecha_contrato,
            CASE WHEN random() > 0.1 THEN true ELSE false END -- 90% activos, 10% inactivos
        );

        -- Generar sueldo base aleatorio entre 8,000 y 50,000 pesos
        -- Distribución: 60% entre 8k-15k, 30% entre 15k-30k, 10% entre 30k-50k
        sueldo_base := CASE
            WHEN random() < 0.6 THEN 8000 + (random() * 7000)::numeric
            WHEN random() < 0.9 THEN 15000 + (random() * 15000)::numeric
            ELSE 30000 + (random() * 20000)::numeric
        END;

        -- Asignar sueldo base al empleado
        INSERT INTO employee_concept_values (employee_id, concept_code, amount, effective_date, end_date)
        VALUES (
            (SELECT id FROM employees WHERE employee_number = 'EMP-' || LPAD(i::TEXT, 6, '0')),
            'P001', -- Código de Sueldo Ordinario
            ROUND(sueldo_base, 2),
            fecha_contrato,
            NULL
        );

        -- Mostrar progreso cada 500 empleados
        IF i % 500 = 0 THEN
            RAISE NOTICE 'Insertados % empleados...', i;
        END IF;
    END LOOP;

    RAISE NOTICE 'Proceso completado: 5000 empleados insertados';
END $$;

-- Verificar inserción
SELECT
    COUNT(*) as total_empleados,
    COUNT(*) FILTER (WHERE active = true) as activos,
    COUNT(*) FILTER (WHERE active = false) as inactivos,
    MIN(hire_date) as fecha_contrato_mas_antigua,
    MAX(hire_date) as fecha_contrato_mas_reciente
FROM employees
WHERE employee_number LIKE 'EMP-%';

-- Verificar sueldos asignados
SELECT
    COUNT(*) as empleados_con_sueldo,
    ROUND(MIN(amount), 2) as sueldo_minimo,
    ROUND(MAX(amount), 2) as sueldo_maximo,
    ROUND(AVG(amount), 2) as sueldo_promedio
FROM employee_concept_values
WHERE concept_code = 'P001'
  AND employee_id IN (SELECT id FROM employees WHERE employee_number LIKE 'EMP-%');
