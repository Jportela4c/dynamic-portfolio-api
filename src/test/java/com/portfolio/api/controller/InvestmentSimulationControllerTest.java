package com.portfolio.api.controller;
import com.portfolio.api.model.enums.PerfilRisco;
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


    @Test
    void shouldSimulateInvestmentSuccessfully() throws Exception {
        // Arrange
        SimulationRequest request = new SimulationRequest(
            123L,
            new BigDecimal("10000.00"),
            12,
            TipoProduto.CDB
        );

        SimulationResponse.ProductValidated product = SimulationResponse.ProductValidated.builder()
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
            .produtoValidado(product)
            .resultadoSimulacao(result)
            .dataSimulacao(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
            .build();

        when(simulationService.simulateInvestment(any(SimulationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/simular-investimento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.produtoValidado.id").value(1))
            .andExpect(jsonPath("$.produtoValidado.nome").value("CDB Caixa 2026"))
            .andExpect(jsonPath("$.produtoValidado.tipo").value("CDB"))
            .andExpect(jsonPath("$.produtoValidado.rentabilidade").value(0.12))
            .andExpect(jsonPath("$.produtoValidado.risco").value("Baixo"))
            .andExpect(jsonPath("$.resultadoSimulacao.valorFinal").value(11200.00))
            .andExpect(jsonPath("$.resultadoSimulacao.rentabilidadeEfetiva").value(0.12))
            .andExpect(jsonPath("$.resultadoSimulacao.prazoMeses").value(12))
            .andExpect(jsonPath("$.dataSimulacao").value("2025-01-15T10:30:00"));

        verify(simulationService).simulateInvestment(any(SimulationRequest.class));
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
