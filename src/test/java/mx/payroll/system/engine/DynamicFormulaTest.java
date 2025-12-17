package mx.payroll.system.engine;

import mx.payroll.system.domain.model.Employee; // Import Employee
import mx.payroll.system.service.TaxService; // Import TaxService
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; // Import ExtendWith
import org.mockito.Mock; // Import Mock
import org.mockito.junit.jupiter.MockitoExtension; // Import MockitoExtension

import java.math.BigDecimal;
import java.time.LocalDate; // Import LocalDate
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class) // Add MockitoExtension
class DynamicFormulaTest {

    @Mock // Mock TaxService
    private TaxService taxService;

    @Test
    void should_calculate_formula_using_context_variables() {
        // 1. Arrange
        // Create a dummy employee and PayrollContext
        Employee dummyEmployee = new Employee("EMP-DYN", "Dynamic Employee", LocalDate.now());
        PayrollContext payrollContext = new PayrollContext(dummyEmployee);

        // Simulamos los valores ya calculados de un empleado
        Map<String, BigDecimal> vars = new HashMap<>(); // Renamed from contextVariables for clarity
        vars.put("INC01", new BigDecimal("1000.00")); // Sueldo
        vars.put("INC02", new BigDecimal("200.00"));  // Bono
        
        // Populate payrollContext with these variables
        vars.forEach((key, value) -> payrollContext.addCalculation(key, value));

        // Esta fórmula viene de la Base de Datos (concept_formulas.formula_expression)
        // Nota: En SpEL, las variables se acceden con #
        String formulaFromDb = "(#INC01 + #INC02) * 0.05"; // Use direct variables

        // Instanciamos nuestro motor
        FormulaEngine engine = new FormulaEngine(taxService); // Pass the mocked TaxService

        // 2. Act
        BigDecimal result = engine.evaluate(formulaFromDb, payrollContext); // Use payrollContext

        // 3. Assert
        // (1000 + 200) * 0.05 = 60.00
        assertEquals(0, new BigDecimal("60.00").compareTo(result));
    }
}
