package com.portfolio.api.service.external;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Declarative HTTP client for OFB Customers API.
 * Provides access to customer personal identification data.
 */
@HttpExchange("/open-banking/customers/v2")
public interface OFBCustomersClient {

    @GetExchange("/personal/identifications")
    String getPersonalIdentifications(
            @RequestHeader("Authorization") String authorization);
}
