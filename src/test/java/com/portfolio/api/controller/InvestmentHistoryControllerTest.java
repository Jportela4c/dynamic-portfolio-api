package com.portfolio.api.controller;
import com.portfolio.api.model.enums.PerfilRisco;
import com.portfolio.api.model.dto.response.InvestmentResponse;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.service.InvestmentService;
import com.portfolio.api.service.TelemetryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvestmentHistoryController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.portfolio\\.api\\.security\\..*"
    ))
class InvestmentHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestmentService investmentService;

    @MockBean
    private TelemetryService telemetryService;

    @Test
    void shouldReturnInvestmentHistoryForClient() throws Exception {
        // Arrange
        List<InvestmentResponse> investments = Arrays.asList(
            InvestmentResponse.builder()
                .id(1L)
                .tipo(TipoProduto.CDB)
                .valor(new BigDecimal("10000.00"))
                .rentabilidade(new BigDecimal("0.12"))
                .data(LocalDate.of(2025, 1, 15))
                .build(),
            InvestmentResponse.builder()
                .id(2L)
                .tipo(TipoProduto.LCI)
                .valor(new BigDecimal("5000.00"))
                .rentabilidade(new BigDecimal("0.09"))
                .data(LocalDate.of(2025, 1, 10))
                .build()
        );

        when(investmentService.getClientInvestments(123L)).thenReturn(investments);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/investimentos/123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].tipo").value("CDB"))
            .andExpect(jsonPath("$[0].valor").value(10000.00))
            .andExpect(jsonPath("$[0].rentabilidade").value(0.12))
            .andExpect(jsonPath("$[0].data").value("2025-01-15"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].tipo").value("LCI"))
            .andExpect(jsonPath("$[1].valor").value(5000.00))
            .andExpect(jsonPath("$[1].rentabilidade").value(0.09))
            .andExpect(jsonPath("$[1].data").value("2025-01-10"));

        verify(investmentService).getClientInvestments(123L);
        verify(telemetryService).recordMetric(eq("investimentos"), anyLong(), eq(true), eq(200));
    }

    @Test
    void shouldReturnEmptyListWhenClientHasNoInvestments() throws Exception {
        // Arrange
        when(investmentService.getClientInvestments(456L)).thenReturn(Collections.emptyList());
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/investimentos/456")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(investmentService).getClientInvestments(456L);
        verify(telemetryService).recordMetric(eq("investimentos"), anyLong(), eq(true), eq(200));
    }
}
