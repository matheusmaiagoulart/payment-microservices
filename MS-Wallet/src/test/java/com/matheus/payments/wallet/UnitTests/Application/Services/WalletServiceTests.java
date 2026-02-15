package com.matheus.payments.wallet.UnitTests.Application.Services;

import com.matheus.payments.wallet.Application.Services.WalletService;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import com.matheus.payments.wallet.UnitTests.Fixtures.WalletFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.Domain.accountType;
import org.springframework.dao.DataAccessException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTests {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet createValidWallet() {
        return WalletFixture.createWallet(UUID.randomUUID(), accountType.CHECKING, "11111111111");
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should get wallet by ID successfully")
        public void shouldGetWalletByIdSuccessfully() {
            // Arrange
            UUID walletId = UUID.randomUUID();
            Wallet wallet = createValidWallet();
            when(walletRepository.findByAccountIdAndIsActiveTrue(walletId)).thenReturn(Optional.of(wallet));

            // Act
            Optional<Wallet> result = walletService.getWalletById(walletId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(wallet.getAccountId(), result.get().getAccountId());
            assertEquals(wallet.getAccountType(), result.get().getAccountType());
            assertEquals(wallet.getSocialId(), result.get().getSocialId());
            verify(walletRepository, times(1)).findByAccountIdAndIsActiveTrue(walletId);
        }

        @Test
        @DisplayName("Should return empty when wallet does not exist")
        public void shouldReturnEmpty_WhenWalletDoesNotExist() {
            // Arrange
            UUID walletId = UUID.randomUUID();
            when(walletRepository.findByAccountIdAndIsActiveTrue(walletId)).thenReturn(Optional.empty());

            // Act
            Optional<Wallet> result = walletService.getWalletById(walletId);

            // Assert
            assertTrue(result.isEmpty());
            verify(walletRepository, times(1)).findByAccountIdAndIsActiveTrue(walletId);
        }

        @Test
        @DisplayName("Should save wallet successfully")
        public void shouldSaveWalletSuccessfully() {
            // Arrange
            Wallet wallet = createValidWallet();
            when(walletRepository.saveAndFlush(any(Wallet.class))).thenReturn(wallet);

            // Act
            Wallet result = walletService.saveWallet(wallet);

            // Assert
            assertNotNull(result);
            assertEquals(wallet.getAccountId(), result.getAccountId());
            assertEquals(wallet.getAccountType(), result.getAccountType());
            assertEquals(wallet.getSocialId(), result.getSocialId());
            verify(walletRepository, times(1)).saveAndFlush(wallet);
        }

        @Test
        @DisplayName("Should return true when social ID exists")
        public void shouldReturnTrue_WhenSocialIdExists() {
            // Arrange
            String socialId = "11111111111";
            when(walletRepository.existsBySocialId(socialId)).thenReturn(true);

            // Act
            boolean result = walletService.existsBySocialId(socialId);

            // Assert
            assertTrue(result);
            verify(walletRepository, times(1)).existsBySocialId(socialId);
        }

        @Test
        @DisplayName("Should return false when social ID does not exist")
        public void shouldReturnFalse_WhenSocialIdDoesNotExist() {
            // Arrange
            String socialId = "99999999999";
            when(walletRepository.existsBySocialId(socialId)).thenReturn(false);

            // Act
            boolean result = walletService.existsBySocialId(socialId);

            // Assert
            assertFalse(result);
            verify(walletRepository, times(1)).existsBySocialId(socialId);
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should throw DataAccessException when save wallet fails")
        public void shouldThrowDataAccessException_WhenSaveWalletFails() {
            // Arrange
            Wallet wallet = createValidWallet();
            when(walletRepository.saveAndFlush(any(Wallet.class)))
                    .thenThrow(new DataAccessException("Database connection error") {});

            // Act & Assert
            DataAccessException exception = assertThrows(
                    DataAccessException.class,
                    () -> walletService.saveWallet(wallet)
            );

            assertNotNull(exception);
            verify(walletRepository, times(1)).saveAndFlush(wallet);
        }

        @Test
        @DisplayName("Should throw DataAccessException when get wallet by ID fails")
        public void shouldThrowDataAccessException_WhenGetWalletByIdFails() {
            // Arrange
            UUID walletId = UUID.randomUUID();
            when(walletRepository.findByAccountIdAndIsActiveTrue(walletId))
                    .thenThrow(new DataAccessException("Database connection error") {});

            // Act & Assert
            DataAccessException exception = assertThrows(
                    DataAccessException.class,
                    () -> walletService.getWalletById(walletId)
            );

            assertNotNull(exception);
            verify(walletRepository, times(1)).findByAccountIdAndIsActiveTrue(walletId);
        }

        @Test
        @DisplayName("Should throw DataAccessException when existsBySocialId fails")
        public void shouldThrowDataAccessException_WhenExistsBySocialIdFails() {
            // Arrange
            String socialId = "11111111111";
            when(walletRepository.existsBySocialId(socialId))
                    .thenThrow(new DataAccessException("Database connection error") {});

            // Act & Assert
            DataAccessException exception = assertThrows(
                    DataAccessException.class,
                    () -> walletService.existsBySocialId(socialId)
            );

            assertNotNull(exception);
            verify(walletRepository, times(1)).existsBySocialId(socialId);
        }
    }
}

