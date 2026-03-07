package com.matheus.payments.Domain.Repositories;

import com.matheus.payments.Domain.Models.Deposit;

import java.util.Optional;
import java.util.UUID;

public interface DepositRepository {

     void saveDeposit(Deposit deposit);
     Optional<Deposit> getDepositById(UUID depositId);
}
