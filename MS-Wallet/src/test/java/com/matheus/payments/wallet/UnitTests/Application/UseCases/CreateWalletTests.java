package com.matheus.payments.wallet.UnitTests.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Application.Services.WalletService;
import com.matheus.payments.wallet.Application.UseCases.CreateWallet;
import com.matheus.payments.wallet.Domain.Exceptions.SocialIdAlreadyExistsException;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated.UserCreatedEvent;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.Domain.accountType;
import org.shared.Domain.keyType;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateWalletTests {

    @Mock
    private WalletService walletService;
    @Mock
    private PixKeyService pixKeyService;
    @Mock
    private WalletServiceAudit audit;

    @InjectMocks
    private CreateWallet createWallet;

    private UserCreatedEvent createValidRequest() {
        UserCreatedEvent request = new UserCreatedEvent();
        request.setKeyType(keyType.CPF);
        request.setKeyValue("00000000000");
        request.setAccountId(UUID.randomUUID());
        request.setTimestamp(LocalDateTime.now());
        request.setAccountType(accountType.CHECKING);
        return request;
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should create Wallet successfully when KeyValue does not exists")
        public void shouldCreateWalletSuccessfully_WhenKeyValueDoesNotExists() {

            // Arrange
            UserCreatedEvent request = createValidRequest();

            Wallet wallet = new Wallet(request.getAccountId(), request.getAccountType(), request.getKeyValue());
            PixKey pixKey = new PixKey(request.getKeyValue(), request.getKeyType(), request.getAccountId());

            when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(false);
            when(walletService.saveWallet(any(Wallet.class))).thenReturn(wallet);
            when(pixKeyService.savePixKey(any(PixKey.class))).thenReturn(pixKey);

            // Act
            boolean result = createWallet.createWallet(request);

            // Assert
            assertTrue(result);
            assertEquals(request.getAccountId(), wallet.getAccountId());
            assertEquals(request.getAccountType(), wallet.getAccountType());
            assertEquals(request.getKeyValue(), wallet.getSocialId());
            assertEquals(request.getKeyType(), pixKey.getType());
            Mockito.verify(walletService, Mockito.times(1)).saveWallet(any(Wallet.class));
            Mockito.verify(pixKeyService, Mockito.times(1)).savePixKey(any(PixKey.class));
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Nested
        @DisplayName("BUSINESS VALIDATION FAILURES")
        class BusinessValidationFailure {
            @Test
            @DisplayName("Should not create Wallet when KeyValue (CPF) exists")
            public void shouldNotCreateWallet_WhenKeyValueExists() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(true);

                // Act & Assert
                SocialIdAlreadyExistsException exception = assertThrows(SocialIdAlreadyExistsException.class, () -> {
                    createWallet.createWallet(request);
                });

                assertNotNull(exception);
                assertThat(exception.getMessage()).contains(request.getKeyValue());
                assertEquals(SocialIdAlreadyExistsException.class, exception.getClass());
                Mockito.verify(walletService, Mockito.times(0)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.times(0)).savePixKey(any(PixKey.class));
            }
        }

        @Nested
        @DisplayName("PERSISTENCE FAILURES")
        class PersistenceFailures {
            @Test
            @DisplayName("Should throw PersistenceException when wallet save fails")
            public void shouldThrowPersistenceException_WhenWalletSaveFails() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(false);

                when(walletService.saveWallet(any(Wallet.class))).thenThrow(new PersistenceException("An error occurred while creating the wallet: "));

                // Act & Assert
                PersistenceException exception = assertThrows(PersistenceException.class, () -> {
                    createWallet.createWallet(request);
                });

                assertNotNull(exception);
                assertEquals(PersistenceException.class, exception.getClass());
                Mockito.verify(walletService, Mockito.times(1)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.never()).savePixKey(any(PixKey.class));
            }

            @Test
            @DisplayName("Should throw PersistenceException when PixKey save fails")
            public void shouldThrowPersistenceException_WhenPixKeySaveFails() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                Wallet wallet = new Wallet(request.getAccountId(), request.getAccountType(), request.getKeyValue());

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(false);

                when(walletService.saveWallet(any(Wallet.class))).thenReturn(wallet);
                when(pixKeyService.savePixKey(any(PixKey.class))).thenThrow(new PersistenceException("An error occurred while creating the wallet: "));

                // Act & Assert
                PersistenceException exception = assertThrows(PersistenceException.class, () -> {
                    createWallet.createWallet(request);
                });

                assertNotNull(exception);
                assertEquals(PersistenceException.class, exception.getClass());
                Mockito.verify(walletService, Mockito.times(1)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.times(1)).savePixKey(any(PixKey.class));
            }

            @Test
            @DisplayName("Should throw DataAccessException when database connection fails")
            public void shouldThrowDataAccessException_WhenDatabaseConnectionFails() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(false);
                when(walletService.saveWallet(any(Wallet.class))).thenThrow(new DataAccessException("Database connection error") {
                });

                // Act & Assert
                DataAccessException exception = assertThrows(DataAccessException.class, () -> {
                    createWallet.createWallet(request);
                });

                assertNotNull(exception);
                Mockito.verify(walletService, Mockito.times(1)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.never()).savePixKey(any(PixKey.class));
            }
        }
    }
}
