package com.portfolio.api.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InvestmentCalculatorTest {

    private final InvestmentCalculator calculator = new InvestmentCalculator();

    @Test
    void shouldCalculateFinalValueCorrectly() {
        BigDecimal initialValue = new BigDecimal("10000.00");
        BigDecimal annualRate = new BigDecimal("0.12");
        int monthsTerm = 12;

        BigDecimal finalValue = calculator.calculateFinalValue(initialValue, annualRate, monthsTerm);

        assertNotNull(finalValue);
        assertTrue(finalValue.compareTo(initialValue) > 0);
        assertEquals(new BigDecimal("11200.00"), finalValue);
    }

    @Test
    void shouldThrowExceptionForNullInitialValue() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateFinalValue(null, new BigDecimal("0.12"), 12)
        );
    }

    @Test
    void shouldThrowExceptionForNullRate() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateFinalValue(new BigDecimal("10000"), null, 12)
        );
    }

    @Test
    void shouldThrowExceptionForInvalidTerm() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateFinalValue(new BigDecimal("10000"), new BigDecimal("0.12"), 0)
        );
    }

    @Test
    void shouldCalculateEffectiveReturn() {
        BigDecimal initialValue = new BigDecimal("10000.00");
        BigDecimal finalValue = new BigDecimal("11200.00");

        BigDecimal effectiveReturn = calculator.calculateEffectiveReturn(initialValue, finalValue);

        assertEquals(new BigDecimal("0.1200"), effectiveReturn);
    }
}
