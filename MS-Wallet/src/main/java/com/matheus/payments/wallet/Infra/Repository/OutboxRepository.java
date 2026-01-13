package com.matheus.payments.wallet.Infra.Repository;

import com.matheus.payments.wallet.Domain.Models.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

    @Query(value = "SELECT TOP 100 * FROM outbox WHERE is_sent = 0", nativeQuery = true)
    List<Outbox> findAllBySentFalse();
}
