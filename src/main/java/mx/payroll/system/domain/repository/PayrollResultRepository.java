package mx.payroll.system.domain.repository;

import mx.payroll.system.engine.PayrollResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollResultRepository extends JpaRepository<PayrollResult, Long> {

    /**
     * Count the number of payroll results for a given period
     * @param periodId the period identifier (String)
     * @return count of results
     */
    int countByPeriodId(String periodId);
}
