package mx.payroll.system.processing.worker;

import mx.payroll.system.domain.model.ConceptFormula;
import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.ConceptFormulaRepository;
import mx.payroll.system.domain.repository.EmployeeRepository;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.domain.repository.PayrollResultRepository;
import mx.payroll.system.engine.PayrollResult; // Added import
import mx.payroll.system.processing.dispatcher.PayrollChunkJob; // Added import
import mx.payroll.system.service.PayrollService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PayrollWorker {

    private final EmployeeRepository employeeRepo;
    private final PayrollResultRepository resultRepo;
    private final PayrollService payrollService;
    private final ConceptFormulaRepository globalRulesRepo; // Added
    private final PayrollPeriodRepository payrollPeriodRepo; // Added

    public PayrollWorker(EmployeeRepository employeeRepo,
                         PayrollResultRepository resultRepo,
                         PayrollService payrollService,
                         ConceptFormulaRepository globalRulesRepo,
                         PayrollPeriodRepository payrollPeriodRepo) {
        this.employeeRepo = employeeRepo;
        this.resultRepo = resultRepo;
        this.payrollService = payrollService;
        this.globalRulesRepo = globalRulesRepo;
        this.payrollPeriodRepo = payrollPeriodRepo;
    }

    @RabbitListener(queues = "payroll_queue")
    public void processChunk(PayrollChunkJob job) {
        System.out.println("Worker procesando lote página: " + job.getPage());

        // 1. Obtener el PayrollPeriod
        // For simplicity, assuming periodId from job is a direct identifier for PayrollPeriod
        // In a real scenario, you might have start/end dates from the job to find the period
        PayrollPeriod period = payrollPeriodRepo.findByPeriodIdentifier(job.getPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("PayrollPeriod not found for identifier: " + job.getPeriodId()));


        // 2. Fetch the Batch of Employees (e.g., 100 people)
        List<Employee> employees = employeeRepo.findAllActive(
                PageRequest.of(job.getPage(), job.getPageSize()) // Corrected to getPage()
        ).getContent(); // Get content from Page object

        // 3. PERFORMANCE OPTIMIZATION:
        // We fetch Global Rules (ISR, IMSS formulas) ONCE per batch, not per employee.
        List<ConceptFormula> globalRules = globalRulesRepo.findByEffectiveDateBeforeAndEndDateAfterOrEndDateIsNull(
                period.getEndDate(), period.getEndDate()); // Corrected method name

        List<PayrollResult> batchResults = new ArrayList<>();

        // 4. Iterar sobre este pequeño lote y procesar cada empleado
        for (Employee emp : employees) {
            System.out.println("  -> Procesando empleado " + emp.getId() + " (" + emp.getFullName() + ") para periodo " + period.getPeriodIdentifier());
            try {
                // Delegate the complex calculation logic to PayrollService
                PayrollResult result = payrollService.calculatePayrollForEmployee(emp, period, globalRules); // Pass emp, period, globalRules
                batchResults.add(result);
                
            } catch (Exception e) {
                System.err.println("Error calculando empleado " + emp.getId() + ": " + e.getMessage());
            }
        }

        // 5. Batch Insert
        if (!batchResults.isEmpty()) {
            System.out.println("Lote " + job.getPage() + ": Guardando " + batchResults.size() + " resultados de nómina.");
            try {
                resultRepo.saveAll(batchResults);
            } catch (Exception e) {
                System.err.println("Error guardando lote de resultados para página " + job.getPage() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Lote " + job.getPage() + ": No se generaron resultados para guardar.");
        }
        
        System.out.println("Lote " + job.getPage() + " finalizado."); // Corrected to getPage()
    }
}
