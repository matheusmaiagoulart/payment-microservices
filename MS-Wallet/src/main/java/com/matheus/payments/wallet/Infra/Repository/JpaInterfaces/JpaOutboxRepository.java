package com.matheus.payments.wallet.Infra.Repository.JpaInterfaces;

import com.matheus.payments.wallet.Domain.Models.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaOutboxRepository extends JpaRepository<Outbox, UUID> {

    @Query(value = "SELECT TOP 20 * FROM outbox WHERE is_sent = 0 ORDER BY created_at ASC", nativeQuery = true)
    List<Outbox> findAllBySentFalse();
}

