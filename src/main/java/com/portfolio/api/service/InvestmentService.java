package com.portfolio.api.service;

import com.portfolio.api.exception.ServiceUnavailableException;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentPlatformProvider investmentPlatformProvider;
    private final CustomerValidationService customerValidationService;
    private final ClientIdentifierMapper clientIdentifierMapper;

    @CircuitBreaker(name = "ofbProvider")
    @Retry(name = "ofbProvider")
    @Cacheable(value = "portfolio-primary", key = "#clienteId")
    public List<InvestmentResponse> getClientInvestments(Long clienteId) {
        customerValidationService.validateClientExists(clienteId);

        String cpf = clientIdentifierMapper.getCpfForClient(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("CPF não encontrado para cliente: " + clienteId));

        log.debug("Fetching investments from provider for client: {}", clienteId);

        try {
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
        } catch (Exception ex) {
            log.error("Failed to fetch investments from OFB provider for client: {}", clienteId, ex);
            throw new ServiceUnavailableException(
                    "Serviço de investimentos temporariamente indisponível. Tente novamente em alguns instantes."
            );
        }
    }
}
