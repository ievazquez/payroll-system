package mx.payroll.system.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "employee_concept_values")
public class EmployeeConceptValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "concept_code", nullable = false)
    private String conceptCode;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4) // Renamed from 'value'
    private BigDecimal amount;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate; // Null if it's currently active

    // Constructors
    public EmployeeConceptValue() {
    }

    public EmployeeConceptValue(Integer employeeId, String conceptCode, BigDecimal amount, LocalDate effectiveDate, LocalDate endDate) {
        this.employeeId = employeeId;
        this.conceptCode = conceptCode;
        this.amount = amount;
        this.effectiveDate = effectiveDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public BigDecimal getAmount() { // Renamed from getValue
        return amount;
    }

    public void setAmount(BigDecimal amount) { // Renamed from setValue
        this.amount = amount;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeConceptValue that = (EmployeeConceptValue) o;
        return Objects.equals(id, that.id) && Objects.equals(employeeId, that.employeeId) && Objects.equals(conceptCode, that.conceptCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeId, conceptCode);
    }

    @Override
    public String toString() {
        return "EmployeeConceptValue{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", conceptCode='" + conceptCode + "'" +
                ", amount=" + amount +
                ", effectiveDate=" + effectiveDate +
                ", endDate=" + endDate +
                '}';
    }
}
