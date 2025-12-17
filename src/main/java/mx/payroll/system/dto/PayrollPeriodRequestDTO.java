package mx.payroll.system.dto;

import java.time.LocalDate;

public class PayrollPeriodRequestDTO {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    public PayrollPeriodRequestDTO() {
    }

    public PayrollPeriodRequestDTO(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
