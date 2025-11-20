package com.portfolio.api.service;

import com.portfolio.api.model.enums.UserRole;
import com.portfolio.api.service.impl.DevAuthorizationValidator;
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
 * Tests for DevAuthorizationValidator (dev profile with ADMIN bypass).
 */
class DevAuthorizationValidatorTest {

    private DevAuthorizationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DevAuthorizationValidator();
    }

    // Helper method to create JWT with claims
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

    // ========== canAccessCustomer() tests ==========

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
    void canAccessCustomer_shouldReturnFalse_whenUserIdClaimIsMissing() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);

        boolean result = validator.canAccessCustomer(auth, 1L);
        assertFalse(result);
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenRoleClaimIsMissing() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);

        boolean result = validator.canAccessCustomer(auth, 1L);
        assertFalse(result);
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
    void canAccessCustomer_shouldReturnTrue_whenAdminAccessesAnyData() {
        // Dev mode: ADMIN bypass is ALWAYS enabled
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");

        assertTrue(validator.canAccessCustomer(auth, 1L));
        assertTrue(validator.canAccessCustomer(auth, 2L));
        assertTrue(validator.canAccessCustomer(auth, 999L));
    }

    // ========== getUserId() tests ==========

    @Test
    void getUserId_shouldReturnNull_whenAuthenticationIsNull() {
        assertNull(validator.getUserId(null));
    }

    @Test
    void getUserId_shouldReturnUserId_whenPresent() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        assertEquals(1L, validator.getUserId(auth));
    }

    // ========== getUserCpf() tests ==========

    @Test
    void getUserCpf_shouldReturnNull_whenAuthenticationIsNull() {
        assertNull(validator.getUserCpf(null));
    }

    @Test
    void getUserCpf_shouldReturnCpf_whenPresent() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        assertEquals("12345678901", validator.getUserCpf(auth));
    }

    // ========== getUserRole() tests ==========

    @Test
    void getUserRole_shouldReturnNull_whenAuthenticationIsNull() {
        assertNull(validator.getUserRole(null));
    }

    @Test
    void getUserRole_shouldReturnCustomerRole_whenPresent() {
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");
        assertEquals(UserRole.CUSTOMER, validator.getUserRole(auth));
    }

    @Test
    void getUserRole_shouldReturnAdminRole_whenPresent() {
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");
        assertEquals(UserRole.ADMIN, validator.getUserRole(auth));
    }

    @Test
    void getUserRole_shouldReturnNull_whenRoleClaimIsMissing() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);

        assertNull(validator.getUserRole(auth));
    }
}
