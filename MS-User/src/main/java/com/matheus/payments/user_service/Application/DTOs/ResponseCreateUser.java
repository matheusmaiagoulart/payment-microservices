package com.matheus.payments.user_service.Application.DTOs;

import com.matheus.payments.user_service.Domain.Models.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ResponseCreateUser {

    private UUID userId;
    private String message;
    private LocalDateTime timestamp;

    protected ResponseCreateUser() {
    }

    public ResponseCreateUser(UUID userId, String message) {
        this.userId = userId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public static ResponseCreateUser userCreatedSuccessfully(User user) {
        String message = "Congrats %s! Your request to create a account has been received and is being processed. You will receive a confirmation email shortly.".formatted(user.getFirstName());
        return new ResponseCreateUser(user.getId(), message);
    }
}
