package com.matheus.payments.wallet.UnitTests.Application.Services;

import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Infra.Repository.PixKeyRepository;
import com.matheus.payments.wallet.UnitTests.Fixtures.PixKeyFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.Domain.keyType;
import org.springframework.dao.DataAccessException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PixKeyServiceTests {

    @Mock
    private PixKeyRepository pixKeyRepository;

    @InjectMocks
    private PixKeyService pixKeyService;

    private PixKey createValidPixKey() {
        return PixKeyFixture.createPixKey("11111111111", keyType.CPF, UUID.randomUUID());
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should get wallet ID by key successfully")
        public void shouldGetWalletIdByKeySuccessfully() {
            // Arrange
            String keyValue = "11111111111";
            PixKey pixKey = createValidPixKey();
            when(pixKeyRepository.findAccountIdByKey(keyValue)).thenReturn(Optional.of(pixKey));

            // Act
            Optional<PixKey> result = pixKeyService.getWalletIdByKey(keyValue);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(pixKey.getKeyValue(), result.get().getKeyValue());
            assertEquals(pixKey.getAccountId(), result.get().getAccountId());
            verify(pixKeyRepository, times(1)).findAccountIdByKey(keyValue);
        }

        @Test
        @DisplayName("Should return empty when PIX key does not exist")
        public void shouldReturnEmpty_WhenPixKeyDoesNotExist() {
            // Arrange
            String keyValue = "99999999999";
            when(pixKeyRepository.findAccountIdByKey(keyValue)).thenReturn(Optional.empty());

            // Act
            Optional<PixKey> result = pixKeyService.getWalletIdByKey(keyValue);

            // Assert
            assertTrue(result.isEmpty());
            verify(pixKeyRepository, times(1)).findAccountIdByKey(keyValue);
        }

        @Test
        @DisplayName("Should save PIX key successfully")
        public void shouldSavePixKeySuccessfully() {
            // Arrange
            PixKey pixKey = createValidPixKey();
            when(pixKeyRepository.saveAndFlush(any(PixKey.class))).thenReturn(pixKey);

            // Act
            PixKey result = pixKeyService.savePixKey(pixKey);

            // Assert
            assertNotNull(result);
            assertEquals(pixKey.getKeyValue(), result.getKeyValue());
            assertEquals(pixKey.getAccountId(), result.getAccountId());
            assertEquals(pixKey.getType(), result.getType());
            verify(pixKeyRepository, times(1)).saveAndFlush(pixKey);
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should throw DataAccessException when save PIX key fails")
        public void shouldThrowDataAccessException_WhenSavePixKeyFails() {
            // Arrange
            PixKey pixKey = createValidPixKey();
            when(pixKeyRepository.saveAndFlush(any(PixKey.class)))
                    .thenThrow(new DataAccessException("Database connection error") {});

            // Act & Assert
            DataAccessException exception = assertThrows(
                    DataAccessException.class,
                    () -> pixKeyService.savePixKey(pixKey)
            );

            assertNotNull(exception);
            verify(pixKeyRepository, times(1)).saveAndFlush(pixKey);
        }

        @Test
        @DisplayName("Should throw DataAccessException when get wallet ID by key fails")
        public void shouldThrowDataAccessException_WhenGetWalletIdByKeyFails() {
            // Arrange
            String keyValue = "11111111111";
            when(pixKeyRepository.findAccountIdByKey(keyValue))
                    .thenThrow(new DataAccessException("Database connection error") {});

            // Act & Assert
            DataAccessException exception = assertThrows(
                    DataAccessException.class,
                    () -> pixKeyService.getWalletIdByKey(keyValue)
            );

            assertNotNull(exception);
            verify(pixKeyRepository, times(1)).findAccountIdByKey(keyValue);
        }
    }
}

