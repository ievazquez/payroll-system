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
}
