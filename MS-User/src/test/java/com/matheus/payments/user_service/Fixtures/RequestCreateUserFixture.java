package com.matheus.payments.user_service.Fixtures;

import com.matheus.payments.user_service.Application.DTOs.RequestCreateUser;
import org.shared.Domain.accountType;

import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * Fixture class for creating RequestCreateUser objects for tests.
 */
public class RequestCreateUserFixture {

    public static RequestCreateUser createValidRequest() {
        return buildRequest(
                "Matheus",
                "Goulart",
                "matheus@email.com",
                "12345678901",
                "11999999999",
                accountType.CHECKING,
                LocalDate.of(1990, 1, 15)
        );
    }

    public static RequestCreateUser createValidMerchantRequest() {
        return buildRequest(
                "João",
                "Silva",
                "joao@empresa.com",
                "98765432101",
                "11888888888",
                accountType.MERCHANT,
                LocalDate.of(1985, 5, 20)
        );
    }

    public static RequestCreateUser createRequestWithInvalidCpf() {
        return buildRequest(
                "Matheus",
                "Goulart",
                "matheus@email.com",
                "123", // Invalid CPF
                "11999999999",
                accountType.CHECKING,
                LocalDate.of(1990, 1, 15)
        );
    }

    public static RequestCreateUser createRequestWithInvalidPhone() {
        return buildRequest(
                "Matheus",
                "Goulart",
                "matheus@email.com",
                "12345678901",
                "119", // Invalid phone
                accountType.CHECKING,
                LocalDate.of(1990, 1, 15)
        );
    }

    private static RequestCreateUser buildRequest(
            String firstName,
            String lastName,
            String email,
            String cpf,
            String phoneNumber,
            accountType accountType,
            LocalDate birthDate
    ) {
        try {
            RequestCreateUser request = createInstance();
            setField(request, "firstName", firstName);
            setField(request, "lastName", lastName);
            setField(request, "email", email);
            setField(request, "cpf", cpf);
            setField(request, "phoneNumber", phoneNumber);
            setField(request, "accountType", accountType);
            setField(request, "birthDate", birthDate);
            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error creating RequestCreateUser fixture", e);
        }
    }

    private static RequestCreateUser createInstance() throws Exception {
        var constructor = RequestCreateUser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
