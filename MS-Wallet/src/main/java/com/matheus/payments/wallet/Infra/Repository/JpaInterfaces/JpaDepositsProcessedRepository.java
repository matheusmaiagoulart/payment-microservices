package com.matheus.payments.wallet.Infra.Repository.JpaInterfaces;

import com.matheus.payments.wallet.Domain.Models.DepositsProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaDepositsProcessedRepository extends JpaRepository<DepositsProcessed, UUID> {
}

