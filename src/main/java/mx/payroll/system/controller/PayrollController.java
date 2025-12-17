package mx.payroll.system.controller;

import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.dto.PayrollPeriodRequestDTO;
import mx.payroll.system.processing.dispatcher.PayrollDispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll/periods")
public class PayrollController {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollDispatcher payrollDispatcher;

    public PayrollController(PayrollPeriodRepository payrollPeriodRepository, PayrollDispatcher payrollDispatcher) {
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollDispatcher = payrollDispatcher;
    }

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
