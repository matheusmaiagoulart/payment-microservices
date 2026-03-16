package com.matheus.payments.user_service.Application.UseCases;

import com.matheus.payments.user_service.Domain.Models.User;
import com.matheus.payments.user_service.Domain.Repositories.UserRepository;
import com.matheus.payments.user_service.Fixtures.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivateUserAccount UseCase Tests")
class ActivateUserAccountTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivateUserAccount activateUserAccount;

    @Test
    @DisplayName("Should activate user account successfully")
    void shouldActivateUserAccountSuccessfully() {
        // Arrange
        User user = UserFixture.createInactiveUser();
        UUID userId = user.getId();
        assertFalse(user.isActive());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        activateUserAccount.executeActivationUserAccount(userId);

        // Assert
        assertTrue(user.isActive());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not found")
    void shouldThrowRuntimeExceptionWhenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            activateUserAccount.executeActivationUserAccount(userId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user updatedAt when activating")
    void shouldUpdateUserUpdatedAtWhenActivating() {
        // Arrange
        User user = UserFixture.createInactiveUser();
        UUID userId = user.getId();
        var originalUpdatedAt = user.getUpdatedAt();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        activateUserAccount.executeActivationUserAccount(userId);

        // Assert
        assertNotNull(user.getUpdatedAt());
        verify(userRepository, times(1)).save(user);
    }
}
