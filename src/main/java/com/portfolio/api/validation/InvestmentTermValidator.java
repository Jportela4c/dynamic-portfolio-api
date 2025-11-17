package com.portfolio.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InvestmentTermValidator implements ConstraintValidator<ValidInvestmentTerm, Integer> {

    private static final int MIN_TERM_MONTHS = 1;
    private static final int MAX_TERM_MONTHS = 600;

    @Override
    public boolean isValid(Integer prazoMeses, ConstraintValidatorContext context) {
        if (prazoMeses == null) {
            return false;
        }
        return prazoMeses >= MIN_TERM_MONTHS && prazoMeses <= MAX_TERM_MONTHS;
    }
}
