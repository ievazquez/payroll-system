package mx.payroll.system.engine;

public interface PayrollRule {
    String getCode();
    int getOrder();
    void execute(PayrollContext context);
}