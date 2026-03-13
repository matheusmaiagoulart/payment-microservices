package com.matheus.payments.UnitTests.DTOs;

import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.UnitTests.Fixtures.TransactionFixture;
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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionRequest Validation Tests")
class TransactionRequestTests {

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
            TransactionRequest request = TransactionFixture.createTransactionRequest();

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should validate with minimum amount")
        public void shouldValidate_WithMinimumAmount() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "sender@test.com", 
                    "receiver@test.com", 
                    new BigDecimal("0.01")
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should have correct getters")
        public void shouldHaveCorrectGetters() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest();

            // Assert
            assertEquals("sender@test.com", request.getSenderKey());
            assertEquals("receiver@test.com", request.getReceiverKey());
            assertEquals(new BigDecimal("100.00"), request.getAmount());
        }

        @Test
        @DisplayName("Should allow setting transaction ID")
        public void shouldAllowSettingTransactionId() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest();
            String transactionId = "123e4567-e89b-12d3-a456-426614174000";

            // Act
            request.setTransactionId(transactionId);

            // Assert
            assertEquals(transactionId, request.getTransactionId());
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when senderKey is null")
        public void shouldFail_WhenSenderKeyIsNull() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    null, 
                    "receiver@test.com", 
                    new BigDecimal("100.00")
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Sender value can't be Null or empty", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when senderKey is empty")
        public void shouldFail_WhenSenderKeyIsEmpty() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "", 
                    "receiver@test.com", 
                    new BigDecimal("100.00")
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Sender value can't be Null or empty", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when senderKey is blank")
        public void shouldFail_WhenSenderKeyIsBlank() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "   ", 
                    "receiver@test.com", 
                    new BigDecimal("100.00")
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Sender value can't be Null or empty", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when receiverKey is null")
        public void shouldFail_WhenReceiverKeyIsNull() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "sender@test.com", 
                    null, 
                    new BigDecimal("100.00")
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Receiver value can't be Null or empty", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when receiverKey is empty")
        public void shouldFail_WhenReceiverKeyIsEmpty() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "sender@test.com", 
                    "", 
                    new BigDecimal("100.00")
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Receiver value can't be Null or empty", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when amount is null")
        public void shouldFail_WhenAmountIsNull() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "sender@test.com", 
                    "receiver@test.com", 
                    null
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Amount value can't be Null", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when amount is zero")
        public void shouldFail_WhenAmountIsZero() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "sender@test.com", 
                    "receiver@test.com", 
                    BigDecimal.ZERO
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Amount must be positive", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when amount is negative")
        public void shouldFail_WhenAmountIsNegative() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(
                    "sender@test.com", 
                    "receiver@test.com", 
                    new BigDecimal("-10.00")
            );

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(1, violations.size());
            assertEquals("Amount must be positive", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail when all required fields are null")
        public void shouldFail_WhenAllRequiredFieldsAreNull() {
            // Arrange
            TransactionRequest request = TransactionFixture.createTransactionRequest(null, null, null);

            // Act
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

            // Assert
            assertEquals(3, violations.size());
        }
    }
}

