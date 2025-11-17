package com.portfolio.api.scorer;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.scorer.ProductRiskScorer;
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
class ProductRiskScorerTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private ProductRiskScorer calculator;

    @Test
    void shouldReturn50ForNoInvestments() {
        when(investmentRepository.findByClienteIdOrderByDataDesc(1L)).thenReturn(Collections.emptyList());

        int score = calculator.calculateProductRiskScore(1L);

        assertEquals(50, score);
    }

    @Test
    void shouldCalculateWeightedRiskForConservativeProducts() {
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.CDB, new BigDecimal("10000")),
                createInvestment(TipoProduto.TESOURO_DIRETO, new BigDecimal("10000"))
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L)).thenReturn(investments);

        int score = calculator.calculateProductRiskScore(1L);

        assertEquals(30, score);
    }

    @Test
    void shouldCalculateWeightedRiskForAggressiveProducts() {
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.FUNDO_ACOES, new BigDecimal("10000")),
                createInvestment(TipoProduto.FUNDO_ACOES, new BigDecimal("10000"))
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L)).thenReturn(investments);

        int score = calculator.calculateProductRiskScore(1L);

        assertEquals(90, score);
    }

    @Test
    void shouldCalculateWeightedRiskForMixedProducts() {
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.CDB, new BigDecimal("5000")),
                createInvestment(TipoProduto.LCI, new BigDecimal("5000")),
                createInvestment(TipoProduto.FUNDO_ACOES, new BigDecimal("10000"))
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L)).thenReturn(investments);

        int score = calculator.calculateProductRiskScore(1L);

        assertEquals(63, score);
    }

    @Test
    void shouldUseDefaultRiskForUnknownProduct() {
        List<Investment> investments = Collections.singletonList(
                createInvestment(TipoProduto.CDB, new BigDecimal("10000"))
        );

        when(investmentRepository.findByClienteIdOrderByDataDesc(1L)).thenReturn(investments);

        int score = calculator.calculateProductRiskScore(1L);

        assertEquals(40, score);
    }

    private Investment createInvestment(TipoProduto type, BigDecimal value) {
        Investment investment = new Investment();
        investment.setTipo(type);
        investment.setValor(value);
        investment.setData(LocalDate.now());
        investment.setRentabilidade(new BigDecimal("0.10"));
        return investment;
    }
}
