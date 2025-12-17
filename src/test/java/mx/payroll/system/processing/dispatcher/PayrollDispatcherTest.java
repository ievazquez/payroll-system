package mx.payroll.system.processing.dispatcher;

import jakarta.persistence.EntityManager;
import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.EmployeeRepository;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.processing.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollDispatcherTest {

    @Mock
    private EmployeeRepository employeeRepo;

    @Mock
    private QueueService queueService;

    @Mock
    private PayrollPeriodRepository periodRepo;

    @Mock
    private EntityManager entityManager; // Mock EntityManager

    @InjectMocks
    private PayrollDispatcher payrollDispatcher;

    @Test
    public void testDispatchCalculation() {
        // Arrange
        String periodId = "2023-12";
        long employeeCount = 250; // Should result in 3 chunks (100, 100, 50)
        when(employeeRepo.countActiveEmployees()).thenReturn(employeeCount);
        
        PayrollPeriod period = new PayrollPeriod();
        period.setPeriodIdentifier(periodId);
        when(periodRepo.findByPeriodIdentifier(periodId)).thenReturn(Optional.of(period));

        // Act
        payrollDispatcher.dispatchCalculation(periodId);

        // Assert
        verify(entityManager).clear(); // Verify clear is called
        verify(employeeRepo).countActiveEmployees();
        verify(periodRepo).findByPeriodIdentifier(periodId);
        verify(periodRepo).save(period);
        verify(queueService, times(3)).push(any(PayrollChunkJob.class));
    }
}
