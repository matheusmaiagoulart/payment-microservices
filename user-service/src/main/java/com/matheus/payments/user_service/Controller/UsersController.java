package com.matheus.payments.user_service.Controller;

import com.matheus.payments.user_service.Application.DTOs.RequestCreateUser;
import com.matheus.payments.user_service.Application.UseCases.CreateUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final CreateUser createUser;

    public UsersController(CreateUser createUser) {
        this.createUser = createUser;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody RequestCreateUser request) {
        var result = createUser.createUser(request);
        return ResponseEntity.ok(result);
    }
}
