package com.portfolio.api.config;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * Customizes JWT tokens to include custom claims for RBAC.
 *
 * Adds the following claims:
 * - userId: Customer's database ID
 * - role: Customer's role (CUSTOMER or ADMIN)
 * - cpf: Customer's CPF (for OFB integration)
 *
 * Uses authenticated user's email (from username/password authentication)
 * to lookup customer and add custom claims to the JWT.
 */
@Configuration
public class JwtTokenCustomizer {

    private final CustomerRepository customerRepository;

    public JwtTokenCustomizer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            // Only customize access tokens (not refresh tokens)
            if (context.getTokenType().getValue().equals("access_token")) {
                // Get authenticated user's email from Spring Security authentication
                String email = context.getPrincipal().getName();
                Customer customer = findCustomerByEmail(email);

                if (customer != null) {
                    context.getClaims().claim("userId", customer.getId());
                    context.getClaims().claim("role", customer.getRole().name());
                    context.getClaims().claim("cpf", customer.getCpf());
                }
            }
        };
    }

    /**
     * Finds customer by email address (used as username in authentication).
     *
     * @param email The authenticated user's email
     * @return Customer entity or null if not found
     */
    private Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
}
