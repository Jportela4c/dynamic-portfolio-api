package com.ofb.mock.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for JWT token operations.
 */
@Slf4j
public class JwtUtils {

    /**
     * Extracts customer ID from Authorization header.
     *
     * @param authorizationHeader Authorization header value (e.g., "Bearer eyJ...")
     * @return Customer ID from JWT 'sub' claim, or null if invalid
     */
    public static String extractCustomerId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format");
            return null;
        }

        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer "
            JWT jwt = JWTParser.parse(token);
            String customerId = jwt.getJWTClaimsSet().getSubject();
            log.debug("Extracted customer ID from token: {}", customerId);
            return customerId;
        } catch (Exception e) {
            log.error("Failed to extract customer ID from token", e);
            return null;
        }
    }

    /**
     * Extracts CPF from Authorization header with fallback to x-customer-cpf header.
     * In OFB tokens, the CPF is in the 'sub' claim.
     *
     * TEMP: Fallback parameter added for testing with auth disabled
     *
     * @param authorizationHeader Authorization header value (e.g., "Bearer eyJ...")
     * @param fallbackCpf TEMP: Fallback CPF from x-customer-cpf header (for testing with auth disabled)
     * @return CPF from JWT 'sub' claim, or fallback CPF if JWT is invalid
     */
    public static String extractCpf(String authorizationHeader, String fallbackCpf) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header, using fallback CPF: {}", fallbackCpf);
            return fallbackCpf;
        }

        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer "
            JWT jwt = JWTParser.parse(token);
            String cpf = jwt.getJWTClaimsSet().getSubject(); // In OFB, sub = CPF
            log.debug("Extracted CPF from token: {}", cpf);
            return cpf;
        } catch (Exception e) {
            log.debug("Failed to extract CPF from token, using fallback: {}", fallbackCpf);
            return fallbackCpf;
        }
    }
}
