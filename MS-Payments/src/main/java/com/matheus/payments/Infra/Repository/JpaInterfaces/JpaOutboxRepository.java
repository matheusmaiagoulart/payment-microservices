package com.matheus.payments.Infra.Repository.JpaInterfaces;

import com.matheus.payments.Domain.Models.TransactionOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOutboxRepository extends JpaRepository<TransactionOutbox, String> {

    Optional<TransactionOutbox> findByTransactionId(String transactionId);

    List<TransactionOutbox> findBySentFalseOrderByCreatedAtAsc();

}


