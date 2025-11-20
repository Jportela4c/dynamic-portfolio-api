package com.portfolio.api.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {

    @Test
    void shouldReturnCorrectCodeForCustomer() {
        assertEquals("CUSTOMER", UserRole.CUSTOMER.getCode());
    }

    @Test
    void shouldReturnCorrectCodeForAdmin() {
        assertEquals("ADMIN", UserRole.ADMIN.getCode());
    }

    @Test
    void shouldReturnCorrectDescriptionForCustomer() {
        assertEquals("Cliente com acesso aos próprios dados", UserRole.CUSTOMER.getDescription());
    }

    @Test
    void shouldReturnCorrectDescriptionForAdmin() {
        assertEquals("Administrador com acesso total (somente demo)", UserRole.ADMIN.getDescription());
    }

    @Test
    void shouldConvertFromCodeCustomer() {
        UserRole role = UserRole.fromCode("CUSTOMER");
        assertEquals(UserRole.CUSTOMER, role);
    }

    @Test
    void shouldConvertFromCodeAdmin() {
        UserRole role = UserRole.fromCode("ADMIN");
        assertEquals(UserRole.ADMIN, role);
    }

    @Test
    void shouldThrowExceptionForInvalidCode() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UserRole.fromCode("INVALID")
        );
        assertEquals("Invalid role code: INVALID", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForNullCode() {
        assertThrows(
            IllegalArgumentException.class,
            () -> UserRole.fromCode(null)
        );
    }

    @Test
    void shouldHaveExactlyTwoRoles() {
        UserRole[] roles = UserRole.values();
        assertEquals(2, roles.length);
    }
}
