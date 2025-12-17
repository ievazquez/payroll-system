package mx.payroll.system.domain.repository;

import mx.payroll.system.domain.model.TaxTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface TaxTableRepository extends JpaRepository<TaxTable, Long> {
    @Query(value = """
        SELECT * FROM tax_tables 
        WHERE fiscal_year = :year 
          AND table_type = :type 
          AND lower_limit <= :base
        ORDER BY lower_limit DESC 
        LIMIT 1
    """, nativeQuery = true)
    Optional<TaxTable> findApplicableBracket(@Param("year") int year, @Param("type") String type, @Param("base") BigDecimal base);
}
