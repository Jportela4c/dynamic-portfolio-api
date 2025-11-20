package com.portfolio.api.config;

import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

/**
 * OAuth2 client configuration with profile-aware grant types.
 *
 * Production (default): AUTHORIZATION_CODE + REFRESH_TOKEN only (secure)
 * Dev profile: Adds PASSWORD grant for testing (INSECURE - deprecated in OAuth 2.1)
 */
@Configuration
public class OAuth2ClientConfig {

    private final PasswordEncoder passwordEncoder;

    @Value("${oauth2.dev.enable-password-grant:false}")
    private boolean enablePasswordGrant;

    public OAuth2ClientConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        var builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("portfolio-web-app")
                .clientSecret(passwordEncoder.encode("webapp-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);

        // DEV ONLY: Add password grant if explicitly enabled
        if (enablePasswordGrant) {
            builder.authorizationGrantType(AuthorizationGrantType.PASSWORD);
        }

        RegisteredClient portfolioWebApp = builder
                .redirectUri("http://localhost:3000/callback")
                .redirectUri("http://localhost:8080/authorized")
                .redirectUri("https://oauth.pstmn.io/v1/callback")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
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

        return new InMemoryRegisteredClientRepository(portfolioWebApp);
    }
}
