package mx.payroll.system.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "concept_formulas")
public class ConceptFormula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concept_code", nullable = false, unique = true)
    private String conceptCode; // e.g., "GROSS_SALARY", "TAXABLE_INCOME"

    @Column(name = "formula_expression", nullable = false, length = 1000)
    private String formulaExpression; // e.g., "BASE_SALARY + OVERTIME_PAY - DEDUCTION"

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate; // Null if it's currently active

    @Column(name = "rule_order", nullable = false)
    private int order; // Order in which the rule should be applied

    // Constructors
    public ConceptFormula() {
    }

    public ConceptFormula(String conceptCode, String formulaExpression, String description, LocalDate effectiveDate, LocalDate endDate, int order) {
        this.conceptCode = conceptCode;
        this.formulaExpression = formulaExpression;
        this.description = description;
        this.effectiveDate = effectiveDate;
        this.endDate = endDate;
        this.order = order;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getFormulaExpression() {
        return formulaExpression;
    }

    public void setFormulaExpression(String formulaExpression) {
        this.formulaExpression = formulaExpression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConceptFormula that = (ConceptFormula) o;
        return Objects.equals(id, that.id) && Objects.equals(conceptCode, that.conceptCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conceptCode);
    }

    @Override
    public String toString() {
        return "ConceptFormula{"
                + "id=" + id + 
                ", conceptCode='" + conceptCode + "'"
                + ", formulaExpression='" + formulaExpression + "'"
                + ", description='" + description + "'"
                + ", effectiveDate=" + effectiveDate + 
                ", endDate=" + endDate + 
                '}';
    }
}
