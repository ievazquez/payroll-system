package mx.payroll.system.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PayrollEngine {

    private List<PayrollRule> rules = new ArrayList<>();

    public void addRule(PayrollRule rule) {
        rules.add(rule);
    }

    public List<PayrollRule> getRules() {
        return new ArrayList<>(rules); // Return a copy to prevent external modification
    }

    public PayrollResult calculate(PayrollContext context, List<PayrollRule> rulesToApply){
        rulesToApply.sort(Comparator.comparingInt(PayrollRule::getOrder)); // Sort the provided rules

        // Phase 1: Execute all EARNING rules (order < 100)
        for (PayrollRule rule: rulesToApply) {
            if (rule.getOrder() < 100) {
                rule.execute(context);
            }
        }

        // Phase 2: Calculate TOTAL_EARNINGS and inject into context
        java.math.BigDecimal totalEarnings = calculateTotalEarnings(context);
        context.setFixedValue("TOTAL_EARNINGS", totalEarnings);
        System.out.println("    💰 TOTAL_EARNINGS calculado: $" + totalEarnings);

        // Phase 3: Execute all DEDUCTION rules (order >= 100)
        for (PayrollRule rule: rulesToApply) {
            if (rule.getOrder() >= 100) {
                rule.execute(context);
            }
        }

        PayrollResult result = new PayrollResult(context);

        // Add fixed values (from employee_concept_values) to result details
        // Only add payroll concepts (P* or D*), exclude economic indicators like UMA
        context.getFixedValuesMap().forEach((code, amount) -> {
            if (code.startsWith("P") || code.startsWith("D")) {
                result.addDetail(code, amount, "Fixed value from employee");
            }
        });

        // Add calculated values (from formulas) to result details
        context.getCalculatedValues().forEach((code, amount) -> {
            result.addDetail(code, amount, "Calculated via formula");
        });

        return result;
    }

    /**
     * Calcula el total de percepciones (EARNINGS) sumando:
     * - Valores fijos que comienzan con 'P' (ej: P001 del employee_concept_values)
     * - Valores calculados que comienzan con 'P' (ej: P002, P003, P004 de las fórmulas)
     */
    private java.math.BigDecimal calculateTotalEarnings(PayrollContext context) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;

        // Sumar valores fijos de percepciones (P001, P004, etc. del employee_concept_values)
        for (java.util.Map.Entry<String, java.math.BigDecimal> entry : context.getFixedValuesMap().entrySet()) {
            if (entry.getKey().startsWith("P")) {
                total = total.add(entry.getValue());
            }
        }

        // Sumar valores calculados de percepciones (P002, P003, etc. de las fórmulas)
        for (java.util.Map.Entry<String, java.math.BigDecimal> entry : context.getCalculatedValues().entrySet()) {
            if (entry.getKey().startsWith("P")) {
                total = total.add(entry.getValue());
            }
        }

        return total;
    }
}
