package mx.payroll.system.engine;

import mx.payroll.system.domain.model.Employee;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FullCalculationTest {

    @Test
    public void testSimplePayrollCalculation() {
        // 1. Setup Employee with fixed concepts (Base Salary)
        Employee employee = new Employee("EMP-TEST", "Test User", LocalDate.now());
        // Employee has a base salary of 10,000 stored in their fixed values
        // Commented out - Employee.setFixedValue() no longer exists
        // employee.setFixedValue("SALARY_BASE", new BigDecimal("10000.00"));

        // 2. Configure Engine with Rules
        PayrollEngine engine = new PayrollEngine(); // Instantiated with no arguments

        List<PayrollRule> rulesToApply = new ArrayList<>();
        // Rule 1: Read SALARY_BASE from employee and put it in context
        // Note: Current FixedIncomeRule implementation uses the 'code' for both lookup and storage
        rulesToApply.add(new FixedIncomeRule("SALARY_BASE", 1));

        // Rule 2: Calculate TAX (10%) based on SALARY_BASE
        rulesToApply.add(new PercentageDeductionRule("TAX_RET", "SALARY_BASE", new BigDecimal("0.10"), 2));

        // Rule 3: Calculate SOCIAL_SECURITY (5%) based on SALARY_BASE
        rulesToApply.add(new PercentageDeductionRule("SS_DED", "SALARY_BASE", new BigDecimal("0.05"), 3));

        // 3. Prepare Context
        final PayrollContext context = new PayrollContext(employee);

        // Fixed values should be loaded from EmployeeConceptValue table in real usage
        // For this test, fixed values can be set manually if needed
        // No longer using Employee.getFixedValues()

        // 4. Execute
        PayrollResult result = engine.calculate(context, rulesToApply); // Pass the list of rules

        // 5. Assert Results
        BigDecimal calculatedSalary = result.getContext().getValue("SALARY_BASE");
        BigDecimal calculatedTax = result.getContext().getValue("TAX_RET");
        BigDecimal calculatedSS = result.getContext().getValue("SS_DED");

        // Use compareTo for BigDecimal equality to ignore scale differences, but ensure expected values are reasonable
        assertEquals(0, new BigDecimal("10000.00").compareTo(calculatedSalary), "Salary should match fixed value");
        assertEquals(0, new BigDecimal("1000.00").setScale(2, RoundingMode.HALF_EVEN).compareTo(calculatedTax), "Tax should be 10% of 10,000");
        assertEquals(0, new BigDecimal("500.00").setScale(2, RoundingMode.HALF_EVEN).compareTo(calculatedSS), "SS should be 5% of 10,000");
    }
}