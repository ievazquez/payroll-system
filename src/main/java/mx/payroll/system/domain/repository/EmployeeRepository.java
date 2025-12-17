package mx.payroll.system.domain.repository;

import mx.payroll.system.domain.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("SELECT e FROM Employee e WHERE e.active = true")
    Page<Employee> findAllActive(Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.active = true")
    long countActiveEmployees();

    /**
     * Busca un empleado por su número de empleado (e.g., "EMP-001")
     */
    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    /**
     * Verifica si existe un empleado con el número de empleado dado
     */
    boolean existsByEmployeeNumber(String employeeNumber);

    /**
     * Busca empleados por nombre (búsqueda parcial, case-insensitive)
     */
    List<Employee> findByFullNameContainingIgnoreCase(String name);

    /**
     * Busca empleados contratados después de una fecha específica
     */
    List<Employee> findByHireDateAfter(LocalDate date);

    /**
     * Busca empleados contratados entre dos fechas
     */
    List<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate);
}
