package mx.payroll.system.web.controller;

import mx.payroll.system.processing.monitoring.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonitorController.class)
class MonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitoringService monitoringService;

    @Test
    void getProgress_whenPeriodExists_shouldReturnProgressReport() throws Exception {
        // Given
        Long periodId = 1L;
        MonitoringService.ProgressReport report = new MonitoringService.ProgressReport(
                periodId,
                100,
                50,
                50,
                "IN_PROGRESS"
        );
        when(monitoringService.getProgress(periodId)).thenReturn(report);

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.periodId", is(1)))
                .andExpect(jsonPath("$.total", is(100)))
                .andExpect(jsonPath("$.processed", is(50)))
                .andExpect(jsonPath("$.percentage", is(50)))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        verify(monitoringService, times(1)).getProgress(periodId);
    }

    @Test
    void getProgress_whenPeriodNotStarted_shouldReturnZeroProgress() throws Exception {
        // Given
        Long periodId = 2L;
        MonitoringService.ProgressReport report = new MonitoringService.ProgressReport(
                periodId,
                100,
                0,
                0,
                "PENDING"
        );
        when(monitoringService.getProgress(periodId)).thenReturn(report);

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodId", is(2)))
                .andExpect(jsonPath("$.total", is(100)))
                .andExpect(jsonPath("$.processed", is(0)))
                .andExpect(jsonPath("$.percentage", is(0)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(monitoringService).getProgress(periodId);
    }

    @Test
    void getProgress_whenPeriodCompleted_shouldReturnFullProgress() throws Exception {
        // Given
        Long periodId = 3L;
        MonitoringService.ProgressReport report = new MonitoringService.ProgressReport(
                periodId,
                100,
                100,
                100,
                "COMPLETED"
        );
        when(monitoringService.getProgress(periodId)).thenReturn(report);

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodId", is(3)))
                .andExpect(jsonPath("$.total", is(100)))
                .andExpect(jsonPath("$.processed", is(100)))
                .andExpect(jsonPath("$.percentage", is(100)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        verify(monitoringService).getProgress(periodId);
    }

    @Test
    void getProgress_whenPeriodNotFound_shouldReturnError() throws Exception {
        // Given
        Long periodId = 999L;
        when(monitoringService.getProgress(periodId))
                .thenThrow(new IllegalArgumentException("Period not found: " + periodId));

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId))
                .andExpect(status().isBadRequest());

        verify(monitoringService).getProgress(periodId);
    }

    @Test
    void getProgress_whenServiceThrowsException_shouldPropagateError() throws Exception {
        // Given
        Long periodId = 1L;
        when(monitoringService.getProgress(periodId))
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId))
                .andExpect(status().is5xxServerError());

        verify(monitoringService).getProgress(periodId);
    }

    @Test
    void getProgress_withInvalidPathVariable_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", "invalid"))
                .andExpect(status().isBadRequest());

        verify(monitoringService, never()).getProgress(any());
    }

    @Test
    void getProgress_withNegativePeriodId_shouldCallServiceAnyway() throws Exception {
        // Given
        Long periodId = -1L;
        when(monitoringService.getProgress(periodId))
                .thenThrow(new IllegalArgumentException("Period not found: " + periodId));

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId))
                .andExpect(status().isBadRequest());

        verify(monitoringService).getProgress(periodId);
    }

    @Test
    void getProgress_whenProgressOver100Percent_shouldReturnCorrectValue() throws Exception {
        // Given
        Long periodId = 4L;
        MonitoringService.ProgressReport report = new MonitoringService.ProgressReport(
                periodId,
                100,
                150,
                150,
                "COMPLETED"
        );
        when(monitoringService.getProgress(periodId)).thenReturn(report);

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodId", is(4)))
                .andExpect(jsonPath("$.total", is(100)))
                .andExpect(jsonPath("$.processed", is(150)))
                .andExpect(jsonPath("$.percentage", is(150)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        verify(monitoringService).getProgress(periodId);
    }

    @Test
    void getProgress_whenEmptyProgress_shouldReturnZeroValues() throws Exception {
        // Given
        Long periodId = 5L;
        MonitoringService.ProgressReport report = new MonitoringService.ProgressReport(
                periodId,
                0,
                0,
                0,
                "PENDING"
        );
        when(monitoringService.getProgress(periodId)).thenReturn(report);

        // When & Then
        mockMvc.perform(get("/api/monitor/{periodId}/progress", periodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodId", is(5)))
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.processed", is(0)))
                .andExpect(jsonPath("$.percentage", is(0)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(monitoringService).getProgress(periodId);
    }
}
