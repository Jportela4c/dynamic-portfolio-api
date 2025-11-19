package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmountScorerTest {

    private final AmountScorer calculator = new AmountScorer();

    @Test
    void shouldReturn50ForNullInvestments() {
        int score = calculator.calculateAmountScore(null);
        assertEquals(50, score);
    }

    @Test
    void shouldReturn50ForEmptyInvestments() {
        int score = calculator.calculateAmountScore(Collections.emptyList());
        assertEquals(50, score);
    }

    @Test
    void shouldReturn50ForZeroAmount() {
        List<Investment> investments = List.of(
                Investment.builder().valor(BigDecimal.ZERO).build()
        );
        int score = calculator.calculateAmountScore(investments);
        assertEquals(50, score);
    }

    @Test
    void shouldReturn50ForAnyAmount() {
        List<Investment> investments = List.of(
                Investment.builder().valor(new BigDecimal("10000")).build(),
                Investment.builder().valor(new BigDecimal("20000")).build()
        );
        int score = calculator.calculateAmountScore(investments);
        assertEquals(50, score);
    }
}
