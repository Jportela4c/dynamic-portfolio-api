package com.portfolio.api.service.external;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/open-banking/treasure-titles/v1")
public interface OFBTreasuryTitlesClient {

    @GetExchange("/investments")
    String getInvestments(@RequestHeader("Authorization") String authorization);

    @GetExchange("/investments/{investmentId}")
    String getInvestmentDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("investmentId") String investmentId);

    @GetExchange("/investments/{investmentId}/balances")
    String getInvestmentBalances(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("investmentId") String investmentId);

    @GetExchange("/investments/{investmentId}/transactions")
    String getInvestmentTransactions(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("investmentId") String investmentId);
}
