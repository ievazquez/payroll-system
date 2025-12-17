package mx.payroll.system.dto;

import java.util.ArrayList;
import java.util.List;

public class EmployeeConceptsResponseDTO {

    private String employee;
    private List<FixedAssignmentDTO> fixedAssignments = new ArrayList<>();
    private List<String> applicableGlobalRules = new ArrayList<>();

    public EmployeeConceptsResponseDTO() {
    }

    public EmployeeConceptsResponseDTO(String employee) {
        this.employee = employee;
    }

    // Getters and Setters
    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public List<FixedAssignmentDTO> getFixedAssignments() {
        return fixedAssignments;
    }

    public void setFixedAssignments(List<FixedAssignmentDTO> fixedAssignments) {
        this.fixedAssignments = fixedAssignments;
    }

    public List<String> getApplicableGlobalRules() {
        return applicableGlobalRules;
    }

    public void setApplicableGlobalRules(List<String> applicableGlobalRules) {
        this.applicableGlobalRules = applicableGlobalRules;
    }
}
