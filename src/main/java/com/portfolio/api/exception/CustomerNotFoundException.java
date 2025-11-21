package com.portfolio.api.exception;

import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends ApiException {

    private static final String MESSAGE = "Cliente não encontrado";

    public CustomerNotFoundException(Long clientId) {
        super(HttpStatus.NOT_FOUND, MESSAGE, clientId);
    }
}
