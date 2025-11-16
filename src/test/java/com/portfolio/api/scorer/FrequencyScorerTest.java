package com.portfolio.api.scorer;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.scorer.FrequencyScorer;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FrequencyScorerTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private FrequencyScorer calculator;

    @Test
    void shouldReturnZeroForNoTransactions() {
        when(investmentRepository.countByClienteId(1L)).thenReturn(0L);

        int score = calculator.calculateFrequencyScore(1L);

        assertEquals(0, score);
    }

    @Test
    void shouldReturnZeroForNullCount() {
        when(investmentRepository.countByClienteId(1L)).thenReturn(null);

        int score = calculator.calculateFrequencyScore(1L);

        assertEquals(0, score);
    }

    @Test
    void shouldReturn100ForHighFrequency() {
        when(investmentRepository.countByClienteId(1L)).thenReturn(24L);
        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(createInvestmentsOverPeriod(24, 365 * 2));

        int score = calculator.calculateFrequencyScore(1L);

        assertEquals(100, score);
    }

    @Test
    void shouldReturn70ForModerateFrequency() {
        when(investmentRepository.countByClienteId(1L)).thenReturn(8L);
        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(createInvestmentsOverPeriod(8, 365 * 2));

        int score = calculator.calculateFrequencyScore(1L);

        assertEquals(70, score);
    }

    @Test
    void shouldReturn40ForLowFrequency() {
        when(investmentRepository.countByClienteId(1L)).thenReturn(2L);
        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(createInvestmentsOverPeriod(2, 365 * 2));

        int score = calculator.calculateFrequencyScore(1L);

        assertEquals(40, score);
    }

    @Test
    void shouldReturn20ForVeryLowFrequency() {
        when(investmentRepository.countByClienteId(1L)).thenReturn(1L);
        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(createInvestmentsOverPeriod(1, 365 * 2));

        int score = calculator.calculateFrequencyScore(1L);

        assertEquals(20, score);
    }

    private List<Investment> createInvestmentsOverPeriod(int count, int daysAgo) {
        Investment investment = new Investment();
        investment.setTipo("CDB");
        investment.setValor(new BigDecimal("10000"));
        investment.setData(LocalDate.now().minusDays(daysAgo));
        investment.setRentabilidade(new BigDecimal("0.10"));
        return Collections.nCopies(count, investment);
    }
}
