package com.portfolio.api.util;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.repository.InvestmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentHorizonCalculatorTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentHorizonCalculator calculator;

    @Test
    void shouldReturn50ForNoInvestments() {
        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(Collections.emptyList());

        int score = calculator.calculateHorizonScore(1L);

        assertEquals(50, score);
    }

    @Test
    void shouldReturn90ForDecadeLongInvestor() {
        List<Investment> investments = Collections.singletonList(
                createInvestment(3700)
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(investments);

        int score = calculator.calculateHorizonScore(1L);

        assertEquals(90, score);
    }

    @Test
    void shouldReturn70ForFiveYearInvestor() {
        List<Investment> investments = Collections.singletonList(
                createInvestment(1900)
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(investments);

        int score = calculator.calculateHorizonScore(1L);

        assertEquals(70, score);
    }

    @Test
    void shouldReturn50ForTwoYearInvestor() {
        List<Investment> investments = Collections.singletonList(
                createInvestment(750)
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(investments);

        int score = calculator.calculateHorizonScore(1L);

        assertEquals(50, score);
    }

    @Test
    void shouldReturn30ForOneYearInvestor() {
        List<Investment> investments = Collections.singletonList(
                createInvestment(365)
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(investments);

        int score = calculator.calculateHorizonScore(1L);

        assertEquals(30, score);
    }

    @Test
    void shouldReturn20ForNewInvestor() {
        List<Investment> investments = Collections.singletonList(
                createInvestment(180)
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L))
                .thenReturn(investments);

        int score = calculator.calculateHorizonScore(1L);

        assertEquals(20, score);
    }

    private Investment createInvestment(int daysAgo) {
        Investment investment = new Investment();
        investment.setTipo("CDB");
        investment.setValor(new BigDecimal("10000"));
        investment.setData(LocalDate.now().minusDays(daysAgo));
        investment.setRentabilidade(new BigDecimal("0.10"));
        return investment;
    }
}
