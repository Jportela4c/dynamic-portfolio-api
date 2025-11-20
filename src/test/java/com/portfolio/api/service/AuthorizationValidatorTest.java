package com.portfolio.api.service;

import com.portfolio.api.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationValidatorTest {

    private AuthorizationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AuthorizationValidator();
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
        ReflectionTestUtils.setField(validator, "adminEnabled", false);
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");

        boolean result = validator.canAccessCustomer(auth, 1L);
        assertTrue(result);
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenCustomerAccessesOtherData() {
        ReflectionTestUtils.setField(validator, "adminEnabled", false);
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");

        boolean result = validator.canAccessCustomer(auth, 2L);
        assertFalse(result);
    }

    @Test
    void canAccessCustomer_shouldReturnTrue_whenAdminAccessesAnyData_inDevMode() {
        ReflectionTestUtils.setField(validator, "adminEnabled", true);
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");

        assertTrue(validator.canAccessCustomer(auth, 1L));
        assertTrue(validator.canAccessCustomer(auth, 2L));
        assertTrue(validator.canAccessCustomer(auth, 999L));
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenAdminTriesToAccess_inProdMode() {
        ReflectionTestUtils.setField(validator, "adminEnabled", false);
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");

        // ADMIN cannot access other customers in prod mode
        assertFalse(validator.canAccessCustomer(auth, 1L));
        assertFalse(validator.canAccessCustomer(auth, 2L));

        // ADMIN can only access own ID (999)
        assertTrue(validator.canAccessCustomer(auth, 999L));
    }

    // ========== isAdmin() tests ==========

    @Test
    void isAdmin_shouldReturnFalse_whenAuthenticationIsNull() {
        ReflectionTestUtils.setField(validator, "adminEnabled", true);
        assertFalse(validator.isAdmin(null));
    }

    @Test
    void isAdmin_shouldReturnFalse_whenAdminIsDisabled() {
        ReflectionTestUtils.setField(validator, "adminEnabled", false);
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");

        assertFalse(validator.isAdmin(auth));
    }

    @Test
    void isAdmin_shouldReturnTrue_whenAdminEnabledAndRoleIsAdmin() {
        ReflectionTestUtils.setField(validator, "adminEnabled", true);
        Authentication auth = createAuthenticationWithClaims(999L, "ADMIN", "00000000000");

        assertTrue(validator.isAdmin(auth));
    }

    @Test
    void isAdmin_shouldReturnFalse_whenAdminEnabledButRoleIsCustomer() {
        ReflectionTestUtils.setField(validator, "adminEnabled", true);
        Authentication auth = createAuthenticationWithClaims(1L, "CUSTOMER", "12345678901");

        assertFalse(validator.isAdmin(auth));
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
