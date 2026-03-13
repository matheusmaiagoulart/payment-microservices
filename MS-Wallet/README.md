# 🏦 MS-Wallet

> Microserviço de carteira digital para sistema de pagamentos instantâneos

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-blue.svg)](https://kafka.apache.org/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-Database-red.svg)](https://www.microsoft.com/sql-server)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-Fault%20Tolerance-yellow.svg)](https://resilience4j.readme.io/)

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Arquitetura](#-arquitetura)
- [Stack Tecnológica](#-stack-tecnológica)
- [Casos de Uso](#-casos-de-uso-detalhados)
- [Modelo de Domínio](#-modelo-de-domínio-ddd)
- [Padrões Arquiteturais](#-padrões-arquiteturais-implementados)
- [Fluxo de Eventos](#-fluxo-de-eventos-kafka)
- [API REST](#-api-rest)
- [Resiliência](#-estratégias-de-resiliência)
- [Observabilidade](#-observabilidade)
- [Como Executar](#-como-executar)

---

## 📋 Sobre o Projeto

O **MS-Wallet** é o microserviço central de um sistema de pagamentos distribuído, responsável pelo ciclo de vida completo das carteiras digitais. Ele atua como o **núcleo financeiro** do ecossistema, garantindo consistência transacional, auditoria e integração fluida com outros serviços via eventos assíncronos.

### Responsabilidades do Serviço

| Funcionalidade | Descrição |
|----------------|-----------|
| 🔐 **Criação de Wallets** | Provisiona carteiras automaticamente via evento de cadastro de usuário |
| 💸 **Transferências PIX** | Processa pagamentos instantâneos P2P com validação de saldo e chave PIX |
| 💰 **Depósitos** | Credita valores nas carteiras via integração assíncrona |
| 📊 **Ledger Contábil** | Registra toda movimentação em partida dobrada (débito/crédito) |
| 🔄 **Publicação de Eventos** | Notifica outros serviços sobre mudanças de estado via Transactional Outbox |

---

## 🏗️ Arquitetura

O projeto implementa **Clean Architecture** combinada com princípios de **Domain-Driven Design (DDD)**, organizando o código em camadas com dependências unidirecionais (de fora para dentro). Isso garante que o domínio de negócio seja independente de frameworks, banco de dados e detalhes de infraestrutura.

### Princípios Aplicados

- **Dependency Inversion Principle (DIP):** As camadas internas definem interfaces (ports) que são implementadas pelas camadas externas (adapters)
- **Single Responsibility:** Cada camada tem uma responsabilidade clara e bem definida
- **Separation of Concerns:** Lógica de negócio isolada de detalhes técnicos

```
src/main/java/com/matheus/payments/wallet/
│
├── Controller/                    # 🌐 Interface Layer (Primary Adapters)
│   └── WalletController.java      # Endpoint REST para transferências PIX
│
├── Application/                   # 🎯 Application Layer (Use Cases)
│   ├── UseCases/                  # Orquestração de regras de negócio
│   │   ├── CreateWallet.java      # Criação de wallet via evento
│   │   ├── InstantPayment.java    # Processamento de transferência PIX
│   │   └── Deposit.java           # Execução de depósitos
│   │
│   ├── Services/                  # Serviços de aplicação
│   │   ├── WalletService.java     # Operações de persistência de wallet
│   │   ├── PixKeyService.java     # Consulta de chaves PIX
│   │   ├── LedgerService.java     # Registro de movimentações contábeis
│   │   ├── TransferExecution.java # Execução atômica de transferências
│   │   └── OutboxService.java     # Gerenciamento do Transactional Outbox
│   │
│   ├── EventHandlers/             # Handlers de eventos de domínio (Spring Events)
│   │   ├── WalletHandlers/        # Handlers de criação de wallet
│   │   │   ├── WalletCreatedInternalEventHandler.java
│   │   │   └── WalletCreationFailedEventHandler.java
│   │   └── DepositHandlers/       # Handlers de depósito
│   │       ├── DepositExecutedInternalEventHandler.java
│   │       └── DepositFailedInternalEventHandler.java
│   │
│   ├── DTOs/                      # Data Transfer Objects
│   │   ├── Context/               # PixTransfer (contexto de transferência)
│   │   └── Response/              # InstantPaymentResponse
│   │
│   └── Audit/                     # Classes de auditoria e logging estruturado
│       ├── CorrelationId.java     # Gerenciamento de Correlation ID via MDC
│       ├── WalletServiceAudit.java
│       ├── DepositAudit.java
│       └── LedgerAudit.java
│
├── Domain/                        # 💎 Domain Layer (Enterprise Business Rules)
│   ├── Models/                    # Entidades e Agregados (Rich Domain Model)
│   │   ├── Wallet.java            # Aggregate Root - Carteira digital
│   │   ├── PixKey.java            # Entity - Chave PIX vinculada à wallet
│   │   ├── WalletLedger.java      # Entity - Registro contábil (ledger)
│   │   ├── Outbox.java            # Entity - Evento pendente de publicação
│   │   ├── TransactionsProcessed.java  # Controle de idempotência (PIX)
│   │   └── DepositsProcessed.java      # Controle de idempotência (Depósitos)
│   │
│   ├── Events/                    # 📨 Eventos de Domínio
│   │   ├── CreateWallet/          # Eventos do ciclo de vida da Wallet
│   │   │   ├── WalletCreatedEvent.java
│   │   │   └── WalletCreationFailed.java
│   │   └── Deposit/               # Eventos do ciclo de vida de Depósitos
│   │       ├── DepositExecuted.java
│   │       └── DepositFailed.java
│   │
│   ├── Repositories/              # 🔌 Repository Interfaces (Ports)
│   │   ├── WalletRepository.java
│   │   ├── PixKeyRepository.java
│   │   ├── WalletLedgerRepository.java
│   │   ├── OutboxRepository.java
│   │   ├── TransactionProcessedRepository.java
│   │   └── DepositsProcessedRepository.java
│   │
│   └── Exceptions/                # Exceções de domínio
│       ├── DomainException.java           # Base exception com errorCode
│       ├── InsufficientBalanceException.java
│       ├── InvalidAmountException.java
│       ├── WalletNotFoundException.java
│       ├── SameUserException.java
│       ├── SocialIdAlreadyExistsException.java
│       ├── TransactionAlreadyProcessed.java
│       └── DepositAlreadyProcessed.java
│
├── Infra/                         # 🔧 Infrastructure Layer (Secondary Adapters)
│   ├── Repository/                # Implementações de persistência
│   │   ├── JpaInterfaces/         # Interfaces Spring Data JPA
│   │   │   ├── JpaWalletRepository.java
│   │   │   ├── JpaPixKeyRepository.java
│   │   │   ├── JpaWalletLedgerRepository.java
│   │   │   ├── JpaOutboxRepository.java
│   │   │   ├── JpaTransactionProcessedRepository.java
│   │   │   └── JpaDepositsProcessedRepository.java
│   │   │
│   │   └── JpaImplements/         # Implementações dos Ports (Adapters)
│   │       ├── WalletRepositoryImpl.java
│   │       ├── PixKeyRepositoryImpl.java
│   │       ├── WalletLedgerRepositoryImpl.java
│   │       ├── OutboxRepositoryImpl.java
│   │       ├── TransactionProcessedRepositoryImpl.java
│   │       └── DepositsProcessedRepositoryImpl.java
│   │
│   ├── Kafka/                     # Integração com Apache Kafka
│   │   ├── Listeners/             # Consumers de eventos externos
│   │   │   ├── UserCreated/       # Listener + DTO do evento user-created
│   │   │   └── DepositCreated/    # Listener + DTO do evento deposit-created
│   │   └── Configs/               # Configurações do Kafka
│   │
│   ├── Schedulers/                # Jobs agendados
│   │   └── OutboxScheduler.java   # Polling do Outbox a cada 10s
│   │
│   ├── Configs/                   # Configurações gerais
│   │   └── JacksonConfig.java
│   │
│   ├── Exceptions/                # Exceções de infraestrutura
│   │   ├── Custom/
│   │   │   ├── FailedToSaveLedgeEntry.java
│   │   │   └── ErrorToSaveOutboxException.java
│   │   └── Handler/               # Exception Handlers globais
│   │
│   └── Audit/                     # Auditoria de infraestrutura
│       └── UserCreatedListenerAudit.java
│
└── utils/                         # 🛠️ Utilitários
    ├── KafkaTopics.java           # Constantes de tópicos Kafka
    └── ApplicationData.java       # Constantes da aplicação
```

### Diagrama de Dependências entre Camadas

```
┌─────────────────────────────────────────────────────────────┐
│              Controller (API REST) / Infra (Kafka)           │
│                    [Primary/Driving Adapters]                │
└──────────────────────────┬──────────────────────────────────┘
                           │ depende de
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application (Use Cases)                    │
│              Services, EventHandlers, DTOs                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ depende de
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Domain (Core Business)                    │
│     Models, Events, Repository Interfaces, Exceptions        │
└──────────────────────────▲──────────────────────────────────┘
                           │ implementa
┌──────────────────────────┴──────────────────────────────────┐
│              Infra/Repository/JpaImplements                  │
│                  [Secondary/Driven Adapters]                 │
└─────────────────────────────────────────────────────────────┘
```

### Clean Architecture + DDD

| Camada | Responsabilidade | Dependências |
|--------|------------------|--------------|
| **Domain** | Regras de negócio, entidades ricas, eventos de domínio, interfaces de repositório | Nenhuma (camada mais interna) |
| **Application** | Casos de uso, orquestração, serviços de aplicação | Domain |
| **Infrastructure** | Implementações técnicas (JPA, Kafka, Schedulers) | Domain, Application |
| **Controller** | Adaptadores primários (HTTP, mensageria) | Application |

---

## 🛠️ Stack Tecnológica

| Categoria | Tecnologia | Versão | Propósito |
|-----------|------------|--------|-----------|
| **Runtime** | Java | 21 | LTS com Virtual Threads support |
| **Framework** | Spring Boot | 3.5.7 | Base do microserviço |
| **Persistência** | Spring Data JPA | - | Abstração de repositórios |
| **Mensageria** | Apache Kafka | - | Comunicação assíncrona entre serviços |
| **Banco de Dados** | SQL Server | - | Persistência transacional |
| **Migrations** | Flyway | - | Versionamento de schema (11 migrations) |
| **Resiliência** | Resilience4j | 2.3.0 | Circuit Breaker, Retry |
| **AOP** | Spring AOP | - | Aspectos para resiliência |
| **Validação** | Jakarta Validation | - | Validação de entidades |
| **Build** | Maven | - | Gerenciamento de dependências |
| **Utilitários** | Lombok | - | Redução de boilerplate |
| **Logging** | Logback + Logstash Encoder | - | Logs estruturados em JSON |

---

## 🎯 Casos de Uso Detalhados

### 1️⃣ CreateWallet — Criação de Carteira

**Trigger:** Evento `user-created` recebido via Kafka

**Fluxo:**
```
┌──────────────┐     ┌─────────────────────┐     ┌──────────────┐
│ UserCreated  │────▶│ UserCreatedListener │────▶│ CreateWallet │
│   (Kafka)    │     │   (Infrastructure)  │     │  (Use Case)  │
└──────────────┘     └─────────────────────┘     └──────┬───────┘
                                                        │
         ┌──────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. Verifica se socialId (CPF) já existe                        │
│  2. Cria entidade Wallet com saldo inicial ZERO                 │
│  3. Cria PixKey vinculando CPF à Wallet                         │
│  4. Publica evento interno (Spring Event)                       │
│  5. EventHandler salva no Outbox (mesma transação)              │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌──────────────────┐     ┌─────────────────┐
│ WalletCreated OR │────▶│ OutboxScheduler │────▶ Kafka
│ WalletFailed     │     │  (polling 10s)  │
└──────────────────┘     └─────────────────┘
```

**Entrada (UserCreatedEvent):**
```java
{
  "userId": "UUID",           // ID da conta (será o accountId da wallet)
  "cpf": "string",            // Documento (será a chave PIX inicial)
  "type": "CPF",              // Tipo da chave PIX
  "accountType": "PF | PJ",   // Pessoa Física ou Jurídica
  "timestamp": "datetime"
}
```

**Saída (WalletCreatedEvent via Outbox):**
```java
{
  "userId": "UUID",
  "cpf": "string",
  "successful": true,
  "timestamp": "datetime"
}
```

**Validações:**
- `SocialIdAlreadyExistsException` — CPF/CNPJ já possui wallet cadastrada

---

### 2️⃣ InstantPayment — Transferência PIX

**Trigger:** Requisição HTTP `POST /wallets/instant-payment`

**Fluxo:**
```
┌──────────────┐     ┌──────────────────┐     ┌────────────────┐
│  HTTP POST   │────▶│ WalletController │────▶│ InstantPayment │
│   Request    │     │                  │     │   (Use Case)   │
└──────────────┘     └──────────────────┘     └───────┬────────┘
                                                      │
         ┌────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. Busca PixKey do sender pela chave                           │
│  2. Busca PixKey do receiver pela chave                         │
│  3. Salva transactionId em TransactionsProcessed (idempotência) │
│  4. Valida se sender ≠ receiver (SameUserException)             │
│  5. Chama TransferExecution com @Retry                          │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  TransferExecution (Nova Transação - REQUIRES_NEW):             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 1. Busca Wallet sender com Optimistic Lock                │  │
│  │ 2. Busca Wallet receiver com Optimistic Lock              │  │
│  │ 3. sender.debitAccount(amount) — valida saldo             │  │
│  │ 4. receiver.creditAccount(amount)                         │  │
│  │ 5. Registra 2 entradas no Ledger (débito + crédito)       │  │
│  │ 6. Persiste ambas as wallets                              │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│ InstantPaymentResponse (HTTP Response)   │
└──────────────────────────────────────────┘
```

**Entrada (TransactionDTO):**
```json
{
  "transactionId": "UUID",       // ID único da transação
  "senderKey": "string",         // Chave PIX do pagador
  "receiverKey": "string",       // Chave PIX do recebedor
  "senderAccountId": "UUID",     // ID da conta do pagador
  "receiverAccountId": "UUID",   // ID da conta do recebedor
  "amount": "decimal",           // Valor da transferência
  "status": "string",            // Status da transação
  "timestamp": "datetime"        // Data/hora da requisição
}
```

**Saída (InstantPaymentResponse):**
```json
// Sucesso
{
  "isSucessful": true,
  "alreadyProcessed": false,
  "senderAccountId": "UUID",
  "receiverAccountId": "UUID",
  "failedMessage": null
}

// Falha
{
  "isSucessful": false,
  "alreadyProcessed": false,
  "senderAccountId": "UUID",
  "receiverAccountId": "UUID",
  "failedMessage": "Insufficient funds in sender's wallet"
}

// Idempotência (já processado)
{
  "isSucessful": true,
  "alreadyProcessed": true,
  "senderAccountId": "UUID",
  "receiverAccountId": "UUID",
  "failedMessage": "Transaction has already been processed."
}
```

**Validações de Domínio:**
- `WalletNotFoundException` — Wallet do sender ou receiver não encontrada
- `SameUserException` — Sender e receiver são a mesma pessoa
- `InsufficientBalanceException` — Saldo insuficiente para débito
- `InvalidAmountException` — Valor deve ser positivo
- `TransactionAlreadyProcessed` — Transação já foi processada (idempotência)

**Mecanismos de Resiliência:**
- **Retry** com backoff exponencial em caso de `OptimisticLockingFailureException`
- **Transação isolada** (REQUIRES_NEW) para permitir rollback parcial

---

### 3️⃣ Deposit — Execução de Depósito

**Trigger:** Evento `deposit-created` recebido via Kafka

**Fluxo:**
```
┌───────────────┐     ┌────────────────────────┐     ┌─────────┐
│ DepositCreated│────▶│ DepositCreatedListener │────▶│ Deposit │
│    (Kafka)    │     │    (Infrastructure)    │     │(UseCase)│
└───────────────┘     └────────────────────────┘     └────┬────┘
                                                          │
         ┌────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. Salva depositId em DepositsProcessed (idempotência)         │
│  2. Chama TransferExecution.depositExecution()                  │
│     ┌────────────────────────────────────────────────────────┐  │
│     │ a. Busca Wallet do receiver                            │  │
│     │ b. receiver.creditAccount(amount)                      │  │
│     │ c. Registra entrada CREDIT no Ledger (tipo DEPOSIT)    │  │
│     │ d. Persiste wallet atualizada                          │  │
│     └────────────────────────────────────────────────────────┘  │
│  3. Publica evento interno (Spring Event)                       │
│  4. EventHandler salva DepositExecuted/Failed no Outbox         │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌────────────────────┐     ┌─────────────────┐
│ DepositExecuted OR │────▶│ OutboxScheduler │────▶ Kafka
│ DepositFailed      │     │  (polling 10s)  │
└────────────────────┘     └─────────────────┘
```

**Entrada (DepositCreated):**
```java
{
  "depositId": "UUID",      // ID único do depósito
  "receiverId": "UUID",     // accountId da wallet de destino
  "amount": "decimal"       // Valor do depósito
}
```

**Saída (DepositExecuted via Outbox):**
```java
{
  "depositId": "UUID",
  "receiverId": "UUID",
  "amount": "decimal",
  "successful": true,
  "timestamp": "datetime"
}
```

**Validações:**
- `WalletNotFoundException` — Wallet do receiver não encontrada
- `DepositAlreadyProcessed` — Depósito já foi processado (idempotência)
- `InvalidAmountException` — Valor deve ser positivo

---

## 💎 Modelo de Domínio (DDD)

### Aggregate Root: Wallet

A entidade `Wallet` é o **Aggregate Root** principal, encapsulando toda a lógica de negócio relacionada a operações financeiras (Rich Domain Model):

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `accountId` | UUID | Identificador único (mesmo ID do usuário) |
| `balance` | BigDecimal | Saldo atual (precisão: 18,2) |
| `accountType` | Enum | PF (Pessoa Física) ou PJ (Pessoa Jurídica) |
| `socialId` | String | CPF ou CNPJ |
| `isActive` | Boolean | Soft delete flag |
| `version` | Integer | Optimistic Locking |

**Métodos de domínio:**
- `debitAccount(amount)` — Debita valor com validação de saldo
- `creditAccount(amount)` — Credita valor com validação de amount > 0

### Entidades do Domínio

| Entidade | Tabela | Descrição |
|----------|--------|-----------|
| `PixKey` | `wallet_keys` | Chave PIX vinculada à wallet (CPF, email, telefone, etc.) |
| `WalletLedger` | `wallet_ledger` | Registro contábil em partida dobrada (DEBIT/CREDIT) |
| `Outbox` | `outbox` | Evento pendente de publicação (Transactional Outbox) |
| `TransactionsProcessed` | `transactions_processed` | Controle de idempotência para PIX |
| `DepositsProcessed` | `deposits_processed` | Controle de idempotência para depósitos |

### Exceções de Domínio

Todas as exceções de domínio herdam de `DomainException`, que inclui um `errorCode` para identificação programática:

| Exceção | Código | Cenário |
|---------|--------|---------|
| `InsufficientBalanceException` | `INSUFFICIENT_FUNDS` | Saldo insuficiente para débito |
| `InvalidAmountException` | `INVALID_AMOUNT` | Valor ≤ 0 |
| `WalletNotFoundException` | `SENDER_WALLET_NOT_FOUND` / `RECEIVER_WALLET_NOT_FOUND` | Wallet não existe ou inativa |
| `SameUserException` | `SAME_USER` | Tentativa de transferir para si mesmo |
| `SocialIdAlreadyExistsException` | `SOCIAL_ID_ALREADY_EXISTS` | CPF/CNPJ já cadastrado |
| `TransactionAlreadyProcessed` | `TRANSACTION_ALREADY_PROCESSED` | Idempotência de PIX |
| `DepositAlreadyProcessed` | `DEPOSIT_ALREADY_PROCESSED` | Idempotência de depósito |

---

## 📐 Padrões Arquiteturais Implementados

### 1. Repository Pattern (Ports & Adapters)

Implementa a **inversão de dependência** entre a camada de domínio e a infraestrutura de persistência, seguindo o padrão Ports & Adapters (Hexagonal Architecture):

```
Domain Layer                         Infrastructure Layer
┌─────────────────────┐              ┌─────────────────────────┐
│ Repository (Port)   │◄─implements──│ RepositoryImpl (Adapter)│
│  - WalletRepository │              │  - WalletRepositoryImpl │
│  - OutboxRepository │              │  - OutboxRepositoryImpl │
└─────────────────────┘              └───────────┬─────────────┘
                                                 │ usa
                                     ┌───────────▼─────────────┐
                                     │ JpaRepository (Spring)  │
                                     │  - JpaWalletRepository  │
                                     └─────────────────────────┘
```

**Benefícios:**
- **Testabilidade:** Fácil mock dos repositórios em testes unitários
- **Flexibilidade:** Permite trocar implementação (ex: JPA → MongoDB) sem afetar o domínio
- **Clean Architecture:** A camada de domínio não conhece JPA, Hibernate ou qualquer framework

### 2. Transactional Outbox Pattern

Garante **consistência eventual** entre o banco de dados e o Apache Kafka, evitando o problema de dual-write:

```
┌─────────────────────────────────────────────────────────────────┐
│                      MESMA TRANSAÇÃO                             │
│  ┌─────────────┐    ┌──────────────────────────────────────┐    │
│  │   Wallet    │    │              Outbox                   │    │
│  │  (UPDATE)   │    │  eventType: "WalletCreated"          │    │
│  │             │    │  payload: { userId, cpf, ... }       │    │
│  │             │    │  isSent: false                       │    │
│  └─────────────┘    └──────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    │ COMMIT
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│  OutboxScheduler (a cada 10 segundos)                           │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. SELECT TOP 20 FROM outbox WHERE is_sent = 0             │ │
│  │ 2. Para cada registro:                                      │ │
│  │    - kafkaTemplate.send(topic, payload).get()              │ │
│  │    - UPDATE outbox SET is_sent = true                      │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**Implementação:**
- `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` — Salva no outbox antes do commit
- `OutboxScheduler` com `@Scheduled(fixedDelay = 10000)` — Polling periódico
- `OutboxService.sendOutboxEvent()` com `@CircuitBreaker` e `@Retry` — Resiliência no envio

### 3. Idempotência via Tabelas de Controle

Previne reprocessamento de mensagens duplicadas do Kafka utilizando tabelas de controle (`TransactionsProcessed` e `DepositsProcessed`).

**Estratégia:** Ao receber uma mensagem, tenta salvar o ID na tabela de controle. Se a PK já existir (`DataIntegrityViolationException`), significa que já foi processada → lança exceção de idempotência.

### 4. Double-Entry Bookkeeping (Partida Dobrada)

Toda movimentação financeira gera **duas entradas no ledger**, garantindo rastreabilidade contábil:

```
Transferência de R$ 100,00 de Alice para Bob:

┌─────────────────────────────────────────────────────────────────┐
│  wallet_ledger                                                   │
├──────────────┬──────────┬──────────────┬────────┬───────────────┤
│ wallet_id    │ counter  │ amount       │ entry  │ tx_type       │
├──────────────┼──────────┼──────────────┼────────┼───────────────┤
│ Alice (UUID) │ Bob      │ 100.00       │ DEBIT  │ INSTANT_PAY   │
│ Bob (UUID)   │ Alice    │ 100.00       │ CREDIT │ INSTANT_PAY   │
└──────────────┴──────────┴──────────────┴────────┴───────────────┘

Depósito de R$ 50,00 na conta do Bob:

┌─────────────────────────────────────────────────────────────────┐
│  wallet_ledger                                                   │
├──────────────┬──────────┬──────────────┬────────┬───────────────┤
│ wallet_id    │ counter  │ amount       │ entry  │ tx_type       │
├──────────────┼──────────┼──────────────┼────────┼───────────────┤
│ Bob (UUID)   │ Bob      │ 50.00        │ CREDIT │ DEPOSIT       │
└──────────────┴──────────┴──────────────┴────────┴───────────────┘
```

### 5. Optimistic Locking

Controle de concorrência otimista na entidade `Wallet` usando `@Version`. Em caso de conflito (`OptimisticLockingFailureException`), a operação é retentada automaticamente pelo mecanismo de Retry.

### 6. Rich Domain Model (DDD)

A lógica de negócio está **encapsulada nas entidades**, não nos serviços. Exemplo: `wallet.debitAccount(amount)` valida saldo e lança exceção se insuficiente, em vez de ter essa lógica em um serviço externo.

**Elementos DDD implementados:**
- **Aggregate Root:** `Wallet` é o agregado principal
- **Entities:** `PixKey`, `WalletLedger`, `Outbox`
- **Domain Events:** `WalletCreatedEvent`, `DepositExecuted`, etc.
- **Domain Exceptions:** Exceções tipadas com código de erro

### 7. Domain Events

Os eventos de domínio representam **fatos que ocorreram** no sistema e estão localizados na camada de Domain (`WalletCreatedEvent`, `WalletCreationFailed`, `DepositExecuted`, `DepositFailed`).

**Fluxo:** `UseCase → publica Domain Event → EventHandler → salva no Outbox → Kafka`

### 8. Event-Driven Architecture com Spring Events

Desacoplamento interno via `ApplicationEventPublisher` e `@TransactionalEventListener`. Os handlers são executados na fase `BEFORE_COMMIT` para garantir atomicidade com a transação principal.

---

## 🔄 Fluxo de Eventos (Kafka)

### Eventos Consumidos

| Tópico | Consumer Group | Handler | Descrição |
|--------|----------------|---------|-----------|
| `user-created` | `wallet-service-group` | `UserCreatedListener` | Cria wallet para novo usuário |
| `deposit-created` | `wallet-service-group` | `DepositCreatedListener` | Executa depósito na wallet |

### Eventos Publicados (via Outbox)

| Tópico | Evento | Trigger |
|--------|--------|---------|
| `wallet-created` | `WalletCreatedEvent` | Wallet criada com sucesso |
| `wallet-creation-failed` | `WalletCreationFailed` | Falha na criação da wallet |
| `deposit-executed` | `DepositExecuted` | Depósito executado com sucesso |
| `deposit-failed` | `DepositFailed` | Falha na execução do depósito |

### Diagrama de Integração

```
                            ┌─────────────────┐
                            │  User Service   │
                            └────────┬────────┘
                                     │ user-created
                                     ▼
┌────────────────────────────────────────────────────────────────┐
│                         MS-WALLET                               │
│                                                                 │
│  ┌──────────────┐    ┌────────────┐    ┌──────────────────┐    │
│  │ Kafka        │───▶│ Use Cases  │───▶│ Outbox +         │    │
│  │ Listeners    │    │            │    │ Scheduler        │    │
│  └──────────────┘    └────────────┘    └────────┬─────────┘    │
│                                                  │              │
└──────────────────────────────────────────────────│──────────────┘
                                                   │
                    ┌──────────────────────────────┼───────────────┐
                    │                              │               │
                    ▼                              ▼               ▼
           wallet-created              deposit-executed    deposit-failed
           wallet-creation-failed
```

---

## 🌐 API REST

### POST /wallets/instant-payment

Processa uma transferência instantânea (PIX) entre duas carteiras.

**Headers:**
```
X-Correlation-Id: UUID  (obrigatório - rastreamento distribuído)
Content-Type: application/json
```

**Request Body (TransactionDTO):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "senderKey": "12345678901",
  "receiverKey": "98765432109",
  "senderAccountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "receiverAccountId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "amount": 150.00,
  "status": "PENDING",
  "timestamp": "2026-03-12T10:30:00"
}
```

**Response (200 OK - Sucesso):**
```json
{
  "isSucessful": true,
  "alreadyProcessed": false,
  "senderAccountId": "a1b2c3d4-...",
  "receiverAccountId": "e5f6g7h8-...",
  "failedMessage": null
}
```

**Response (200 OK - Falha de Negócio):**
```json
{
  "isSucessful": false,
  "alreadyProcessed": false,
  "senderAccountId": "a1b2c3d4-...",
  "receiverAccountId": null,
  "failedMessage": "Insufficient funds in sender's wallet"
}
```

**Response (200 OK - Idempotência):**
```json
{
  "isSucessful": true,
  "alreadyProcessed": true,
  "senderAccountId": "a1b2c3d4-...",
  "receiverAccountId": "e5f6g7h8-...",
  "failedMessage": "Transaction has already been processed."
}
```

---

## 🛡️ Estratégias de Resiliência

### Circuit Breaker (Outbox Scheduler)

Protege o sistema contra falhas em cascata no envio de eventos para o Kafka:

```yaml
resilience4j.circuitbreaker:
  instances:
    outboxScheduler:
      minimumNumberOfCalls: 10        # Mínimo de chamadas para avaliar
      failureRateThreshold: 50        # % de falhas para abrir o circuito
      slidingWindowSize: 30           # Janela de avaliação
      permittedNumberOfCallsInHalfOpenState: 3  # Chamadas em half-open
      waitDurationInOpenState: 15000  # Tempo em estado aberto (15s)
```

**Estados:**
- **CLOSED:** Operação normal
- **OPEN:** Circuito aberto, fallback ativado (marca outbox como failed)
- **HALF_OPEN:** Testando recuperação

### Retry com Exponential Backoff

Retentativas automáticas para operações de banco de dados e conflitos de concorrência:

```yaml
resilience4j.retry:
  instances:
    databaseRetry:
      maxAttempts: 3                           # Máximo de tentativas
      waitDuration: 2000                       # Espera inicial (2s)
      exponentialBackoffMultiplier: 2          # Multiplicador (2s, 4s, 8s)
      retryExceptions:
        - org.springframework.dao.DataAccessException
        - java.sql.SQLException
        - org.springframework.dao.OptimisticLockingFailureException
        - org.springframework.dao.TransientDataAccessException
      ignoreExceptions:                        # Não retentar para erros de negócio
        - WalletNotFoundException
        - InsufficientBalanceException
        - SameUserException
        - DomainException
        - DepositAlreadyProcessed
```

**Aplicação:**
- `TransferExecution.transferExecutionWithRetry()` — Transferências PIX
- `TransferExecution.depositExecution()` — Depósitos
- `OutboxService.createOutbox()` — Salvamento no outbox
- `OutboxService.sendOutboxEvent()` — Envio para Kafka

---

## 📊 Observabilidade

### Logs Estruturados (JSON)

Logs em formato JSON via Logback + Logstash Encoder para integração com ELK Stack. Cada log inclui: `@timestamp`, `level`, `message`, `applicationName`, `correlationId`, `className`, `methodName` e campos customizados.

### Correlation ID

Rastreamento distribuído entre serviços via MDC (Mapped Diagnostic Context). O `correlationId` é recebido via Kafka Header ou HTTP Header (`X-Correlation-Id`) e propagado para todos os eventos publicados.

### Rotação de Logs

Logs diários com retenção de 30 dias configurado via `TimeBasedRollingPolicy`.

---

## 🗄️ Banco de Dados

### Schema (Flyway Migrations)

O schema é versionado através de **12 migrations** Flyway:

| Versão | Descrição |
|--------|-----------|
| V1 | Criação da tabela `wallets` |
| V2 | Atualização do tipo da coluna `account_type` |
| V3 | Criação da tabela `wallet_keys` (PixKey) |
| V4 | Criação da tabela `transactions_processed` |
| V5 | Criação da tabela `wallet_ledger` com índices |
| V6 | Adição da coluna `version` em wallets |
| V7 | Criação da tabela `outbox` |
| V8 | Adição da coluna `social_id` em wallets |
| V9 | Adição de `transaction_type` em wallet_ledger |
| V10 | Criação da tabela `deposits_processed` |
| V11 | Adição de `correlation_id` em outbox |
| V12 | Adição de `processed_at` nas tabelas de idempotência |

### Índices Otimizados

Índices criados para otimizar queries frequentes em `wallet_ledger`: `wallet_id`, `counterparty_wallet_id`, `transaction_id` e `timestamp`.

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.8+
- SQL Server (ou container Docker)
- Apache Kafka (ou container Docker)

### Variáveis de Ambiente

```properties
# application.properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=wallet_db
spring.datasource.username=sa
spring.datasource.password=yourPassword

spring.kafka.bootstrap-servers=localhost:9092
```

### Build

```bash
./mvnw clean install
```

### Executar

```bash
./mvnw spring-boot:run
```

### Executar Testes

```bash
./mvnw test
```

---


## 👤 Autor

**Matheus Maia Goulart**

---

## 📄 Licença

Este projeto está sob a licença MIT.
