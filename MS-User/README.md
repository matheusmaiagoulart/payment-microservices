# MS-User - User Microservice

Microserviço responsável pelo gerenciamento de usuários do sistema de pagamentos. Este serviço é o ponto de entrada para criação de contas, validação de dados pessoais e integração com outros serviços através de eventos assíncronos.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Eventos](#eventos)
- [Endpoints](#endpoints)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Configuração](#configuração)
- [Executando o Projeto](#executando-o-projeto)
- [Testes](#testes)

## 📖 Sobre o Projeto

O **MS-User** é um microserviço que resolve o problema de **cadastro e gerenciamento centralizado de usuários** em um sistema de pagamentos distribuído. Ele garante:

- ✅ Validação de dados únicos (CPF, Email, Telefone)
- ✅ Criação de usuários com validações de negócio
- ✅ Comunicação assíncrona com outros serviços via Kafka
- ✅ Consistência eventual através do padrão Outbox
- ✅ Ativação de conta após confirmação de criação de carteira

### Fluxo Principal

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Cliente   │────▶│   MS-User   │────▶│    Kafka    │────▶│  MS-Wallet  │
│  (Request)  │     │ (Cria User) │     │   (Event)   │     │(Cria Wallet)│
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                                                   │
                                                                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Usuário   │◀────│   MS-User   │◀────│    Kafka    │◀────│  MS-Wallet  │
│   (Ativo)   │     │(Ativa User) │     │   (Event)   │     │  (Confirm)  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

## 🏗️ Arquitetura

O projeto segue os princípios da **Clean Architecture**, garantindo separação de responsabilidades e independência de frameworks.

### Camadas

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CONTROLLER                                │
│                    (Recebe requests HTTP)                           │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                          APPLICATION                                │
│              (UseCases, Services, DTOs, EventHandlers)              │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                            DOMAIN                                   │
│            (Models, Repositories Interfaces, Events)                │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                             INFRA                                   │
│         (JPA Implementations, Kafka, Configs, Schedulers)           │
└─────────────────────────────────────────────────────────────────────┘
```

### Padrões Utilizados

| Padrão | Descrição |
|--------|-----------|
| **Clean Architecture** | Separação em camadas com inversão de dependência |
| **Repository Pattern** | Abstração de acesso a dados com interfaces em Domain |
| **Outbox Pattern** | Garantia de consistência eventual em eventos assíncronos |
| **Static Factory Method** | Criação de objetos com métodos descritivos |
| **Retry Pattern** | Resiliência em operações de banco com Resilience4j |

## 🛠️ Tecnologias

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Java | 21 | Linguagem de programação |
| Spring Boot | 4.0.1 | Framework principal |
| Spring Data JPA | - | Persistência de dados |
| Spring Kafka | - | Mensageria assíncrona |
| SQL Server | - | Banco de dados relacional |
| Flyway | - | Versionamento de banco de dados |
| Resilience4j | 2.3.0 | Padrões de resiliência (Retry) |
| Lombok | - | Redução de boilerplate |
| JUnit 5 | - | Testes unitários |
| Mockito | - | Mocking para testes |

## 📨 Eventos

### Eventos Produzidos

| Evento | Tópico | Descrição |
|--------|--------|-----------|
| `UserCreatedEvent` | `user-created` | Emitido quando um usuário é criado com sucesso |

**Payload do UserCreatedEvent:**
```json
{
  "userId": "UUID",
  "cpf": "12345678901",
  "accountType": "CHECKING | MERCHANT"
}
```

### Eventos Consumidos

| Evento | Tópico | Descrição |
|--------|--------|-----------|
| `WalletCreatedEvent` | `wallet-created` | Recebido quando a carteira do usuário é criada |

**Ação:** Ativa a conta do usuário (`active = true`)

## 🔌 Endpoints

### Criar Usuário

```http
POST /users/create
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Matheus",
  "lastName": "Goulart",
  "email": "matheus@email.com",
  "cpf": "12345678901",
  "phoneNumber": "11999999999",
  "accountType": "CHECKING",
  "birthDate": "1990-01-15"
}
```

**Response (200 OK):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Congrats Matheus! Your request to create a account has been received and is being processed. You will receive a confirmation email shortly.",
  "timestamp": "2026-03-16T10:30:00"
}
```

**Validações:**
- `firstName`: 1-50 caracteres
- `lastName`: 1-50 caracteres
- `email`: formato válido, 5-100 caracteres
- `cpf`: exatamente 11 dígitos
- `phoneNumber`: exatamente 11 dígitos
- `accountType`: CHECKING ou MERCHANT
- `birthDate`: data no passado

## 📁 Estrutura do Projeto

```
src/main/java/com/matheus/payments/user_service/
├── Controller/
│   └── UsersController.java
├── Application/
│   ├── Audit/
│   │   ├── CorrelationId.java
│   │   ├── CorrelationFilter.java
│   │   ├── CreateUserAudit.java
│   │   └── OutboxServiceAudit.java
│   ├── DTOs/
│   │   ├── RequestCreateUser.java
│   │   └── ResponseCreateUser.java
│   ├── EventHandlers/
│   │   ├── UserCreatedEventHandler.java
│   │   └── WalletCreatedEventHandler.java
│   ├── Services/
│   │   ├── UserService.java
│   │   └── OutboxService.java
│   └── UseCases/
│       ├── CreateUser.java
│       └── ActivateUserAccount.java
├── Domain/
│   ├── Events/
│   │   └── UserCreatedEvent.java
│   ├── Exceptions/
│   │   ├── InvalidCpfException.java
│   │   ├── PhoneNumberFormatException.java
│   │   ├── CpfAlreadyExistsException.java
│   │   ├── EmailAlreadyExists.java
│   │   └── PhoneNumberAlreadyExistsException.java
│   ├── Models/
│   │   ├── User.java
│   │   └── Outbox.java
│   └── Repositories/
│       ├── UserRepository.java
│       └── OutboxRepository.java
├── Infra/
│   ├── Configs/
│   ├── Exceptions/
│   │   ├── Custom/
│   │   └── Handler/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── UserExceptionHandler.java
│   │       └── HandlerMessage.java
│   ├── Kafka/
│   │   └── WalletCreatedListener.java
│   ├── Repository/
│   │   ├── JpaInterfaces/
│   │   │   ├── JpaUserRepository.java
│   │   │   └── JpaOutboxRepository.java
│   │   └── JpaImplements/
│   │       ├── UserRepositoryImpl.java
│   │       └── OutboxRepositoryImpl.java
│   └── Schedulers/
│       └── OutboxScheduler.java
└── Utils/
    ├── ApplicationData.java
    └── KafkaTopics.java
```

## ⚙️ Configuração

### application.properties

```properties
# Application
spring.application.name=MS-User
server.port=8082

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=none

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Kafka
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}
spring.kafka.producer.group-id=user-service-group
```

### Banco de Dados

O projeto utiliza **Flyway** para versionamento do banco de dados. As migrations estão em `src/main/resources/db/migration/`:

| Migration | Descrição |
|-----------|-----------|
| V1__create_users_table.sql | Cria tabela de usuários com constraints |
| V2__create_outbox_table.sql | Cria tabela de outbox para eventos |
| V3__add_index_users_table.sql | Adiciona índices para performance |

## 🚀 Executando o Projeto

### Pré-requisitos

- Java 21
- SQL Server
- Apache Kafka
- Maven

### Passos

1. **Clone o repositório**
```bash
git clone https://github.com/matheusmaiagoulart/payment-microservices.git
cd MS-User
```

2. **Configure o banco de dados**
```sql
CREATE DATABASE [MS-User];
```

3. **Inicie o Kafka**
```bash
# Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Kafka
bin/kafka-server-start.sh config/server.properties
```

4. **Execute a aplicação**
```bash
./mvnw spring-boot:run
```

## 🧪 Testes

O projeto possui testes unitários seguindo o padrão **AAA (Arrange, Act, Assert)** com **Fixtures** para criação de objetos de teste.

### Estrutura de Testes

```
src/test/java/com/matheus/payments/user_service/
├── Fixtures/
│   ├── UserFixture.java
│   ├── RequestCreateUserFixture.java
│   └── OutboxFixture.java
└── Application/
    ├── Services/
    │   ├── UserServiceTest.java
    │   └── OutboxServiceTest.java
    └── UseCases/
        └── ActivateUserAccountTest.java
```

### Executar Testes

```bash
# Todos os testes
./mvnw test

# Testes específicos
./mvnw test -Dtest="UserServiceTest,OutboxServiceTest,ActivateUserAccountTest"
```

## 👤 Autor

**Matheus Maia Goulart**

## 📄 Licença

Este projeto está sob a licença MIT.


