package mx.payroll.system.config;

import mx.payroll.system.engine.FixedIncomeRule;
import mx.payroll.system.engine.PayrollEngine;
import mx.payroll.system.engine.PayrollRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("test") // This configuration will only be active when the "test" profile is active
public class PayrollTestConfig {

    @Bean
    public PayrollEngine payrollEngine() { // No TaxService here
        PayrollEngine engine = new PayrollEngine(); // No TaxService here
        // Add some basic rules for testing purposes
        // Rule to capture a fixed income (e.g., BASE_SALARY)
        engine.addRule(new FixedIncomeRule("BASE_SALARY", 1));
        // You can add more test rules here if needed for more complex scenarios
        return engine;
    }
}
