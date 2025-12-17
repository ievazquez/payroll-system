package mx.payroll.system.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_number", length = 50, unique = true)
    private String employeeNumber;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "active")
    private boolean active = true;

    // Commented out - We use EmployeeConceptValue entity instead
    // @ElementCollection(fetch = FetchType.EAGER)
    // @CollectionTable(name = "employee_fixed_values", joinColumns = @JoinColumn(name = "employee_id"))
    // @MapKeyColumn(name = "concept_code")
    // @Column(name = "amount")
    // private Map<String, BigDecimal> fixedValues = new HashMap<>();

    // Constructors
    public Employee() {
    }

    public Employee(String employeeNumber, String fullName, LocalDate hireDate) {
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.hireDate = hireDate;
    }

    // Commented out - We use EmployeeConceptValue entity instead
    // public BigDecimal getFixedValue(String code) {
    //     return fixedValues.getOrDefault(code, BigDecimal.ZERO);
    // }

    // public void setFixedValue(String code, BigDecimal value) {
    //     this.fixedValues.put(code, value);
    // }

    // public Map<String, BigDecimal> getFixedValues() {
    //     return new HashMap<>(fixedValues); // Return a copy to prevent external modification
    // }

    // Getters and Setters
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) &&
               Objects.equals(employeeNumber, employee.employeeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeNumber);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", employeeNumber='" + employeeNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", hireDate=" + hireDate +
                '}';
    }
}
