package mx.payroll.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.dto.EmployeeRequestDTO;
import mx.payroll.system.dto.EmployeeResponseDTO;
import mx.payroll.system.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
@DisplayName("Employee Controller Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("GET /api/employees - Debe retornar todos los empleados paginados")
    void shouldGetAllEmployees() throws Exception {
        // Given
        Employee emp1 = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        emp1.setId(1);
        Employee emp2 = new Employee("EMP-002", "María García", LocalDate.of(2021, 6, 10));
        emp2.setId(2);

        List<Employee> employees = Arrays.asList(emp1, emp2);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), employees.size());

        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(employeePage);

        // When & Then
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].employeeNumber", is("EMP-001")))
                .andExpect(jsonPath("$.content[0].fullName", is("Juan Pérez")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].employeeNumber", is("EMP-002")));

        verify(employeeService, times(1)).getAllEmployees(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Debe retornar empleado por ID")
    void shouldGetEmployeeById() throws Exception {
        // Given
        Employee employee = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        employee.setId(1);

        when(employeeService.getEmployeeById(1)).thenReturn(Optional.of(employee));

        // When & Then
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.employeeNumber", is("EMP-001")))
                .andExpect(jsonPath("$.fullName", is("Juan Pérez")));

        verify(employeeService, times(1)).getEmployeeById(1);
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Debe retornar 404 si no existe")
    void shouldReturn404WhenEmployeeNotFound() throws Exception {
        // Given
        when(employeeService.getEmployeeById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(999);
    }

    @Test
    @DisplayName("GET /api/employees/number/{employeeNumber} - Debe retornar empleado por número de empleado")
    void shouldGetEmployeeByEmployeeNumber() throws Exception {
        // Given
        Employee employee = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        employee.setId(1);

        when(employeeService.getEmployeeByEmployeeNumber("EMP-001")).thenReturn(Optional.of(employee));

        // When & Then
        mockMvc.perform(get("/api/employees/number/EMP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeNumber", is("EMP-001")));

        verify(employeeService, times(1)).getEmployeeByEmployeeNumber("EMP-001");
    }

    @Test
    @DisplayName("GET /api/employees/search?name={name} - Debe buscar empleados por nombre")
    void shouldSearchEmployeesByName() throws Exception {
        // Given
        Employee employee = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        employee.setId(1);

        when(employeeService.searchEmployeesByName("juan")).thenReturn(Arrays.asList(employee));

        // When & Then
        mockMvc.perform(get("/api/employees/search")
                        .param("name", "juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Juan Pérez")));

        verify(employeeService, times(1)).searchEmployeesByName("juan");
    }

    @Test
    @DisplayName("GET /api/employees/hired-between - Debe retornar empleados en rango de fechas")
    void shouldGetEmployeesHiredBetween() throws Exception {
        // Given
        Employee emp1 = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        emp1.setId(1);
        Employee emp2 = new Employee("EMP-002", "María García", LocalDate.of(2021, 6, 10));
        emp2.setId(2);

        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2021, 12, 31);

        when(employeeService.getEmployeesHiredBetween(startDate, endDate))
                .thenReturn(Arrays.asList(emp1, emp2));

        // When & Then
        mockMvc.perform(get("/api/employees/hired-between")
                        .param("startDate", "2020-01-01")
                        .param("endDate", "2021-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(employeeService, times(1)).getEmployeesHiredBetween(startDate, endDate);
    }

    @Test
    @DisplayName("POST /api/employees - Debe crear un nuevo empleado")
    void shouldCreateEmployee() throws Exception {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "EMP-001",
                "Juan Pérez",
                LocalDate.of(2020, 1, 15)
        );

        Employee created = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        created.setId(1);

        when(employeeService.createEmployee(any(Employee.class))).thenReturn(created);

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.employeeNumber", is("EMP-001")))
                .andExpect(jsonPath("$.fullName", is("Juan Pérez")));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("POST /api/employees - Debe retornar 400 cuando el ID ya existe")
    void shouldReturn400WhenCreatingDuplicateEmployee() throws Exception {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "EMP-001",
                "Juan Pérez",
                LocalDate.of(2020, 1, 15)
        );

        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(new IllegalArgumentException("Ya existe un empleado con el ID: EMP-001"));

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Ya existe un empleado con el ID: EMP-001")));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Debe actualizar un empleado")
    void shouldUpdateEmployee() throws Exception {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "EMP-001-UPDATED",
                "Juan Carlos Pérez",
                LocalDate.of(2020, 2, 1)
        );

        Employee updated = new Employee("EMP-001-UPDATED", "Juan Carlos Pérez", LocalDate.of(2020, 2, 1));
        updated.setId(1);

        when(employeeService.updateEmployee(anyInt(), any(Employee.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.employeeNumber", is("EMP-001-UPDATED")))
                .andExpect(jsonPath("$.fullName", is("Juan Carlos Pérez")));

        verify(employeeService, times(1)).updateEmployee(anyInt(), any(Employee.class));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Debe retornar 400 si el empleado no existe")
    void shouldReturn400WhenUpdatingNonExistentEmployee() throws Exception {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "EMP-999",
                "Test User",
                LocalDate.now()
        );

        when(employeeService.updateEmployee(anyInt(), any(Employee.class)))
                .thenThrow(new IllegalArgumentException("Empleado no encontrado con ID: 999"));

        // When & Then
        mockMvc.perform(put("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Empleado no encontrado con ID: 999")));

        verify(employeeService, times(1)).updateEmployee(anyInt(), any(Employee.class));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Debe eliminar un empleado")
    void shouldDeleteEmployee() throws Exception {
        // Given
        doNothing().when(employeeService).deleteEmployee(1);

        // When & Then
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1);
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Debe retornar 400 si el empleado no existe")
    void shouldReturn400WhenDeletingNonExistentEmployee() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Empleado no encontrado con ID: 999"))
                .when(employeeService).deleteEmployee(999);

        // When & Then
        mockMvc.perform(delete("/api/employees/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Empleado no encontrado con ID: 999")));

        verify(employeeService, times(1)).deleteEmployee(999);
    }
}
