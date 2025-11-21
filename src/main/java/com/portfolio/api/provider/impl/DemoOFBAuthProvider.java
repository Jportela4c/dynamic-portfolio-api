package com.portfolio.api.provider.impl;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.provider.OFBAuthProvider;
import com.portfolio.api.repository.CustomerRepository;
import com.portfolio.api.service.OFBAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Demo implementation of OFB authentication provider.
 * Loads customer from database and authenticates via CPF with mock server.
 */
@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class DemoOFBAuthProvider implements OFBAuthProvider {

    private final CustomerRepository customerRepository;
    private final OFBAuthService authService;

    @Override
    @Cacheable(value = "ofbTokens", key = "#customerId")
    public String authenticateCustomer(Long customerId) throws Exception {
        log.info("Authenticating customer {} with OFB mock server", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Cliente não encontrado: " + customerId
            ));

        String cpf = customer.getCpf();
        log.debug("Authenticating with CPF: {}", maskCpf(cpf));

        return authService.authenticateWithCPF(cpf);
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "***";
        }
        return cpf.substring(0, 3) + "*****" + cpf.substring(9);
    }
}
