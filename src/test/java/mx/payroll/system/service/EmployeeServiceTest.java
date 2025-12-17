package mx.payroll.system.service;

import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.domain.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        employee1 = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        employee1.setId(1);

        employee2 = new Employee("EMP-002", "María García", LocalDate.of(2021, 6, 10));
        employee2.setId(2);
    }

    @Test
    @DisplayName("Debe crear un empleado exitosamente")
    void shouldCreateEmployeeSuccessfully() {
        // Given
        when(employeeRepository.existsByEmployeeNumber(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        // When
        Employee created = employeeService.createEmployee(employee1);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getEmployeeNumber()).isEqualTo("EMP-001");
        verify(employeeRepository, times(1)).existsByEmployeeNumber("EMP-001");
        verify(employeeRepository, times(1)).save(employee1);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el número de empleado ya existe")
    void shouldThrowExceptionWhenEmployeeNumberExists() {
        // Given
        when(employeeRepository.existsByEmployeeNumber("EMP-001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> employeeService.createEmployee(employee1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un empleado con el ID: EMP-001");

        verify(employeeRepository, times(1)).existsByEmployeeNumber("EMP-001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Debe obtener todos los empleados paginados")
    void shouldGetAllEmployeesPaginated() {
        // Given
        List<Employee> employees = Arrays.asList(employee1, employee2);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getContent()).containsExactly(employee1, employee2);
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Debe encontrar empleado por ID")
    void shouldFindEmployeeById() {
        // Given
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee1));

        // When
        Optional<Employee> found = employeeService.getEmployeeById(1);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(1);
        assertThat(found.get().getEmployeeNumber()).isEqualTo("EMP-001");
        verify(employeeRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debe retornar vacío cuando el empleado no existe")
    void shouldReturnEmptyWhenEmployeeNotFound() {
        // Given
        when(employeeRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Employee> found = employeeService.getEmployeeById(999);

        // Then
        assertThat(found).isEmpty();
        verify(employeeRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Debe encontrar empleado por número de empleado")
    void shouldFindEmployeeByEmployeeNumber() {
        // Given
        when(employeeRepository.findByEmployeeNumber("EMP-001")).thenReturn(Optional.of(employee1));

        // When
        Optional<Employee> found = employeeService.getEmployeeByEmployeeNumber("EMP-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmployeeNumber()).isEqualTo("EMP-001");
        verify(employeeRepository, times(1)).findByEmployeeNumber("EMP-001");
    }

    @Test
    @DisplayName("Debe buscar empleados por nombre")
    void shouldSearchEmployeesByName() {
        // Given
        when(employeeRepository.findByFullNameContainingIgnoreCase("juan"))
                .thenReturn(Arrays.asList(employee1));

        // When
        List<Employee> result = employeeService.searchEmployeesByName("juan");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).contains("Juan");
        verify(employeeRepository, times(1)).findByFullNameContainingIgnoreCase("juan");
    }

    @Test
    @DisplayName("Debe actualizar un empleado exitosamente")
    void shouldUpdateEmployeeSuccessfully() {
        // Given
        Employee updatedData = new Employee("EMP-001-UPDATED", "Juan Carlos Pérez", LocalDate.of(2020, 2, 1));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee1));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        // When
        Employee updated = employeeService.updateEmployee(1, updatedData);

        // Then
        assertThat(updated).isNotNull();
        verify(employeeRepository, times(1)).findById(1);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar empleado inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentEmployee() {
        // Given
        Employee updatedData = new Employee("EMP-999", "Test User", LocalDate.now());
        when(employeeRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> employeeService.updateEmployee(999, updatedData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Empleado no encontrado con ID: 999");

        verify(employeeRepository, times(1)).findById(999);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Debe eliminar un empleado exitosamente")
    void shouldDeleteEmployeeSuccessfully() {
        // Given
        when(employeeRepository.existsById(1)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1);

        // When
        employeeService.deleteEmployee(1);

        // Then
        verify(employeeRepository, times(1)).existsById(1);
        verify(employeeRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar empleado inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentEmployee() {
        // Given
        when(employeeRepository.existsById(999)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> employeeService.deleteEmployee(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Empleado no encontrado con ID: 999");

        verify(employeeRepository, times(1)).existsById(999);
        verify(employeeRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Debe obtener empleados contratados en un rango de fechas")
    void shouldGetEmployeesHiredBetween() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2021, 12, 31);
        when(employeeRepository.findByHireDateBetween(startDate, endDate))
                .thenReturn(Arrays.asList(employee1, employee2));

        // When
        List<Employee> result = employeeService.getEmployeesHiredBetween(startDate, endDate);

        // Then
        assertThat(result).hasSize(2);
        verify(employeeRepository, times(1)).findByHireDateBetween(startDate, endDate);
    }
}

