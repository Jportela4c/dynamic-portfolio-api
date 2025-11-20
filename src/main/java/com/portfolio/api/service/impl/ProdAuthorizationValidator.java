package com.portfolio.api.service.impl;

import com.portfolio.api.model.enums.UserRole;
import com.portfolio.api.service.AuthorizationValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Production authorization validator with strict validation only.
 *
 * Authorization rules:
 * - ALL users (including those with ADMIN role): Can ONLY access their own data
 * - userId from JWT MUST equal customerId from request
 * - ADMIN role has NO special privileges in production (OFB compliant)
 *
 * This implementation is active in prod profile (and default if no profile set).
 * Development uses DevAuthorizationValidator which enables ADMIN bypass.
 */
@Service
@Profile({"prod", "default"})
public class ProdAuthorizationValidator implements AuthorizationValidator {

    @Override
    public boolean canAccessCustomer(Authentication authentication, Long customerId) {
        if (authentication == null || customerId == null) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        if (userId == null) {
            return false;
        }

        // ALWAYS strict validation - no ADMIN bypass
        // Role claim is ignored in production
        return userId.equals(customerId);
    }

    @Override
    public Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }

    @Override
    public String getUserCpf(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("cpf");
    }

    @Override
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
