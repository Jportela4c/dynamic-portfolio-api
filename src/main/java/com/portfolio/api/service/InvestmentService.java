package com.portfolio.api.service;

import com.portfolio.api.mapper.ClientIdentifierMapper;
import com.portfolio.api.model.dto.response.InvestmentResponse;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.dto.Investment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentPlatformProvider investmentPlatformProvider;
    private final CustomerValidationService customerValidationService;
    private final ClientIdentifierMapper clientIdentifierMapper;

    @CircuitBreaker(name = "ofbProvider", fallbackMethod = "getClientInvestmentsFallback")
    @Retry(name = "ofbProvider")
    @Cacheable(value = "portfolio-primary", key = "#clienteId")
    public List<InvestmentResponse> getClientInvestments(Long clienteId) {
        customerValidationService.validateClientExists(clienteId);

        String cpf = clientIdentifierMapper.getCpfForClient(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("CPF not found for client: " + clienteId));

        log.debug("Fetching investments from provider for client: {}", clienteId);

        List<Investment> investments = investmentPlatformProvider.getInvestmentHistory(cpf);

        return investments.stream()
                .map(inv -> InvestmentResponse.builder()
                        .id(inv.getId())
                        .tipo(inv.getTipo())
                        .valor(inv.getValor())
                        .rentabilidade(inv.getRentabilidade())
                        .data(inv.getData())
                        .build())
                .collect(Collectors.toList());
    }

    @Cacheable(value = "portfolio-fallback", key = "#clienteId")
    public List<InvestmentResponse> getClientInvestmentsFallback(Long clienteId, Exception ex) {
        log.warn("Circuit breaker activated for client: {}. Returning fallback data. Error: {}",
                clienteId, ex.getMessage());

        return List.of(
                InvestmentResponse.builder()
                        .id(0L)
                        .tipo("SISTEMA_INDISPONIVEL")
                        .valor(BigDecimal.ZERO)
                        .rentabilidade(BigDecimal.ZERO)
                        .data(LocalDate.now())
                        .build()
        );
    }
}
