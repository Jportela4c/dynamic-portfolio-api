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

    private final FrequencyScorer calculator = new FrequencyScorer();

    @Test
    void shouldReturnZeroForNoTransactions() {
        int score = calculator.calculateFrequencyScore(Collections.emptyList());
        assertEquals(0, score);
    }

    @Test
    void shouldReturnZeroForNullList() {
        int score = calculator.calculateFrequencyScore(null);
        assertEquals(0, score);
    }

    @Test
    void shouldReturn100ForHighFrequency() {
        List<Investment> investments = createInvestmentsOverPeriod(24, 365 * 2);
        int score = calculator.calculateFrequencyScore(investments);
        assertEquals(100, score);
    }

    @Test
    void shouldReturn70ForModerateFrequency() {
        List<Investment> investments = createInvestmentsOverPeriod(8, 365 * 2);
        int score = calculator.calculateFrequencyScore(investments);
        assertEquals(70, score);
    }

    @Test
    void shouldReturn40ForLowFrequency() {
        List<Investment> investments = createInvestmentsOverPeriod(2, 365 * 2);
        int score = calculator.calculateFrequencyScore(investments);
        assertEquals(40, score);
    }

    @Test
    void shouldReturn20ForVeryLowFrequency() {
        List<Investment> investments = createInvestmentsOverPeriod(1, 365 * 2);
        int score = calculator.calculateFrequencyScore(investments);
        assertEquals(20, score);
    }

    private List<Investment> createInvestmentsOverPeriod(int count, int daysAgo) {
        Investment investment = Investment.builder()
                .tipo(TipoProduto.CDB)
                .valor(new BigDecimal("10000"))
                .data(LocalDate.now().minusDays(daysAgo))
                .rentabilidade(new BigDecimal("0.10"))
                .build();
        return Collections.nCopies(count, investment);
    }
}
