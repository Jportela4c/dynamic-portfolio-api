package com.portfolio.api.controller;
import com.portfolio.api.model.enums.PerfilRisco;
import com.portfolio.api.model.dto.response.DailyAggregationResponse;
import com.portfolio.api.model.dto.response.SimulationHistoryResponse;
import com.portfolio.api.service.SimulationService;
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
import java.time.LocalDateTime;
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

@WebMvcTest(controllers = SimulationController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.portfolio\\.api\\.security\\..*"
    ))
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimulationService simulationService;


    @Test
    void shouldReturnAllSimulations() throws Exception {
        // Arrange
        List<SimulationHistoryResponse> simulations = Arrays.asList(
            SimulationHistoryResponse.builder()
                .id(1L)
                .clienteId(123L)
                .produto("CDB Caixa 2026")
                .valorInvestido(new BigDecimal("10000.00"))
                .valorFinal(new BigDecimal("11200.00"))
                .prazoMeses(12)
                .dataSimulacao(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
                .build(),
            SimulationHistoryResponse.builder()
                .id(2L)
                .clienteId(456L)
                .produto("LCI Itaú 2025")
                .valorInvestido(new BigDecimal("5000.00"))
                .valorFinal(new BigDecimal("5400.00"))
                .prazoMeses(6)
                .dataSimulacao(LocalDateTime.of(2025, 1, 15, 11, 0, 0))
                .build()
        );

        when(simulationService.getAllSimulations()).thenReturn(simulations);

        // Act & Assert
        mockMvc.perform(get("/simulacoes")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].clienteId").value(123))
            .andExpect(jsonPath("$[0].produto").value("CDB Caixa 2026"))
            .andExpect(jsonPath("$[0].valorInvestido").value(10000.00))
            .andExpect(jsonPath("$[0].valorFinal").value(11200.00))
            .andExpect(jsonPath("$[0].prazoMeses").value(12))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].clienteId").value(456))
            .andExpect(jsonPath("$[1].produto").value("LCI Itaú 2025"));

        verify(simulationService).getAllSimulations();
    }

    @Test
    void shouldReturnEmptyListWhenNoSimulations() throws Exception {
        // Arrange
        when(simulationService.getAllSimulations()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/simulacoes")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(simulationService).getAllSimulations();
    }

    @Test
    void shouldReturnDailyAggregations() throws Exception {
        // Arrange
        List<DailyAggregationResponse> aggregations = Arrays.asList(
            DailyAggregationResponse.builder()
                .produto("CDB Caixa 2026")
                .data(LocalDate.of(2025, 1, 15))
                .quantidadeSimulacoes(25L)
                .mediaValorFinal(new BigDecimal("11500.00"))
                .build(),
            DailyAggregationResponse.builder()
                .produto("LCI Itaú 2025")
                .data(LocalDate.of(2025, 1, 15))
                .quantidadeSimulacoes(15L)
                .mediaValorFinal(new BigDecimal("5300.00"))
                .build()
        );

        when(simulationService.getDailyAggregations()).thenReturn(aggregations);

        // Act & Assert
        mockMvc.perform(get("/simulacoes/por-produto-dia")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].produto").value("CDB Caixa 2026"))
            .andExpect(jsonPath("$[0].data").value("2025-01-15"))
            .andExpect(jsonPath("$[0].quantidadeSimulacoes").value(25))
            .andExpect(jsonPath("$[0].mediaValorFinal").value(11500.00))
            .andExpect(jsonPath("$[1].produto").value("LCI Itaú 2025"))
            .andExpect(jsonPath("$[1].data").value("2025-01-15"))
            .andExpect(jsonPath("$[1].quantidadeSimulacoes").value(15))
            .andExpect(jsonPath("$[1].mediaValorFinal").value(5300.00));

        verify(simulationService).getDailyAggregations();
    }

    @Test
    void shouldReturnEmptyListWhenNoAggregations() throws Exception {
        // Arrange
        when(simulationService.getDailyAggregations()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/simulacoes/por-produto-dia")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(simulationService).getDailyAggregations();
    }
}
