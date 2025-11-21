package com.portfolio.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.mapper.ClientIdentifierMapper;
import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.model.enums.PerfilRisco;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.dto.Investment;
import com.portfolio.api.repository.InvestmentDataCacheRepository;
import com.portfolio.api.scorer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskProfileServiceTest {

    @Mock
    private AmountScorer amountCalculator;

    @Mock
    private FrequencyScorer frequencyCalculator;

    @Mock
    private ProductRiskScorer productRiskCalculator;

    @Mock
    private LiquidityScorer liquidityCalculator;

    @Mock
    private HorizonScorer horizonCalculator;

    @Mock
    private CustomerValidationService customerValidationService;

    @Mock
    private InvestmentPlatformProvider investmentPlatformProvider;

    @Mock
    private ClientIdentifierMapper clientIdentifierMapper;

    @Mock
    private InvestmentDataCacheRepository cacheRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RiskProfileService service;

    @Test
    void shouldClassifyAsConservadorLowScore() {
        Long clienteId = 1L;
        String cpf = "12345678900";
        List<Investment> conservativeInvestments = createConservativeInvestments();

        when(clientIdentifierMapper.getCpfForClient(clienteId)).thenReturn(Optional.of(cpf));
        when(investmentPlatformProvider.getInvestmentHistory(cpf)).thenReturn(conservativeInvestments);
        when(amountCalculator.calculateAmountScore(conservativeInvestments)).thenReturn(20);
        when(frequencyCalculator.calculateFrequencyScore(conservativeInvestments)).thenReturn(10);
        when(productRiskCalculator.calculateProductRiskScore(conservativeInvestments)).thenReturn(20);
        when(liquidityCalculator.calculateLiquidityScore(conservativeInvestments)).thenReturn(10);
        when(horizonCalculator.calculateHorizonScore(conservativeInvestments)).thenReturn(10);

        RiskProfileResponse response = service.calculateRiskProfile(clienteId);

        assertEquals(PerfilRisco.CONSERVADOR, response.getPerfil());
        assertTrue(response.getPontuacao() <= 40);
        assertEquals("Perfil de baixo risco, focado em segurança e liquidez.", response.getDescricao());
    }

    @Test
    void shouldClassifyAsModerado() {
        Long clienteId = 1L;
        String cpf = "12345678900";
        List<Investment> moderateInvestments = createModerateInvestments();

        when(clientIdentifierMapper.getCpfForClient(clienteId)).thenReturn(Optional.of(cpf));
        when(investmentPlatformProvider.getInvestmentHistory(cpf)).thenReturn(moderateInvestments);
        when(amountCalculator.calculateAmountScore(moderateInvestments)).thenReturn(50);
        when(frequencyCalculator.calculateFrequencyScore(moderateInvestments)).thenReturn(50);
        when(productRiskCalculator.calculateProductRiskScore(moderateInvestments)).thenReturn(45);
        when(liquidityCalculator.calculateLiquidityScore(moderateInvestments)).thenReturn(45);
        when(horizonCalculator.calculateHorizonScore(moderateInvestments)).thenReturn(40);

        RiskProfileResponse response = service.calculateRiskProfile(clienteId);

        assertEquals(PerfilRisco.MODERADO, response.getPerfil());
        assertTrue(response.getPontuacao() > 40 && response.getPontuacao() <= 70);
        assertEquals("Perfil equilibrado entre segurança e rentabilidade.", response.getDescricao());
    }

    @Test
    void shouldClassifyAsAgressivo() {
        Long clienteId = 1L;
        String cpf = "12345678900";
        List<Investment> aggressiveInvestments = createAggressiveInvestments();

        when(clientIdentifierMapper.getCpfForClient(clienteId)).thenReturn(Optional.of(cpf));
        when(investmentPlatformProvider.getInvestmentHistory(cpf)).thenReturn(aggressiveInvestments);
        when(amountCalculator.calculateAmountScore(aggressiveInvestments)).thenReturn(90);
        when(frequencyCalculator.calculateFrequencyScore(aggressiveInvestments)).thenReturn(100);
        when(productRiskCalculator.calculateProductRiskScore(aggressiveInvestments)).thenReturn(90);
        when(liquidityCalculator.calculateLiquidityScore(aggressiveInvestments)).thenReturn(100);
        when(horizonCalculator.calculateHorizonScore(aggressiveInvestments)).thenReturn(90);

        RiskProfileResponse response = service.calculateRiskProfile(clienteId);

        assertEquals(PerfilRisco.AGRESSIVO, response.getPerfil());
        assertTrue(response.getPontuacao() > 70);
        assertEquals("Perfil de alto risco, focado em alta rentabilidade.", response.getDescricao());
    }

    private List<Investment> createConservativeInvestments() {
        return Arrays.asList(
                createInvestment(TipoProduto.POUPANCA, new BigDecimal("5000")),
                createInvestment(TipoProduto.TESOURO_SELIC, new BigDecimal("10000"))
        );
    }

    private List<Investment> createModerateInvestments() {
        return Arrays.asList(
                createInvestment(TipoProduto.CDB, new BigDecimal("10000")),
                createInvestment(TipoProduto.LCI, new BigDecimal("10000")),
                createInvestment(TipoProduto.RENDA_FIXA, new BigDecimal("5000"))
        );
    }

    private List<Investment> createAggressiveInvestments() {
        return Arrays.asList(
                createInvestment(TipoProduto.ACOES, new BigDecimal("20000")),
                createInvestment(TipoProduto.MULTIMERCADO, new BigDecimal("15000"))
        );
    }

    private Investment createInvestment(TipoProduto tipo, BigDecimal valor) {
        return Investment.builder()
                .id(1L)
                .tipo(tipo)
                .tipoOperacao("APLICACAO")
                .valor(valor)
                .rentabilidade(new BigDecimal("0.10"))
                .data(LocalDate.now().minusMonths(6))
                .nomeProduto("Test Product")
                .build();
    }
}
