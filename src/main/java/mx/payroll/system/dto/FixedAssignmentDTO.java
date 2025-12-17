package mx.payroll.system.dto;

import java.math.BigDecimal;

public class FixedAssignmentDTO {

    private String code;
    private String name;
    private BigDecimal amount;
    private String source;

    public FixedAssignmentDTO() {
    }

    public FixedAssignmentDTO(String code, String name, BigDecimal amount, String source) {
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.source = source;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
