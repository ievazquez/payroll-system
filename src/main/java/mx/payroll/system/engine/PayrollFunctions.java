package mx.payroll.system.engine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PayrollFunctions {

    public static double diasTrabajadosAnio(LocalDate hireDate) {
        if (hireDate == null) return 0;
        return 365.0; // Simplification
    }

    public static int diasVacaciones(LocalDate hireDate) {
        if (hireDate == null) return 0;
        long years = ChronoUnit.YEARS.between(hireDate, LocalDate.now());
        if (years < 1) return 0;
        if (years == 1) return 12;
        return 12 + ((int)years - 1) * 2; // Simplification
    }

    public static BigDecimal calcularIMSS(BigDecimal salary, BigDecimal uma) {
        if (salary == null) return BigDecimal.ZERO;
        return salary.multiply(new BigDecimal("0.027")); // 2.7% simplification
    }
    
    public static BigDecimal calcularISR(BigDecimal amount) {
         if (amount == null) return BigDecimal.ZERO;
         return amount.multiply(new BigDecimal("0.10")); // 10% simplification
    }
}
