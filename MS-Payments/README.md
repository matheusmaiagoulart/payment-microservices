# 💳 MS-Payments — Payment Gateway Microservice

> **Revisão Técnica (Tech Lead Review)**
> Análise completa de ponta a ponta: arquitetura, padrões, stacks, escalabilidade, POO e contexto do projeto.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Contexto do Projeto](#-contexto-do-projeto)
- [Stacks & Tecnologias](#-stacks--tecnologias)
- [Arquitetura](#-arquitetura)
- [Padrões de Projeto Utilizados](#-padrões-de-projeto-utilizados)
- [Estrutura de Pastas](#-estrutura-de-pastas)
- [Fluxo de Pagamento (Pix)](#-fluxo-de-pagamento-pix)
- [Fluxo de Depósito](#-fluxo-de-depósito)
- [Comunicação entre Serviços](#-comunicação-entre-serviços)
- [Observabilidade & Auditoria](#-observabilidade--auditoria)
- [Banco de Dados & Migrações](#-banco-de-dados--migrações)
- [Segurança (mTLS)](#-segurança-mtls)
- [Análise de POO & SOLID](#-análise-de-poo--solid)
- [Escalabilidade](#-escalabilidade)
- [Pontos Positivos](#-pontos-positivos)
- [Correções Aplicadas (v2)](#-correções-aplicadas-v2)
- [Pontos de Melhoria Restantes](#-pontos-de-melhoria-restantes)
- [Nota Final & Roadmap](#-nota-final)

---

## 🎯 Visão Geral

O **MS-Payments** é um microsserviço responsável por atuar como **porta de entrada** (Payment Gateway) para operações financeiras dentro de um ecossistema de pagamentos. Ele recebe requisições de pagamento instantâneo (Pix) e depósitos, orquestra o processamento delegando ao serviço de Wallet (MS-Wallet), e garante a rastreabilidade e consistência de cada transação.

---

## 🌐 Contexto do Projeto

```
                    ┌─────────────────────┐
                    │      Cliente        │
                    └────────┬────────────┘
                             │ HTTPS (mTLS)
                             ▼
                  ┌──────────────────────┐
                  │    MS-Payments       │ ◀── ESTE SERVIÇO
                  │  (Payment Gateway)   │
                  └────┬───────────┬─────┘
                       │           │
            HTTPS/mTLS │           │ Kafka (Async)
            (Síncrono) │           │
                       ▼           ▼
              ┌──────────────┐  ┌──────────────┐
              │  MS-Wallet   │  │    Kafka      │
              │ (Processing) │  │   Broker      │
              └──────────────┘  └──────────────┘
```

### Responsabilidades

| Operação | Tipo | Destino | Descrição |
|----------|------|---------|-----------|
| **Pix (Instant Payment)** | Síncrono (HTTPS) | MS-Wallet | Orquestra pagamento instantâneo com chamada direta |
| **Depósito (Cash Deposit)** | Assíncrono (Kafka) | MS-Wallet | Registra depósito e publica evento via Outbox Pattern |
| **Extrato (Statement)** | Local (DB) | — | Consulta transações locais por accountId |

---

## 🛠 Stacks & Tecnologias

| Categoria | Tecnologia | Versão |
|-----------|-----------|--------|
| **Linguagem** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 3.5.7 |
| **Build** | Maven | — |
| **Banco de Dados** | SQL Server | — |
| **Migrações** | Flyway | — |
| **ORM** | Spring Data JPA / Hibernate | — |
| **Mensageria** | Apache Kafka | — |
| **Serialização** | Jackson (ObjectMapper) | — |
| **Validação** | Jakarta Bean Validation | — |
| **Logging Estruturado** | Logback + Logstash Encoder | 7.4 |
| **Resiliência** | Spring Retry | — |
| **Scheduling** | Spring Scheduling | — |
| **Segurança** | mTLS (PKCS12 + JKS Truststore) | — |
| **Boilerplate** | Lombok | — |
| **Biblioteca Compartilhada** | `com.matheus.shared` | 1.1.7 |
| **AOP** | Spring AOP | — |

---

## 🏗 Arquitetura

O projeto implementa **Clean Architecture** com influências de **DDD (Domain-Driven Design)**, respeitando a **Regra de Dependência** onde camadas externas dependem das internas, nunca o contrário.

```
┌─────────────────────────────────────────────────────────────┐
│  Controller (Presentation Layer)                            │
│  └── TransactionController.java                             │
├─────────────────────────────────────────────────────────────┤
│  Application Layer                                          │
│  ├── UseCases/    → Orquestradores de workflow              │
│  ├── Services/    → Lógica de aplicação encapsulada         │
│  ├── DTOs/        → Objetos de entrada/saída                │
│  ├── Mappers/     → Conversão entre camadas                 │
│  ├── EventHandlers/ → Listeners de eventos internos         │
│  └── Audit/       → Logging estruturado e Correlation ID    │
├─────────────────────────────────────────────────────────────┤
│  Domain Layer (Núcleo)                                      │
│  ├── Models/       → Entidades de domínio (Transaction,     │
│  │                   Deposit, TransactionOutbox)             │
│  ├── Repositories/ → Interfaces (contratos de persistência) │
│  ├── Exceptions/   → Exceções de negócio com error codes    │
│  └── Events/       → Eventos de domínio                     │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                       │
│  ├── Repository/JpaInterfaces/ → Interfaces Spring Data JPA │
│  ├── Repository/JpaImplements/ → Adapters (Repository Pat.) │
│  ├── Http/          → Cliente HTTP para MS-Wallet (mTLS)    │
│  ├── Kafka/         → Configuração Producer/Consumer        │
│  ├── Schedulers/    → Jobs agendados (Outbox Polling)       │
│  └── Exceptions/    → Handlers globais + Exceções técnicas  │
└─────────────────────────────────────────────────────────────┘
```

### Regra de Dependência

```
Controller ──→ Application ──→ Domain ◀── Infrastructure
                                 ▲              │
                                 └──────────────┘
                          (Infra implementa interfaces do Domain)
```

- ✅ **Domain** não depende de nenhuma outra camada
- ✅ **Application** depende apenas de **Domain**
- ✅ **Infrastructure** implementa contratos definidos em **Domain**
- ✅ **Controller** depende apenas de **Application**

---

## 🎨 Padrões de Projeto Utilizados

### 1. **Transactional Outbox Pattern**
Garante consistência eventual entre persistência e publicação de eventos Kafka. Toda mensagem é salva na tabela `transaction_outbox` antes de ser enviada, evitando perda de eventos em caso de falha.

```
[Operação] → [Salva Outbox no DB] → [Scheduler Polling] → [Kafka Publish]
```

- **Classe**: `OutboxService`, `OutboxScheduler`, `TransactionOutbox`
- **Scheduler**: Polling a cada 10 segundos (`@Scheduled(fixedDelay = 10000)`)

### 2. **Repository Pattern (com Adapter)**
Três camadas de abstração para persistência:

```
Domain/Repositories/TransactionRepository.java       → Interface de negócio (sem JPA)
Infra/JpaInterfaces/JpaTransactionRepository.java    → Interface Spring Data JPA
Infra/JpaImplements/TransactionRepositoryImpl.java   → Adapter que conecta ambos
```

### 3. **Facade Pattern (Use Cases)**
Use Cases atuam como fachadas que orquestram múltiplos Services:

- **`InstantPayment`**: Orquestra `TransactionService` → `OutboxService` → `PaymentProcessorService`
- **`CashDeposit`**: Delega para `DepositService`

### 4. **Domain Events**
Eventos internos publicados via `ApplicationEventPublisher` do Spring:

```
DepositService.saveDeposit()
    → publishEvent(DepositCreatedEvent)
        → DepositCreatedInternalEventHandler (AFTER_COMMIT)
            → OutboxService.createOutboxEntry() (REQUIRES_NEW)
```

### 5. **Correlation ID Pattern**
Rastreabilidade de requisições de ponta a ponta via `MDC` (Mapped Diagnostic Context):

- Gerado automaticamente por `CorrelationFilter` (Servlet Filter)
- Propagado via header `X-Correlation-Id` na chamada HTTP para MS-Wallet
- Armazenado na tabela `transaction_outbox` para rastreio assíncrono

### 6. **Retry Pattern**
Resiliência na comunicação com MS-Wallet:

```java
@Retryable(retryFor = {IOException.class, InterruptedException.class}, 
           maxAttempts = 4, 
           backoff = @Backoff(delay = 1000, multiplier = 1.5))
```

### 7. **Strategy Pattern (Mappers)**
Interface `TransactionMapper` com implementação `TransactionMapperImpl`, permitindo troca de estratégia de mapeamento sem alterar Services.

---

## 📂 Estrutura de Pastas

```
src/main/java/com/matheus/payments/
│
├── PaymentsServiceApplication.java          # Entry point
│
├── Controller/
│   └── TransactionController.java           # REST API (3 endpoints)
│
├── Application/
│   ├── UseCases/
│   │   ├── InstantPayment.java              # Orquestrador de Pix
│   │   └── CashDeposit.java                 # Orquestrador de Depósito
│   ├── Services/
│   │   ├── TransactionService.java          # CRUD de transações
│   │   ├── DepositService.java              # Persistência + evento de depósito
│   │   ├── OutboxService.java               # Gerenciamento do Outbox
│   │   ├── PaymentProcessorService.java     # Comunicação com MS-Wallet
│   │   └── StatementService.java            # Consulta de extratos
│   ├── DTOs/
│   │   ├── TransactionRequest.java          # Input de pagamento Pix
│   │   └── DepositRequest.java              # Input de depósito
│   ├── Mappers/
│   │   ├── TransactionMapper.java           # Interface de mapeamento
│   │   └── TransactionMapperImpl.java       # Implementação
│   ├── EventHandlers/
│   │   └── DepositCreatedInternalEventHandler.java  # Listener AFTER_COMMIT
│   └── Audit/
│       ├── CorrelationFilter.java           # Servlet Filter (gera Correlation ID)
│       ├── CorrelationId.java               # ThreadLocal via MDC
│       ├── InstantPaymentFacadeAudit.java   # Logs do Use Case
│       ├── TransactionServiceAudit.java     # Logs do TransactionService
│       ├── OutboxServiceAudit.java          # Logs do OutboxService
│       └── PaymentProcessorAudit.java       # Logs da comunicação com Wallet
│
├── Domain/
│   ├── Models/
│   │   ├── Transaction.java                # Entidade de transação
│   │   ├── Deposit.java                    # Entidade de depósito
│   │   ├── TransactionOutbox.java          # Entidade do Outbox
│   │   └── TransactionStatus.java          # Enum (PENDING, COMPLETED, FAILED)
│   ├── Repositories/
│   │   ├── TransactionRepository.java      # Interface (sem JPA)
│   │   ├── DepositRepository.java          # Interface (sem JPA)
│   │   └── OutboxRepository.java           # Interface (sem JPA)
│   ├── Events/
│   │   └── DepositCreatedEvent.java        # Evento de domínio
│   └── Exceptions/
│       ├── DomainException.java            # Base exception com error code
│       ├── InvalidAmountException.java     # Valor inválido
│       ├── TransactionFailedException.java # Falha no processamento
│       └── TransactionNotFound.java        # Transação não encontrada
│
├── Infra/
│   ├── Repository/
│   │   ├── JpaInterfaces/
│   │   │   ├── JpaTransactionRepository.java    # extends JpaRepository
│   │   │   ├── JpaDepositRepository.java        # extends JpaRepository
│   │   │   └── JpaOutboxRepository.java         # extends JpaRepository
│   │   └── JpaImplements/
│   │       ├── TransactionRepositoryImpl.java   # Adapter
│   │       ├── DepositRepositoryImpl.java       # Adapter
│   │       └── OutboxRepositoryImpl.java        # Adapter
│   ├── Http/
│   │   └── WalletService.java              # Cliente HTTPS com mTLS
│   ├── Kafka/
│   │   ├── Configs/
│   │   │   ├── Producer.java               # Configuração Kafka Producer
│   │   │   └── Consumer.java               # Configuração Kafka Consumer
│   │   └── Listeners/                      # Listeners de eventos externos
│   ├── Schedulers/
│   │   └── OutboxScheduler.java            # Polling do Outbox Pattern
│   └── Exceptions/
│       ├── HandlerMessage.java             # DTO padronizado de erro
│       ├── Custom/
│       │   ├── CreatePaymentProcessException.java
│       │   ├── DataBaseException.java
│       │   ├── FailedToSentException.java
│       │   └── TransactionAlreadySentException.java
│       └── Handler/
│           ├── DataBaseExceptionHandler.java       # @RestControllerAdvice
│           ├── TransactionExceptionHandler.java    # @RestControllerAdvice
│           └── ValidationEntryHandler.java         # Bean Validation errors
│
└── Utils/
    ├── ApplicationData.java                # Constantes do serviço
    └── KafkaTopics.java                    # Constantes de tópicos Kafka
```

---

## 🔄 Fluxo de Pagamento (Pix)

```
POST /transaction/pix
         │
         ▼
┌─ TransactionController ─┐
│  paymentService          │
│  .paymentOrchestration() │
└────────┬─────────────────┘
         │
         ▼
┌─ InstantPayment (Use Case / Facade) ────────────────────────────┐
│                                                                   │
│  1. transactionService.createPaymentProcess(request)             │
│     └─ Cria Transaction (status: PENDING) → salva no DB          │
│                                                                   │
│  2. outboxService.createOutboxEntry(transactionId, payload)      │
│     └─ Cria registro Outbox → salva no DB (REQUIRES_NEW)         │
│                                                                   │
│  3. audit.logPaymentProcessStarting(transactionId)               │
│     └─ Log estruturado JSON com correlationId                     │
│                                                                   │
│  4. paymentProcessorService.sendPaymentToProcessor(transactionId)│
│     ├─ Busca Outbox → valida não enviado                          │
│     ├─ Envia HTTPS (mTLS) → MS-Wallet /wallets/instant-payment   │
│     │   └─ @Retryable (4 tentativas, backoff 1.5x)               │
│     └─ Marca Outbox como sent=true                                │
│                                                                   │
│  5. paymentProcessorService.paymentStatusUpdate(response)        │
│     ├─ SUCCESS → Transaction.status = COMPLETED                   │
│     ├─ FAILED  → Transaction.status = FAILED + Outbox.failed     │
│     └─ ALREADY_PROCESSED → Retorna resposta idempotente           │
│                                                                   │
│  return PaymentProcessorResponse                                  │
└───────────────────────────────────────────────────────────────────┘
```

**Tipo de comunicação**: **Síncrona (Request-Response)** via HTTPS com mTLS.  
O MS-Payments aguarda a resposta do MS-Wallet para atualizar o status da transação e devolver ao cliente.

---

## 💰 Fluxo de Depósito

```
POST /transaction/deposit
         │
         ▼
┌─ TransactionController ─┐
│  cashDeposit.execute()   │
└────────┬─────────────────┘
         │
         ▼
┌─ CashDeposit (Use Case) ────────────────────────────────────────┐
│                                                                   │
│  1. depositService.saveDeposit(deposit)                          │
│     ├─ Valida amount > 0 (InvalidAmountException)                │
│     ├─ Persiste Deposit (status: PENDING)                        │
│     └─ publishEvent(DepositCreatedEvent)                         │
│                                                                   │
│  2. [AFTER_COMMIT] DepositCreatedInternalEventHandler            │
│     └─ outboxService.createOutboxEntry(...)                       │
│        └─ Salva na tabela transaction_outbox (REQUIRES_NEW)       │
│                                                                   │
│  3. [SCHEDULER - cada 10s] OutboxScheduler                       │
│     └─ Busca outbox pendentes → Publica no Kafka                  │
│        └─ Tópico: "deposit-created"                               │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

**Tipo de comunicação**: **Assíncrona (Event-Driven)** via Kafka.  
O MS-Payments registra o depósito e publica um evento. O MS-Wallet consome o evento e processa o depósito.

---

## 🔌 Comunicação entre Serviços

| Operação | Protocolo | Padrão | Garantia | Detalhes |
|----------|-----------|--------|----------|----------|
| **Pix** | HTTPS (mTLS) | Request-Response | Síncrono + Retry (4x) | Chamada direta com backoff exponencial |
| **Depósito** | Kafka | Event-Driven | Outbox Pattern + Polling | Consistência eventual com at-least-once delivery |

### Kafka Producer

- `acks=all` — Garante que todos os brokers confirmem a escrita
- `enable.idempotence=true` — Exactly-once semantics no producer
- `max.in.flight.requests.per.connection=1` — Ordenação garantida
- `retries=3` — Retentativas automáticas

### Kafka Consumer

- `auto-offset-reset=earliest` — Lê desde o início em caso de novo consumer group
- `enable-auto-commit=false` — ACK manual após processamento
- Error handler com `FixedBackOff(5000ms, 3 tentativas)`

---

## 🔍 Observabilidade & Auditoria

### Logging Estruturado (JSON)

Cada operação gera logs JSON com campos padronizados via `LogBuilder` (shared library):

```json
{
  "@timestamp": "2026-03-01T14:30:00.000Z",
  "level": "INFO",
  "message": "Sending payment to Wallet Server",
  "service_name": "MS-InstantPayment",
  "correlation_id": "550e8400-e29b-41d4-a716-446655440000",
  "class_name": "PaymentProcessorService",
  "method_name": "sendPaymentToProcessor",
  "event": "payment.request.sending",
  "transactionId": "a1b2c3d4-...",
  "http_method": "POST",
  "endpoint": "/wallets/instant-payment",
  "target_service": "MS-Wallet"
}
```

### Correlation ID

- Gerado via **Servlet Filter** (`CorrelationFilter`) em toda requisição HTTP
- Armazenado no **MDC** (SLF4J) para propagação automática nos logs
- Propagado via header `X-Correlation-Id` para MS-Wallet
- Persistido na tabela `transaction_outbox` para rastreio assíncrono

### Classes de Audit por Serviço

| Audit Class | Serviço Monitorado |
|-------------|-------------------|
| `InstantPaymentFacadeAudit` | Use Case de pagamento Pix |
| `TransactionServiceAudit` | Criação de transações |
| `OutboxServiceAudit` | Operações no Outbox |
| `PaymentProcessorAudit` | Comunicação com MS-Wallet |

### Rolling File Appender

- Logs em arquivo JSON com rotação diária
- Retenção de 30 dias (`maxHistory=30`)
- Stack traces inclusos em logs de erro

---

## 🗄 Banco de Dados & Migrações

**SGBD**: SQL Server (via JDBC)  
**Migrações**: Flyway (versionadas)

### Tabelas

| Tabela | Descrição | Índices |
|--------|-----------|---------|
| `deposits` | Depósitos (PENDING → CONFIRMED) | sender_id, receiver_id, status, payed_at |
| `transactions` | Transações Pix (PENDING → COMPLETED/FAILED) | sender/receiver account_id, status, timestamp, sender/receiver key |
| `transaction_outbox` | Outbox Pattern (mensagens pendentes) | sent, failed, created_at, topic |

### Versionamento (Flyway)

```
V1 → CREATE TABLE deposits
V2 → CREATE TABLE transactions
V3 → CREATE TABLE transaction_outbox
V4 → ALTER TABLE transaction_outbox ADD correlation_id
```

---

## 🔒 Segurança (mTLS)

O serviço implementa **mutual TLS** (mTLS) para comunicação segura com o MS-Wallet:

| Componente | Tipo | Descrição |
|-----------|------|-----------|
| `keystore.p12` | PKCS12 | Certificado + chave privada do MS-Payments |
| `truststore.jks` | JKS | Certificado público do MS-Wallet (trusted) |
| `wallet-cert.crt` | X.509 | Certificado do MS-Wallet importado no truststore |

- **Server SSL**: MS-Payments expõe HTTPS (porta padrão)
- **Client SSL**: `WalletService` configura `SSLContext` com truststore para confiar no certificado do MS-Wallet
- **Comunicação bidirecional autenticada**: Ambos os serviços verificam a identidade um do outro

---

## 🏛 Análise de POO & SOLID

### ✅ Single Responsibility Principle (SRP)

| Classe | Responsabilidade Única |
|--------|----------------------|
| `TransactionService` | Gerenciamento de transações |
| `DepositService` | Persistência e evento de depósitos |
| `OutboxService` | Gerenciamento do Outbox Pattern |
| `PaymentProcessorService` | Comunicação com MS-Wallet |
| `StatementService` | Consulta de extratos |
| `TransactionServiceAudit` | Logging da camada de transação |

### ✅ Open/Closed Principle (OCP)
- Novos tipos de pagamento podem ser adicionados criando novos **Use Cases** sem alterar os existentes
- Novos `@RestControllerAdvice` podem ser adicionados sem alterar os handlers existentes

### ✅ Liskov Substitution Principle (LSP)
- `TransactionRepositoryImpl` pode substituir `TransactionRepository` sem quebrar comportamento
- Exceções de domínio estendem `DomainException` mantendo contrato

### ✅ Interface Segregation Principle (ISP)
- `TransactionRepository` expõe apenas métodos necessários (save, findById, findByAccounts)
- `DepositRepository` tem apenas `saveDeposit()` — interface enxuta
- `TransactionMapper` define apenas `mapToDTO()` e `mapToEntity()`

### ✅ Dependency Inversion Principle (DIP)
- Services dependem de **interfaces** do Domain (`TransactionRepository`, `OutboxRepository`, `DepositRepository`), não de implementações concretas
- Construtores recebem interfaces, não `Impl` — inversão de dependência correta em toda a camada Application
- Use Cases dependem de abstrações (`TransactionMapper`), não de classes concretas

### Encapsulamento

- Entidades de domínio com **comportamento rico**:
  - `Transaction.setTransactionCompleted()` — muda status + atribui accounts
  - `Transaction.setTransactionFailed()` — muda status
  - `Deposit.normalizeAmount()` — valida e normaliza valor
  - `Deposit.confirmDeposit()` — muda status + registra timestamp
  - `TransactionOutbox.failedTransaction()` — marca falha com reason

### Herança de Exceções

```
RuntimeException
  └── DomainException (errorCode + message)
        ├── InvalidAmountException
        ├── TransactionFailedException
        └── TransactionNotFound
```

---

## 📈 Escalabilidade

### ✅ Pontos Fortes

| Aspecto | Implementação |
|---------|--------------|
| **Comunicação Assíncrona** | Kafka para depósitos (desacoplamento) |
| **Outbox Pattern** | Consistência sem Two-Phase Commit |
| **Idempotência** | Producer Kafka com `enable.idempotence=true` |
| **Retry com Backoff** | `@Retryable` com backoff exponencial (1.5x) |
| **ACK Manual** | Consumer Kafka com commit manual |
| **Database Indexing** | Índices em todas as colunas de consulta |
| **Stateless** | Correlation ID via MDC (thread-local) |

### ⚠️ Limitações Atuais

| Aspecto | Limitação | Sugestão |
|---------|-----------|----------|
| **Kafka Partitions** | 1 partição por tópico | Aumentar para N partições para paralelismo |
| **Kafka Replicas** | 1 réplica | Mínimo 3 para tolerância a falhas em produção |
| **DB Connection Pool** | Não configurado explicitamente | Configurar HikariCP com pool sizing |
| **Outbox Polling** | Intervalo fixo de 10s | Considerar CDC (Change Data Capture) com Debezium |
| **URL Hardcoded** | `https://localhost:8081` no WalletService | Usar Service Discovery (Eureka/Consul) ou config externalizada |

---

## ✅ Pontos Positivos

### Arquitetura & Design
1. **Clean Architecture bem implementada** — Regra de Dependência respeitada em todas as camadas
2. **Repository Pattern com Adapter** — Domain isolado de JPA, permitindo troca de tecnologia sem impacto
3. **Outbox Pattern** — Solução robusta para consistência eventual em arquitetura distribuída
4. **Use Cases como Facades** — Orquestração clara e bem delimitada do workflow de pagamento
5. **Domain Events com @TransactionalEventListener(AFTER_COMMIT)** — Eventos disparados somente após commit, com propagação transacional correta (`REQUIRES_NEW`)

### Observabilidade
6. **Logging Estruturado JSON** — Pronto para ingestão em Elastic/Loki/Splunk
7. **Correlation ID end-to-end** — Rastreabilidade completa da requisição
8. **Classes de Audit separadas por contexto** — SRP aplicado ao logging
9. **Rolling File com retenção de 30 dias** — Gestão de logs em produção

### Segurança
10. **mTLS** — Comunicação entre microsserviços autenticada mutuamente
11. **Validação de entrada** — `@Valid` + Bean Validation nos DTOs

### Resiliência
12. **Spring Retry com Backoff Exponencial** — Tolerância a falhas transientes na comunicação com MS-Wallet
13. **Kafka Producer Idempotente** — `acks=all` + `enable.idempotence=true`
14. **Kafka Consumer com ACK Manual** — Processamento garantido antes do commit de offset
15. **Error Handler no Consumer** — `FixedBackOff` com 3 retentativas

### Código
16. **Entidades com comportamento rico** — Métodos de domínio encapsulados (`setTransactionCompleted`, `normalizeAmount`)
17. **Exception hierarchy organizada** — `DomainException` com error codes padronizados
18. **Global Exception Handlers** — `@RestControllerAdvice` para tratamento centralizado
19. **Flyway Migrations** — Schema versionado e reproduzível
20. **Shared Library** — Reutilização de DTOs e LogBuilder entre microsserviços

---

## ✅ Correções Aplicadas (v2)

> Correções realizadas após a primeira revisão técnica.

### ~~1. Vazamento de Abstração — `TransactionService` e `OutboxService`~~ ✅ CORRIGIDO
Construtores agora recebem as **interfaces do Domain** em vez das implementações concretas. Inversão de dependência aplicada corretamente em todos os Services.

### ~~2. `OutboxScheduler` acessa JPA diretamente~~ ✅ CORRIGIDO
Scheduler agora depende da interface `OutboxRepository` (Domain) em vez de `JpaOutboxRepository`.

### ~~3. `CorrelationFilter` usa `System.out.println`~~ ✅ CORRIGIDO
Log de debug removido. CorrelationFilter agora opera silenciosamente, gerando e limpando o Correlation ID via MDC.

---

## ⚠️ Pontos de Melhoria Restantes

### 🟡 Importantes

#### 1. `OutboxService` contém lógica de envio Kafka
```java
// OutboxService.java
private final KafkaTemplate<String, String> kafkaTemplate; // Infra concern
public void sendOutboxEntry(TransactionOutbox outbox) {
    kafkaTemplate.send(message); // ❌ Deveria estar em Infra
}
```
**Solução planejada**: Criar interface `MessagePublisher` em Application (Port) e implementar `KafkaMessagePublisher` em Infra, removendo o acoplamento direto com `KafkaTemplate` na camada de Application.

```
Application/
  Ports/
    MessagePublisher.java           → Interface (o que fazer)

Infra/
  Kafka/
    KafkaMessagePublisher.java      → Implementação (como fazer com KafkaTemplate)
```

#### 2. `PaymentProcessorService` em Application depende de `Infra`
```java
import com.matheus.payments.Infra.Exceptions.Custom.FailedToSentException;      // ❌
import com.matheus.payments.Infra.Exceptions.Custom.TransactionAlreadySentException; // ❌
import com.matheus.payments.Infra.Http.WalletService;                            // ❌
```
**Sugestão**: Criar interfaces/ports em Application para abstrair a comunicação HTTP e mover exceções técnicas para o pacote adequado.

#### 3. Convenção de Pacotes Java
```
Application/  → application/   (lowercase por convenção Java)
Domain/       → domain/
Infra/        → infrastructure/
Controller/   → controller/
Utils/        → utils/
```

#### 4. `TransactionNotFound` importa classe desnecessária
```java
import org.apache.kafka.common.protocol.types.Field; // ❌ Import não utilizado
```

### 🔵 Em Desenvolvimento (Roadmap)

#### 5. Testes Unitários 🔜
Testes unitários para cada Service e Use Case (com mocks) estão em desenvolvimento pelo autor.

#### 6. Testes de Integração (Circuit Breaker) 🔜
Testes de integração para validar o comportamento de Circuit Breaker na comunicação com MS-Wallet serão implementados após a adição do padrão.

#### 7. Kafka Listeners para Depósito 🔜
Listeners para consumir eventos de resposta do MS-Wallet sobre o status do depósito (sucesso/falha), permitindo atualizar o `Deposit.status` localmente:

```
Infra/
  Kafka/
    Listeners/
      DepositExecutedListener.java    → Tópico: "deposit-executed"
      DepositFailedListener.java      → Tópico: "deposit-failed"
```

Fluxo planejado:
```
[MS-Wallet] processa depósito
    ↓ publica evento
[Kafka] "deposit-executed" ou "deposit-failed"
    ↓ consumido por
[MS-Payments/Listeners] → atualiza Deposit.status (CONFIRMED / FAILED)
```

#### 8. Circuit Breaker 🔜
Implementação de Circuit Breaker (Resilience4j) para proteger a comunicação síncrona com MS-Wallet, complementando o Spring Retry já existente.

#### 9. Docker & CI/CD
Não há `Dockerfile`, `docker-compose.yml` ou pipelines de CI/CD. Para um ambiente de microsserviços, recomenda-se:
- `Dockerfile` multi-stage para build otimizado
- `docker-compose.yml` com Kafka, SQL Server e serviços dependentes
- Pipeline de CI com build, test e análise estática

#### 10. Health Check & Metrics
Considerar adicionar:
- Spring Boot Actuator para health checks
- Micrometer + Prometheus para métricas
- Endpoints `/actuator/health` e `/actuator/prometheus`

#### 11. API Documentation
Considerar adicionar Swagger/OpenAPI (`springdoc-openapi`) para documentação automática da API.

---

## 🎯 Nota Final

| Critério | Nota | Comentário |
|----------|------|------------|
| **Arquitetura (Clean Architecture)** | 9.0/10 | Regra de Dependência respeitada, Repository Pattern com Adapter, pequeno acoplamento restante no `OutboxService` (Kafka) e `PaymentProcessorService` (Infra) |
| **Padrões de Projeto** | 9.5/10 | Outbox, Repository, Facade, Domain Events, Retry, Correlation ID — excelente variedade |
| **POO & SOLID** | 9.0/10 | DIP corrigido em todos os construtores, interfaces bem definidas, entidades ricas |
| **Segurança** | 9.0/10 | mTLS bem implementado, validações de entrada presentes |
| **Observabilidade** | 9.5/10 | Logging estruturado JSON + Correlation ID end-to-end + Audit classes por contexto |
| **Resiliência** | 9.0/10 | Retry, Outbox, idempotência Kafka, ACK manual. Circuit Breaker planejado |
| **Escalabilidade** | 7.5/10 | Bom design, mas partições Kafka e pool de conexões precisam ajuste para produção |
| **Qualidade de Código** | 8.5/10 | Bem organizado, Javadoc presente, testes unitários em desenvolvimento |
| **DevOps & Infraestrutura** | 5.0/10 | Sem Docker, CI/CD ou health checks |

### **Nota Geral: 8.7/10** ⭐

> O MS-Payments demonstra **maturidade arquitetural** significativa para um serviço de pagamentos. A implementação de Clean Architecture com Repository Pattern, Outbox Pattern, mTLS, logging estruturado com Correlation ID e resiliência com Spring Retry mostra domínio de padrões enterprise. Após as correções de inversão de dependência nos construtores e no Scheduler, o projeto alcançou conformidade sólida com Clean Architecture. Os próximos passos incluem a abstração do KafkaTemplate via Port/Adapter, implementação de Circuit Breaker, Kafka Listeners para feedback de depósitos, testes unitários e de integração.

---

### 🗺️ Roadmap

| Feature | Status | Responsável |
|---------|--------|-------------|
| Correção DIP (construtores) | ✅ Concluído | Autor |
| Correção OutboxScheduler | ✅ Concluído | Autor |
| Correção CorrelationFilter | ✅ Concluído | Autor |
| MessagePublisher Port/Adapter (Kafka) | 🔜 Planejado | Autor |
| Testes Unitários | 🔜 Em desenvolvimento | Autor |
| Kafka Listeners (Deposit feedback) | 🔜 Planejado | Autor |
| Circuit Breaker (Resilience4j) | 🔜 Planejado | Autor |
| Testes de Integração (Circuit Breaker) | 🔜 Planejado | Copilot |

---

**Autor da Revisão**: Tech Lead Review  
**Data**: Março 2026  
**Última Atualização**: 04/03/2026 (v2 — Correções aplicadas)  
**Projeto**: MS-Payments — Payment Gateway Microservice





