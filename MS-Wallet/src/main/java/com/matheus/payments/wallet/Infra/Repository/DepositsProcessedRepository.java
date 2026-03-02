package com.matheus.payments.wallet.Infra.Repository;

import com.matheus.payments.wallet.Domain.Models.DepositsProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DepositsProcessedRepository extends JpaRepository<DepositsProcessed, UUID> {
}
