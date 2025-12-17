package mx.payroll.system.domain.repository;

import mx.payroll.system.domain.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Employee Repository Tests")
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        employeeRepository.deleteAll();
        entityManager.flush();

        // Crear empleados de prueba
        employee1 = new Employee("EMP-001", "Juan Pérez", LocalDate.of(2020, 1, 15));
        employee2 = new Employee("EMP-002", "María García", LocalDate.of(2021, 6, 10));
        employee3 = new Employee("EMP-003", "Pedro López", LocalDate.of(2023, 3, 20));

        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(employee3);
        entityManager.flush();
    }

    @Test
    @DisplayName("Debe guardar un empleado correctamente")
    void shouldSaveEmployee() {
        // Given
        Employee newEmployee = new Employee("EMP-004", "Ana Martínez", LocalDate.of(2024, 1, 1));

        // When
        Employee savedEmployee = employeeRepository.save(newEmployee);

        // Then
        assertThat(savedEmployee).isNotNull();
        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(savedEmployee.getEmployeeNumber()).isEqualTo("EMP-004");
        assertThat(savedEmployee.getFullName()).isEqualTo("Ana Martínez");
        assertThat(savedEmployee.getHireDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    @DisplayName("Debe encontrar empleado por número de empleado")
    void shouldFindByEmployeeNumber() {
        // When
        Optional<Employee> found = employeeRepository.findByEmployeeNumber("EMP-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("Debe retornar vacío cuando no existe el número de empleado")
    void shouldReturnEmptyWhenEmployeeNumberNotFound() {
        // When
        Optional<Employee> found = employeeRepository.findByEmployeeNumber("EMP-999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe verificar si existe un empleado por número de empleado")
    void shouldCheckIfExistsByEmployeeNumber() {
        // When
        boolean exists = employeeRepository.existsByEmployeeNumber("EMP-002");
        boolean notExists = employeeRepository.existsByEmployeeNumber("EMP-999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Debe buscar empleados por nombre (case-insensitive)")
    void shouldFindByFullNameContainingIgnoreCase() {
        // When
        List<Employee> foundByGarcia = employeeRepository.findByFullNameContainingIgnoreCase("garcía");
        List<Employee> foundByPerez = employeeRepository.findByFullNameContainingIgnoreCase("PÉREZ");

        // Then
        assertThat(foundByGarcia).hasSize(1);
        assertThat(foundByGarcia.get(0).getEmployeeNumber()).isEqualTo("EMP-002");

        assertThat(foundByPerez).hasSize(1);
        assertThat(foundByPerez.get(0).getEmployeeNumber()).isEqualTo("EMP-001");
    }

    @Test
    @DisplayName("Debe buscar empleados contratados después de una fecha")
    void shouldFindByHireDateAfter() {
        // Given
        LocalDate cutoffDate = LocalDate.of(2021, 1, 1);

        // When
        List<Employee> employees = employeeRepository.findByHireDateAfter(cutoffDate);

        // Then
        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getEmployeeNumber)
                .containsExactlyInAnyOrder("EMP-002", "EMP-003");
    }

    @Test
    @DisplayName("Debe buscar empleados contratados en un rango de fechas")
    void shouldFindByHireDateBetween() {
        // Given
        LocalDate startDate = LocalDate.of(2021, 1, 1);
        LocalDate endDate = LocalDate.of(2022, 12, 31);

        // When
        List<Employee> employees = employeeRepository.findByHireDateBetween(startDate, endDate);

        // Then
        assertThat(employees).hasSize(1);
        assertThat(employees.get(0).getEmployeeNumber()).isEqualTo("EMP-002");
    }

    @Test
    @DisplayName("Debe encontrar todos los empleados")
    void shouldFindAllEmployees() {
        // When
        List<Employee> allEmployees = employeeRepository.findAll();

        // Then
        assertThat(allEmployees).hasSize(3);
    }

    @Test
    @DisplayName("Debe actualizar un empleado")
    void shouldUpdateEmployee() {
        // Given
        Employee employee = employeeRepository.findByEmployeeNumber("EMP-001").orElseThrow();
        employee.setFullName("Juan Carlos Pérez");
        employee.setHireDate(LocalDate.of(2020, 2, 1));

        // When
        Employee updated = employeeRepository.save(employee);

        // Then
        assertThat(updated.getFullName()).isEqualTo("Juan Carlos Pérez");
        assertThat(updated.getHireDate()).isEqualTo(LocalDate.of(2020, 2, 1));
    }

    @Test
    @DisplayName("Debe eliminar un empleado")
    void shouldDeleteEmployee() {
        // Given
        Employee employee = employeeRepository.findByEmployeeNumber("EMP-001").orElseThrow();

        // When
        employeeRepository.delete(employee);

        // Then
        Optional<Employee> deleted = employeeRepository.findByEmployeeNumber("EMP-001");
        assertThat(deleted).isEmpty();
        assertThat(employeeRepository.findAll()).hasSize(2);
    }
}
