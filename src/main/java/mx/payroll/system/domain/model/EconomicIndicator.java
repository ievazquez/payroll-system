package mx.payroll.system.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "economic_indicators")
public class EconomicIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code; // e.g., "UMA", "SMI"

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    public EconomicIndicator() {
    }

    public EconomicIndicator(String code, BigDecimal value, LocalDate effectiveDate) {
        this.code = code;
        this.value = value;
        this.effectiveDate = effectiveDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EconomicIndicator that = (EconomicIndicator) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(code, that.code) &&
               Objects.equals(effectiveDate, that.effectiveDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, effectiveDate);
    }

    @Override
    public String toString() {
        return "EconomicIndicator{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", value=" + value +
                ", effectiveDate=" + effectiveDate +
                '}';
    }
}
