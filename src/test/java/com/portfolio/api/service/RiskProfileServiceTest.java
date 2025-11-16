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
    void shouldClassifyAsUltraConservador() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(10);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(10);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(20);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(10);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(10);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Ultra Conservador", response.getPerfil());
        assertTrue(response.getPontuacao() <= 20);
        assertNotNull(response.getFatores());
    }

    @Test
    void shouldClassifyAsConservador() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(30);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(30);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(30);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(30);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(30);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Conservador", response.getPerfil());
        assertTrue(response.getPontuacao() > 20 && response.getPontuacao() <= 40);
    }

    @Test
    void shouldClassifyAsModeradoConservador() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(50);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(50);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(50);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(50);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(50);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Moderado Conservador", response.getPerfil());
        assertTrue(response.getPontuacao() > 40 && response.getPontuacao() <= 55);
    }

    @Test
    void shouldClassifyAsModerado() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(60);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(60);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(60);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(60);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(60);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Moderado", response.getPerfil());
        assertTrue(response.getPontuacao() > 55 && response.getPontuacao() <= 70);
    }

    @Test
    void shouldClassifyAsModeradoAgressivo() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(80);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(80);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(75);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(75);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(70);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertEquals("Moderado Agressivo", response.getPerfil());
        assertTrue(response.getPontuacao() > 70 && response.getPontuacao() <= 85);
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
        assertTrue(response.getPontuacao() > 85);
    }

    @Test
    void shouldIncludeAllFactorScoresInResponse() {
        when(volumeCalculator.calculateVolumeScore(1L)).thenReturn(50);
        when(frequencyCalculator.calculateFrequencyScore(1L)).thenReturn(60);
        when(productRiskCalculator.calculateProductRiskScore(1L)).thenReturn(70);
        when(liquidityCalculator.calculateLiquidityScore(1L)).thenReturn(40);
        when(horizonCalculator.calculateHorizonScore(1L)).thenReturn(30);

        RiskProfileResponse response = service.calculateRiskProfile(1L);

        assertNotNull(response.getFatores());
        assertEquals(50, response.getFatores().get("volume"));
        assertEquals(60, response.getFatores().get("frequencia"));
        assertEquals(70, response.getFatores().get("riscoProdutos"));
        assertEquals(40, response.getFatores().get("liquidez"));
        assertEquals(30, response.getFatores().get("horizonte"));
    }
}
