package com.matheus.payments.user_service.Application.DTOs;

import jakarta.validation.constraints.*;
import lombok.Getter;
import org.hibernate.validator.constraints.br.CPF;
import org.shared.Domain.accountType;

import java.time.LocalDate;

@Getter
public class RequestCreateUser {
    @NotBlank
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @NotBlank
    @Email
    @Size(min = 5, max = 100, message = "Email must be between 5 and 100 characters")
    private String email;

    @NotBlank
    //@CPF
    @Size(min=11, max=11, message = "CPF must be 11 digits")
    private String cpf;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "Phone number must be 11 digits")
    private String phoneNumber;

    @NotNull(message = "Account type is required: CHECKING or MERCHANT")
    private accountType accountType;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
}
