-- LIMPIEZA PREVIA (Opcional, para reiniciar pruebas)
TRUNCATE TABLE payroll_result_details CASCADE;
TRUNCATE TABLE payroll_results CASCADE;
TRUNCATE TABLE employee_concept_values CASCADE;
TRUNCATE TABLE concept_formulas CASCADE;
TRUNCATE TABLE employees CASCADE;
TRUNCATE TABLE concepts CASCADE;
TRUNCATE TABLE economic_indicators CASCADE;

-- 0. INDICADORES ECONÓMICOS
-- Valores históricos reales de la UMA en México
INSERT INTO economic_indicators (code, value, effective_date) VALUES 
('UMA', 89.62, '2021-02-01'),
('UMA', 96.22, '2022-02-01'),
('UMA', 103.74, '2023-02-01'),
('UMA', 108.57, '2024-02-01');

-- 1. SUELDO (Base)
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('P001', 'Sueldo Ordinario', 'EARNING', 10);
-- Sin fórmula global, viene de 'employee_concept_values'

-- 2. AGUINALDO (Ley: 15 días mínimo)
-- Fórmula: (SueldoDiario * 15) * (DíasTrabajadosEnAño / 365)
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('P002', 'Aguinaldo', 'EARNING', 20);

INSERT INTO concept_formulas (concept_code, formula_expression, effective_date, rule_order) VALUES
('P002',
 '(#P001 / 30.0) * 15 * (#diasTrabajadosAnio(#HIRE_DATE) / 365.0)',
 '2020-01-01',
 20);

-- 3. PRIMA VACACIONAL (Ley: 25% sobre los días de vacaciones)
-- Nota: Requiere saber cuántos días le tocan según antigüedad (Tabla de Vacaciones Digna)
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('P003', 'Prima Vacacional', 'EARNING', 30);

INSERT INTO concept_formulas (concept_code, formula_expression, effective_date, rule_order) VALUES
('P003',
 '(#P001 / 30.0) * #diasVacaciones(#HIRE_DATE) * 0.25',
 '2023-01-01',
 30);

-- 4. VALES DE DESPENSA (Previsión Social - Tope 40% UMA mensual)
-- Ejemplo: 10% del sueldo, pero topado a 40% de la UMA * 30 días
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('P004', 'Vales de Despensa', 'EARNING', 40);

INSERT INTO concept_formulas (concept_code, formula_expression, effective_date, rule_order) VALUES
('P004',
 '(#P001 * 0.10).min(#UMA * 30 * 0.40)',
 '2024-01-01',
 40);
 -- Explicación: Paga el 10% del sueldo, PERO si eso excede el tope legal del IMSS/ISR, usa el tope.

-- =================================================================================
-- B. BASE GRAVABLE (Conceptos Virtuales) - Orden 100 a 199
-- No se pagan, sirven para calcular impuestos
-- =================================================================================

-- En México, no todo ingreso paga impuestos (ej. Aguinaldo exento 30 UMAS)
-- Para simplificar, asumiremos una función #baseGravableISR()

-- =================================================================================
-- C. DEDUCCIONES (DESCUENTOS) - Orden 200 a 299
-- =================================================================================

-- 5. IMSS (Cuota Obrero)
-- Es complejo calcularlo en una sola línea de texto. Usamos función Java.
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('D001', 'Cuota IMSS', 'DEDUCTION', 200);

INSERT INTO concept_formulas (concept_code, formula_expression, effective_date, rule_order) VALUES
('D001',
 '#calcularIMSS(#P001, #UMA)',
 '2024-01-01',
 200);

-- 6. ISR (Impuesto Sobre la Renta)
-- Requiere Tabla de Límites Inferiores y Porcentajes.
-- La fórmula usa el total de Percepciones (Earnings) menos lo exento.
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('D002', 'ISR Retenido', 'DEDUCTION', 210);

INSERT INTO concept_formulas (concept_code, formula_expression, effective_date, rule_order) VALUES
('D002',
 '#calcularISR(#TOTAL_EARNINGS)',
 '2024-01-01',
 210);

-- 7. INFONAVIT (Crédito de Vivienda)
-- Puede ser Cuota Fija, Porcentaje o Veces Salario Mínimo (VSM/UMI)
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('D003', 'Amortización Infonavit', 'DEDUCTION', 220);
-- Fórmula: Generalmente es valor fijo o % asignado al empleado en employee_concept_values
-- Si fuera porcentaje global: '#P001 * 0.20' (poco común que sea global fijo)

-- 8. FONDO DE AHORRO (Deducción al empleado)
INSERT INTO concepts (code, name, type, calculation_order) VALUES 
('D004', 'Fondo de Ahorro Empleado', 'DEDUCTION', 230);

INSERT INTO concept_formulas (concept_code, formula_expression, effective_date, rule_order) VALUES
('D004',
 '#P001 * 0.05',
 '2020-01-01',
 230);

-- ---------------------------------------------------------
-- 1. EL MENÚ (Catálogo de Conceptos)
-- Nota el 'calculation_order': Es vital para dependencias.
-- ---------------------------------------------------------
--INSERT INTO concepts (code, name, type, calculation_order, is_active) VALUES
--('INC01', 'Sueldo Base',       'EARNING',   10, TRUE), -- Se calcula primero
--('INC02', 'Bono Antigüedad',   'EARNING',   20, TRUE), -- Depende de fecha ingreso
--('INC03', 'Bono Transporte',   'EARNING',   30, TRUE), -- Fijo para algunos
--('DED01', 'Seguro Salud (4%)', 'DEDUCTION', 50, TRUE), -- Depende del total ingresos
--('DED02', 'Préstamo Empresa',  'DEDUCTION', 60, TRUE); -- Fijo por empleado

-- ---------------------------------------------------------
-- 2. LAS REGLAS DEL JUEGO (Fórmulas Globales)
-- Aquí usamos SpEL. Nota como DED01 usa una variable '#TOTAL_EARNINGS'
-- ---------------------------------------------------------
--INSERT INTO concept_formulas (concept_code, formula_expression, valid_from) VALUES
-- El Sueldo Base (INC01) no suele tener fórmula global, se toma del valor fijo del empleado.
-- Pero si quisiéramos un default (sueldo mínimo), lo pondríamos aquí.

-- INC02: 100 pesos por cada año trabajado. Usa nuestra función custom #antiguedad
--('INC02', '#antiguedad(#HIRE_DATE, #CURRENT_DATE) * 100.00', '2020-01-01'),

-- DED01: 4% de todo lo ganado (Sueldo + Bonos)
--('DED01', '#TOTAL_EARNINGS * 0.04', '2020-01-01');


-- Insertamos la tabla MENSUAL del 2024
INSERT INTO tax_tables (fiscal_year, table_type, lower_limit, fixed_fee, percent_excess) VALUES
(2024, 'MENSUAL', 0.01,       0.00,    0.0192),
(2024, 'MENSUAL', 746.05,     14.32,   0.0640),
(2024, 'MENSUAL', 6332.06,    371.83,  0.1088),
(2024, 'MENSUAL', 11128.02,   893.63,  0.1600),
(2024, 'MENSUAL', 12935.83,   1182.88, 0.1792),
(2024, 'MENSUAL', 15487.72,   1640.18, 0.2136),
(2024, 'MENSUAL', 31236.50,   5004.12, 0.2352),
(2024, 'MENSUAL', 49233.01,   9236.89, 0.3000),
(2024, 'MENSUAL', 93993.32,   22665.01, 0.3200),
(2024, 'MENSUAL', 125325.21,  32691.18, 0.3400),
(2024, 'MENSUAL', 375975.62,  117912.32, 0.3500);

-- ---------------------------------------------------------
-- 3. LOS EMPLEADOS
-- ---------------------------------------------------------
INSERT INTO employees (id, employee_number, full_name, hire_date, active) VALUES
(1, 'EMP-001', 'Ana Gerente', '2015-06-01', TRUE),  -- Tiene 10 años aprox
(2, 'EMP-002', 'Carlos Junior', '2024-01-01', TRUE), -- Nuevo ingreso
(3, 'EMP-003', 'Juan', '2020-03-15', TRUE); -- Empleado de ejemplo para el endpoint

-- ---------------------------------------------------------
-- 4. LA PERSONALIZACIÓN (Valores Fijos)
-- Aquí definimos qué hace único a cada empleado
-- ---------------------------------------------------------

-- CONFIGURACIÓN DE JUAN (ID 3)
-- Ejemplo para el endpoint /api/employees/Juan/concepts
INSERT INTO employee_concept_values (employee_id, concept_code, amount, effective_date, end_date) VALUES
(3, 'P001', 3000.00, '2020-01-01', NULL), -- Sueldo Base
(3, 'D003', 500.00,  '2024-01-01', NULL); -- Préstamo Infonavit

-- CONFIGURACIÓN DE ANA (ID 1)
-- Gana 5000 base
INSERT INTO employee_concept_values (employee_id, concept_code, amount, effective_date, end_date) VALUES
(1, 'P001', 5000.00, '2024-01-01', NULL); -- Sueldo Base

-- CONFIGURACIÓN DE CARLOS (ID 2)
-- Gana 2000 base
INSERT INTO employee_concept_values (employee_id, concept_code, amount, effective_date, end_date) VALUES
(2, 'P001', 2000.00, '2024-01-01', NULL); -- Sueldo Base
