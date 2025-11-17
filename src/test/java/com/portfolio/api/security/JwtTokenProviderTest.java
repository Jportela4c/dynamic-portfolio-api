package com.portfolio.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private static final String TEST_SECRET = "VEVTVF9TRUNSRVRfRk9SX1RFU1RJTkdfT05MWV9DSEFOR0VfTUVfMDEyMzQ1Njc4OQ==";
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", TEST_EXPIRATION);
        tokenProvider.init();
    }

    @Test
    void shouldGenerateValidToken() {
        // When
        String token = tokenProvider.generateToken("testuser");

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String username = "testuser";
        String token = tokenProvider.generateToken(username);

        // When
        String extractedUsername = tokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        String token = tokenProvider.generateToken("testuser");

        // When
        boolean isValid = tokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectTamperedToken() {
        // Given
        String token = tokenProvider.generateToken("testuser");
        String tamperedToken = token.substring(0, token.length() - 5) + "AAAAA";

        // When
        boolean isValid = tokenProvider.validateToken(tamperedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        // Given - Create token with different secret
        byte[] differentKeyBytes = Base64.getDecoder().decode("RElGRkVSRU5UX1NFQ1JFVF9LRVlfRk9SX1RFU1RJTkdfT05MWV9DSEFOR0VfTUU=");
        SecretKey differentKey = Keys.hmacShaKeyFor(differentKeyBytes);

        String tokenWithDifferentKey = Jwts.builder()
            .setSubject("testuser")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(differentKey)
            .compact();

        // When
        boolean isValid = tokenProvider.validateToken(tokenWithDifferentKey);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When
        boolean isValid = tokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectNullToken() {
        // When
        boolean isValid = tokenProvider.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectEmptyToken() {
        // When
        boolean isValid = tokenProvider.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // When
        String token1 = tokenProvider.generateToken("user1");
        String token2 = tokenProvider.generateToken("user2");

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(tokenProvider.getUsernameFromToken(token1)).isEqualTo("user1");
        assertThat(tokenProvider.getUsernameFromToken(token2)).isEqualTo("user2");
    }

    @Test
    void shouldGenerateDifferentTokensForSameUserOverTime() throws InterruptedException {
        // Given
        String username = "testuser";

        // When
        String token1 = tokenProvider.generateToken(username);
        Thread.sleep(1100); // Wait for different second timestamp
        String token2 = tokenProvider.generateToken(username);

        // Then
        assertThat(token1).isNotEqualTo(token2); // Different due to different issuedAt
        assertThat(tokenProvider.getUsernameFromToken(token1)).isEqualTo(username);
        assertThat(tokenProvider.getUsernameFromToken(token2)).isEqualTo(username);
    }
}
