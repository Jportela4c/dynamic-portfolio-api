package com.portfolio.api.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus statusCode;
    private final Object context;

    protected ApiException(HttpStatus statusCode, String message, Object context) {
        super(message);
        this.statusCode = statusCode;
        this.context = context;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public Object getContext() {
        return context;
    }
}
