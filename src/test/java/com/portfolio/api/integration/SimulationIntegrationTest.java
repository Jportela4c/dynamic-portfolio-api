package com.portfolio.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
@WithMockUser(username = "testuser", roles = {"USER"})
class SimulationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {

        Product cdb = new Product();
        cdb.setNome("CDB Test Product");
        cdb.setTipo(TipoProduto.CDB);
        cdb.setRentabilidade(new BigDecimal("0.12"));
        cdb.setRisco("Baixo");
        cdb.setValorMinimo(new BigDecimal("5000.00"));
        cdb.setPrazoMinimoMeses(6);
        cdb.setPrazoMaximoMeses(24);
        cdb.setPerfilAdequado("Conservador");
        cdb.setAtivo(true);
        productRepository.save(cdb);
    }

    @Test
    void shouldSimulateInvestmentSuccessfully() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(12);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
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
        request.setTipoProduto(null);

        mockMvc.perform(post("/simular-investimento")
                        
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
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetAllSimulations() throws Exception {
        mockMvc.perform(get("/simulacoes")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetDailyAggregations() throws Exception {
        mockMvc.perform(get("/simulacoes/por-produto-dia")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetTelemetry() throws Exception {
        mockMvc.perform(get("/telemetria")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos").isArray())
                .andExpect(jsonPath("$.periodo").exists());
    }

    @Test
    void shouldRejectSimulationBelowMinimumValue() throws Exception {
        // Product minimum: R$ 5,000
        // Service returns 404 when no product matches criteria
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("1000.00"));  // Below minimum
        request.setPrazoMeses(12);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldRejectSimulationBelowMinimumTerm() throws Exception {
        // Product minimum term: 6 months
        // Service returns 404 when no product matches criteria
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(3);  // Below minimum
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldRejectSimulationAboveMaximumTerm() throws Exception {
        // Product maximum term: 24 months
        // Service validates term and returns 404 when no product matches
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(200);  // Way above maximum (no CDB product has this term)
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not available"));
    }

    @Test
    void shouldAcceptSimulationAtMinimumBoundary() throws Exception {
        // Test exact minimum values
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("5000.00"));  // Exact minimum
        request.setPrazoMeses(6);  // Exact minimum term
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoValidado").exists())
                .andExpect(jsonPath("$.resultadoSimulacao").exists());
    }

    @Test
    void shouldAcceptSimulationAtMaximumBoundary() throws Exception {
        // Test exact maximum values
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("100000.00"));
        request.setPrazoMeses(24);  // Exact maximum term
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoValidado").exists())
                .andExpect(jsonPath("$.resultadoSimulacao").exists());
    }

    @Test
    void shouldReturn404ForNonExistentProductType() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(12);
        request.setTipoProduto(TipoProduto.ACOES);  // No ACOES products in test DB

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForNullClientId() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(null);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(12);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNullValue() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(null);
        request.setPrazoMeses(12);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNullTerm() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(null);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNullProductType() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(12);
        request.setTipoProduto(null);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForZeroValue() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(BigDecimal.ZERO);
        request.setPrazoMeses(12);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNegativeValue() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("-1000.00"));
        request.setPrazoMeses(12);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForZeroTerm() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(0);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNegativeTerm() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setClienteId(123L);
        request.setValor(new BigDecimal("10000.00"));
        request.setPrazoMeses(-12);
        request.setTipoProduto(TipoProduto.CDB);

        mockMvc.perform(post("/simular-investimento")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
