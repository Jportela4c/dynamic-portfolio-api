package com.portfolio.api.scorer;

import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.dto.Investment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LiquidityScorerTest {

    private final LiquidityScorer scorer = new LiquidityScorer();

    @Test
    void shouldReturn50ForNoInvestments() {
        int score = scorer.calculateLiquidityScore(Collections.emptyList());
        assertEquals(50, score);
    }

    @Test
    void shouldReturn50ForNullInvestments() {
        int score = scorer.calculateLiquidityScore(null);
        assertEquals(50, score);
    }

    @Test
    void shouldReturnLowScoreForHighlyLiquidProducts() {
        // POUPANCA has liquidity level 1 (most liquid)
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.POUPANCA, new BigDecimal("10000")),
                createInvestment(TipoProduto.POUPANCA, new BigDecimal("10000"))
        );

        int score = scorer.calculateLiquidityScore(investments);

        // Liquidity level 1 → (1/10) * 100 = 10
        assertEquals(10, score);
    }

    @Test
    void shouldReturnHighScoreForIlliquidProducts() {
        // TESOURO_RENDA_MAIS has liquidity level 9 (illiquid/long-term)
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.TESOURO_RENDA_MAIS, new BigDecimal("10000")),
                createInvestment(TipoProduto.TESOURO_EDUCA_MAIS, new BigDecimal("10000"))
        );

        int score = scorer.calculateLiquidityScore(investments);

        // Liquidity level 9 → (9/10) * 100 = 90
        assertEquals(90, score);
    }

    @Test
    void shouldReturnMediumScoreForMixedProducts() {
        // CDB has liquidity level 5 (medium)
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.CDB, new BigDecimal("10000")),
                createInvestment(TipoProduto.LCI, new BigDecimal("10000")),
                createInvestment(TipoProduto.LCA, new BigDecimal("10000"))
        );

        int score = scorer.calculateLiquidityScore(investments);

        // All have liquidity level 5 → (5/10) * 100 = 50
        assertEquals(50, score);
    }

    @Test
    void shouldWeightByInvestmentValue() {
        // Higher value investment should have more weight
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.POUPANCA, new BigDecimal("1000")),      // Level 1, 10% weight
                createInvestment(TipoProduto.TESOURO_RENDA_MAIS, new BigDecimal("9000"))  // Level 9, 90% weight
        );

        int score = scorer.calculateLiquidityScore(investments);

        // Weighted: (1 * 0.1) + (9 * 0.9) = 0.1 + 8.1 = 8.2
        // Score: (8.2 / 10) * 100 = 82
        assertEquals(82, score);
    }

    @Test
    void shouldHandleVariableIncomeProducts() {
        // ACOES has liquidity level 8
        List<Investment> investments = List.of(
                createInvestment(TipoProduto.ACOES, new BigDecimal("10000"))
        );

        int score = scorer.calculateLiquidityScore(investments);

        // Level 8 → (8/10) * 100 = 80
        assertEquals(80, score);
    }

    @Test
    void shouldReturn50ForZeroValueInvestments() {
        List<Investment> investments = List.of(
                createInvestment(TipoProduto.CDB, BigDecimal.ZERO)
        );

        int score = scorer.calculateLiquidityScore(investments);

        // Total value is 0, should return neutral 50
        assertEquals(50, score);
    }

    private Investment createInvestment(TipoProduto tipo, BigDecimal valor) {
        return Investment.builder()
                .id(1L)
                .tipo(tipo)
                .valor(valor)
                .nomeProduto(tipo.name())
                .data(LocalDate.now())
                .build();
    }
}
