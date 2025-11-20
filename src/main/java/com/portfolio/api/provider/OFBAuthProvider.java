package com.portfolio.api.provider;

/**
 * Provider for OFB authentication token management.
 *
 * In dev mode, uses pre-generated demo tokens.
 * In prod mode, performs real OAuth2 flows.
 */
public interface OFBAuthProvider {

    /**
     * Get authentication token for the specified customer.
     *
     * @param customerId Customer database ID
     * @return Bearer token for OFB API calls
     * @throws Exception if authentication fails
     */
    String authenticateCustomer(Long customerId) throws Exception;
}
