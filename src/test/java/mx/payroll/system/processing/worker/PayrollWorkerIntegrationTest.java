package mx.payroll.system.processing.worker;

import mx.payroll.system.domain.model.ConceptFormula; // Added import
import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.ConceptFormulaRepository; // Added import
import mx.payroll.system.domain.repository.EmployeeRepository;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.domain.repository.PayrollResultRepository;
import mx.payroll.system.engine.FixedIncomeRule;
import mx.payroll.system.engine.PayrollEngine;
import mx.payroll.system.engine.PayrollResult;
import mx.payroll.system.processing.dispatcher.PayrollChunkJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test") // Uses H2 in-memory DB
@Transactional // Rolls back changes after test
public class PayrollWorkerIntegrationTest {

    @Autowired
    private PayrollWorker payrollWorker;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private PayrollResultRepository resultRepo;

    @Autowired
    private PayrollPeriodRepository payrollPeriodRepo; // Autowired

    @Autowired
    private PayrollEngine payrollEngine; // Inject PayrollEngine to inspect its state

    @Autowired
    private ConceptFormulaRepository conceptFormulaRepo; // Added

    @BeforeEach
    void setUp() {
        resultRepo.deleteAll(); // Clean up before each test
        employeeRepo.deleteAll();
        payrollPeriodRepo.deleteAll(); // Also clean up payroll periods
        conceptFormulaRepo.deleteAll(); // Clean up concept formulas

        // Create and save a PayrollPeriod for the test
        PayrollPeriod testPeriod = new PayrollPeriod(
            LocalDate.of(2023, 12, 1),
            LocalDate.of(2023, 12, 31),
            "OPEN",
            "PERIOD-2023-12"
        );
        payrollPeriodRepo.save(testPeriod);

        // Add a ConceptFormula for BASE_SALARY so PayrollService can pick it up
        // Simplified formula using direct variable access which is now supported by FormulaEngine
        ConceptFormula baseSalaryFormula = new ConceptFormula(
            "BASE_SALARY",
            "#SALARY_BASE", // Formula to get fixed value from employee, now mapped to a variable
            "Formula for employee's base salary",
            LocalDate.of(2023, 1, 1),
            null, // Active indefinitely
            1 // Order of execution
        );
        conceptFormulaRepo.save(baseSalaryFormula);
    }

    @Test
    public void testProcessChunk_OnlyActiveEmployeesAreProcessed() {
        // Assert that the PayrollEngine has rules from PayrollTestConfig
        assertFalse(payrollEngine.getRules().isEmpty(), "PayrollEngine should have rules configured for the test profile.");
        assertEquals(1, payrollEngine.getRules().size(), "PayrollEngine should have exactly one rule (FixedIncomeRule) configured.");
        assertTrue(payrollEngine.getRules().get(0) instanceof FixedIncomeRule, "The configured rule should be FixedIncomeRule.");

        // 1. Setup Data
        // Active Employee
        Employee activeEmp = new Employee("EMP-ACTIVE", "Juan Perez", LocalDate.now());
        activeEmp.setActive(true);
        // Commented out - Employee.setFixedValue() no longer exists
        // activeEmp.setFixedValue("BASE_SALARY", new BigDecimal("10000.00")); // Ensure some value for engine
        employeeRepo.save(activeEmp);

        // Inactive Employee
        Employee inactiveEmp = new Employee("EMP-INACTIVE", "Maria Lopez", LocalDate.now());
        inactiveEmp.setActive(false);
        // Commented out - Employee.setFixedValue() no longer exists
        // inactiveEmp.setFixedValue("BASE_SALARY", new BigDecimal("5000.00")); // Ensure some value for engine
        employeeRepo.save(inactiveEmp);

        // 2. Create Job
        // Page 0, Size 10 (Should pick up only the active one due to findAllActive)
        PayrollChunkJob job = new PayrollChunkJob("PERIOD-2023-12", 0, 10);

        // 3. Act
        payrollWorker.processChunk(job);

        // 4. Assert
        List<PayrollResult> results = resultRepo.findAll();
        
        // Only one result should be generated because only one employee is active
        assertEquals(1, results.size(), "Only one result should be generated for the active employee.");
        
        PayrollResult result = results.get(0);
        assertNotNull(result.getId(), "PayrollResult should have an ID after saving.");
        assertNotNull(result.getDetails(), "PayrollResult should have details.");
        assertFalse(result.getDetails().isEmpty(), "PayrollResult should not have empty details.");

        // Verify the result belongs to the active employee
        assertEquals(activeEmp.getId(), result.getEmployeeId(), "The result should belong to the active employee.");
        assertEquals("PERIOD-2023-12", result.getPeriodId(), "The period ID should match the job.");
        
        // Verify a detail for the base salary was captured
        assertTrue(result.getDetails().stream().anyMatch(d -> d.getConceptCode().equals("BASE_SALARY")), "Should contain BASE_SALARY detail.");
    }
}
