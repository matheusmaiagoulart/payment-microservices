package com.matheus.payments.UnitTests.Schedulers;

import com.matheus.payments.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.Application.Services.OutboxService;
import com.matheus.payments.Domain.Models.TransactionOutbox;
import com.matheus.payments.Domain.Repositories.OutboxRepository;
import com.matheus.payments.Infra.Exceptions.Custom.DataBaseException;
import com.matheus.payments.Infra.Schedulers.OutboxScheduler;
import com.matheus.payments.UnitTests.Fixtures.OutboxFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxScheduler Tests")
class OutboxSchedulerTests {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private OutboxServiceAudit audit;

    @InjectMocks
    private OutboxScheduler outboxScheduler;

    private TransactionOutbox outbox1;
    private TransactionOutbox outbox2;
    private List<TransactionOutbox> pendingOutboxes;

    @BeforeEach
    void setUp() {
        outbox1 = OutboxFixture.createOutbox();
        outbox2 = OutboxFixture.createOutbox();
        pendingOutboxes = Arrays.asList(outbox1, outbox2);
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should process pending outbox entries successfully")
        public void shouldProcessPendingOutboxEntriesSuccessfully() throws Exception {
            // Arrange
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(pendingOutboxes);
            doNothing().when(outboxService).sendOutboxEntry(any(TransactionOutbox.class));

            // Act
            outboxScheduler.processPendingOutbox();

            // Assert
            verify(outboxRepository, times(1)).findBySentFalseOrderByCreatedAtAsc();
            verify(outboxService, times(2)).sendOutboxEntry(any(TransactionOutbox.class));
            verify(outboxService, times(1)).sendOutboxEntry(outbox1);
            verify(outboxService, times(1)).sendOutboxEntry(outbox2);
        }

        @Test
        @DisplayName("Should do nothing when no pending outbox entries exist")
        public void shouldDoNothing_WhenNoPendingOutboxEntriesExist() throws Exception {
            // Arrange
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

            // Act
            outboxScheduler.processPendingOutbox();

            // Assert
            verify(outboxRepository, times(1)).findBySentFalseOrderByCreatedAtAsc();
            verify(outboxService, never()).sendOutboxEntry(any(TransactionOutbox.class));
        }

        @Test
        @DisplayName("Should process single outbox entry")
        public void shouldProcessSingleOutboxEntry() throws Exception {
            // Arrange
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(Collections.singletonList(outbox1));
            doNothing().when(outboxService).sendOutboxEntry(any(TransactionOutbox.class));

            // Act
            outboxScheduler.processPendingOutbox();

            // Assert
            verify(outboxRepository, times(1)).findBySentFalseOrderByCreatedAtAsc();
            verify(outboxService, times(1)).sendOutboxEntry(outbox1);
        }

        @Test
        @DisplayName("Should retrieve outbox entries ordered by creation time")
        public void shouldRetrieveOutboxEntries_OrderedByCreationTime() throws Exception {
            // Arrange
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(pendingOutboxes);
            doNothing().when(outboxService).sendOutboxEntry(any(TransactionOutbox.class));

            // Act
            outboxScheduler.processPendingOutbox();

            // Assert
            verify(outboxRepository, times(1)).findBySentFalseOrderByCreatedAtAsc();
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when sending outbox entry fails")
        public void shouldFail_WhenSendingOutboxEntryFails() throws Exception {
            // Arrange
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(Collections.singletonList(outbox1));
            doThrow(new RuntimeException("Kafka error"))
                    .when(outboxService).sendOutboxEntry(any(TransactionOutbox.class));

            // Act & Assert
            DataBaseException exception = assertThrows(
                    DataBaseException.class,
                    () -> outboxScheduler.processPendingOutbox()
            );

            assertTrue(exception.getMessage().contains("An error occurred while sending Outbox Entry"));
            verify(audit, times(1)).logErrorCreateOutbox(anyString(), anyString());
        }

        @Test
        @DisplayName("Should fail with transaction ID when error occurs")
        public void shouldFail_WithTransactionId_WhenErrorOccurs() throws Exception {
            // Arrange
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(Collections.singletonList(outbox1));
            doThrow(new RuntimeException("Connection error"))
                    .when(outboxService).sendOutboxEntry(any(TransactionOutbox.class));

            // Act & Assert
            DataBaseException exception = assertThrows(
                    DataBaseException.class,
                    () -> outboxScheduler.processPendingOutbox()
            );

            assertTrue(exception.getMessage().contains(outbox1.getTransactionId()));
        }

        @Test
        @DisplayName("Should stop processing on first error")
        public void shouldStopProcessing_OnFirstError() throws Exception {
            // Arrange
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(pendingOutboxes);
            doThrow(new RuntimeException("Error"))
                    .when(outboxService).sendOutboxEntry(outbox1);

            // Act & Assert
            assertThrows(
                    DataBaseException.class,
                    () -> outboxScheduler.processPendingOutbox()
            );

            verify(outboxService, times(1)).sendOutboxEntry(outbox1);
            verify(outboxService, never()).sendOutboxEntry(outbox2);
        }

        @Test
        @DisplayName("Should log error with correct transaction ID when processing fails")
        public void shouldLogError_WithCorrectTransactionId_WhenProcessingFails() throws Exception {
            // Arrange
            String expectedTransactionId = outbox1.getTransactionId();
            when(outboxRepository.findBySentFalseOrderByCreatedAtAsc()).thenReturn(Collections.singletonList(outbox1));
            doThrow(new RuntimeException("Processing error"))
                    .when(outboxService).sendOutboxEntry(any(TransactionOutbox.class));

            // Act & Assert
            assertThrows(
                    DataBaseException.class,
                    () -> outboxScheduler.processPendingOutbox()
            );

            verify(audit, times(1)).logErrorCreateOutbox(eq(expectedTransactionId), anyString());
        }
    }
}
