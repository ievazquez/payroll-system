package mx.payroll.system.engine;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_results")
public class PayrollResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_id")
    private String periodId;

    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "total_earnings", precision = 19, scale = 4)
    private BigDecimal totalEarnings;

    @Column(name = "total_deductions", precision = 19, scale = 4)
    private BigDecimal totalDeductions;

    @Column(name = "net_pay", precision = 19, scale = 4)
    private BigDecimal netPay;

    @OneToMany(mappedBy = "payrollResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollResultDetail> details = new ArrayList<>();

    @Transient
    private PayrollContext context;

    public PayrollResult() {}

    public PayrollResult(PayrollContext context) {
        this.context = context;
        if (context != null && context.getEmployee() != null) {
            this.employeeId = context.getEmployee().getId();
        }
    }

    public void addDetail(String code, BigDecimal amount, String log) {
        PayrollResultDetail detail = new PayrollResultDetail(code, amount, log);
        detail.setPayrollResult(this);
        this.details.add(detail);
    }

    /**
     * Calcula y persiste los totales antes de guardar en la base de datos
     * Se ejecuta automáticamente antes de INSERT o UPDATE
     */
    @PrePersist
    @PreUpdate
    private void calculateTotals() {
        this.totalEarnings = calculateTotalEarnings();
        this.totalDeductions = calculateTotalDeductions();
        this.netPay = calculateNetPay();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPeriodId() { return periodId; }
    public void setPeriodId(String periodId) { this.periodId = periodId; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public List<PayrollResultDetail> getDetails() { return details; }
    public void setDetails(List<PayrollResultDetail> details) { this.details = details; }

    public PayrollContext getContext() { return context; }

    /**
     * Obtiene el total de percepciones
     * Si ya está calculado (cargado desde BD), devuelve el valor persistido
     * Si no, lo calcula dinámicamente desde los detalles
     */
    public BigDecimal getTotalEarnings() {
        if (totalEarnings != null) {
            return totalEarnings;
        }
        return calculateTotalEarnings();
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    /**
     * Obtiene el total de deducciones
     * Si ya está calculado (cargado desde BD), devuelve el valor persistido
     * Si no, lo calcula dinámicamente desde los detalles
     */
    public BigDecimal getTotalDeductions() {
        if (totalDeductions != null) {
            return totalDeductions;
        }
        return calculateTotalDeductions();
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    /**
     * Obtiene el pago neto
     * Si ya está calculado (cargado desde BD), devuelve el valor persistido
     * Si no, lo calcula dinámicamente desde los detalles
     */
    public BigDecimal getNetPay() {
        if (netPay != null) {
            return netPay;
        }
        return calculateNetPay();
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    /**
     * Calcula el total de percepciones desde los detalles
     * Considera conceptos que comienzan con 'P' como percepciones
     */
    private BigDecimal calculateTotalEarnings() {
        return details.stream()
            .filter(detail -> detail.getConceptCode().startsWith("P"))
            .map(PayrollResultDetail::getCalculatedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el total de deducciones desde los detalles
     * Considera conceptos que comienzan con 'D' como deducciones
     */
    private BigDecimal calculateTotalDeductions() {
        return details.stream()
            .filter(detail -> detail.getConceptCode().startsWith("D"))
            .map(PayrollResultDetail::getCalculatedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el pago neto
     * Fórmula: Percepciones - Deducciones
     */
    private BigDecimal calculateNetPay() {
        BigDecimal earnings = calculateTotalEarnings();
        BigDecimal deductions = calculateTotalDeductions();
        return earnings.subtract(deductions);
    }
}
