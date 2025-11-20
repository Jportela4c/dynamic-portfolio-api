package com.portfolio.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * OAuth2 client configuration with property-based settings.
 *
 * Production (default): AUTHORIZATION_CODE + REFRESH_TOKEN only (secure)
 * Dev profile: Adds PASSWORD grant for testing (INSECURE - deprecated in OAuth 2.1)
 */
@Configuration
@EnableConfigurationProperties(OAuth2ClientConfig.OAuth2ClientProperties.class)
public class OAuth2ClientConfig {

    private final PasswordEncoder passwordEncoder;
    private final OAuth2ClientProperties properties;

    public OAuth2ClientConfig(PasswordEncoder passwordEncoder, OAuth2ClientProperties properties) {
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        var builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(properties.clientId())
                .clientSecret(passwordEncoder.encode(properties.clientSecret()))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);

        // DEV ONLY: Add password grant if explicitly enabled
        if (properties.enablePasswordGrant()) {
            builder.authorizationGrantType(AuthorizationGrantType.PASSWORD);
        }

        RegisteredClient portfolioWebApp = builder
                .redirectUris(uris -> uris.addAll(properties.redirectUris()))
                .scopes(scopes -> scopes.addAll(properties.scopes()))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(properties.requireAuthorizationConsent())
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(properties.accessTokenTtl())
                        .refreshTokenTimeToLive(properties.refreshTokenTtl())
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(portfolioWebApp);
    }

    @ConfigurationProperties(prefix = "oauth2.client")
    public record OAuth2ClientProperties(
            String clientId,
            String clientSecret,
            List<String> redirectUris,
            List<String> scopes,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            boolean requireAuthorizationConsent,
            boolean enablePasswordGrant
    ) {}
}
