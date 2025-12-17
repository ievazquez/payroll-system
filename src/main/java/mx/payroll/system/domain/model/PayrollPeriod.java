package mx.payroll.system.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "payroll_periods")
public class PayrollPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "period_identifier", unique = true)
    private String periodIdentifier; // To match the ID passed by dispatcher

    private String status;

    @Column(name = "total_expected")
    private Integer totalExpected = 0; // Total number of employees expected to process

    // Constructors
    public PayrollPeriod() {
    }

    public PayrollPeriod(LocalDate startDate, LocalDate endDate, String status, String periodIdentifier) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.periodIdentifier = periodIdentifier;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPeriodIdentifier() {
        return periodIdentifier;
    }

    public void setPeriodIdentifier(String periodIdentifier) {
        this.periodIdentifier = periodIdentifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalExpected() {
        return totalExpected;
    }

    public void setTotalExpected(Integer totalExpected) {
        this.totalExpected = totalExpected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayrollPeriod that = (PayrollPeriod) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startDate, endDate);
    }

    @Override
    public String toString() {
        return "PayrollPeriod{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                '}';
    }
}

