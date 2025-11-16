package com.portfolio.api.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RiskProfileCalculatorTest {

    private final RiskProfileCalculator calculator = new RiskProfileCalculator();

    @Test
    void shouldClassifyAsConservative() {
        int score = calculator.calculateScore(new BigDecimal("10000"), 2L);
        String profile = calculator.classifyProfile(score);

        assertEquals("Conservador", profile);
        assertTrue(score <= 40);
    }

    @Test
    void shouldClassifyAsModerate() {
        int score = calculator.calculateScore(new BigDecimal("75000"), 8L);
        String profile = calculator.classifyProfile(score);

        assertEquals("Moderado", profile);
        assertTrue(score > 40 && score <= 70);
    }

    @Test
    void shouldClassifyAsAggressive() {
        int score = calculator.calculateScore(new BigDecimal("200000"), 20L);
        String profile = calculator.classifyProfile(score);

        assertEquals("Agressivo", profile);
        assertTrue(score > 70);
    }

    @Test
    void shouldReturnCorrectDescription() {
        String conservativeDesc = calculator.getProfileDescription("Conservador");
        String moderateDesc = calculator.getProfileDescription("Moderado");
        String aggressiveDesc = calculator.getProfileDescription("Agressivo");

        assertNotNull(conservativeDesc);
        assertNotNull(moderateDesc);
        assertNotNull(aggressiveDesc);
        assertTrue(conservativeDesc.contains("baixo risco"));
        assertTrue(moderateDesc.contains("equilibrado"));
        assertTrue(aggressiveDesc.contains("alto risco"));
    }

    @Test
    void shouldHandleZeroVolume() {
        int score = calculator.calculateScore(BigDecimal.ZERO, 0L);
        assertEquals(0, score);
    }

    @Test
    void shouldHandleNullVolume() {
        int score = calculator.calculateScore(null, 5L);
        assertTrue(score >= 0);
    }
}
