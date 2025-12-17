package mx.payroll.system.service;

import mx.payroll.system.domain.model.TaxTable;
import mx.payroll.system.domain.repository.TaxTableRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class TaxService {

    private final TaxTableRepository repository;

    public TaxService(TaxTableRepository repository) {
        this.repository = repository;
    }

    /**
     * Calcula el ISR (Impuesto Sobre la Renta) usando las tablas fiscales de México
     * Fórmula: ISR = Cuota Fija + (Base Gravable - Límite Inferior) × Porcentaje sobre Excedente
     *
     * @param baseGravable Base gravable (ingresos totales gravables)
     * @return ISR calculado
     */
    public BigDecimal calculateISR(BigDecimal baseGravable) {
        return calculateISR(baseGravable, LocalDate.now().getYear(), "MENSUAL");
    }

    /**
     * Calcula el ISR con año y tipo de tabla específicos
     *
     * @param baseGravable Base gravable
     * @param fiscalYear Año fiscal (ej: 2024)
     * @param tableType Tipo de tabla ('MENSUAL', 'QUINCENAL', 'ANUAL')
     * @return ISR calculado
     */
    public BigDecimal calculateISR(BigDecimal baseGravable, int fiscalYear, String tableType) {
        System.out.println("\n    🧮 ========== CÁLCULO ISR ==========");
        System.out.println("    📊 Base Gravable: $" + baseGravable);
        System.out.println("    📅 Año Fiscal: " + fiscalYear);
        System.out.println("    📋 Tipo Tabla: " + tableType);

        // Validación
        if (baseGravable == null || baseGravable.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("    ⚠️  Base gravable <= 0, ISR = $0.00");
            System.out.println("    ===================================\n");
            return BigDecimal.ZERO;
        }

        // Buscar el tramo fiscal aplicable
        Optional<TaxTable> bracketOpt = repository.findApplicableBracket(fiscalYear, tableType, baseGravable);

        if (bracketOpt.isEmpty()) {
            System.err.println("    ❌ ERROR: No se encontró tramo fiscal para:");
            System.err.println("       - Año: " + fiscalYear);
            System.err.println("       - Tipo: " + tableType);
            System.err.println("       - Base: $" + baseGravable);
            System.err.println("    ⚠️  Usando ISR = $0.00 por defecto");
            System.out.println("    ===================================\n");
            return BigDecimal.ZERO;
        }

        TaxTable bracket = bracketOpt.get();
        System.out.println("    ✅ Tramo encontrado:");
        System.out.println("       - Límite Inferior: $" + bracket.getLowerLimit());
        System.out.println("       - Cuota Fija: $" + bracket.getFixedFee());
        System.out.println("       - % sobre Excedente: " + bracket.getPercentExcess().multiply(new BigDecimal("100")) + "%");

        // Calcular excedente sobre límite inferior
        BigDecimal excedente = baseGravable.subtract(bracket.getLowerLimit());
        System.out.println("    📐 Excedente: $" + baseGravable + " - $" + bracket.getLowerLimit() + " = $" + excedente);

        // Calcular impuesto sobre excedente
        BigDecimal impuestoExcedente = excedente.multiply(bracket.getPercentExcess())
                .setScale(2, RoundingMode.HALF_EVEN);
        System.out.println("    💵 Impuesto sobre excedente: $" + excedente + " × " + bracket.getPercentExcess() + " = $" + impuestoExcedente);

        // ISR total = Cuota Fija + Impuesto sobre Excedente
        BigDecimal isr = bracket.getFixedFee().add(impuestoExcedente)
                .setScale(2, RoundingMode.HALF_EVEN);

        System.out.println("    🎯 ISR Total: $" + bracket.getFixedFee() + " + $" + impuestoExcedente + " = $" + isr);
        System.out.println("    ===================================\n");

        return isr;
    }
}
