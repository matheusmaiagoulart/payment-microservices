package com.matheus.payments.Infra.Repository.JpaInterfaces;

import com.matheus.payments.Domain.Models.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDepositRepository extends JpaRepository<Deposit, UUID> {
}
