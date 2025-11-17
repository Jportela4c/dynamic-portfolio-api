package com.portfolio.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InvestmentTermValidator.class)
public @interface ValidInvestmentTerm {
    String message() default "Invalid request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
