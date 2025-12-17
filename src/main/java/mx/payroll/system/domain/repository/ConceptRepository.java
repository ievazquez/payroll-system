package mx.payroll.system.domain.repository;

import mx.payroll.system.domain.model.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, String> {

    List<Concept> findByIsActiveTrue();

    List<Concept> findByType(String type);

    @Query("SELECT c FROM Concept c WHERE c.isActive = true ORDER BY c.calculationOrder")
    List<Concept> findActiveConceptsOrderedByCalculation();
}
