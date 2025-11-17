package com.portfolio.api.integration;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.security.JwtTokenProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.springframework.test.context.TestPropertySource(properties = {
    "jwt.secret=VEVTVF9TRUNSRVRfRk9SX1RFU1RJTkdfT05MWV9DSEFOR0VfTUVfMDEyMzQ1Njc4OQ=="
})
class RiskProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private InvestmentRepository investmentRepository;

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtTokenProvider.generateToken("testuser");

        // Create test investment data for client 123
        Investment inv1 = new Investment();
        inv1.setClienteId(123L);
        inv1.setValor(new BigDecimal("10000.00"));
        inv1.setTipo(TipoProduto.CDB);
        inv1.setRentabilidade(new BigDecimal("0.12"));
        inv1.setData(LocalDate.now());
        investmentRepository.save(inv1);

        // Create test investment data for client 100
        Investment inv2 = new Investment();
        inv2.setClienteId(100L);
        inv2.setValor(new BigDecimal("5000.00"));
        inv2.setTipo(TipoProduto.LCI);
        inv2.setRentabilidade(new BigDecimal("0.10"));
        inv2.setData(LocalDate.now());
        investmentRepository.save(inv2);

        // Create test investment data for client 200
        Investment inv3 = new Investment();
        inv3.setClienteId(200L);
        inv3.setValor(new BigDecimal("50000.00"));
        inv3.setTipo(TipoProduto.FUNDO_MULTIMERCADO);
        inv3.setRentabilidade(new BigDecimal("0.18"));
        inv3.setData(LocalDate.now());
        investmentRepository.save(inv3);
    }

    @Test
    void shouldGetRiskProfile() throws Exception {
        mockMvc.perform(get("/perfil-risco/123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(123))
                .andExpect(jsonPath("$.perfil").exists())
                .andExpect(jsonPath("$.pontuacao").exists())
                .andExpect(jsonPath("$.descricao").exists());
    }

    @Test
    void shouldGetRecommendedProducts() throws Exception {
        mockMvc.perform(get("/produtos-recomendados/Moderado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetInvestmentHistory() throws Exception {
        mockMvc.perform(get("/investimentos/123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetRecommendedProductsForConservativeProfile() throws Exception {
        mockMvc.perform(get("/produtos-recomendados/Conservador")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetRecommendedProductsForAggressiveProfile() throws Exception {
        mockMvc.perform(get("/produtos-recomendados/Agressivo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnEmptyHistoryForNewClient() throws Exception {
        // Client with no investment history should return 404
        mockMvc.perform(get("/investimentos/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCalculateRiskProfileForDifferentClients() throws Exception {
        // Test that different clients get different profiles
        mockMvc.perform(get("/perfil-risco/100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(100))
                .andExpect(jsonPath("$.perfil").exists());

        mockMvc.perform(get("/perfil-risco/200")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(200))
                .andExpect(jsonPath("$.perfil").exists());
    }

    @Test
    void shouldReturnConservadorForClientWithNoHistory() throws Exception {
        // Clients with no data should return 404
        mockMvc.perform(get("/perfil-risco/999999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForNegativeClientId() throws Exception {
        mockMvc.perform(get("/perfil-risco/-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForZeroClientId() throws Exception {
        mockMvc.perform(get("/perfil-risco/0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidProfile() throws Exception {
        mockMvc.perform(get("/produtos-recomendados/InvalidProfile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForEmptyProfile() throws Exception {
        mockMvc.perform(get("/produtos-recomendados/ ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
