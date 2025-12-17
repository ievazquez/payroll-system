package mx.payroll.system.service;

import mx.payroll.system.domain.model.Concept;
import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.domain.model.EmployeeConceptValue;
import mx.payroll.system.domain.repository.ConceptRepository;
import mx.payroll.system.domain.repository.EmployeeConceptValueRepository;
import mx.payroll.system.domain.repository.EmployeeRepository;
import mx.payroll.system.dto.EmployeeConceptsResponseDTO;
import mx.payroll.system.dto.FixedAssignmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeConceptValueRepository employeeConceptValueRepository;
    private final ConceptRepository conceptRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                          EmployeeConceptValueRepository employeeConceptValueRepository,
                          ConceptRepository conceptRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeConceptValueRepository = employeeConceptValueRepository;
        this.conceptRepository = conceptRepository;
    }

    /**
     * Crear un nuevo empleado
     */
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmployeeNumber(employee.getEmployeeNumber())) {
            throw new IllegalArgumentException("Ya existe un empleado con el ID: " + employee.getEmployeeNumber());
        }
        return employeeRepository.save(employee);
    }

    /**
     * Obtener todos los empleados (paginado)
     */
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Buscar empleado por ID
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    /**
     * Buscar empleado por número de empleado
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmployeeNumber(String employeeNumber) {
        return employeeRepository.findByEmployeeNumber(employeeNumber);
    }

    /**
     * Buscar empleados por nombre
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(String name) {
        return employeeRepository.findByFullNameContainingIgnoreCase(name);
    }

    /**
     * Actualizar empleado
     */
    public Employee updateEmployee(Integer id, Employee employeeData) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con ID: " + id));

        employee.setFullName(employeeData.getFullName());
        employee.setEmployeeNumber(employeeData.getEmployeeNumber());
        employee.setHireDate(employeeData.getHireDate());

        return employeeRepository.save(employee);
    }

    /**
     * Eliminar empleado
     */
    public void deleteEmployee(Integer id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Empleado no encontrado con ID: " + id);
        }
        employeeRepository.deleteById(id);
    }

    /**
     * Obtener empleados contratados en un rango de fechas
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesHiredBetween(LocalDate startDate, LocalDate endDate) {
        return employeeRepository.findByHireDateBetween(startDate, endDate);
    }

    /**
     * Obtener los conceptos aplicables a un empleado por nombre
     */
    @Transactional(readOnly = true)
    public EmployeeConceptsResponseDTO getEmployeeConceptsByName(String employeeName) {
        // Buscar empleado por nombre (case insensitive)
        List<Employee> employees = employeeRepository.findByFullNameContainingIgnoreCase(employeeName);

        if (employees.isEmpty()) {
            throw new IllegalArgumentException("No se encontró ningún empleado con el nombre: " + employeeName);
        }

        // Si hay múltiples coincidencias, tomar el primero
        Employee employee = employees.get(0);

        EmployeeConceptsResponseDTO response = new EmployeeConceptsResponseDTO(employee.getFullName());

        // Obtener valores fijos del empleado (employee_concept_values)
        LocalDate now = LocalDate.now();
        List<EmployeeConceptValue> employeeValues = employeeConceptValueRepository
                .findByEmployeeIdAndEffectiveDateBeforeAndEndDateAfterOrEndDateIsNull(
                        employee.getId(), now, now);

        // Convertir valores fijos a DTOs
        List<FixedAssignmentDTO> fixedAssignments = employeeValues.stream()
                .map(ecv -> {
                    Concept concept = conceptRepository.findById(ecv.getConceptCode()).orElse(null);
                    String conceptName = concept != null ? concept.getName() : "Unknown";
                    return new FixedAssignmentDTO(
                            ecv.getConceptCode(),
                            conceptName,
                            ecv.getAmount(),
                            "Employee Specific Value"
                    );
                })
                .collect(Collectors.toList());

        response.setFixedAssignments(fixedAssignments);

        // Obtener conceptos calculados (los que tienen fórmulas globales)
        List<Concept> allConcepts = conceptRepository.findActiveConceptsOrderedByCalculation();

        // Filtrar conceptos que tienen valores específicos del empleado
        List<String> employeeConceptCodes = employeeValues.stream()
                .map(EmployeeConceptValue::getConceptCode)
                .collect(Collectors.toList());

        // Los conceptos globales son aquellos que no tienen valor específico
        List<String> globalRules = allConcepts.stream()
                .filter(c -> !employeeConceptCodes.contains(c.getCode()))
                .map(c -> c.getCode() + " - " + c.getName() + " (Calculated)")
                .collect(Collectors.toList());

        response.setApplicableGlobalRules(globalRules);

        return response;
    }
}
