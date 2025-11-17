package com.portfolio.api.exception;

import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends ApiException {

    private static final String MESSAGE = "Customer not found";

    public CustomerNotFoundException(Long clientId) {
        super(HttpStatus.NOT_FOUND, MESSAGE, clientId);
    }
}
