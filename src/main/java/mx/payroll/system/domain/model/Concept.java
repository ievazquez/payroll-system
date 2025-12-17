package mx.payroll.system.domain.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "concepts")
public class Concept {

    @Id
    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "type", length = 20, nullable = false)
    private String type; // 'EARNING', 'DEDUCTION'

    @Column(name = "calculation_order", nullable = false)
    private Integer calculationOrder;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public Concept() {
    }

    public Concept(String code, String name, String type, Integer calculationOrder) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.calculationOrder = calculationOrder;
        this.isActive = true;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCalculationOrder() {
        return calculationOrder;
    }

    public void setCalculationOrder(Integer calculationOrder) {
        this.calculationOrder = calculationOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concept concept = (Concept) o;
        return Objects.equals(code, concept.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Concept{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", calculationOrder=" + calculationOrder +
                ", isActive=" + isActive +
                '}';
    }
}
