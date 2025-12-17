package mx.payroll.system.engine;

import java.math.BigDecimal;
import java.util.Map; // Import Map if not already present

public class DynamicDbRule implements PayrollRule {
    private final String code; // conceptCode
    private final int order;
    private final String expression; // formulaExpression

    // Assuming FormulaEngine is needed to evaluate the expression
    private final FormulaEngine formulaEngine; // Injected later or passed in constructor

    public DynamicDbRule(String code, int order, String expression, FormulaEngine formulaEngine) {
        this.code = code;
        this.order = order;
        this.expression = expression;
        this.formulaEngine = formulaEngine;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void execute(PayrollContext context) {
        // Here we evaluate the expression using the FormulaEngine and store the result
        // The context will provide the variables needed for evaluation
        // Assuming context.getCalculatedValues() returns a Map<String, BigDecimal> suitable for SpEL
        try {
            BigDecimal result = formulaEngine.evaluate(expression, context); // Pass the full context
            context.addCalculation(code, result); // Add the result to the context
        } catch (Exception e) {
            // DEBUG: Log which concept/formula is failing
            System.err.println("ERROR evaluating concept " + code + " with formula: " + expression);
            System.err.println("Employee ID: " + (context.getEmployee() != null ? context.getEmployee().getId() : "unknown"));
            System.err.println("Available variables: " + context.getVariables().keySet());
            throw e; // Re-throw to maintain original behavior
        }
    }
}
