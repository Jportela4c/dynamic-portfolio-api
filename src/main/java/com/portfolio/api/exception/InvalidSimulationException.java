package com.portfolio.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidSimulationException extends ApiException {

    private static final String MESSAGE = "Invalid request";

    public InvalidSimulationException(String field) {
        super(HttpStatus.BAD_REQUEST, MESSAGE, field);
    }
}
