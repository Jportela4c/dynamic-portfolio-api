package com.portfolio.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class InvestmentValueValidator implements ConstraintValidator<ValidInvestmentValue, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
}
