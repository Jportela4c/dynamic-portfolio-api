package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmountScorerTest {

    private final AmountScorer scorer = new AmountScorer();

    @Test
    void shouldReturn20ForNullInvestments() {
        int score = scorer.calculateAmountScore(null);
        assertEquals(20, score); // Conservative - no investment history
    }

    @Test
    void shouldReturn20ForEmptyInvestments() {
        int score = scorer.calculateAmountScore(Collections.emptyList());
        assertEquals(20, score); // Conservative - no investment history
    }

    @Test
    void shouldReturn20ForZeroAmount() {
        List<Investment> investments = List.of(
                Investment.builder().valor(BigDecimal.ZERO).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertEquals(20, score); // Conservative - no meaningful investment
    }

    @Test
    void shouldReturnLowScoreForVeryLowAmount() {
        // < R$ 10k: score 10-25
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("5000")).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertTrue(score >= 10 && score <= 25, "Score should be between 10-25 for < R$10k");
    }

    @Test
    void shouldReturnModerateScoreForLowAmount() {
        // R$ 10k - R$ 50k: score 25-40
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("30000")).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertTrue(score >= 25 && score <= 40, "Score should be between 25-40 for R$10k-50k");
    }

    @Test
    void shouldReturnModerateScoreForModerateAmount() {
        // R$ 50k - R$ 150k: score 40-60
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("100000")).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertTrue(score >= 40 && score <= 60, "Score should be between 40-60 for R$50k-150k");
    }

    @Test
    void shouldReturnHighScoreForHighAmount() {
        // R$ 150k - R$ 500k: score 60-80
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("300000")).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertTrue(score >= 60 && score <= 80, "Score should be between 60-80 for R$150k-500k");
    }

    @Test
    void shouldReturnVeryHighScoreForVeryHighAmount() {
        // R$ 500k - R$ 1M: score 80-95
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("750000")).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertTrue(score >= 80 && score <= 95, "Score should be between 80-95 for R$500k-1M");
    }

    @Test
    void shouldReturn100ForProfessionalInvestor() {
        // R$ 1M+: score 100
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("1500000")).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertEquals(100, score); // Professional Investor
    }

    @Test
    void shouldSumMultipleInvestments() {
        // Two investments totaling R$ 1M+ should score 100
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("600000")).build(),
                Investment.builder().valor(new BigDecimal("500000")).build()
        );
        int score = scorer.calculateAmountScore(investments);
        assertEquals(100, score); // Total R$ 1.1M = Professional Investor
    }
}
