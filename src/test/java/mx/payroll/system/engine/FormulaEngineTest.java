package mx.payroll.system.engine;

import mx.payroll.system.domain.model.Employee; // Import Employee
import mx.payroll.system.exception.FormulaEvaluationException;
import mx.payroll.system.service.TaxService; // Import TaxService
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith; // Import ExtendWith
import org.mockito.Mock; // Import Mock
import org.mockito.junit.jupiter.MockitoExtension; // Import MockitoExtension

import java.math.BigDecimal;
import java.time.LocalDate; // Import LocalDate
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when; // For TaxService mock

@ExtendWith(MockitoExtension.class) // Add MockitoExtension
class FormulaEngineTest {

    @Mock // Mock TaxService
    private TaxService taxService;

    private FormulaEngine engine;
    private PayrollContext dummyContext; // Dummy context for testing

    @BeforeEach
    void setUp() {
        engine = new FormulaEngine(taxService); // Pass the mocked TaxService
        // Create a dummy employee and context for testing
        Employee dummyEmployee = new Employee("EMP-TEST", "Dummy Employee", LocalDate.now());
        dummyContext = new PayrollContext(dummyEmployee);
        // For tests that expect #impuestos (taxService) to be set, ensure mock behavior if needed
        // For basic tests, it's fine if taxService is just a mock
    }

    // Helper to populate dummyContext with variables
    private void populateContext(Map<String, BigDecimal> vars) {
        vars.forEach((key, value) -> dummyContext.addCalculation(key, value));
    }


    @Nested
    @DisplayName("Basic Arithmetic Operations")
    class BasicArithmeticTests {

        @Test
        @DisplayName("Should calculate simple addition")
        void shouldCalculateSimpleAddition() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("a", new BigDecimal("100.00"));
            vars.put("b", new BigDecimal("50.00"));
            populateContext(vars); // Populate the dummyContext

            BigDecimal result = engine.evaluate("#a + #b", dummyContext); // Use dummyContext and #root.getValue

            assertEquals(0, new BigDecimal("150.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate simple subtraction")
        void shouldCalculateSimpleSubtraction() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("salary", new BigDecimal("5000.00"));
            vars.put("deduction", new BigDecimal("250.00"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#salary - #deduction", dummyContext);

            assertEquals(0, new BigDecimal("4750.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate simple multiplication")
        void shouldCalculateSimpleMultiplication() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("hours", new BigDecimal("8"));
            vars.put("rate", new BigDecimal("150.50"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#hours * #rate", dummyContext);

            assertEquals(0, new BigDecimal("1204.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate simple division")
        void shouldCalculateSimpleDivision() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("total", new BigDecimal("1000.00"));
            vars.put("parts", new BigDecimal("4"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#total / #parts", dummyContext);

            assertEquals(0, new BigDecimal("250.00").compareTo(result));
        }
    }

    @Nested
    @DisplayName("Complex Formula Tests")
    class ComplexFormulaTests {

        @Test
        @DisplayName("Should calculate formula with multiple operations")
        void shouldCalculateComplexFormula() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("INC01", new BigDecimal("1000.00")); // Sueldo base
            vars.put("INC02", new BigDecimal("200.00"));  // Bono
            vars.put("INC03", new BigDecimal("150.00"));  // Comisión
            populateContext(vars);

            // Formula: (Sueldo + Bono + Comisión) * 10%
            BigDecimal result = engine.evaluate("(#INC01 + #INC02 + #INC03) * 0.10", dummyContext);

            // (1000 + 200 + 150) * 0.10 = 135.00
            assertEquals(0, new BigDecimal("135.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate ISR formula")
        void shouldCalculateISRFormula() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("baseGravable", new BigDecimal("10000.00"));
            vars.put("limiteInferior", new BigDecimal("8601.51"));
            vars.put("porcentaje", new BigDecimal("0.1088"));
            vars.put("cuotaFija", new BigDecimal("692.96"));
            populateContext(vars);

            // Formula típica de ISR: ((baseGravable - limiteInferior) * porcentaje) + #cuotaFija
            String formula = "((#baseGravable - #limiteInferior) * #porcentaje) + #cuotaFija";
            BigDecimal result = engine.evaluate(formula, dummyContext);

            // Calculation:
            // 10000 - 8601.51 = 1398.49
            // 1398.49 * 0.1088 = 152.155712
            // 152.155712 + 692.96 = 845.115712
            // Rounded HALF_EVEN to 2 decimals -> 845.12
            assertEquals(0, new BigDecimal("845.12").compareTo(result), "ISR Calculation mismatch");
        }

        @Test
        @DisplayName("Should calculate IMSS formula")
        void shouldCalculateIMSSFormula() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("sueldo", new BigDecimal("5000.00"));
            vars.put("tasaIMSS", new BigDecimal("0.0250")); // 2.5%
            populateContext(vars);

            BigDecimal result = engine.evaluate("#sueldo * #tasaIMSS", dummyContext);

            assertEquals(0, new BigDecimal("125.00").compareTo(result));
        }

        @Test
        @DisplayName("Should handle parentheses correctly")
        void shouldHandleParentheses() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("a", new BigDecimal("2"));
            vars.put("b", new BigDecimal("3"));
            vars.put("c", new BigDecimal("4"));
            populateContext(vars);

            // (2 + 3) * 4 = 20
            BigDecimal result1 = engine.evaluate("(#a + #b) * #c", dummyContext);
            assertEquals(0, new BigDecimal("20.00").compareTo(result1));

            // 2 + (3 * 4) = 14
            BigDecimal result2 = engine.evaluate("#a + (#b * #c)", dummyContext);
            assertEquals(0, new BigDecimal("14.00").compareTo(result2));
        }
    }

    @Nested
    @DisplayName("Rounding and Precision Tests")
    class RoundingTests {

        @Test
        @DisplayName("Should round to 2 decimal places")
        void shouldRoundToTwoDecimals() {
            Map<String, BigDecimal> vars = new HashMap<>();
            // Use scale to ensure decimal division behavior if needed by SpEL
            vars.put("value", new BigDecimal("100.00")); 
            vars.put("divisor", new BigDecimal("3.00"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#value / #divisor", dummyContext);

            // 100 / 3 = 33.333... should round to 33.33
            assertEquals(0, new BigDecimal("33.33").compareTo(result), "Rounding mismatch");
        }

        @Test
        @DisplayName("Should use banker's rounding (HALF_EVEN)")
        void shouldUseBankersRounding() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("value1", new BigDecimal("2.225"));
            vars.put("value2", new BigDecimal("2.235"));
            populateContext(vars);

            // These are literals, evaluate them in context to ensure rounding is applied
            BigDecimal result1 = engine.evaluate("#value1", dummyContext);
            BigDecimal result2 = engine.evaluate("#value2", dummyContext);

            // 2.225 rounds to 2.22 (even)
            assertEquals(0, new BigDecimal("2.22").compareTo(result1));
            // 2.235 rounds to 2.24 (even)
            assertEquals(0, new BigDecimal("2.24").compareTo(result2));
        }

        @Test
        @DisplayName("Should handle very small numbers")
        void shouldHandleVerySmallNumbers() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("tiny", new BigDecimal("0.001"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#tiny * 1000", dummyContext);

            assertEquals(0, new BigDecimal("1.00").compareTo(result));
        }

        @Test
        @DisplayName("Should handle very large numbers")
        void shouldHandleVeryLargeNumbers() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("large", new BigDecimal("999999999.99"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#large * 1", dummyContext);

            assertEquals(0, new BigDecimal("999999999.99").compareTo(result));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception for null formula")
        void shouldThrowExceptionForNullFormula() {
            // No need to populate context with vars for this test
            FormulaEvaluationException exception = assertThrows(
                FormulaEvaluationException.class,
                () -> engine.evaluate(null, dummyContext) // Use dummyContext
            );

            assertTrue(exception.getMessage().contains("Formula cannot be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception for empty formula")
        void shouldThrowExceptionForEmptyFormula() {
            // No need to populate context with vars for this test
            FormulaEvaluationException exception = assertThrows(
                FormulaEvaluationException.class,
                () -> engine.evaluate("", dummyContext) // Use dummyContext
            );

            assertTrue(exception.getMessage().contains("Formula cannot be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only formula")
        void shouldThrowExceptionForWhitespaceFormula() {
            // No need to populate context with vars for this test
            FormulaEvaluationException exception = assertThrows(
                FormulaEvaluationException.class,
                () -> engine.evaluate("   ", dummyContext) // Use dummyContext
            );

            assertTrue(exception.getMessage().contains("Formula cannot be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception for null PayrollContext")
        void shouldThrowExceptionForNullVariables() {
            FormulaEvaluationException exception = assertThrows(
                FormulaEvaluationException.class,
                () -> engine.evaluate("#a + #b", null) // Pass null PayrollContext
            );

            assertTrue(exception.getMessage().contains("PayrollContext cannot be null"));
        }

        @Test
        @DisplayName("Should throw exception for invalid syntax")
        void shouldThrowExceptionForInvalidSyntax() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("a", new BigDecimal("100"));
            populateContext(vars);

            FormulaEvaluationException exception = assertThrows(
                FormulaEvaluationException.class,
                () -> engine.evaluate("#a + + #b", dummyContext) // Use dummyContext
            );

            String msg = exception.getMessage();
            assertTrue(msg.contains("Invalid formula syntax")
                    || msg.contains("Error evaluating formula") || msg.contains("Error during SpEL evaluation"));
        }

        @Test
        @DisplayName("Should handle missing variable gracefully")
        void shouldHandleMissingVariable() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("a", new BigDecimal("100"));
            populateContext(vars);
            // Variable 'b' is missing

            // SpEL will return null for missing properties, then it fails when trying to add/sub null
            assertThrows(
                Exception.class, // Expecting a generic exception from SpEL when trying to operate on null
                () -> engine.evaluate("#a + #b", dummyContext)
            );
        }

        @Test
        @DisplayName("Should throw exception for division by zero")
        void shouldThrowExceptionForDivisionByZero() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("numerator", new BigDecimal("100"));
            vars.put("denominator", BigDecimal.ZERO);
            populateContext(vars);

            assertThrows(
                Exception.class, // SpEL will throw a generic exception for div by zero
                () -> engine.evaluate("#numerator / #denominator", dummyContext)
            );
        }
    }

    @Nested
    @DisplayName("Cache Functionality Tests")
    class CacheTests {

        @Test
        @DisplayName("Should cache compiled expressions")
        void shouldCacheCompiledExpressions() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("a", new BigDecimal("100"));
            vars.put("b", new BigDecimal("50"));
            populateContext(vars);

            int initialSize = engine.getCacheSize();

            // First evaluation - should compile and cache
            engine.evaluate("#a + #b", dummyContext);
            int afterFirstCall = engine.getCacheSize();

            // Second evaluation with same formula - should use cache
            engine.evaluate("#a + #b", dummyContext);
            int afterSecondCall = engine.getCacheSize();

            // Cache size should increase after first call but not after second
            assertTrue(afterFirstCall > initialSize);
            assertEquals(afterFirstCall, afterSecondCall);
        }

        @Test
        @DisplayName("Should clear cache")
        void shouldClearCache() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("a", new BigDecimal("100"));
            populateContext(vars);

            engine.evaluate("#a * 2", dummyContext);
            assertTrue(engine.getCacheSize() > 0);

            engine.clearCache();
            assertEquals(0, engine.getCacheSize());
        }

        @Test
        @DisplayName("Should cache different formulas separately")
        void shouldCacheDifferentFormulasSeparately() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("a", new BigDecimal("100"));
            vars.put("b", new BigDecimal("50"));
            populateContext(vars);

            engine.clearCache();
            int initialSize = engine.getCacheSize();

            engine.evaluate("#a + #b", dummyContext);
            engine.evaluate("#a - #b", dummyContext);
            engine.evaluate("#a * #b", dummyContext);

            int finalSize = engine.getCacheSize();
            assertEquals(initialSize + 3, finalSize);
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should prevent Java method invocation")
        void shouldPreventJavaMethodInvocation() {
            // No need to populate context with vars for this test
            assertThrows(
                Exception.class, // Generic exception from SpEL security
                () -> engine.evaluate("T(java.lang.System).exit(0)", dummyContext) // Use dummyContext
            );
        }

        @Test
        @DisplayName("Should prevent accessing Java classes")
        void shouldPreventAccessingJavaClasses() {
            // No need to populate context with vars for this test
            assertThrows(
                Exception.class, // Generic exception from SpEL security
                () -> engine.evaluate("T(java.lang.Runtime).getRuntime()", dummyContext) // Use dummyContext
            );
        }

        @Test
        @DisplayName("Should prevent constructor invocation")
        void shouldPreventConstructorInvocation() {
            // No need to populate context with vars for this test
            assertThrows(
                Exception.class, // Generic exception from SpEL security
                () -> engine.evaluate("new java.io.File('/etc/passwd')", dummyContext) // Use dummyContext
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty variable map")
        void shouldHandleEmptyVariableMap() {
            // No need to populate context with vars, this test is about literals
            BigDecimal result = engine.evaluate("10 + 20", dummyContext); // Use dummyContext

            assertEquals(0, new BigDecimal("30.00").compareTo(result));
        }

        @Test
        @DisplayName("Should handle single variable")
        void shouldHandleSingleVariable() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("salary", new BigDecimal("5000.00"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#salary", dummyContext);

            assertEquals(0, new BigDecimal("5000.00").compareTo(result));
        }

        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("zero", BigDecimal.ZERO);
            vars.put("value", new BigDecimal("100"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#zero + #value", dummyContext);

            assertEquals(0, new BigDecimal("100.00").compareTo(result));
        }

        @Test
        @DisplayName("Should handle negative numbers")
        void shouldHandleNegativeNumbers() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("income", new BigDecimal("1000"));
            vars.put("expenses", new BigDecimal("1500"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#income - #expenses", dummyContext);

            assertEquals(0, new BigDecimal("-500.00").compareTo(result));
        }

        @Test
        @DisplayName("Should handle decimal percentages")
        void shouldHandleDecimalPercentages() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("amount", new BigDecimal("10000"));
            vars.put("percentage", new BigDecimal("0.16")); // 16%
            populateContext(vars);

            BigDecimal result = engine.evaluate("#amount * #percentage", dummyContext);

            assertEquals(0, new BigDecimal("1600.00").compareTo(result));
        }
    }

    @Nested
    @DisplayName("Mexican Payroll Specific Tests")
    class MexicanPayrollTests {

        @Test
        @DisplayName("Should calculate Aguinaldo (15 days)")
        void shouldCalculateAguinaldo() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("dailySalary", new BigDecimal("500.00"));
            vars.put("days", new BigDecimal("15"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#dailySalary * #days", dummyContext);

            assertEquals(0, new BigDecimal("7500.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate Prima Vacacional (25% of 6 days)")
        void shouldCalculatePrimaVacacional() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("dailySalary", new BigDecimal("500.00"));
            vars.put("vacationDays", new BigDecimal("6"));
            vars.put("percentage", new BigDecimal("0.25"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#dailySalary * #vacationDays * #percentage", dummyContext);

            assertEquals(0, new BigDecimal("750.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate total percepciones")
        void shouldCalculateTotalPercepciones() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("sueldoBase", new BigDecimal("10000.00"));
            vars.put("bonoAsistencia", new BigDecimal("500.00"));
            vars.put("bonoPuntualidad", new BigDecimal("300.00"));
            vars.put("valesDespensa", new BigDecimal("1000.00"));
            populateContext(vars);

            String formula = "#sueldoBase + #bonoAsistencia + #bonoPuntualidad + #valesDespensa";
            BigDecimal result = engine.evaluate(formula, dummyContext);

            assertEquals(0, new BigDecimal("11800.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate total deducciones")
        void shouldCalculateTotalDeducciones() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("ISR", new BigDecimal("1200.00"));
            vars.put("IMSS", new BigDecimal("250.00"));
            vars.put("prestamo", new BigDecimal("500.00"));
            populateContext(vars);

            String formula = "#ISR + #IMSS + #prestamo";
            BigDecimal result = engine.evaluate(formula, dummyContext);

            assertEquals(0, new BigDecimal("1950.00").compareTo(result));
        }

        @Test
        @DisplayName("Should calculate neto (percepciones - deducciones)")
        void shouldCalculateNeto() {
            Map<String, BigDecimal> vars = new HashMap<>();
            vars.put("totalPercepciones", new BigDecimal("11800.00"));
            vars.put("totalDeducciones", new BigDecimal("1950.00"));
            populateContext(vars);

            BigDecimal result = engine.evaluate("#totalPercepciones - #totalDeducciones", dummyContext);

            assertEquals(0, new BigDecimal("9850.00").compareTo(result));
        }
    }
}