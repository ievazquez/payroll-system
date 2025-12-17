package mx.payroll.system.config;

import mx.payroll.system.engine.PayrollEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PayrollConfig {

    @Bean
    @Profile("!test") // Only active when 'test' profile is NOT active
    public PayrollEngine payrollEngine() { // No TaxService here
        return new PayrollEngine(); // No TaxService here
    }
}
