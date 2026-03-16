package com.matheus.payments.user_service.Application.UseCases;

import com.matheus.payments.user_service.Application.DTOs.ResponseCreateUser;
import com.matheus.payments.user_service.Application.Services.UserService;
import com.matheus.payments.user_service.Application.DTOs.RequestCreateUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CreateUser {

    private final UserService userService;

    public CreateUser(UserService userService) { this.userService = userService; }

    @Transactional
    public ResponseCreateUser execute(RequestCreateUser data) {
        return userService.createUser(data);
    }
}
