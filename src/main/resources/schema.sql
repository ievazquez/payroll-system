-- 1. CATALOGS & CONFIGURATION
-- Stores definitions like "01 Base Salary", "D03 Health Insurance"
CREATE TABLE IF NOT EXISTS concepts (
    code VARCHAR(10) PRIMARY KEY, -- e.g., 'INC01', 'DED03'
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'EARNING', 'DEDUCTION'
    calculation_order INT NOT NULL, -- Critical: 10 runs before 20.
    is_active BOOLEAN DEFAULT TRUE
);

-- Stores the logic. Separated to allow versioning of formulas without losing history.
CREATE TABLE IF NOT EXISTS concept_formulas (
    id SERIAL PRIMARY KEY,
    concept_code VARCHAR(10) REFERENCES concepts(code),
    formula_expression VARCHAR(1000) NOT NULL, -- e.g., "BASE_SALARY * 0.04"
    description VARCHAR(500),
    effective_date DATE NOT NULL,
    end_date DATE, -- NULL means currently active
    rule_order INT NOT NULL DEFAULT 0
);

-- 2. EMPLOYEE DATA
CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    employee_number VARCHAR(50) UNIQUE,
    full_name VARCHAR(150),
    hire_date DATE,
    active BOOLEAN DEFAULT TRUE
);

-- Stores fixed values specific to an employee (e.g., Base Salary amount, Loan amount)
CREATE TABLE IF NOT EXISTS employee_concept_values (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employees(id),
    concept_code VARCHAR(10) REFERENCES concepts(code),
    amount DECIMAL(19, 4) NOT NULL, -- Fixed input value
    effective_date DATE NOT NULL,
    end_date DATE
);

-- 3. PAYROLL PROCESSING (TRANSACTIONAL)
CREATE TABLE IF NOT EXISTS payroll_periods (
    id BIGSERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20), -- 'OPEN', 'PROCESSING', 'COMPLETED', 'CLOSED'
    period_identifier VARCHAR(100) UNIQUE, -- e.g., "PERIOD-2024-01"
    total_expected INT DEFAULT 0 -- Total number of employees expected to process
);

-- The Header (The "Paystub")
CREATE TABLE IF NOT EXISTS payroll_results (
    id BIGSERIAL PRIMARY KEY,
    period_id VARCHAR(100), -- References period_identifier, not id
    employee_id INT REFERENCES employees(id),
    total_earnings DECIMAL(19, 4),
    total_deductions DECIMAL(19, 4),
    net_pay DECIMAL(19, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- The Details (The Lines in the Paystub)
CREATE TABLE IF NOT EXISTS payroll_result_details (
    id BIGSERIAL PRIMARY KEY,
    payroll_result_id BIGINT REFERENCES payroll_results(id),
    concept_code VARCHAR(10) NOT NULL,
    calculated_amount DECIMAL(19, 4) NOT NULL,
    calculation_log TEXT -- Log of calculation steps
);

CREATE TABLE IF NOT EXISTS tax_tables (
    id SERIAL PRIMARY KEY,
    fiscal_year INT NOT NULL,       -- Ej: 2024, 2025
    table_type VARCHAR(20) NOT NULL, -- 'MENSUAL', 'QUINCENAL', 'ANUAL'

    lower_limit DECIMAL(19, 4) NOT NULL,    -- Límite Inferior
    fixed_fee DECIMAL(19, 4) NOT NULL,      -- Cuota Fija
    percent_excess DECIMAL(10, 4) NOT NULL  -- Porcentaje sobre excedente (Ej: 0.1088)
);

-- Índice para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_tax_lookup ON tax_tables(fiscal_year, table_type, lower_limit);

CREATE TABLE IF NOT EXISTS economic_indicators (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL, -- e.g., 'UMA', 'SMI'
    value DECIMAL(19, 4) NOT NULL,
    effective_date DATE NOT NULL
);

-- Index for finding the latest effective date efficiently
CREATE INDEX IF NOT EXISTS idx_indicators_lookup ON economic_indicators(code, effective_date);
