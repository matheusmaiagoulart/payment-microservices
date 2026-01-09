package com.matheus.payments.wallet.Application.Interfaces;

import com.matheus.payments.wallet.Domain.Events.UserCreatedEvent;

public interface ICreateWallet {
    boolean createWallet(UserCreatedEvent request);
}
