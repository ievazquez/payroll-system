package mx.payroll.system.processing.dispatcher;

import jakarta.persistence.EntityManager;
import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.EmployeeRepository;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.processing.service.QueueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollDispatcher {

    private final EmployeeRepository employeeRepo;
    private final QueueService queueService; // Interfaz genérica a RabbitMQ/Redis
    private final PayrollPeriodRepository periodRepo;
    private final EntityManager entityManager;

    // Tamaño del lote: 100 empleados por worker es un buen balance
    private static final int CHUNK_SIZE = 100;

    public PayrollDispatcher(EmployeeRepository employeeRepo, QueueService queueService, PayrollPeriodRepository periodRepo, EntityManager entityManager) {
        this.employeeRepo = employeeRepo;
        this.queueService = queueService;
        this.periodRepo = periodRepo;
        this.entityManager = entityManager;
    }

    @Transactional
    public void dispatchCalculation(String periodIdentifier) {
        entityManager.clear(); // Clear the persistence context to ensure fresh data is loaded
        // 1. Contar cuántos empleados activos hay
        long totalEmployees = employeeRepo.countActiveEmployees();
        
        // 1.5 Actualizar el periodo con el total esperado
        PayrollPeriod period = periodRepo.findByPeriodIdentifier(periodIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodIdentifier));
        
        period.setTotalExpected((int) totalEmployees);
        periodRepo.save(period);

        // 2. Calcular cuántas páginas (chunks) necesitamos
        int totalChunks = (int) Math.ceil((double) totalEmployees / CHUNK_SIZE);

        System.out.println("Iniciando Dispatch: " + totalEmployees + " empleados en " + totalChunks + " lotes.");

        // 3. Enviar mensajes a la cola
        for (int i = 0; i < totalChunks; i++) {
            PayrollChunkJob job = new PayrollChunkJob(periodIdentifier, i, CHUNK_SIZE);
            queueService.push(job);
        }
    }
}