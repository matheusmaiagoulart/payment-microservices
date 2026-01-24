package com.matheus.payments.user_service.Application.UseCases;

import com.matheus.payments.user_service.Domain.Models.User;
import com.matheus.payments.user_service.Infra.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivateUserAccount {

    private final UserRepository userRepository;

    public ActivateUserAccount(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void executeActivationUserAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.activateUser();
        userRepository.save(user);
    }
}
