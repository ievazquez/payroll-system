package mx.payroll.system.service;

import mx.payroll.system.domain.model.ConceptFormula;
import mx.payroll.system.domain.model.Employee;
import mx.payroll.system.domain.model.EmployeeConceptValue;
import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.ConceptFormulaRepository;
import mx.payroll.system.domain.repository.EconomicIndicatorRepository;
import mx.payroll.system.domain.repository.EmployeeConceptValueRepository;
import mx.payroll.system.engine.DynamicDbRule; // Concrete rule implementation
import mx.payroll.system.engine.FormulaEngine;
import mx.payroll.system.engine.PayrollContext;
import mx.payroll.system.engine.PayrollEngine;
import mx.payroll.system.engine.PayrollRule;
import mx.payroll.system.engine.PayrollResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final EmployeeConceptValueRepository specificRepo; // Datos de Juan
    private final ConceptFormulaRepository globalRepo;         // Reglas de Todos
    private final EconomicIndicatorRepository indicatorRepo;   // Datos Económicos
    private final PayrollEngine engine;
    private final FormulaEngine formulaEngine; // Needed to create DynamicDbRules

    public PayrollService(EmployeeConceptValueRepository specificRepo,
                          ConceptFormulaRepository globalRepo,
                          EconomicIndicatorRepository indicatorRepo,
                          PayrollEngine engine,
                          FormulaEngine formulaEngine) {
        this.specificRepo = specificRepo;
        this.globalRepo = globalRepo;
        this.indicatorRepo = indicatorRepo;
        this.engine = engine;
        this.formulaEngine = formulaEngine;
    }

    public PayrollResult processEmployee(Employee employee, PayrollPeriod period) {
        // This method will be refactored into calculatePayrollForEmployee
        // It's just a placeholder now as the worker will call the more specific one.
        throw new UnsupportedOperationException("This method should not be called directly. Use calculatePayrollForEmployee.");
    }

    // New method to encapsulate the payroll calculation for a single employee
    @Transactional(readOnly = true)
    public PayrollResult calculatePayrollForEmployee(Employee employee, PayrollPeriod period, List<ConceptFormula> globalFormulas) {
        // -------------------------------------------------------------
        // PASO 1: RECUPERAR LO ESPECÍFICO DEL EMPLEADO (valores fijos)
        // -------------------------------------------------------------
        // DB dice: Juan tiene INC01=3000 y D05=500
        List<EmployeeConceptValue> specificValues = specificRepo.findByEmployeeIdAndEffectiveDateBeforeAndEndDateAfterOrEndDateIsNull(
            employee.getId(), period.getEndDate(), period.getEndDate());

        // -------------------------------------------------------------
        // PASO 2: CONSTRUIR EL CONTEXTO (LA MOCHILA)
        // -------------------------------------------------------------
        PayrollContext context = new PayrollContext(employee);
        
        // A. Cargamos los valores fijos del EmployeeConceptValue en el contexto
        for (EmployeeConceptValue val : specificValues) {
            context.setFixedValue(val.getConceptCode(), val.getAmount());
        }

        // B. Cargar TODOS los Indicadores Económicos vigentes (UMA, SMI, etc.)
        // Esto permite que el sistema evolucione sin cambios de código
        List<mx.payroll.system.domain.model.EconomicIndicator> indicators = indicatorRepo.findAllEffectiveIndicators(period.getEndDate());
        for (mx.payroll.system.domain.model.EconomicIndicator ind : indicators) {
            context.setFixedValue(ind.getCode(), ind.getValue());
        }

        // -------------------------------------------------------------
        // PASO 3: CONVERTIR FÓRMULAS GLOBALES A REGLAS DE EJECUCIÓN
        // -------------------------------------------------------------
        // Convierte tu entidad DB a objeto de negocio Rule
        List<PayrollRule> rulesToApply = globalFormulas.stream()
            .map(f -> new DynamicDbRule(f.getConceptCode(), f.getOrder(), f.getFormulaExpression(), formulaEngine))
            .collect(Collectors.toList());
        
        System.out.println("    -> Aplicando reglas: " + rulesToApply.stream().map(PayrollRule::getCode).collect(Collectors.joining(", ")));
        System.out.println("    -> Contexto de Cálculo para empleado " + employee.getId() + ": " + context.getVariables());
        System.out.println("    -> HIRE_DATE: " + employee.getHireDate());

        // -------------------------------------------------------------
        // PASO 4: EJECUTAR EL MOTOR
        // -------------------------------------------------------------
        PayrollResult result = engine.calculate(context, rulesToApply);
        
        // Asignar metadatos del periodo al resultado
        result.setPeriodId(period.getPeriodIdentifier()); // Using new periodIdentifier
        
        return result;
    }
    
    // Helper simple
    private List<PayrollRule> convertFormulasToRules(List<ConceptFormula> formulas) {
        // This helper is now redundant as its logic is in calculatePayrollForEmployee
        // But kept for now to avoid breaking other calls if any.
        return formulas.stream()
            .map(f -> new DynamicDbRule(f.getConceptCode(), f.getOrder(), f.getFormulaExpression(), formulaEngine))
            .collect(Collectors.toList());
    }

}
