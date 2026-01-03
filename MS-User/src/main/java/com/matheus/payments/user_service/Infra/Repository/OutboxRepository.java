package com.matheus.payments.user_service.Infra.Repository;

import com.matheus.payments.user_service.Domain.Models.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

    @Query(value = "SELECT o FROM Outbox o WHERE o.isSent = false LIMIT 100")
    List<Outbox> findAllBySentFalse();
}
