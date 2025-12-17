package mx.payroll.system.dto;

import mx.payroll.system.domain.model.Employee;

import java.time.LocalDate;

public class EmployeeResponseDTO {

    private Integer id;
    private String employeeNumber;
    private String fullName;
    private LocalDate hireDate;

    public EmployeeResponseDTO() {
    }

    public EmployeeResponseDTO(Integer id, String employeeNumber, String fullName, LocalDate hireDate) {
        this.id = id;
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.hireDate = hireDate;
    }

    public static EmployeeResponseDTO fromEntity(Employee employee) {
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getEmployeeNumber(),
                employee.getFullName(),
                employee.getHireDate()
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
