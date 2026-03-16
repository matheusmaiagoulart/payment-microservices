package com.matheus.payments.user_service.Domain.Repositories;

import com.matheus.payments.user_service.Domain.Models.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User saveAndFlush(User user);

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByCpf(String cpf);
}

