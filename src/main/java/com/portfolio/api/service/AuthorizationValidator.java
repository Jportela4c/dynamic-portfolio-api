package com.portfolio.api.service;

import com.portfolio.api.model.enums.UserRole;
import org.springframework.security.core.Authentication;

/**
 * Service to validate authorization based on user roles and customer IDs.
 *
 * Two implementations:
 * - DevAuthorizationValidator: ADMIN bypass enabled (dev profile)
 * - ProdAuthorizationValidator: Strict validation only (prod profile)
 */
public interface AuthorizationValidator {

    /**
     * Validates if the authenticated user can access the specified customer's data.
     *
     * @param authentication Spring Security authentication (contains JWT with claims)
     * @param customerId     The customer ID being accessed
     * @return true if access is allowed, false otherwise
     */
    boolean canAccessCustomer(Authentication authentication, Long customerId);

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param authentication Spring Security authentication
     * @return userId claim from JWT, or null if not present
     */
    Long getUserId(Authentication authentication);

    /**
     * Extracts the user's CPF from the JWT token.
     *
     * @param authentication Spring Security authentication
     * @return cpf claim from JWT, or null if not present
     */
    String getUserCpf(Authentication authentication);

    /**
     * Extracts the user's role from the JWT token.
     *
     * @param authentication Spring Security authentication
     * @return UserRole from JWT, or null if not present
     */
    UserRole getUserRole(Authentication authentication);
}
