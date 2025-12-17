-- Migration: Add UNIQUE constraint to prevent duplicate employee processing
-- This script removes existing duplicates and adds the constraint

-- Step 1: Backup - Show duplicates that will be removed
SELECT
    'Duplicados a eliminar' as info,
    COUNT(*) - COUNT(DISTINCT (employee_id, period_id)) as total_duplicados
FROM payroll_results;

-- Step 2: Delete details of duplicate records first (cascade manually)
DELETE FROM payroll_result_details
WHERE payroll_result_id IN (
    SELECT pr.id
    FROM payroll_results pr
    WHERE pr.id NOT IN (
        SELECT MIN(id)
        FROM payroll_results
        GROUP BY employee_id, period_id
    )
);

-- Step 3: Delete duplicate payroll_results, keeping only the oldest record for each (employee_id, period_id)
DELETE FROM payroll_results
WHERE id NOT IN (
    SELECT MIN(id)
    FROM payroll_results
    GROUP BY employee_id, period_id
);

-- Step 4: Show what was kept
SELECT
    'Registros mantenidos' as info,
    COUNT(*) as total_registros,
    COUNT(DISTINCT employee_id) as empleados_unicos
FROM payroll_results;

-- Step 5: Create the UNIQUE index
CREATE UNIQUE INDEX IF NOT EXISTS idx_payroll_unique_employee_period
ON payroll_results(employee_id, period_id);

-- Step 6: Verify the index was created
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'payroll_results'
AND indexname = 'idx_payroll_unique_employee_period';

-- Success message
SELECT '✅ Migración completada: UNIQUE constraint agregado exitosamente' as status;
