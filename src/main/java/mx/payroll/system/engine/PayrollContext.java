package mx.payroll.system.engine;

import mx.payroll.system.domain.model.Employee;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PayrollContext {
    private Employee employee;
    private Map<String, BigDecimal> calculatedValues = new HashMap<>();
    private Map<String, BigDecimal> fixedValues = new HashMap<>(); // New map for fixed values

    public PayrollContext(Employee employee) { this.employee = employee; }
    
    public void addCalculation(String code, BigDecimal value) {
        calculatedValues.put(code, value);
    }
    
    public void setFixedValue(String code, BigDecimal value) { // New method
        fixedValues.put(code, value);
    }

    // This method needs to retrieve from both fixed and calculated, prioritizing fixed if both exist
    public BigDecimal getValue(String code) {
        if (fixedValues.containsKey(code)) {
            return fixedValues.get(code);
        }
        return calculatedValues.getOrDefault(code, BigDecimal.ZERO);
    }

    public Employee getEmployee() {
        return employee;
    }
    
    /* 
    public BigDecimal getEmployeeFixedValue(String code) {
        return employee.getFixedValue(code);
    }
    */

    public Map<String, BigDecimal> getCalculatedValues() {
        return calculatedValues;
    }

    public Map<String, BigDecimal> getFixedValuesMap() {
        return fixedValues;
    }

    // New method to expose all variables (fixed and calculated) for SpEL
    public Map<String, BigDecimal> getVariables() {
        Map<String, BigDecimal> allVariables = new HashMap<>(fixedValues);
        calculatedValues.forEach((key, value) -> allVariables.merge(key, value, (oldVal, newVal) -> newVal)); // Calculated overrides fixed
        return allVariables;
    }
}