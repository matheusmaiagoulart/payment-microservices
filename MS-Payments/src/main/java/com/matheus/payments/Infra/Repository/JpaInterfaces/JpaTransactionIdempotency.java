package com.matheus.payments.Infra.Repository.JpaInterfaces;

import com.matheus.payments.Domain.Models.TransactionIdempotency;
import com.matheus.payments.Domain.Repositories.TransactionIdempotencyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaTransactionIdempotency extends JpaRepository<TransactionIdempotency, UUID> {
}
