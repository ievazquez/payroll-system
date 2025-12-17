package mx.payroll.system.domain.repository;

import mx.payroll.system.domain.model.ConceptFormula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptFormulaRepository extends JpaRepository<ConceptFormula, Long> {

    // Find the currently active formulas for a given date
    List<ConceptFormula> findByEffectiveDateBeforeAndEndDateAfterOrEndDateIsNull(
            LocalDate date1, LocalDate date2);

    // Find a formula by concept code and a specific effective date
    Optional<ConceptFormula> findByConceptCodeAndEffectiveDate(String conceptCode, LocalDate effectiveDate);

    // Find formulas effective before a given date, ordered by calculation order
    @Query("SELECT cf FROM ConceptFormula cf WHERE cf.effectiveDate <= :date ORDER BY cf.order ASC")
    List<ConceptFormula> findByEffectiveDateBeforeOrderByOrder(@Param("date") LocalDate date);
}
