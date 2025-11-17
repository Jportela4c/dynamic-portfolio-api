package com.portfolio.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.model.dto.response.SimulationResponse;
import com.portfolio.api.model.enums.TipoProduto;
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
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvestmentSimulationController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.portfolio\\.api\\.security\\..*"
    ))
class InvestmentSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SimulationService simulationService;

    @MockBean
    private TelemetryService telemetryService;

    @Test
    void shouldSimulateInvestmentSuccessfully() throws Exception {
        // Arrange
        SimulationRequest request = new SimulationRequest(
            123L,
            new BigDecimal("10000.00"),
            12,
            TipoProduto.CDB
        );

        SimulationResponse.SelectedProduct product = SimulationResponse.SelectedProduct.builder()
            .id(1L)
            .nome("CDB Caixa 2026")
            .tipo(TipoProduto.CDB)
            .rentabilidade(new BigDecimal("0.12"))
            .risco("Baixo")
            .build();

        SimulationResponse.SimulationResult result = SimulationResponse.SimulationResult.builder()
            .valorFinal(new BigDecimal("11200.00"))
            .rentabilidadeEfetiva(new BigDecimal("0.12"))
            .prazoMeses(12)
            .build();

        SimulationResponse response = SimulationResponse.builder()
            .selectedProduct(product)
            .resultadoSimulacao(result)
            .dataSimulacao(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
            .build();

        when(simulationService.simulateInvestment(any(SimulationRequest.class))).thenReturn(response);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/simular-investimento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.selectedProduct.id").value(1))
            .andExpect(jsonPath("$.selectedProduct.nome").value("CDB Caixa 2026"))
            .andExpect(jsonPath("$.selectedProduct.tipo").value("CDB"))
            .andExpect(jsonPath("$.selectedProduct.rentabilidade").value(0.12))
            .andExpect(jsonPath("$.selectedProduct.risco").value("Baixo"))
            .andExpect(jsonPath("$.resultadoSimulacao.valorFinal").value(11200.00))
            .andExpect(jsonPath("$.resultadoSimulacao.rentabilidadeEfetiva").value(0.12))
            .andExpect(jsonPath("$.resultadoSimulacao.prazoMeses").value(12))
            .andExpect(jsonPath("$.dataSimulacao").value("2025-01-15T10:30:00"));

        verify(simulationService).simulateInvestment(any(SimulationRequest.class));
        verify(telemetryService).recordMetric(eq("simular-investimento"), anyLong(), eq(true), eq(200));
    }

    @Test
    void shouldReturnBadRequestForNullClienteId() throws Exception {
        // Arrange
        SimulationRequest request = new SimulationRequest(
            null,
            new BigDecimal("10000.00"),
            12,
            TipoProduto.CDB
        );

        // Act & Assert
        mockMvc.perform(post("/simular-investimento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNegativeValue() throws Exception {
        // Arrange
        SimulationRequest request = new SimulationRequest(
            123L,
            new BigDecimal("-1000.00"),
            12,
            TipoProduto.CDB
        );

        // Act & Assert
        mockMvc.perform(post("/simular-investimento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidPrazo() throws Exception {
        // Arrange
        SimulationRequest request = new SimulationRequest(
            123L,
            new BigDecimal("10000.00"),
            0,
            TipoProduto.CDB
        );

        // Act & Assert
        mockMvc.perform(post("/simular-investimento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
