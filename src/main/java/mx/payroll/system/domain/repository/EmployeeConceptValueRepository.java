package mx.payroll.system.domain.repository;

import mx.payroll.system.domain.model.EmployeeConceptValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeConceptValueRepository extends JpaRepository<EmployeeConceptValue, Long> {

    // Find active employee concept values for a given date
    @Query("SELECT ecv FROM EmployeeConceptValue ecv WHERE ecv.employeeId = :employeeId " +
           "AND ecv.effectiveDate <= :date AND (ecv.endDate IS NULL OR ecv.endDate >= :date)")
    List<EmployeeConceptValue> findByEmployeeIdAndEffectiveDateBeforeAndEndDateAfterOrEndDateIsNull(
            @Param("employeeId") Integer employeeId,
            @Param("date") LocalDate date1,
            LocalDate date2);

    // Simplified version - find active values for an employee on a given date
    @Query("SELECT ecv FROM EmployeeConceptValue ecv WHERE ecv.employeeId = :employeeId " +
           "AND ecv.effectiveDate <= :date AND (ecv.endDate IS NULL OR ecv.endDate >= :date)")
    List<EmployeeConceptValue> findActiveByEmployeeAndDate(
            @Param("employeeId") Integer employeeId,
            @Param("date") LocalDate date);
}
