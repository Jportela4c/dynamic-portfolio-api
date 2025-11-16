package com.portfolio.api.service;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.scorer.FrequencyScorer;
import com.portfolio.api.scorer.HorizonScorer;
import com.portfolio.api.scorer.LiquidityScorer;
import com.portfolio.api.scorer.ProductRiskScorer;
import com.portfolio.api.scorer.VolumeScorer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskProfileServiceTest {

    @Mock
    private VolumeScorer volumeCalculator;

    @Mock
    private FrequencyScorer frequencyCalculator;

    @Mock
    private ProductRiskScorer productRiskCalculator;

    @Mock
    private LiquidityScorer liquidityCalculator;

    @Mock
    private HorizonScorer horizonCalculator;

    @InjectMocks
    private RiskProfileService service;

    @Test
    void shouldClassifyAsConservadorLowScore() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(20);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(10);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(20);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(10);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(10);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Conservador", response.getPerfil());
        assertTrue(response.getPontuacao() <= 40);
        assertEquals("Perfil de baixo risco, focado em segurança e liquidez.", response.getDescricao());
    }

    @Test
    void shouldClassifyAsConservadorHighScore() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(40);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(40);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(40);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(40);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(40);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Conservador", response.getPerfil());
        assertTrue(response.getPontuacao() <= 40);
    }

    @Test
    void shouldClassifyAsModeradoLowScore() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(50);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(50);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(45);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(45);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(40);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Moderado", response.getPerfil());
        assertTrue(response.getPontuacao() > 40 && response.getPontuacao() <= 70);
        assertEquals("Perfil equilibrado entre segurança e rentabilidade.", response.getDescricao());
    }

    @Test
    void shouldClassifyAsModeradoHighScore() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(70);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(70);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(70);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(70);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(70);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Moderado", response.getPerfil());
        assertTrue(response.getPontuacao() > 40 && response.getPontuacao() <= 70);
    }

    @Test
    void shouldClassifyAsAgressivo() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(90);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(100);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(90);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(100);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(90);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Agressivo", response.getPerfil());
        assertTrue(response.getPontuacao() > 70);
        assertEquals("Perfil de alto risco, focado em alta rentabilidade.", response.getDescricao());
    }

    @Test
    void shouldCalculateWeightedScore() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(50);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(60);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(70);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(40);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(30);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertNotNull(response);
        assertNotNull(response.getClienteId());
        assertNotNull(response.getPerfil());
        assertNotNull(response.getPontuacao());
        assertNotNull(response.getDescricao());

        // Weighted score: 50*0.25 + 60*0.20 + 70*0.30 + 40*0.15 + 30*0.10 = 57
        assertEquals(57, response.getPontuacao());
        assertEquals("Moderado", response.getPerfil());
    }
}
