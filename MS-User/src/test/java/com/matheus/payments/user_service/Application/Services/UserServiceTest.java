package com.matheus.payments.user_service.Application.Services;

import com.matheus.payments.user_service.Application.Audit.CreateUserAudit;
import com.matheus.payments.user_service.Application.DTOs.RequestCreateUser;
import com.matheus.payments.user_service.Application.DTOs.ResponseCreateUser;
import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;
import com.matheus.payments.user_service.Domain.Exceptios.CpfAlreadyExistsException;
import com.matheus.payments.user_service.Domain.Exceptios.EmailAlreadyExists;
import com.matheus.payments.user_service.Domain.Exceptios.PhoneNumberAlreadyExistsException;
import com.matheus.payments.user_service.Domain.Models.User;
import com.matheus.payments.user_service.Domain.Repositories.UserRepository;
import com.matheus.payments.user_service.Fixtures.RequestCreateUserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreateUserAudit audit;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Arrange
        RequestCreateUser request = RequestCreateUserFixture.createValidRequest();
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseCreateUser response = userService.createUser(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertTrue(response.getMessage().contains("Congrats Matheus!"));
        verify(userRepository, times(1)).saveAndFlush(any(User.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserCreatedEvent.class));
        verify(audit, times(1)).logUserCreationStarting(request.getCpf());
        verify(audit, times(1)).logUserCreationSuccess(request.getCpf());
    }

    @Test
    @DisplayName("Should throw CpfAlreadyExistsException when CPF is duplicated")
    void shouldThrowCpfAlreadyExistsExceptionWhenCpfIsDuplicated() {
        // Arrange
        RequestCreateUser request = RequestCreateUserFixture.createValidRequest();
        DataIntegrityViolationException dbException = createDataIntegrityException("uk_users_cpf");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(dbException);

        // Act & Assert
        assertThrows(CpfAlreadyExistsException.class, () -> {
            userService.createUser(request);
        });
        verify(audit, times(1)).logUserCreationFailed(eq(request.getCpf()), anyString());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExists when email is duplicated")
    void shouldThrowEmailAlreadyExistsWhenEmailIsDuplicated() {
        // Arrange
        RequestCreateUser request = RequestCreateUserFixture.createValidRequest();
        DataIntegrityViolationException dbException = createDataIntegrityException("uk_users_email");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(dbException);

        // Act & Assert
        assertThrows(EmailAlreadyExists.class, () -> {
            userService.createUser(request);
        });
        verify(audit, times(1)).logUserCreationFailed(eq(request.getCpf()), anyString());
    }

    @Test
    @DisplayName("Should throw PhoneNumberAlreadyExistsException when phone is duplicated")
    void shouldThrowPhoneNumberAlreadyExistsExceptionWhenPhoneIsDuplicated() {
        // Arrange
        RequestCreateUser request = RequestCreateUserFixture.createValidRequest();
        DataIntegrityViolationException dbException = createDataIntegrityException("uk_users_phone_number");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(dbException);

        // Act & Assert
        assertThrows(PhoneNumberAlreadyExistsException.class, () -> {
            userService.createUser(request);
        });
        verify(audit, times(1)).logUserCreationFailed(eq(request.getCpf()), anyString());
    }

    @Test
    @DisplayName("Should publish UserCreatedEvent after user creation")
    void shouldPublishUserCreatedEventAfterUserCreation() {
        // Arrange
        RequestCreateUser request = RequestCreateUserFixture.createValidRequest();
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.createUser(request);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(UserCreatedEvent.class));
    }

    private DataIntegrityViolationException createDataIntegrityException(String constraintName) {
        Exception cause = new Exception("Violation of UNIQUE KEY constraint '" + constraintName + "'");
        return new DataIntegrityViolationException("Database error", cause);
    }
}



