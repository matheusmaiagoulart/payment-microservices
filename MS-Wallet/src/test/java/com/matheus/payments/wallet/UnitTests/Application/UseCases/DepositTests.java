package com.matheus.payments.wallet.UnitTests.Application.UseCases;
import com.matheus.payments.wallet.Application.Audit.DepositAudit;
import com.matheus.payments.wallet.Application.Events.Deposit.DepositExecuted;
import com.matheus.payments.wallet.Application.Events.Deposit.DepositFailed;
import com.matheus.payments.wallet.Application.Services.TransferExecution;
import com.matheus.payments.wallet.Application.UseCases.Deposit;
import com.matheus.payments.wallet.Domain.Exceptions.DepositAlreadyProcessed;
import com.matheus.payments.wallet.Domain.Models.DepositsProcessed;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.DepositCreated.DepositCreated;
import com.matheus.payments.wallet.Infra.Repository.DepositsProcessedRepository;
import com.matheus.payments.wallet.UnitTests.Fixtures.DepositCreatedFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class DepositTests {
    @Mock
    private DepositAudit audit;
    @Mock
    private TransferExecution transferExecution;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private DepositsProcessedRepository depositsProcessedRepository;
    @InjectMocks
    private Deposit deposit;
    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {
        @Test
        @DisplayName("Should execute deposit successfully")
        public void shouldExecuteDepositSuccessfully() {
            DepositCreated depositCreated = DepositCreatedFixture.createDepositCreated(new BigDecimal("100.00"));
            deposit.executeDeposit(depositCreated);
            verify(depositsProcessedRepository, times(1)).saveAndFlush(any(DepositsProcessed.class));
            verify(transferExecution, times(1)).depositExecution(depositCreated);
            verify(publisher, times(1)).publishEvent(any(DepositExecuted.class));
        }
    }
    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {
        @Test
        @DisplayName("Should handle idempotent request when deposit was already processed")
        public void shouldHandleIdempotentRequest_WhenDepositWasAlreadyProcessed() {
            DepositCreated depositCreated = DepositCreatedFixture.createDepositCreated(new BigDecimal("100.00"));
            doThrow(DataIntegrityViolationException.class).when(depositsProcessedRepository).saveAndFlush(any(DepositsProcessed.class));
            deposit.executeDeposit(depositCreated);
            verify(depositsProcessedRepository, times(1)).saveAndFlush(any(DepositsProcessed.class));
            verify(transferExecution, never()).depositExecution(any());
            verify(publisher, times(1)).publishEvent(any(DepositFailed.class));
        }
        @Test
        @DisplayName("Should publish failure event when deposit execution fails")
        public void shouldPublishFailureEvent_WhenDepositExecutionFails() {
            DepositCreated depositCreated = DepositCreatedFixture.createDepositCreated(new BigDecimal("100.00"));
            doThrow(new RuntimeException("Database error")).when(transferExecution).depositExecution(depositCreated);
            deposit.executeDeposit(depositCreated);
            verify(depositsProcessedRepository, times(1)).saveAndFlush(any(DepositsProcessed.class));
            verify(transferExecution, times(1)).depositExecution(depositCreated);
            verify(publisher, times(1)).publishEvent(any(DepositFailed.class));
            verify(publisher, never()).publishEvent(any(DepositExecuted.class));
        }
    }
}
