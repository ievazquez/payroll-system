package mx.payroll.system.domain.repository;

import mx.payroll.system.domain.model.EconomicIndicator;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {

    @Query("SELECT e FROM EconomicIndicator e WHERE e.code = :code AND e.effectiveDate <= :date ORDER BY e.effectiveDate DESC")
    List<EconomicIndicator> findHistory(@Param("code") String code, @Param("date") LocalDate date, Pageable pageable);

    default BigDecimal findCurrentValue(String code, LocalDate date) {
        List<EconomicIndicator> list = findHistory(code, date, PageRequest.of(0, 1));
        return list.isEmpty() ? BigDecimal.ZERO : list.get(0).getValue();
    }

    /**
     * Finds the effective value for ALL indicators on a specific date.
     * Returns the record with the max effective_date <= date for each unique code.
     */
    @Query("SELECT e FROM EconomicIndicator e WHERE e.effectiveDate = (" +
           "  SELECT MAX(e2.effectiveDate) FROM EconomicIndicator e2 " +
           "  WHERE e2.code = e.code AND e2.effectiveDate <= :date" +
           ")")
    List<EconomicIndicator> findAllEffectiveIndicators(@Param("date") LocalDate date);
}
