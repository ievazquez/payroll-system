package mx.payroll.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.payroll.system.domain.model.PayrollPeriod;
import mx.payroll.system.domain.repository.PayrollPeriodRepository;
import mx.payroll.system.dto.PayrollPeriodRequestDTO;
import mx.payroll.system.processing.dispatcher.PayrollDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollController.class)
@ActiveProfiles("test")
@DisplayName("Payroll Controller Tests")
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PayrollPeriodRepository payrollPeriodRepository;

    @MockBean
    private PayrollDispatcher payrollDispatcher;

    @Test
    @DisplayName("POST /api/payroll/periods - Should create period and dispatch job")
    void shouldCreatePeriodAndDispatch() throws Exception {
        // Given
        PayrollPeriodRequestDTO request = new PayrollPeriodRequestDTO(
                "Nómina Enero 2025",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        PayrollPeriod savedPeriod = new PayrollPeriod(
                request.getStartDate(),
                request.getEndDate(),
                "PROCESSING",
                request.getName()
        );
        savedPeriod.setId(1L);

        when(payrollPeriodRepository.save(any(PayrollPeriod.class))).thenReturn(savedPeriod);

        // When & Then
        mockMvc.perform(post("/api/payroll/periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(payrollPeriodRepository, times(1)).save(any(PayrollPeriod.class));
        verify(payrollDispatcher, times(1)).dispatchCalculation("Nómina Enero 2025");
    }
}
