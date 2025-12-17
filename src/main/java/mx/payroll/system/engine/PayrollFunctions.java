package mx.payroll.system.engine;

import mx.payroll.system.service.TaxService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PayrollFunctions {

    // Instance variable to hold TaxService (will be set by FormulaEngine)
    private static TaxService taxService;

    /**
     * Sets the TaxService instance to be used by calcularISR
     * This is called by FormulaEngine during initialization
     */
    public static void setTaxService(TaxService service) {
        taxService = service;
    }

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
        System.out.println("    💊 IMSS: $" + salary + " × 2.7% = $" + salary.multiply(new BigDecimal("0.027")));
        return salary.multiply(new BigDecimal("0.027")); // 2.7% simplification
    }

    /**
     * Calcula el ISR usando las tablas fiscales reales de México
     * Si TaxService no está disponible, usa simplificación del 10%
     */
    public static BigDecimal calcularISR(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("    📊 ISR: Base gravable = $0, ISR = $0.00");
            return BigDecimal.ZERO;
        }

        if (taxService != null) {
            // Usar el servicio real con tablas fiscales
            return taxService.calculateISR(amount);
        } else {
            // Fallback: simplificación del 10%
            System.err.println("    ⚠️  TaxService no disponible, usando simplificación 10%");
            BigDecimal isr = amount.multiply(new BigDecimal("0.10"));
            System.out.println("    📊 ISR (simplificado): $" + amount + " × 10% = $" + isr);
            return isr;
        }
    }
}
