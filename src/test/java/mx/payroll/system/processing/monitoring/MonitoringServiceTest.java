package mx.payroll.system.processing.monitoring;

import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.domain.repository.PayrollResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private PayrollPeriodRepository periodRepo;

    @Mock
    private PayrollResultRepository resultRepo;

    @InjectMocks
    private MonitoringService monitoringService;

    private PayrollPeriod testPeriod;

    @BeforeEach
    void setUp() {
        testPeriod = new PayrollPeriod();
        testPeriod.setId(1L);
        testPeriod.setPeriodIdentifier("2024-01");
        testPeriod.setStartDate(LocalDate.of(2024, 1, 1));
        testPeriod.setEndDate(LocalDate.of(2024, 1, 31));
        testPeriod.setStatus("IN_PROGRESS");
        testPeriod.setTotalExpected(100);
    }

    @Test
    void getProgress_whenPeriodNotFound_shouldThrowException() {
        // Given
        Long periodId = 999L;
        when(periodRepo.findById(periodId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> monitoringService.getProgress(periodId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Period not found: 999");

        verify(periodRepo).findById(periodId);
        verifyNoInteractions(resultRepo);
    }

    @Test
    void getProgress_whenTotalExpectedIsZero_shouldReturnZeroProgress() {
        // Given
        testPeriod.setTotalExpected(0);
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(0);
        assertThat(report.processed()).isEqualTo(0);
        assertThat(report.percentage()).isEqualTo(0);
        assertThat(report.status()).isEqualTo("IN_PROGRESS");

        verify(periodRepo).findById(1L);
        verifyNoInteractions(resultRepo);
    }

    @Test
    void getProgress_whenTotalExpectedIsNull_shouldReturnZeroProgress() {
        // Given
        testPeriod.setTotalExpected(null);
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(0);
        assertThat(report.processed()).isEqualTo(0);
        assertThat(report.percentage()).isEqualTo(0);
        assertThat(report.status()).isEqualTo("IN_PROGRESS");

        verify(periodRepo).findById(1L);
        verifyNoInteractions(resultRepo);
    }

    @Test
    void getProgress_whenNoResultsProcessed_shouldReturnZeroPercentage() {
        // Given
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));
        when(resultRepo.countByPeriodId("2024-01")).thenReturn(0);

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(100);
        assertThat(report.processed()).isEqualTo(0);
        assertThat(report.percentage()).isEqualTo(0);
        assertThat(report.status()).isEqualTo("IN_PROGRESS");

        verify(periodRepo).findById(1L);
        verify(resultRepo).countByPeriodId("2024-01");
        verify(periodRepo, never()).save(any());
    }

    @Test
    void getProgress_whenPartiallyProcessed_shouldReturnCorrectPercentage() {
        // Given
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));
        when(resultRepo.countByPeriodId("2024-01")).thenReturn(50);

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(100);
        assertThat(report.processed()).isEqualTo(50);
        assertThat(report.percentage()).isEqualTo(50);
        assertThat(report.status()).isEqualTo("IN_PROGRESS");

        verify(periodRepo).findById(1L);
        verify(resultRepo).countByPeriodId("2024-01");
        verify(periodRepo, never()).save(any());
    }

    @Test
    void getProgress_whenFullyProcessed_shouldUpdateStatusToCompleted() {
        // Given
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));
        when(resultRepo.countByPeriodId("2024-01")).thenReturn(100);
        when(periodRepo.save(any(PayrollPeriod.class))).thenReturn(testPeriod);

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(100);
        assertThat(report.processed()).isEqualTo(100);
        assertThat(report.percentage()).isEqualTo(100);
        assertThat(report.status()).isEqualTo("COMPLETED");

        verify(periodRepo).findById(1L);
        verify(resultRepo).countByPeriodId("2024-01");
        verify(periodRepo).save(testPeriod);
        assertThat(testPeriod.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void getProgress_whenAlreadyCompleted_shouldNotUpdateStatus() {
        // Given
        testPeriod.setStatus("COMPLETED");
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));
        when(resultRepo.countByPeriodId("2024-01")).thenReturn(100);

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(100);
        assertThat(report.processed()).isEqualTo(100);
        assertThat(report.percentage()).isEqualTo(100);
        assertThat(report.status()).isEqualTo("COMPLETED");

        verify(periodRepo).findById(1L);
        verify(resultRepo).countByPeriodId("2024-01");
        verify(periodRepo, never()).save(any());
    }

    @Test
    void getProgress_whenProcessedExceedsExpected_shouldReturnOver100Percent() {
        // Given
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));
        when(resultRepo.countByPeriodId("2024-01")).thenReturn(150);
        when(periodRepo.save(any(PayrollPeriod.class))).thenReturn(testPeriod);

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(100);
        assertThat(report.processed()).isEqualTo(150);
        assertThat(report.percentage()).isEqualTo(150);
        assertThat(report.status()).isEqualTo("COMPLETED");

        verify(periodRepo).findById(1L);
        verify(resultRepo).countByPeriodId("2024-01");
        verify(periodRepo).save(testPeriod);
    }

    @Test
    void getProgress_whenPercentageIsExactly99_shouldNotComplete() {
        // Given
        when(periodRepo.findById(1L)).thenReturn(Optional.of(testPeriod));
        when(resultRepo.countByPeriodId("2024-01")).thenReturn(99);

        // When
        MonitoringService.ProgressReport report = monitoringService.getProgress(1L);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.periodId()).isEqualTo(1L);
        assertThat(report.total()).isEqualTo(100);
        assertThat(report.processed()).isEqualTo(99);
        assertThat(report.percentage()).isEqualTo(99);
        assertThat(report.status()).isEqualTo("IN_PROGRESS");

        verify(periodRepo).findById(1L);
        verify(resultRepo).countByPeriodId("2024-01");
        verify(periodRepo, never()).save(any());
    }
}
