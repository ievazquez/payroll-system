package mx.payroll.system.processing.monitoring;

import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.domain.repository.PayrollResultRepository;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {

    private final PayrollPeriodRepository periodRepo;
    private final PayrollResultRepository resultRepo;

    // DTO interno para respuesta limpia
    public record ProgressReport(Long periodId, int total, int processed, int percentage, String status) {}

    public MonitoringService(PayrollPeriodRepository periodRepo, PayrollResultRepository resultRepo) {
        this.periodRepo = periodRepo;
        this.resultRepo = resultRepo;
    }

    public ProgressReport getProgress(Long periodId) {
        // 1. Obtener la meta
        PayrollPeriod period = periodRepo.findById(periodId)
            .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));

        if (period.getTotalExpected() == null || period.getTotalExpected() == 0) {
            return new ProgressReport(periodId, 0, 0, 0, period.getStatus());
        }

        // 2. Obtener el avance actual (Cuántos resultados ya se guardaron en la DB)
        // Note: PayrollResult.periodId is a String (periodIdentifier), not the Long id
        int currentCount = resultRepo.countByPeriodId(period.getPeriodIdentifier());

        // 3. Calcular porcentaje
        int percentage = (int) ((currentCount * 100.0) / period.getTotalExpected());
        
        // 4. Lógica de finalización automática (opcional)
        if (percentage >= 100 && !"COMPLETED".equals(period.getStatus())) {
            period.setStatus("COMPLETED");
            periodRepo.save(period);
        }

        return new ProgressReport(
            periodId, 
            period.getTotalExpected(), 
            currentCount, 
            percentage, 
            period.getStatus()
        );
    }
}
