package mx.payroll.system.controller;

import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.dto.PayrollPeriodRequestDTO;
import mx.payroll.system.processing.dispatcher.PayrollDispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll/periods")
public class PayrollController {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollDispatcher payrollDispatcher;

    public PayrollController(PayrollPeriodRepository payrollPeriodRepository, PayrollDispatcher payrollDispatcher) {
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollDispatcher = payrollDispatcher;
    }

    /**
     * GET /api/payroll/periods
     * Lista todos los periodos de nómina
     */
    @GetMapping
    public ResponseEntity<List<PayrollPeriod>> getAllPeriods() {
        List<PayrollPeriod> periods = payrollPeriodRepository.findAll();
        return ResponseEntity.ok(periods);
    }

    /**
     * GET /api/payroll/periods/{id}
     * Obtiene un periodo específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PayrollPeriod> getPeriodById(@PathVariable Long id) {
        return payrollPeriodRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/payroll/periods
     * Crea un nuevo periodo de nómina
     */
    @PostMapping
    public ResponseEntity<PayrollPeriod> createPeriod(@RequestBody PayrollPeriodRequestDTO request) {
        PayrollPeriod period = new PayrollPeriod();
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setPeriodIdentifier(request.getName()); // Using name as identifier for simplicity based on request
        period.setStatus("PROCESSING");

        PayrollPeriod savedPeriod = payrollPeriodRepository.save(period);

        // Trigger asynchronous processing
        payrollDispatcher.dispatchCalculation(savedPeriod.getPeriodIdentifier());

        return ResponseEntity.ok(savedPeriod);
    }

    /**
     * POST /api/payroll/periods/{id}/calculate
     * Dispara el cálculo de nómina para un periodo específico
     */
    @PostMapping("/{id}/calculate")
    public ResponseEntity<PayrollPeriod> calculatePeriod(@PathVariable Long id) {
        return payrollPeriodRepository.findById(id)
                .map(period -> {
                    period.setStatus("PROCESSING");
                    payrollPeriodRepository.save(period);
                    payrollDispatcher.dispatchCalculation(period.getPeriodIdentifier());
                    return ResponseEntity.ok(period);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
