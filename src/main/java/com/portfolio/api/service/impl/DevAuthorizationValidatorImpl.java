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
@Service
@Profile("dev")
public class DevAuthorizationValidatorImpl implements AuthorizationValidator {

    @Override
    public boolean canAccessCustomer(Authentication authentication, Long customerId) {
        if (authentication == null || customerId == null) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        String roleCode = jwt.getClaim("role");

        if (userId == null || roleCode == null) {
            return false;
        }

        UserRole role = UserRole.fromCode(roleCode);

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
