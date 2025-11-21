package com.portfolio.api.service.impl;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.model.enums.UserRole;
import com.portfolio.api.repository.CustomerRepository;
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

    private final CustomerRepository customerRepository;

    public DevAuthorizationValidatorImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

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

        // Handle form login authentication (UsernamePasswordAuthenticationToken)
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            // Lookup customer by email from database
            String email = userDetails.getUsername();
            Customer customer = customerRepository.findByEmail(email).orElse(null);
            return customer != null ? customer.getId() : null;
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

        // Handle form login authentication
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            String email = userDetails.getUsername();
            Customer customer = customerRepository.findByEmail(email).orElse(null);
            return customer != null ? customer.getCpf() : null;
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

        // Handle form login authentication
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            String email = userDetails.getUsername();
            Customer customer = customerRepository.findByEmail(email).orElse(null);
            return customer != null ? customer.getRole() : null;
        }

        return null;
    }
}
