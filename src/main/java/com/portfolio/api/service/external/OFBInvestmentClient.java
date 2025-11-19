package com.portfolio.api.service.external;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Declarative HTTP client for OFB Investment API.
 * Uses Spring 6.1+ HTTP Interface for clean, annotation-based API calls.
 */
@HttpExchange("/open-banking/bank-fixed-incomes/v1")
public interface OFBInvestmentClient {

    @GetExchange("/investments")
    String getInvestments(@RequestHeader("Authorization") String authorization);
}
