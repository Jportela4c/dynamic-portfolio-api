package com.portfolio.api.service;

import com.portfolio.api.model.enums.UserRole;
import com.portfolio.api.service.impl.AuthorizationValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AuthorizationValidatorImpl (default/prod profile, ADMIN bypass disabled).
 */
class AuthorizationValidatorImplTest {

    private AuthorizationValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new AuthorizationValidatorImpl();
    }

    private Authentication createAuthenticationWithClaims(Long userId, String roleCode, String cpf) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", roleCode);
        claims.put("cpf", cpf);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        return auth;
    }

    @Test
    void canAccessCustomer_shouldReturnTrue_whenCustomerAccessesOwnData() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        boolean result = validator.canAccessCustomer(auth, 1L);
        assertTrue(result);
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenCustomerAccessesOtherData() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        boolean result = validator.canAccessCustomer(auth, 2L);
        assertFalse(result);
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenAdminTriesToAccessOtherData() {
        // Prod mode: ADMIN role has NO effect, strict validation only
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");

        // ADMIN cannot access other customers
        assertFalse(validator.canAccessCustomer(auth, 1L));
        assertFalse(validator.canAccessCustomer(auth, 2L));
    }

    @Test
    void canAccessCustomer_shouldReturnTrue_whenAdminAccessesOwnId() {
        // ADMIN can still access their own data (userId == customerId)
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");
        assertTrue(validator.canAccessCustomer(auth, 999L));
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenAuthenticationIsNull() {
        boolean result = validator.canAccessCustomer(null, 1L);
        assertFalse(result);
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenCustomerIdIsNull() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        boolean result = validator.canAccessCustomer(auth, null);
        assertFalse(result);
    }

    @Test
    void getUserId_shouldReturnUserId_whenPresent() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        assertEquals(1L, validator.getUserId(auth));
    }

    @Test
    void getUserCpf_shouldReturnCpf_whenPresent() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        assertEquals("12345678901", validator.getUserCpf(auth));
    }

    @Test
    void getUserRole_shouldReturnRole_whenPresent() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        assertEquals(UserRole.CUSTOMER, validator.getUserRole(auth));
    }
}
