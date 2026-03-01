package com.matheus.payments.wallet.UnitTests.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.Events.CreateWallet.WalletCreatedEvent;
import com.matheus.payments.wallet.Application.Events.CreateWallet.WalletCreationFailed;
import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Application.Services.WalletService;
import com.matheus.payments.wallet.Application.UseCases.CreateWallet;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated.UserCreatedEvent;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.Domain.accountType;
import org.shared.Domain.keyType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Mock
    private ApplicationEventPublisher internalEventPublisher;

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

            // Verify WalletCreatedEvent was published
            ArgumentCaptor<WalletCreatedEvent> eventCaptor = ArgumentCaptor.forClass(WalletCreatedEvent.class);
            verify(internalEventPublisher, times(1)).publishEvent(eventCaptor.capture());
            WalletCreatedEvent publishedEvent = eventCaptor.getValue();
            assertEquals(request.getAccountId(), publishedEvent.getUserId());
            assertEquals(request.getKeyValue(), publishedEvent.getCpf());
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Nested
        @DisplayName("BUSINESS VALIDATION FAILURES")
        class BusinessValidationFailure {
            @Test
            @DisplayName("Should return false when KeyValue (CPF) already exists")
            public void shouldReturnFalse_WhenKeyValueExists() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(true);

                // Act
                boolean result = createWallet.createWallet(request);

                // Assert
                assertFalse(result);
                Mockito.verify(walletService, Mockito.times(0)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.times(0)).savePixKey(any(PixKey.class));
                Mockito.verify(internalEventPublisher, Mockito.never()).publishEvent(any());
            }
        }

        @Nested
        @DisplayName("PERSISTENCE FAILURES")
        class PersistenceFailures {
            @Test
            @DisplayName("Should return false and publish WalletCreationFailed when wallet save fails")
            public void shouldReturnFalseAndPublishFailedEvent_WhenWalletSaveFails() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(false);
                when(walletService.saveWallet(any(Wallet.class))).thenThrow(new PersistenceException("Database error"));

                // Act
                boolean result = createWallet.createWallet(request);

                // Assert
                assertFalse(result);
                Mockito.verify(walletService, Mockito.times(1)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.never()).savePixKey(any(PixKey.class));

                // Verify WalletCreationFailed event was published
                ArgumentCaptor<WalletCreationFailed> eventCaptor = ArgumentCaptor.forClass(WalletCreationFailed.class);
                verify(internalEventPublisher, times(1)).publishEvent(eventCaptor.capture());
                WalletCreationFailed publishedEvent = eventCaptor.getValue();
                assertEquals(request.getAccountId(), publishedEvent.getUserId());
                assertEquals(request.getKeyValue(), publishedEvent.getCpf());
                assertEquals("DATABASE_ERROR", publishedEvent.getErrorMessage());
            }

            @Test
            @DisplayName("Should return false and publish WalletCreationFailed when PixKey save fails")
            public void shouldReturnFalseAndPublishFailedEvent_WhenPixKeySaveFails() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                Wallet wallet = new Wallet(request.getAccountId(), request.getAccountType(), request.getKeyValue());

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(false);
                when(walletService.saveWallet(any(Wallet.class))).thenReturn(wallet);
                when(pixKeyService.savePixKey(any(PixKey.class))).thenThrow(new PersistenceException("Database error"));

                // Act
                boolean result = createWallet.createWallet(request);

                // Assert
                assertFalse(result);
                Mockito.verify(walletService, Mockito.times(1)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.times(1)).savePixKey(any(PixKey.class));

                // Verify WalletCreationFailed event was published
                ArgumentCaptor<WalletCreationFailed> eventCaptor = ArgumentCaptor.forClass(WalletCreationFailed.class);
                verify(internalEventPublisher, times(1)).publishEvent(eventCaptor.capture());
                WalletCreationFailed publishedEvent = eventCaptor.getValue();
                assertEquals(request.getAccountId(), publishedEvent.getUserId());
                assertEquals(request.getKeyValue(), publishedEvent.getCpf());
                assertEquals("DATABASE_ERROR", publishedEvent.getErrorMessage());
            }

            @Test
            @DisplayName("Should return false and publish WalletCreationFailed when database connection fails")
            public void shouldReturnFalseAndPublishFailedEvent_WhenDatabaseConnectionFails() {
                // Arrange
                UserCreatedEvent request = createValidRequest();

                when(walletService.existsBySocialId(request.getKeyValue())).thenReturn(false);
                when(walletService.saveWallet(any(Wallet.class))).thenThrow(new DataAccessException("Database connection error") {
                });

                // Act
                boolean result = createWallet.createWallet(request);

                // Assert
                assertFalse(result);
                Mockito.verify(walletService, Mockito.times(1)).saveWallet(any(Wallet.class));
                Mockito.verify(pixKeyService, Mockito.never()).savePixKey(any(PixKey.class));

                // Verify WalletCreationFailed event was published
                ArgumentCaptor<WalletCreationFailed> eventCaptor = ArgumentCaptor.forClass(WalletCreationFailed.class);
                verify(internalEventPublisher, times(1)).publishEvent(eventCaptor.capture());
                WalletCreationFailed publishedEvent = eventCaptor.getValue();
                assertEquals(request.getAccountId(), publishedEvent.getUserId());
                assertEquals("DATABASE_ERROR", publishedEvent.getErrorMessage());
            }
        }
    }
}
