package mx.payroll.system.controller;

import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.dto.EmployeeConceptsResponseDTO;
import mx.payroll.system.dto.EmployeeRequestDTO;
import mx.payroll.system.dto.EmployeeResponseDTO;
import mx.payroll.system.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * GET /api/employees
     * Obtener todos los empleados
     */
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(Pageable pageable) {
        Page<Employee> employees = employeeService.getAllEmployees(pageable);
        Page<EmployeeResponseDTO> response = employees.map(EmployeeResponseDTO::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/employees/{id}
     * Obtener empleado por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Integer id) {
        return employeeService.getEmployeeById(id)
                .map(employee -> ResponseEntity.ok(EmployeeResponseDTO.fromEntity(employee)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/employees/number/{employeeNumber}
     * Obtener empleado por número de empleado
     */
    @GetMapping("/number/{employeeNumber}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByEmployeeNumber(@PathVariable String employeeNumber) {
        return employeeService.getEmployeeByEmployeeNumber(employeeNumber)
                .map(employee -> ResponseEntity.ok(EmployeeResponseDTO.fromEntity(employee)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/employees/search?name={name}
     * Buscar empleados por nombre
     */
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDTO>> searchEmployees(@RequestParam String name) {
        List<Employee> employees = employeeService.searchEmployeesByName(name);
        List<EmployeeResponseDTO> response = employees.stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/employees/hired-between?startDate={date}&endDate={date}
     * Obtener empleados contratados en un rango de fechas
     */
    @GetMapping("/hired-between")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesHiredBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Employee> employees = employeeService.getEmployeesHiredBetween(startDate, endDate);
        List<EmployeeResponseDTO> response = employees.stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/employees
     * Crear un nuevo empleado
     */
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@RequestBody EmployeeRequestDTO request) {
        Employee employee = new Employee(
                request.getEmployeeNumber(),
                request.getFullName(),
                request.getHireDate()
        );
        Employee created = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EmployeeResponseDTO.fromEntity(created));
    }

    /**
     * PUT /api/employees/{id}
     * Actualizar un empleado existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Integer id,
            @RequestBody EmployeeRequestDTO request) {
        Employee employeeData = new Employee(
                request.getEmployeeNumber(),
                request.getFullName(),
                request.getHireDate()
        );
        Employee updated = employeeService.updateEmployee(id, employeeData);
        return ResponseEntity.ok(EmployeeResponseDTO.fromEntity(updated));
    }

    /**
     * DELETE /api/employees/{id}
     * Eliminar un empleado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/employees/{name}/concepts
     * Obtener conceptos aplicables a un empleado por nombre
     */
    @GetMapping("/{name}/concepts")
    public ResponseEntity<EmployeeConceptsResponseDTO> getEmployeeConcepts(@PathVariable String name) {
        EmployeeConceptsResponseDTO response = employeeService.getEmployeeConceptsByName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler para IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Clase interna para respuestas de error
     */
    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
