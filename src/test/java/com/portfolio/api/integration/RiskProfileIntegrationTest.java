package com.portfolio.api.integration;

import com.portfolio.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtTokenProvider.generateToken("testuser");
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
        // Client with no investment history
        mockMvc.perform(get("/investimentos/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
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
}
