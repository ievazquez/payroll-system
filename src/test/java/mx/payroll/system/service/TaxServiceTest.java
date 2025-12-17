package mx.payroll.system.service;

import mx.payroll.system.domain.model.TaxTable;
import mx.payroll.system.domain.repository.TaxTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaxService - Cálculo de ISR")
class TaxServiceTest {

    @Mock
    private TaxTableRepository taxTableRepository;

    @InjectMocks
    private TaxService taxService;

    // Tramo fiscal de ejemplo (Basado en tabla ISR 2024)
    // Para ingresos entre $12,935.83 y $15,487.72
    private TaxTable mockBracket;

    @BeforeEach
    void setUp() {
        // Tramo: Límite Inferior = $12,935.83, Cuota Fija = $1,182.88, % Excedente = 17.92%
        mockBracket = new TaxTable();
        mockBracket.setId(5L);
        mockBracket.setFiscalYear(2024);
        mockBracket.setTableType("MENSUAL");
        mockBracket.setLowerLimit(new BigDecimal("12935.83"));
        mockBracket.setFixedFee(new BigDecimal("1182.88"));
        mockBracket.setPercentExcess(new BigDecimal("0.1792"));
    }

    @Test
    @DisplayName("Debe calcular ISR correctamente para ingreso de $15,000")
    void testCalculateISR_NormalIncome() {
        // Given
        BigDecimal baseGravable = new BigDecimal("15000.00");

        when(taxTableRepository.findApplicableBracket(2024, "MENSUAL", baseGravable))
                .thenReturn(Optional.of(mockBracket));

        // When
        BigDecimal isr = taxService.calculateISR(baseGravable, 2024, "MENSUAL");

        // Then
        // Cálculo esperado:
        // Excedente = $15,000.00 - $12,935.83 = $2,064.17
        // Impuesto Excedente = $2,064.17 × 0.1792 = $369.90
        // ISR Total = $1,182.88 + $369.90 = $1,552.78

        assertThat(isr).isNotNull();
        assertThat(isr).isEqualByComparingTo(new BigDecimal("1552.78"));
    }

    @Test
    @DisplayName("Debe calcular ISR correctamente para ingreso exacto en límite inferior")
    void testCalculateISR_ExactlyAtLowerLimit() {
        // Given
        BigDecimal baseGravable = new BigDecimal("12935.83");

        when(taxTableRepository.findApplicableBracket(2024, "MENSUAL", baseGravable))
                .thenReturn(Optional.of(mockBracket));

        // When
        BigDecimal isr = taxService.calculateISR(baseGravable, 2024, "MENSUAL");

        // Then
        // Excedente = $12,935.83 - $12,935.83 = $0
        // ISR Total = $1,182.88 + $0 = $1,182.88

        assertThat(isr).isNotNull();
        assertThat(isr).isEqualByComparingTo(new BigDecimal("1182.88"));
    }

    @Test
    @DisplayName("Debe retornar $0 cuando base gravable es nula")
    void testCalculateISR_NullBaseGravable() {
        // When
        BigDecimal isr = taxService.calculateISR(null, 2024, "MENSUAL");

        // Then
        assertThat(isr).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe retornar $0 cuando base gravable es cero")
    void testCalculateISR_ZeroBaseGravable() {
        // When
        BigDecimal isr = taxService.calculateISR(BigDecimal.ZERO, 2024, "MENSUAL");

        // Then
        assertThat(isr).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe retornar $0 cuando base gravable es negativa")
    void testCalculateISR_NegativeBaseGravable() {
        // When
        BigDecimal isr = taxService.calculateISR(new BigDecimal("-1000"), 2024, "MENSUAL");

        // Then
        assertThat(isr).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe retornar $0 cuando no se encuentra tramo fiscal")
    void testCalculateISR_NoTaxBracketFound() {
        // Given
        BigDecimal baseGravable = new BigDecimal("50000.00");

        when(taxTableRepository.findApplicableBracket(anyInt(), anyString(), any(BigDecimal.class)))
                .thenReturn(Optional.empty());

        // When
        BigDecimal isr = taxService.calculateISR(baseGravable, 2024, "MENSUAL");

        // Then
        assertThat(isr).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe calcular ISR con método simplificado (año actual)")
    void testCalculateISR_DefaultYearAndType() {
        // Given
        BigDecimal baseGravable = new BigDecimal("15000.00");
        int currentYear = java.time.LocalDate.now().getYear();

        when(taxTableRepository.findApplicableBracket(currentYear, "MENSUAL", baseGravable))
                .thenReturn(Optional.of(mockBracket));

        // When
        BigDecimal isr = taxService.calculateISR(baseGravable);

        // Then
        assertThat(isr).isNotNull();
        assertThat(isr).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe calcular ISR para diferentes tipos de tabla")
    void testCalculateISR_QuincenalTable() {
        // Given
        BigDecimal baseGravable = new BigDecimal("7500.00");
        TaxTable quincenalBracket = new TaxTable();
        quincenalBracket.setLowerLimit(new BigDecimal("6467.92"));
        quincenalBracket.setFixedFee(new BigDecimal("591.44"));
        quincenalBracket.setPercentExcess(new BigDecimal("0.1792"));

        when(taxTableRepository.findApplicableBracket(2024, "QUINCENAL", baseGravable))
                .thenReturn(Optional.of(quincenalBracket));

        // When
        BigDecimal isr = taxService.calculateISR(baseGravable, 2024, "QUINCENAL");

        // Then
        // Excedente = $7,500.00 - $6,467.92 = $1,032.08
        // Impuesto Excedente = $1,032.08 × 0.1792 = $184.95
        // ISR Total = $591.44 + $184.95 = $776.39

        assertThat(isr).isNotNull();
        assertThat(isr).isEqualByComparingTo(new BigDecimal("776.39"));
    }

    @Test
    @DisplayName("Debe redondear correctamente a 2 decimales")
    void testCalculateISR_RoundingCorrectly() {
        // Given
        BigDecimal baseGravable = new BigDecimal("13000.50");

        when(taxTableRepository.findApplicableBracket(2024, "MENSUAL", baseGravable))
                .thenReturn(Optional.of(mockBracket));

        // When
        BigDecimal isr = taxService.calculateISR(baseGravable, 2024, "MENSUAL");

        // Then
        // Verificar que tiene exactamente 2 decimales
        assertThat(isr.scale()).isEqualTo(2);
    }
}
