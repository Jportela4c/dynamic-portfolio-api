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

class ProductRiskScorerTest {

    private final ProductRiskScorer calculator = new ProductRiskScorer();

    @Test
    void shouldReturn50ForNoInvestments() {
        int score = calculator.calculateProductRiskScore(Collections.emptyList());
        assertEquals(50, score);
    }

    @Test
    void shouldReturn50ForNullInvestments() {
        int score = calculator.calculateProductRiskScore(null);
        assertEquals(50, score);
    }

    @Test
    void shouldCalculateWeightedRiskForConservativeProducts() {
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.CDB, new BigDecimal("10000")),
                createInvestment(TipoProduto.TESOURO_DIRETO, new BigDecimal("10000"))
        );
        int score = calculator.calculateProductRiskScore(investments);
        assertEquals(30, score);
    }

    @Test
    void shouldCalculateWeightedRiskForAggressiveProducts() {
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.FUNDO_ACOES, new BigDecimal("10000")),
                createInvestment(TipoProduto.FUNDO_ACOES, new BigDecimal("10000"))
        );
        int score = calculator.calculateProductRiskScore(investments);
        assertEquals(90, score);
    }

    @Test
    void shouldCalculateWeightedRiskForMixedProducts() {
        List<Investment> investments = Arrays.asList(
                createInvestment(TipoProduto.CDB, new BigDecimal("5000")),
                createInvestment(TipoProduto.LCI, new BigDecimal("5000")),
                createInvestment(TipoProduto.FUNDO_ACOES, new BigDecimal("10000"))
        );
        int score = calculator.calculateProductRiskScore(investments);
        assertEquals(63, score);
    }

    private Investment createInvestment(TipoProduto type, BigDecimal value) {
        return Investment.builder()
                .tipo(type)
                .valor(value)
                .data(LocalDate.now())
                .rentabilidade(new BigDecimal("0.10"))
                .build();
    }
}
