package mx.payroll.system.dto;

import java.time.LocalDate;

public class EmployeeRequestDTO {

    private String employeeNumber;
    private String fullName;
    private LocalDate hireDate;

    public EmployeeRequestDTO() {
    }

    public EmployeeRequestDTO(String employeeNumber, String fullName, LocalDate hireDate) {
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.hireDate = hireDate;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
