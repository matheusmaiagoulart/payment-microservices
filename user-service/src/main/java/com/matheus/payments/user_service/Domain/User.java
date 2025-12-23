package com.matheus.payments.user_service.Domain;


import com.matheus.payments.user_service.Domain.Exceptios.InvalidCpfException;
import com.matheus.payments.user_service.Domain.Exceptios.PhoneNumberFormatException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.shared.Domain.accountType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String cpf;
    private String phoneNumber;
    private accountType accountType;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    @Version
    private Integer version;

    public User(String FirstName, String LastName, String Email, String Cpf, String PhoneNumber, accountType accountType, LocalDate birthDate) {
        this.email = Email;
        this.active = false; // Users are inactive until Wallet creation is confirmed
        this.lastName = LastName;
        this.birthDate = birthDate;
        this.firstName = FirstName;
        this.id = UUID.randomUUID();
        this.cpf = cpfValidator(Cpf);
        this.accountType = accountType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.phoneNumber = phoneNumberFormatter(PhoneNumber);

    }

    private String phoneNumberFormatter(String phoneNumber) {
        if (phoneNumber.length() != 11) {
            throw new PhoneNumberFormatException();
        }
        return phoneNumber.replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace(" ", "");
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    private String cpfValidator(String cpf) {
        if (cpf.length() != 11) {
            throw new InvalidCpfException();
        }
        return cpf;
    }
}
