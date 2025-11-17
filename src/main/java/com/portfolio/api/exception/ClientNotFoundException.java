package com.portfolio.api.exception;

import org.springframework.http.HttpStatus;

public class ClientNotFoundException extends ApiException {

    private static final String MESSAGE = "Client not found";

    public ClientNotFoundException(Long clientId) {
        super(HttpStatus.NOT_FOUND, MESSAGE, clientId);
    }
}
