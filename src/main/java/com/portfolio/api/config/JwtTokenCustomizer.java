package com.portfolio.api.config;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Customizes JWT tokens to include custom claims for RBAC.
 *
 * Adds the following claims:
 * - userId: Customer's database ID
 * - role: Customer's role (CUSTOMER or ADMIN)
 * - cpf: Customer's CPF (for OFB integration)
 *
 * Client ID naming convention:
 * - "customer-{customerId}" -> Maps to customer with that ID
 * - "admin-client" -> Maps to ADMIN user (id=999)
 */
@Configuration
public class JwtTokenCustomizer {

    private static final Pattern CUSTOMER_CLIENT_PATTERN = Pattern.compile("^customer-(\\d+)$");
    private final CustomerRepository customerRepository;

    public JwtTokenCustomizer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            // Only customize access tokens (not refresh tokens)
            if (context.getTokenType().getValue().equals("access_token")) {
                String clientId = context.getRegisteredClient().getClientId();
                Customer customer = findCustomerByClientId(clientId);

                if (customer != null) {
                    context.getClaims().claim("userId", customer.getId());
                    context.getClaims().claim("role", customer.getRole().getCode());
                    context.getClaims().claim("cpf", customer.getCpf());
                }
            }
        };
    }

    /**
     * Maps OAuth2 client_id to a Customer entity using naming convention.
     *
     * Mapping rules:
     * - "customer-{id}" -> Customer with that database ID
     * - "admin-client" -> ADMIN user (id=999)
     *
     * Examples:
     * - "customer-1" -> Customer ID 1
     * - "customer-2" -> Customer ID 2
     * - "admin-client" -> Customer ID 999 (ADMIN role)
     */
    private Customer findCustomerByClientId(String clientId) {
        // Admin client (demo only)
        if ("admin-client".equals(clientId)) {
            return customerRepository.findById(999L).orElse(null);
        }

        // Customer-specific clients: customer-{id}
        Matcher matcher = CUSTOMER_CLIENT_PATTERN.matcher(clientId);
        if (matcher.matches()) {
            Long customerId = Long.parseLong(matcher.group(1));
            return customerRepository.findById(customerId).orElse(null);
        }

        return null;
    }
}
