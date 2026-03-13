package com.matheus.payments.UnitTests.Services;

import com.matheus.payments.Application.Audit.DepositServiceAudit;
import com.matheus.payments.Application.DTOs.DepositRequest;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Domain.Events.DepositCreatedEvent;
import com.matheus.payments.Domain.Exceptions.DepositNotFound;
import com.matheus.payments.Domain.Models.Deposit;
import com.matheus.payments.Domain.Repositories.DepositRepository;
import com.matheus.payments.Infra.Exceptions.Custom.DataBaseException;
import com.matheus.payments.UnitTests.Fixtures.DepositFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepositService Tests")
class DepositServiceTests {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DepositServiceAudit audit;

    @InjectMocks
    private DepositService depositService;

    private DepositRequest depositRequest;
    private Deposit deposit;

    @BeforeEach
    void setUp() {
        depositRequest = DepositFixture.createDepositRequest();
        deposit = DepositFixture.createDeposit();
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should create deposit successfully")
        public void shouldCreateDepositSuccessfully() {
            // Arrange
            doNothing().when(depositRepository).saveDeposit(any(Deposit.class));

            // Act
            Deposit result = depositService.createDeposit(depositRequest);

            // Assert
            assertNotNull(result);
            assertEquals(depositRequest.getReceiverId(), result.getReceiverId());
            assertEquals(Deposit.DepositStatus.PENDING, result.getStatus());
            verify(audit, times(1)).logCreateDepositEntry();
            verify(depositRepository, times(1)).saveDeposit(any(Deposit.class));
        }

        @Test
        @DisplayName("Should publish DepositCreatedEvent after creating deposit")
        public void shouldPublishDepositCreatedEvent_AfterCreatingDeposit() {
            // Arrange
            doNothing().when(depositRepository).saveDeposit(any(Deposit.class));
            ArgumentCaptor<DepositCreatedEvent> eventCaptor = ArgumentCaptor.forClass(DepositCreatedEvent.class);

            // Act
            depositService.createDeposit(depositRequest);

            // Assert
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            DepositCreatedEvent capturedEvent = eventCaptor.getValue();
            assertNotNull(capturedEvent);
            assertEquals(depositRequest.getReceiverId(), capturedEvent.getReceiverId());
        }

        @Test
        @DisplayName("Should update deposit status to SENT")
        public void shouldUpdateDepositStatus_ToSent() {
            // Arrange
            String depositId = deposit.getDepositId().toString();
            when(depositRepository.getDepositById(any(UUID.class))).thenReturn(Optional.of(deposit));
            doNothing().when(depositRepository).saveDeposit(any(Deposit.class));

            // Act
            depositService.updateDepositStatus(depositId, Deposit.DepositStatus.SENT);

            // Assert
            assertEquals(Deposit.DepositStatus.SENT, deposit.getStatus());
            verify(depositRepository, times(1)).getDepositById(UUID.fromString(depositId));
            verify(depositRepository, times(1)).saveDeposit(deposit);
        }

        @Test
        @DisplayName("Should update deposit status to CONFIRMED")
        public void shouldUpdateDepositStatus_ToConfirmed() {
            // Arrange
            String depositId = deposit.getDepositId().toString();
            when(depositRepository.getDepositById(any(UUID.class))).thenReturn(Optional.of(deposit));
            doNothing().when(depositRepository).saveDeposit(any(Deposit.class));

            // Act
            depositService.updateDepositStatus(depositId, Deposit.DepositStatus.CONFIRMED);

            // Assert
            assertEquals(Deposit.DepositStatus.CONFIRMED, deposit.getStatus());
            assertNotNull(deposit.getConfirmedAt());
            verify(depositRepository, times(1)).saveDeposit(deposit);
        }

        @Test
        @DisplayName("Should update deposit status to FAILED")
        public void shouldUpdateDepositStatus_ToFailed() {
            // Arrange
            String depositId = deposit.getDepositId().toString();
            when(depositRepository.getDepositById(any(UUID.class))).thenReturn(Optional.of(deposit));
            doNothing().when(depositRepository).saveDeposit(any(Deposit.class));

            // Act
            depositService.updateDepositStatus(depositId, Deposit.DepositStatus.FAILED);

            // Assert
            assertEquals(Deposit.DepositStatus.FAILED, deposit.getStatus());
            assertNotNull(deposit.getConfirmedAt());
            verify(depositRepository, times(1)).saveDeposit(deposit);
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when repository fails on create")
        public void shouldFail_WhenRepositoryFailsOnCreate() {
            // Arrange
            doThrow(new DataBaseException("Database connection error"))
                    .when(depositRepository).saveDeposit(any(Deposit.class));

            // Act & Assert
            DataBaseException exception = assertThrows(
                    DataBaseException.class,
                    () -> depositService.createDeposit(depositRequest)
            );

            assertEquals("Database connection error", exception.getMessage());
            verify(audit, times(1)).logCreateDepositEntry();
            verify(audit, times(1)).logErrorCreateDeposit(anyString());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should fail when deposit does not exist for SENT update")
        public void shouldFail_WhenDepositDoesNotExist_ForSentUpdate() {
            // Arrange
            String depositId = UUID.randomUUID().toString();
            when(depositRepository.getDepositById(any(UUID.class))).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    DepositNotFound.class,
                    () -> depositService.updateDepositStatus(depositId, Deposit.DepositStatus.SENT)
            );

            verify(depositRepository, times(1)).getDepositById(UUID.fromString(depositId));
            verify(depositRepository, never()).saveDeposit(any());
        }

        @Test
        @DisplayName("Should fail when deposit does not exist for CONFIRMED update")
        public void shouldFail_WhenDepositDoesNotExist_ForConfirmedUpdate() {
            // Arrange
            String depositId = UUID.randomUUID().toString();
            when(depositRepository.getDepositById(any(UUID.class))).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    DepositNotFound.class,
                    () -> depositService.updateDepositStatus(depositId, Deposit.DepositStatus.CONFIRMED)
            );

            verify(depositRepository, never()).saveDeposit(any());
        }

        @Test
        @DisplayName("Should fail when deposit does not exist for FAILED update")
        public void shouldFail_WhenDepositDoesNotExist_ForFailedUpdate() {
            // Arrange
            String depositId = UUID.randomUUID().toString();
            when(depositRepository.getDepositById(any(UUID.class))).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    DepositNotFound.class,
                    () -> depositService.updateDepositStatus(depositId, Deposit.DepositStatus.FAILED)
            );

            verify(depositRepository, never()).saveDeposit(any());
        }
    }
}

