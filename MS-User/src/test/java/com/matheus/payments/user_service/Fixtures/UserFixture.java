package com.matheus.payments.user_service.Fixtures;

import com.matheus.payments.user_service.Domain.Models.User;
import org.shared.Domain.accountType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Fixture class for creating User objects for tests.
 */
public class UserFixture {

    public static User createValidUser() {
        return new User(
                "Matheus",
                "Goulart",
                "matheus@email.com",
                "12345678901",
                "11999999999",
                accountType.CHECKING,
                LocalDate.of(1990, 1, 15)
        );
    }

    public static User createValidMerchantUser() {
        return new User(
                "João",
                "Silva",
                "joao@empresa.com",
                "98765432101",
                "11888888888",
                accountType.MERCHANT,
                LocalDate.of(1985, 5, 20)
        );
    }

    public static User createUserWithCustomData(String firstName, String lastName, String email, String cpf, String phone) {
        return new User(
                firstName,
                lastName,
                email,
                cpf,
                phone,
                accountType.CHECKING,
                LocalDate.of(1990, 1, 15)
        );
    }

    public static User createInactiveUser() {
        User user = createValidUser();
        // User is inactive by default
        return user;
    }

    public static User createActiveUser() {
        User user = createValidUser();
        user.activateUser();
        return user;
    }
}
