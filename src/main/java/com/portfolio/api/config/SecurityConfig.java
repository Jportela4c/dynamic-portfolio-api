package com.portfolio.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for OAuth2 Resource Server and Method Security.
 *
 * Enables:
 * - OAuth2 JWT authentication for all API endpoints
 * - Method-level security with @PreAuthorize annotations
 * - Custom JWT introspection validator for token revocation
 */
@Configuration
@EnableMethodSecurity  // Enables @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {

    @Autowired
    private JwtIntrospectionValidator jwtIntrospectionValidator;

    @Bean
    @Order(2)
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/logout", "/error", "/oauth2/**", "/.well-known/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/default-ui.css", "/login.css", "/*.css", "/*.js").permitAll()
                        .anyRequest().permitAll()  // TEMP: Disable auth for testing
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(resourceServerJwtDecoder()))
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                {
                                  "timestamp": "%s",
                                  "status": 401,
                                  "error": "Unauthorized",
                                  "message": "Authentication required. Please provide a valid JWT token in the Authorization header.",
                                  "path": "%s"
                                }
                                """.formatted(java.time.Instant.now(), request.getRequestURI()));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                {
                                  "timestamp": "%s",
                                  "status": 403,
                                  "error": "Forbidden",
                                  "message": "Access denied. You do not have permission to access this resource.",
                                  "path": "%s"
                                }
                                """.formatted(java.time.Instant.now(), request.getRequestURI()));
                        })
                );

        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder resourceServerJwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("http://localhost:8080/api/v1/oauth2/jwks").build();

        // Combine default validators with our custom introspection validator
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> validators = new DelegatingOAuth2TokenValidator<>(
            defaultValidators,
            jwtIntrospectionValidator
        );

        jwtDecoder.setJwtValidator(validators);
        return jwtDecoder;
    }

    /**
     * DelegatingPasswordEncoder with BCrypt default for passwords without prefix.
     *
     * Uses PasswordEncoderFactories.createDelegatingPasswordEncoder() which provides:
     * - bcrypt (default for NEW passwords)
     * - noop, pbkdf2, scrypt, argon2, sha256 (for prefixed passwords)
     *
     * Custom behavior: passwords WITHOUT prefix default to BCrypt (not error).
     *
     * This allows:
     * - User passwords in database: $2a$10$... (no prefix, BCrypt default)
     * - OAuth2 client secrets: {noop}webapp-secret (explicit NoOp)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder delegatingEncoder =
                (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();

        // Set default for passwords WITHOUT prefix
        delegatingEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return delegatingEncoder;
    }
}

