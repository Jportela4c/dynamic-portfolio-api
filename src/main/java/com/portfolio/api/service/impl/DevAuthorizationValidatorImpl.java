package com.portfolio.api.service.impl;

import com.portfolio.api.model.enums.UserRole;
import com.portfolio.api.service.AuthorizationValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Dev-only authorization validator with ADMIN bypass for testing.
 *
 * Authorization rules:
 * - ADMIN users: Can access ANY customer's data (bypass validation)
 * - CUSTOMER users: Can only access their own data (userId must equal customerId)
 *
 * Active profiles: dev only
 * Default/prod uses AuthorizationValidatorImpl (strict validation).
 */
@Service("authorizationValidator")
@Profile("dev")
public class DevAuthorizationValidatorImpl implements AuthorizationValidator {

    @Override
    public boolean canAccessCustomer(Authentication authentication, Long customerId) {
        if (authentication == null || customerId == null) {
            return false;
        }

        Long userId = getUserId(authentication);
        UserRole role = getUserRole(authentication);

        if (userId == null || role == null) {
            return false;
        }

        // ADMIN bypass: Can access any customer
        if (role == UserRole.ADMIN) {
            return true;
        }

        // CUSTOMER: Strict validation
        return userId.equals(customerId);
    }

    @Override
    public Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // Handle JWT authentication (OAuth2 Resource Server)
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("userId");
        }

        return null;
    }

    @Override
    public String getUserCpf(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // Handle JWT authentication
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("cpf");
        }

        return null;
    }

    @Override
    public UserRole getUserRole(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // Handle JWT authentication
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String roleCode = jwt.getClaim("role");
            if (roleCode == null) {
                return null;
            }
            return UserRole.fromCode(roleCode);
        }

        return null;
    }
}
