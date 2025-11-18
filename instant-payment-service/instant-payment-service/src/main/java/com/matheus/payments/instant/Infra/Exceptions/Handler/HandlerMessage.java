package com.matheus.payments.instant.Infra.Exceptions.Handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


@Getter
@Setter
public class HandlerMessage {
    private HttpStatus status;
    private String message;
    private LocalDateTime timestamp;

    public HandlerMessage(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
