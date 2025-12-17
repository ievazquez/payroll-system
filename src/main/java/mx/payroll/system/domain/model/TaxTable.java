package mx.payroll.system.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate; // Keep LocalDate if needed for other parts, though not for bracket query
import java.util.Objects;

@Entity
@Table(name = "tax_tables")
public class TaxTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "table_type", nullable = false)
    private String tableType; // e.g., "ISR_MONTHLY", "SUBSIDY_MONTHLY"

    @Column(name = "lower_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal lowerLimit;

    @Column(name = "upper_limit", precision = 19, scale = 4) // Upper limit can be null for the last bracket
    private BigDecimal upperLimit;

    @Column(name = "rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal rate;

    // Constructors
    public TaxTable() {
    }

    public TaxTable(Integer fiscalYear, String tableType, BigDecimal lowerLimit, BigDecimal upperLimit, BigDecimal rate) {
        this.fiscalYear = fiscalYear;
        this.tableType = tableType;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.rate = rate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public BigDecimal getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(BigDecimal lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public BigDecimal getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(BigDecimal upperLimit) {
        this.upperLimit = upperLimit;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxTable taxTable = (TaxTable) o;
        return Objects.equals(id, taxTable.id) &&
               Objects.equals(fiscalYear, taxTable.fiscalYear) &&
               Objects.equals(tableType, taxTable.tableType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fiscalYear, tableType);
    }

    @Override
    public String toString() {
        return "TaxTable{" +
                "id=" + id +
                ", fiscalYear=" + fiscalYear +
                ", tableType='" + tableType + '\'' +
                ", lowerLimit=" + lowerLimit +
                ", upperLimit=" + upperLimit +
                ", rate=" + rate +
                '}';
    }
}


