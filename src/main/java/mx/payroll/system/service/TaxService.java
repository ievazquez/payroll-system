package mx.payroll.system.service;

import mx.payroll.system.domain.model.TaxTable;
import mx.payroll.system.domain.repository.TaxTableRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TaxService {

    private final TaxTableRepository repository;

    public TaxService(TaxTableRepository repository ) {
        this.repository = repository;
    }

    public BigDecimal calculateISR ( BigDecimal baseGravable) {
        return BigDecimal.ZERO;
    }
}
