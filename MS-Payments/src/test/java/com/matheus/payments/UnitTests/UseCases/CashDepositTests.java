package com.matheus.payments.UnitTests.UseCases;

import com.matheus.payments.Application.DTOs.DepositRequest;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Application.UseCases.CashDeposit;
import com.matheus.payments.Domain.Models.Deposit;
import com.matheus.payments.UnitTests.Fixtures.DepositFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashDeposit Use Case Tests")
class CashDepositTests {

    @Mock
    private DepositService depositService;

    @InjectMocks
    private CashDeposit cashDeposit;

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
        @DisplayName("Should execute cash deposit successfully")
        public void shouldExecuteCashDepositSuccessfully() {
            // Arrange
            when(depositService.createDeposit(any(DepositRequest.class))).thenReturn(deposit);

            // Act
            Deposit result = cashDeposit.execute(depositRequest);

            // Assert
            assertNotNull(result);
            assertEquals(DepositFixture.DEFAULT_AMOUNT, result.getAmount());
            assertEquals(Deposit.DepositStatus.PENDING, result.getStatus());
            verify(depositService, times(1)).createDeposit(depositRequest);
        }

        @Test
        @DisplayName("Should return deposit with generated ID")
        public void shouldReturnDeposit_WithGeneratedId() {
            // Arrange
            when(depositService.createDeposit(any(DepositRequest.class))).thenReturn(deposit);

            // Act
            Deposit result = cashDeposit.execute(depositRequest);

            // Assert
            assertNotNull(result.getDepositId());
            verify(depositService, times(1)).createDeposit(depositRequest);
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when deposit service fails")
        public void shouldFail_WhenDepositServiceFails() {
            // Arrange
            when(depositService.createDeposit(any(DepositRequest.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> cashDeposit.execute(depositRequest)
            );

            assertEquals("Database error", exception.getMessage());
            verify(depositService, times(1)).createDeposit(depositRequest);
        }
    }
}
