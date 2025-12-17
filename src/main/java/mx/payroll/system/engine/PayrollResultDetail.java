package mx.payroll.system.engine;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payroll_result_details")
public class PayrollResultDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_result_id")
    private PayrollResult payrollResult;

    @Column(name = "concept_code", nullable = false)
    private String conceptCode;

    @Column(name = "calculated_amount", nullable = false)
    private BigDecimal calculatedAmount;

    @Column(name = "calculation_log")
    private String calculationLog;

    // Constructors
    public PayrollResultDetail() {}

    public PayrollResultDetail(String conceptCode, BigDecimal calculatedAmount, String calculationLog) {
        this.conceptCode = conceptCode;
        this.calculatedAmount = calculatedAmount;
        this.calculationLog = calculationLog;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PayrollResult getPayrollResult() { return payrollResult; }
    public void setPayrollResult(PayrollResult payrollResult) { this.payrollResult = payrollResult; }

    public String getConceptCode() { return conceptCode; }
    public void setConceptCode(String conceptCode) { this.conceptCode = conceptCode; }

    public BigDecimal getCalculatedAmount() { return calculatedAmount; }
    public void setCalculatedAmount(BigDecimal calculatedAmount) { this.calculatedAmount = calculatedAmount; }

    public String getCalculationLog() { return calculationLog; }
    public void setCalculationLog(String calculationLog) { this.calculationLog = calculationLog; }
}
