package com.matheus.payments.user_service.Infra.Repository.JpaImplements;

import com.matheus.payments.user_service.Domain.Models.User;
import com.matheus.payments.user_service.Domain.Repositories.UserRepository;
import com.matheus.payments.user_service.Infra.Repository.JpaInterfaces.JpaUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryImpl(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User saveAndFlush(User user) {
        return jpaUserRepository.saveAndFlush(user);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByCpf(String cpf) {
        return jpaUserRepository.findByCpf(cpf);
    }
}

