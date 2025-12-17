package mx.payroll.system.engine;

import java.math.BigDecimal;

public class FixedIncomeRule implements PayrollRule {
    private String code;
    private int order;

    public FixedIncomeRule(String code, int order) {
        this.code = code;
        this.order = order;
    }

    @Override
    public int getOrder() { return order; }
    @Override
    public String getCode() { return code; }

    @Override
    public void execute(PayrollContext context) {
        // Logic: Get value assigned to employee, default to 0
        // Fixed values are now pre-loaded into the context's variable map or fixedValues map
        // context.getValue(code) retrieves from both fixed and calculated values
        BigDecimal value = context.getValue(code);
        context.addCalculation(code, value);
    }
}
