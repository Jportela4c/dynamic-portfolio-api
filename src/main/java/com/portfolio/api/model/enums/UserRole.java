package com.portfolio.api.model.enums;

/**
 * User roles for authorization.
 *
 * CUSTOMER: Regular customer with access to own data only
 * ADMIN: Administrator with access to all customers (demo mode only)
 */
public enum UserRole {

    /**
     * Regular customer - can only access own data.
     * Valid in both demo and production modes.
     */
    CUSTOMER("CUSTOMER", "Cliente com acesso aos próprios dados"),

    /**
     * Administrator - can access all customer data.
     * ONLY available in demo mode for testing purposes.
     * NOT available in production (OFB does not support privileged access).
     */
    ADMIN("ADMIN", "Administrador com acesso total (somente demo)");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Código de perfil inválido: " + code);
    }
}
