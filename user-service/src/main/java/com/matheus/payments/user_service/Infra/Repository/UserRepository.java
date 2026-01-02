package com.matheus.payments.user_service.Infra.Repository;

import com.matheus.payments.user_service.Domain.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {}
