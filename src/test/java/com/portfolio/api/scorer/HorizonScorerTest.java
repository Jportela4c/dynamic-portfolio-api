package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HorizonScorerTest {

    private final HorizonScorer calculator = new HorizonScorer();

    @Test
    void shouldReturn50ForNoInvestments() {
        int score = calculator.calculateHorizonScore(Collections.emptyList());
        assertEquals(50, score);
    }

    @Test
    void shouldReturn50ForNullInvestments() {
        int score = calculator.calculateHorizonScore(null);
        assertEquals(50, score);
    }

    @Test
    void shouldReturn90ForDecadeLongInvestor() {
        List<Investment> investments = Collections.singletonList(createInvestment(3700));
        int score = calculator.calculateHorizonScore(investments);
        assertEquals(90, score);
    }

    @Test
    void shouldReturn70ForFiveYearInvestor() {
        List<Investment> investments = Collections.singletonList(createInvestment(1900));
        int score = calculator.calculateHorizonScore(investments);
        assertEquals(70, score);
    }

    @Test
    void shouldReturn50ForTwoYearInvestor() {
        List<Investment> investments = Collections.singletonList(createInvestment(750));
        int score = calculator.calculateHorizonScore(investments);
        assertEquals(50, score);
    }

    @Test
    void shouldReturn30ForOneYearInvestor() {
        List<Investment> investments = Collections.singletonList(createInvestment(365));
        int score = calculator.calculateHorizonScore(investments);
        assertEquals(30, score);
    }

    @Test
    void shouldReturn20ForNewInvestor() {
        List<Investment> investments = Collections.singletonList(createInvestment(180));
        int score = calculator.calculateHorizonScore(investments);
        assertEquals(20, score);
    }

    private Investment createInvestment(int daysAgo) {
        return Investment.builder()
                .tipo("CDB")
                .valor(new BigDecimal("10000"))
                .data(LocalDate.now().minusDays(daysAgo))
                .rentabilidade(new BigDecimal("0.10"))
                .build();
    }
}
