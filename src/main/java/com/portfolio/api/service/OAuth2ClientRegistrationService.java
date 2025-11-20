package com.portfolio.api.service;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service to dynamically generate OAuth2 client registrations for customers.
 *
 * Each customer gets their own OAuth2 client with the naming convention:
 * - Client ID: customer-{customerId}
 * - Client Secret: customer{customerId}Secret
 *
 * ADMIN user gets special client:
 * - Client ID: admin-client
 * - Client Secret: admin-secret
 */
@Service
public class OAuth2ClientRegistrationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2ClientRegistrationService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Generates RegisteredClient instances for all customers in the database.
     *
     * @return List of registered clients (one per customer)
     */
    public List<RegisteredClient> generateClientRegistrations() {
        List<RegisteredClient> clients = new ArrayList<>();

        // Generate client for each customer
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            RegisteredClient client = createClientForCustomer(customer);
            clients.add(client);
        }

        return clients;
    }

    /**
     * Creates a RegisteredClient for a specific customer.
     *
     * Client credentials:
     * - Client ID: customer-{customerId} or admin-client
     * - Client Secret: customer{customerId}Secret or admin-secret
     */
    private RegisteredClient createClientForCustomer(Customer customer) {
        String clientId;
        String clientSecret;

        // Special handling for ADMIN user
        if (customer.getId() == 999L) {
            clientId = "admin-client";
            clientSecret = "admin-secret";
        } else {
            clientId = "customer-" + customer.getId();
            clientSecret = "customer" + customer.getId() + "Secret";
        }

        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .build())
                .build();
    }
}
