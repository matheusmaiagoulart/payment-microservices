package com.matheus.payments.user_service.Application.Services;

import com.matheus.payments.user_service.Application.Audit.CreateUserAudit;
import com.matheus.payments.user_service.Application.DTOs.RequestCreateUser;
import com.matheus.payments.user_service.Application.DTOs.ResponseCreateUser;
import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;
import com.matheus.payments.user_service.Domain.Exceptios.CpfAlreadyExistsException;
import com.matheus.payments.user_service.Domain.Exceptios.EmailAlreadyExists;
import com.matheus.payments.user_service.Domain.Exceptios.PhoneNumberAlreadyExistsException;
import com.matheus.payments.user_service.Domain.Exceptios.UniqueFieldsEntryViolationException;
import com.matheus.payments.user_service.Domain.Models.User;
import com.matheus.payments.user_service.Domain.Repositories.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {
    private final CreateUserAudit audit;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, CreateUserAudit audit, ApplicationEventPublisher eventPublisher) {
        this.audit = audit;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseCreateUser createUser(RequestCreateUser data) {

        audit.logUserCreationStarting(data.getCpf());
        User user = new User(
                data.getFirstName(),
                data.getLastName(),
                data.getEmail(),
                data.getCpf(),
                data.getPhoneNumber(),
                data.getAccountType(),
                data.getBirthDate()
        );

        try {
            userRepository.saveAndFlush(user);

            UserCreatedEvent userCreatedEvent = new UserCreatedEvent(user.getId(), user.getCpf(), user.getAccountType());
            eventPublisher.publishEvent(userCreatedEvent);

            audit.logUserCreationSuccess(data.getCpf());
            return ResponseCreateUser.userCreatedSuccessfully(user);
        } catch (DataIntegrityViolationException e) {
            audit.logUserCreationFailed(data.getCpf(), "Unique field violation: " + e.getMessage());
            throw handleUniqueFieldException(e);
        }
    }


    public RuntimeException handleUniqueFieldException(DataIntegrityViolationException e) throws PhoneNumberAlreadyExistsException, CpfAlreadyExistsException, EmailAlreadyExists, UniqueFieldsEntryViolationException {
        var error = e.getCause().getMessage();

        if (error.contains("uk_users_phone_number")) {
            throw new PhoneNumberAlreadyExistsException();
        }
        if (error.contains("uk_users_cpf")) {
            throw new CpfAlreadyExistsException();
        }
        if (error.contains("uk_users_email")) {
            throw new EmailAlreadyExists();
        }

        return new UniqueFieldsEntryViolationException(e.getMessage());
    }
}
