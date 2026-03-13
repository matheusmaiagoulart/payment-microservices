package com.matheus.payments.UnitTests.DTOs;

import com.matheus.payments.Application.DTOs.DepositRequest;
import com.matheus.payments.UnitTests.Fixtures.DepositFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DepositRequest Validation Tests")
class DepositRequestTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should validate successfully with valid data")
        public void shouldValidateSuccessfully_WithValidData() {
            // Arrange
            DepositRequest request = DepositFixture.createDepositRequest();

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should validate with minimum amount")
        public void shouldValidate_WithMinimumAmount() {
            // Arrange
            DepositRequest request = DepositFixture.createDepositRequest(
                    UUID.randomUUID(),
                    new BigDecimal("0.01")
            );

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should validate with large amount")
        public void shouldValidate_WithLargeAmount() {
            // Arrange
            DepositRequest request = DepositFixture.createDepositRequest(
                    UUID.randomUUID(),
                    new BigDecimal("999999.99")
            );

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should have correct getters")
        public void shouldHaveCorrectGetters() {
            // Arrange
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");
            DepositRequest request = DepositFixture.createDepositRequest(receiverId, amount);

            // Assert
            assertEquals(receiverId, request.getReceiverId());
            assertEquals(amount, request.getAmount());
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when receiverId is null")
        public void shouldFail_WhenReceiverIdIsNull() {
            // Arrange
            DepositRequest request = new DepositRequest(null, new BigDecimal("100.00"));

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Receiver Account ID cannot be null", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when amount is null")
        public void shouldFail_WhenAmountIsNull() {
            // Arrange
            DepositRequest request = new DepositRequest(UUID.randomUUID(), null);

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Amount cannot be blank", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when amount is zero")
        public void shouldFail_WhenAmountIsZero() {
            // Arrange
            DepositRequest request = new DepositRequest(UUID.randomUUID(), BigDecimal.ZERO);

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Amount must be greater than zero", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when amount is negative")
        public void shouldFail_WhenAmountIsNegative() {
            // Arrange
            DepositRequest request = new DepositRequest(UUID.randomUUID(), new BigDecimal("-10.00"));

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Amount must be greater than zero", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when both fields are null")
        public void shouldFail_WhenBothFieldsAreNull() {
            // Arrange
            DepositRequest request = new DepositRequest(null, null);

            // Act
            Set<ConstraintViolation<DepositRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(2, violations.size());
        }
    }
}
