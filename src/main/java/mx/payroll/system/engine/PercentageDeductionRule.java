package mx.payroll.system.engine;

import java.math.BigDecimal;

public class PercentageDeductionRule implements PayrollRule {
    private String code;
    private String targetCode; // The concept we apply the % to
    private BigDecimal percentage;
    private int order;

    public PercentageDeductionRule(String code, String targetCode, BigDecimal percentage, int order) {
        this.code = code;
        this.targetCode = targetCode;
        this.percentage = percentage;
        this.order = order;
    }

    @Override
    public int getOrder() { return order; }
    @Override
    public String getCode() { return code; }

    @Override
    public void execute(PayrollContext context) {
        // Logic: Find the calculated value of the target (e.g., INC01) and multiply
        BigDecimal targetValue = context.getValue(targetCode);
        BigDecimal deduction = targetValue.multiply(percentage);
        context.addCalculation(code, deduction);
    }
}
