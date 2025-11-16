package com.portfolio.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SimulationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtTokenProvider.generateToken("testuser");
    }

    @Test
    void shouldSimulateInvestmentSuccessfully() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(12);
        request.setTipoProduto("CDB");

        mockMvc.perform(post("/simular-investimento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoValidado").exists())
                .andExpect(jsonPath("$.produtoValidado.nome").exists())
                .andExpect(jsonPath("$.resultadoSimulacao.valorFinal").exists())
                .andExpect(jsonPath("$.dataSimulacao").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(null);
        request.setValor(new BigDecimal("-100"));
        request.setPrazoMeses(0);
        request.setTipoProduto("");

        mockMvc.perform(post("/simular-investimento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(12);
        request.setTipoProduto("CDB");

        mockMvc.perform(post("/simular-investimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetAllSimulations() throws Exception {
        mockMvc.perform(get("/simulacoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetDailyAggregations() throws Exception {
        mockMvc.perform(get("/simulacoes/por-produto-dia")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetTelemetry() throws Exception {
        mockMvc.perform(get("/telemetria")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos").isArray())
                .andExpect(jsonPath("$.periodo").exists());
    }
}
