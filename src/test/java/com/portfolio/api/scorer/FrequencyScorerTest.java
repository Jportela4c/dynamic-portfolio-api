package com.portfolio.api.scorer;

import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.dto.Investment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyScorerTest {

    private final FrequencyScorer scorer = new FrequencyScorer();

    // Fixed reference dates for consistent testing
    private static final LocalDate TODAY = LocalDate.of(2025, 11, 22);
    private static final LocalDate TWO_YEARS_AGO = LocalDate.of(2023, 11, 22);

    @Test
    void shouldReturnZeroForNoTransactions() {
        int score = scorer.calculateFrequencyScore(Collections.emptyList());
        assertEquals(0, score);
    }

    @Test
    void shouldReturnZeroForNullList() {
        int score = scorer.calculateFrequencyScore(null);
        assertEquals(0, score);
    }

    @Test
    void shouldReturn20ForInvestmentsWithNoTransactionCount() {
        // Investments with null transactionCount should return 20 (conservative)
        List<Investment> investments = List.of(
                Investment.builder()
                        .tipo(TipoProduto.CDB)
                        .valor(new BigDecimal("10000"))
                        .data(TODAY.minusYears(1))
                        .build()
        );
        int score = scorer.calculateFrequencyScore(investments);
        assertEquals(20, score); // No transaction activity = conservative
    }

    @Test
    void shouldReturn100ForHighFrequency() {
        // 24 transactions over 2 years = 12 per year = monthly = Aggressive
        // Need enough transactions to get >= 12/year
        List<Investment> investments = List.of(
                createInvestmentWithTransactions(30, TWO_YEARS_AGO, TODAY)
        );
        int score = scorer.calculateFrequencyScore(investments);
        assertEquals(100, score);
    }

    @Test
    void shouldReturn70ForModerateFrequency() {
        // Need 4-12 transactions per year
        // 10 transactions over 2 years = 5 per year = quarterly = Moderate-Aggressive
        List<Investment> investments = List.of(
                createInvestmentWithTransactions(10, TWO_YEARS_AGO, TODAY)
        );
        int score = scorer.calculateFrequencyScore(investments);
        assertEquals(70, score);
    }

    @Test
    void shouldReturn40ForLowFrequency() {
        // Need 1-4 transactions per year
        // 3 transactions over 2 years = 1.5 per year = annual = Moderate
        List<Investment> investments = List.of(
                createInvestmentWithTransactions(3, TWO_YEARS_AGO, TODAY)
        );
        int score = scorer.calculateFrequencyScore(investments);
        assertEquals(40, score);
    }

    @Test
    void shouldReturn20ForVeryLowFrequency() {
        // 1 transaction over 2 years = 0.5 per year = less than annual = Conservative
        List<Investment> investments = List.of(
                createInvestmentWithTransactions(1, TWO_YEARS_AGO, TODAY)
        );
        int score = scorer.calculateFrequencyScore(investments);
        assertEquals(20, score);
    }

    @Test
    void shouldSumTransactionsAcrossMultipleInvestments() {
        // 15 + 15 = 30 transactions over 2 years = 15 per year = 100
        List<Investment> investments = List.of(
                createInvestmentWithTransactions(15, TWO_YEARS_AGO, TODAY),
                createInvestmentWithTransactions(15, TWO_YEARS_AGO, TODAY)
        );
        int score = scorer.calculateFrequencyScore(investments);
        assertEquals(100, score);
    }

    private Investment createInvestmentWithTransactions(int transactionCount,
                                                        LocalDate firstDate,
                                                        LocalDate lastDate) {
        return Investment.builder()
                .tipo(TipoProduto.CDB)
                .valor(new BigDecimal("10000"))
                .data(firstDate)
                .transactionCount(transactionCount)
                .firstTransactionDate(firstDate)
                .lastTransactionDate(lastDate)
                .build();
    }
}
