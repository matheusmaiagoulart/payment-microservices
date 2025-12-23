package com.matheus.payments.user_service.Application.Interfaces;

import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;

public interface UserEventPublisher {
    void publisher(UserCreatedEvent event);
}
