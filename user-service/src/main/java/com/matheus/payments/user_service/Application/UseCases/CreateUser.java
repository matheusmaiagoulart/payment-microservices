package com.matheus.payments.user_service.Application.UseCases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.matheus.payments.user_service.Application.Audit.CreateUserAudit;
import tools.jackson.databind.ObjectMapper;
import com.matheus.payments.user_service.Application.DTOs.RequestCreateUser;
import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;
import com.matheus.payments.user_service.Domain.Exceptios.*;
import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Domain.Models.User;
import com.matheus.payments.user_service.Infra.Repository.OutboxRepository;
import com.matheus.payments.user_service.Infra.Repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CreateUser {

    private final ObjectMapper mapper;
    private final CreateUserAudit audit;
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;

    public CreateUser(ObjectMapper mapper, UserRepository userRepository, OutboxRepository outboxRepository, CreateUserAudit audit) {
        this.audit = audit;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public String createUser(RequestCreateUser data) {

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
            String payload = mapper.writeValueAsString(userCreatedEvent);

            Outbox outbox = new Outbox(user.getId(), userCreatedEvent.getTopic(), payload);
            outboxRepository.saveAndFlush(outbox);

            audit.logUserCreationSuccess(data.getCpf());
            return "Congrats! Your request to create a account has been received and is being processed. You will receive a confirmation email shortly.";
        }
        catch (DataIntegrityViolationException e) {
            audit.logUserCreationFailed(data.getCpf(), "Unique field violation: " + e.getMessage());
            throw handleUniqueFieldException(e);
        }
    }


    public RuntimeException handleUniqueFieldException(DataIntegrityViolationException e) throws PhoneNumberAlreadyExistsException, CpfAlreadyExistsException, EmailAlreadyExists, UniqueFieldsEntryViolationException {
        var error = e.getCause().getMessage();

        if(error.contains("uk_users_phone_number")){ throw new PhoneNumberAlreadyExistsException(); }
        if(error.contains("uk_users_cpf")){ throw new CpfAlreadyExistsException(); }
        if(error.contains("uk_users_email")){ throw new EmailAlreadyExists(); }

        return new UniqueFieldsEntryViolationException("Unique field violation: " + e.getMessage());
    }

}
