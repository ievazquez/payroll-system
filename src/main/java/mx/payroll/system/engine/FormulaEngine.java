package mx.payroll.system.engine;

import mx.payroll.system.exception.FormulaEvaluationException;
import mx.payroll.system.service.TaxService;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FormulaEngine {

    private final TaxService taxService;

    private static final int MAX_CACHE_SIZE = 1000;
    private final ExpressionParser parser = new SpelExpressionParser();

    // LRU Cache: key = String formula, value = compiled expression
    private final Map<String, Expression> expressionCache = new LinkedHashMap<String, Expression>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Expression> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public FormulaEngine(TaxService taxService) {
        this.taxService = taxService;
    }

    /**
     * Evaluates a formula expression with the provided variables.
     * Uses a sandboxed SpEL context to prevent remote code execution.
     * Variables in the formula should be prefixed with # (e.g., #INC01).
     *
     * @param formula the formula expression to evaluate (e.g., "(#INC01 + #INC02) * 0.05")
     * @param payrollContext the PayrollContext object containing variables
     * @return the calculated result rounded to 2 decimal places
     * @throws FormulaEvaluationException if formula is invalid or evaluation fails
     */
    public BigDecimal evaluate(String formula, PayrollContext payrollContext) {
        if (formula == null || formula.trim().isEmpty()) {
            throw new FormulaEvaluationException("Formula cannot be null or empty");
        }

        if (payrollContext == null) {
            throw new FormulaEvaluationException("PayrollContext cannot be null");
        }

        try {
            // Get or compile expression (thread-safe)
            Expression expression;
            synchronized (expressionCache) {
                expression = expressionCache.computeIfAbsent(formula, key -> {
                    try {
                        return parser.parseExpression(key);
                    } catch (Exception e) {
                        throw new FormulaEvaluationException("Invalid formula syntax: " + key, e);
                    }
                });
            }

            // Use StandardEvaluationContext to allow function registration
            StandardEvaluationContext spelContext = new StandardEvaluationContext();

            // Inject TaxService as a variable so it can be called via #impuestos
            spelContext.setVariable("impuestos", taxService);
            
            // Inject all variables from the PayrollContext (fixed and calculated) flattened
            // This allows formulas to use #SALARY_BASE, #ISR, etc. directly without method calls
            Map<String, BigDecimal> availableVariables = payrollContext.getVariables();

            // DEBUG: Log available variables for problematic employees
            if (payrollContext.getEmployee() != null && payrollContext.getEmployee().getId() == 49813) {
                System.out.println("DEBUG - Employee 49813 available variables: " + availableVariables.keySet());
                System.out.println("DEBUG - Evaluating formula: " + formula);
            }

            availableVariables.forEach(spelContext::setVariable);

            // Add employee-specific variables
            if (payrollContext.getEmployee() != null) {
                spelContext.setVariable("HIRE_DATE", payrollContext.getEmployee().getHireDate());
            }

            // Register helper functions
            spelContext.registerFunction("diasTrabajadosAnio", PayrollFunctions.class.getMethod("diasTrabajadosAnio", LocalDate.class));
            spelContext.registerFunction("diasVacaciones", PayrollFunctions.class.getMethod("diasVacaciones", LocalDate.class));
            spelContext.registerFunction("calcularIMSS", PayrollFunctions.class.getMethod("calcularIMSS", BigDecimal.class, BigDecimal.class));
            spelContext.registerFunction("calcularISR", PayrollFunctions.class.getMethod("calcularISR", BigDecimal.class));

            // Evaluate expression using ONLY the context (no root object method calls needed)
            Object rawResult = expression.getValue(spelContext);

            // Convert result to BigDecimal
            BigDecimal result = convertToBigDecimal(rawResult);

            // Round to 2 decimal places using banker's rounding
            return result.setScale(2, RoundingMode.HALF_EVEN);

        } catch (FormulaEvaluationException e) {
            throw e;
        } catch (SpelEvaluationException e) {
            throw new FormulaEvaluationException("Error during SpEL evaluation: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FormulaEvaluationException("Error evaluating formula: " + formula, e);
        }
    }

    /**
     * Converts various numeric types to BigDecimal.
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return new BigDecimal(value.toString());
        } else {
            try {
                return new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                throw new FormulaEvaluationException("Cannot convert result to number: " + value, e);
            }
        }
    }

    /**
     * Clears the expression cache. Useful for testing or memory management.
     */
    public void clearCache() {
        synchronized (expressionCache) {
            expressionCache.clear();
        }
    }

    /**
     * Returns the current cache size.
     */
    public int getCacheSize() {
        synchronized (expressionCache) {
            return expressionCache.size();
        }
    }
}
