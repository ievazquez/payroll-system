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

        for (PayrollRule rule: rulesToApply ) { // Use the provided rules
            rule.execute(context);
        }

        PayrollResult result = new PayrollResult(context);
        
        // Convert Context Map to Result Details
        context.getCalculatedValues().forEach((code, amount) -> {
            // Note: In a real engine, we'd capture the "log" during execution. 
            // For now, we'll leave the log empty or static.
            result.addDetail(code, amount, "Calculated via Engine Rule");
        });

        return result;
    }
}
