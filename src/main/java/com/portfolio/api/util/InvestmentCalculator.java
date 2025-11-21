package com.portfolio.api.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InvestmentCalculator {

    public BigDecimal calculateFinalValue(
            BigDecimal initialValue,
            BigDecimal annualRate,
            int monthsTerm
    ) {
        if (initialValue == null || annualRate == null || monthsTerm <= 0) {
            throw new IllegalArgumentException("Parâmetros de cálculo inválidos");
        }

        BigDecimal monthlyRate = annualRate.divide(
                BigDecimal.valueOf(12),
                10,
                RoundingMode.HALF_UP
        );

        double base = 1 + monthlyRate.doubleValue();
        double power = Math.pow(base, monthsTerm);

        return initialValue.multiply(
                BigDecimal.valueOf(power)
        ).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEffectiveReturn(
            BigDecimal initialValue,
            BigDecimal finalValue
    ) {
        if (initialValue == null || finalValue == null ||
                initialValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valores inválidos para cálculo de retorno");
        }

        return finalValue.subtract(initialValue)
                .divide(initialValue, 4, RoundingMode.HALF_UP);
    }
}
