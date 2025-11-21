package com.portfolio.api.service;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.repository.CustomerRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * UserDetailsService implementation for database-backed customer authentication.
 *
 * Loads customer data by email for OAuth2 username/password authentication.
 * Maps Customer entity to Spring Security UserDetails with role-based authorities.
 */
@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomerUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Load customer by email (username in authentication flow).
     *
     * @param email The email address used as username
     * @return UserDetails with customer credentials and authorities
     * @throws UsernameNotFoundException if customer not found or inactive
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Cliente não encontrado com email: " + email));

        // Check if customer account is active
        if (!customer.getAtivo()) {
            throw new UsernameNotFoundException(
                    "Conta do cliente está inativa: " + email);
        }

        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPassword())
                .authorities(getAuthorities(customer))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!customer.getAtivo())
                .build();
    }

    /**
     * Convert customer role to Spring Security authority.
     *
     * @param customer The customer entity
     * @return Collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Customer customer) {
        String roleName = "ROLE_" + customer.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}
