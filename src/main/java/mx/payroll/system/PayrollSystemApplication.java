package mx.payroll.system;

import mx.payroll.system.domain.model.ConceptFormula;
import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.ConceptFormulaRepository;
import mx.payroll.system.domain.repository.EmployeeRepository;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.engine.PayrollResult;
import mx.payroll.system.service.PayrollService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class PayrollSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayrollSystemApplication.class, args);
	}

	/**
	 * CommandLineRunner que se ejecuta al iniciar la aplicación
	 * Ejemplo de cómo llamar al PayrollService para procesar nómina
	 */
	@Bean
	public CommandLineRunner runPayrollDemo(
			PayrollService payrollService,
			EmployeeRepository employeeRepository,
			PayrollPeriodRepository periodRepository,
			ConceptFormulaRepository formulaRepository) {

		return args -> {
			System.out.println("\n========================================");
			System.out.println("DEMO: Procesamiento de Nómina");
			System.out.println("========================================\n");

			// Método público que puede ser llamado para procesar nómina
			processPayrollForAllEmployees(
				payrollService,
				employeeRepository,
				periodRepository,
				formulaRepository
			);
		};
	}

	/**
	 * Método público para procesar nómina de todos los empleados activos
	 * @param payrollService Servicio de nómina
	 * @param employeeRepository Repositorio de empleados
	 * @param periodRepository Repositorio de periodos
	 * @param formulaRepository Repositorio de fórmulas
	 */
	public void processPayrollForAllEmployees(
			PayrollService payrollService,
			EmployeeRepository employeeRepository,
			PayrollPeriodRepository periodRepository,
			ConceptFormulaRepository formulaRepository) {

		// Obtener el periodo actual (o el más reciente)
		List<PayrollPeriod> periods = periodRepository.findAll();
		if (periods.isEmpty()) {
			System.out.println("⚠️  No hay periodos de nómina configurados");
			return;
		}

		PayrollPeriod currentPeriod = periods.get(0);
		System.out.println("📅 Procesando periodo: " + currentPeriod.getPeriodIdentifier());
		System.out.println("   Inicio: " + currentPeriod.getStartDate() + " | Fin: " + currentPeriod.getEndDate());

		// Obtener fórmulas globales vigentes
		List<ConceptFormula> globalFormulas = formulaRepository.findByEffectiveDateBeforeOrderByOrder(
			currentPeriod.getEndDate()
		);
		System.out.println("📝 Fórmulas cargadas: " + globalFormulas.size());

		// Obtener empleados activos
		List<Employee> activeEmployees = employeeRepository.findByActiveTrue();
		System.out.println("👥 Empleados activos: " + activeEmployees.size());
		System.out.println();

		// Procesar primeros 5 empleados como demo
		int processedCount = 0;
		int maxDemo = 5;

		for (Employee employee : activeEmployees) {
			if (processedCount >= maxDemo) {
				System.out.println("... (limitado a " + maxDemo + " empleados en demo)");
				break;
			}

			try {
				System.out.println("🔄 Procesando: " + employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");

				PayrollResult result = payrollService.calculatePayrollForEmployee(
					employee,
					currentPeriod,
					globalFormulas
				);

				System.out.println("   ✅ Percepciones: $" + result.getTotalEarnings());
				System.out.println("   ✅ Deducciones: $" + result.getTotalDeductions());
				System.out.println("   ✅ Neto: $" + result.getNetPay());
				System.out.println();

				processedCount++;

			} catch (Exception e) {
				System.err.println("   ❌ Error procesando empleado: " + e.getMessage());
				e.printStackTrace();
			}
		}

		System.out.println("========================================");
		System.out.println("✅ Demo completado: " + processedCount + " empleados procesados");
		System.out.println("========================================\n");
	}

}
