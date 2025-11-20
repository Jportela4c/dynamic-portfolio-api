package com.portfolio.api.service;

import com.portfolio.api.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service to validate authorization based on user roles and customer IDs.
 *
 * Implements RBAC with environment-specific behavior:
 *
 * DEV Profile (security.admin.enabled=true):
 * - ADMIN users can access any customer's data
 * - CUSTOMER users can only access their own data
 *
 * PROD Profile (security.admin.enabled=false):
 * - ADMIN role is disabled (returns false)
 * - ALL users must match userId == customerId (strict mode)
 */
@Service
public class AuthorizationValidator {

    @Value("${security.admin.enabled:false}")
    private boolean adminEnabled;

    /**
     * Validates if the authenticated user can access the specified customer's data.
     *
     * @param authentication Spring Security authentication (contains JWT with claims)
     * @param customerId     The customer ID being accessed
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessCustomer(Authentication authentication, Long customerId) {
        if (authentication == null || customerId == null) {
            return false;
        }

        // Extract JWT claims
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        String roleCode = jwt.getClaim("role");

        if (userId == null || roleCode == null) {
            return false;
        }

        UserRole role = UserRole.fromCode(roleCode);

        // ADMIN bypass (only in dev mode)
        if (adminEnabled && role == UserRole.ADMIN) {
            return true;
        }

        // CUSTOMER or production: strict validation
        return userId.equals(customerId);
    }

    /**
     * Checks if the current user has ADMIN role (only valid in dev mode).
     *
     * @param authentication Spring Security authentication
     * @return true if user is ADMIN and admin is enabled
     */
    public boolean isAdmin(Authentication authentication) {
        if (!adminEnabled || authentication == null) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String roleCode = jwt.getClaim("role");

        if (roleCode == null) {
            return false;
        }

        UserRole role = UserRole.fromCode(roleCode);
        return role == UserRole.ADMIN;
    }

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param authentication Spring Security authentication
     * @return userId claim from JWT, or null if not present
     */
    public Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }

    /**
     * Extracts the user's CPF from the JWT token.
     *
     * @param authentication Spring Security authentication
     * @return cpf claim from JWT, or null if not present
     */
    public String getUserCpf(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("cpf");
    }

    /**
     * Extracts the user's role from the JWT token.
     *
     * @param authentication Spring Security authentication
     * @return UserRole from JWT, or null if not present
     */
    public UserRole getUserRole(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String roleCode = jwt.getClaim("role");

        if (roleCode == null) {
            return null;
        }

        return UserRole.fromCode(roleCode);
    }
}
