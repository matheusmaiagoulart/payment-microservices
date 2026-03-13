# 💳 MS-Payments

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Clean-blue.svg)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Microsserviço de processamento de pagamentos instantâneos (PIX) e depósitos**

[Funcionalidades](#-funcionalidades) • [Arquitetura](#️-arquitetura) • [Padrões](#-padrões-de-design) • [API](#-api) • [Executar](#-como-executar)

</div>

---

## 📋 Índice

- [O que é](#-o-que-é)
- [Funcionalidades](#-funcionalidades)
- [Problema que Resolve](#-problema-que-resolve)
- [Fluxos de Dados](#-fluxos-de-dados)
- [Arquitetura](#️-arquitetura)
- [Padrões de Design](#-padrões-de-design)
- [API](#-endpoints-da-api)
- [Exemplos Práticos](#-exemplos-práticos)
- [Modelo de Entidades](#-modelo-de-entidades)
- [Stack Tecnológico](#️-stack-tecnológico)
- [Resiliência](#️-resiliência)
- [Observabilidade](#-observabilidade)
- [Testes](#-testes)
- [Como Executar](#-como-executar)
- [Recursos Adicionais](#-recursos-adicionais)

---

## 🎯 O que é

O **MS-Payments** é um microsserviço responsável por **orquestrar pagamentos instantâneos (PIX)** e **depósitos em dinheiro** em um sistema bancário distribuído. 

Construído seguindo **Clean Architecture** e princípios de **Domain-Driven Design (DDD)**, o serviço garante:

- ✅ **Consistência de dados** através do Transactional Outbox Pattern
- ✅ **Idempotência** para prevenir duplicação de transações
- ✅ **Resiliência** com Circuit Breaker e Retry automático
- ✅ **Rastreabilidade completa** via Correlation ID
- ✅ **Observabilidade** com logs estruturados e métricas

---

## 🚀 Funcionalidades

| Funcionalidade | Descrição | Método |
|----------------|-----------|--------|
| **💸 Pagamentos PIX** | Transferências instantâneas entre contas via chaves PIX | `POST /transaction/pix` |
| **💵 Depósitos** | Registro e processamento de depósitos em espécie | `POST /transaction/deposit` |
| **📄 Extrato** | Consulta de histórico de transações por conta | `GET /transaction/account-statement` |
| **🔄 Garantia de Entrega** | Outbox Pattern para eventos Kafka | Automático |
| **🔒 Idempotência** | Prevenção de duplicação de transações | Automático |

---

## 🧩 Problema que Resolve

Em sistemas bancários distribuídos, desafios críticos precisam ser endereçados:

| Desafio | Problema | Solução Implementada |
|---------|----------|---------------------|
| **⚡ Consistência Eventual** | Eventos podem ser perdidos se Kafka estiver down | **Transactional Outbox Pattern** |
| **🔁 Duplicação de Pagamentos** | Requisições duplicadas podem gerar cobranças duplicadas | **Idempotency Pattern** com tabela dedicada |
| **💥 Falhas em Cascata** | Serviço externo down pode derrubar toda aplicação | **Circuit Breaker + Retry** (Resilience4j) |
| **🔍 Rastreabilidade** | Difícil debugar transações entre múltiplos serviços | **Correlation ID** em logs, HTTP e Kafka |
| **🔒 Segurança** | Dados trafegam sem criptografia | **SSL/TLS** em todas comunicações externas |

---

## 🔄 Fluxos de Dados

### Fluxo PIX (Pagamento Instantâneo)

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /transaction/pix
       ▼
┌─────────────────────────────────────────────────────┐
│              MS-Payments                            │
│                                                     │
│  1. TransactionController                          │
│     └─> InstantPayment (Use Case)                 │
│          ├─> Check Idempotency (DB)               │
│          ├─> Validate Keys (CPF/CNPJ)             │
│          ├─> Create Transaction (DB)              │
│          ├─> Create Outbox Entry (DB)             │
│          └─> HTTP Call to MS-Wallet              │
│               └─> Debit/Credit (HTTPS + SSL)      │
│                                                     │
│  2. OutboxScheduler (every 10s)                    │
│     └─> Fetch pending events                       │
│          └─> Publish to Kafka                      │
│               └─> Mark as sent (DB)                │
└─────────────────────────────────────────────────────┘
       │
       │ Event: TransactionCompleted
       ▼
┌─────────────┐
│    Kafka    │
└─────────────┘
```

### Fluxo Depósito (Cash Deposit)

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /transaction/deposit
       ▼
┌─────────────────────────────────────────────────────┐
│              MS-Payments                            │
│                                                     │
│  1. TransactionController                          │
│     └─> CashDeposit (Use Case)                    │
│          ├─> Validate receiverId (UUID)           │
│          ├─> Create Deposit (DB)                  │
│          └─> Create Outbox Entry (DB)             │
│                                                     │
│  2. OutboxScheduler (every 10s)                    │
│     └─> Fetch pending events                       │
│          └─> Publish to Kafka                      │
│               └─> Mark as sent (DB)                │
└─────────────────────────────────────────────────────┘
       │
       │ Event: DepositCreatedEvent
       ▼
┌─────────────┐
│    Kafka    │──────> MS-Wallet (async)
└─────────────┘        └─> Credit Account
```

---
## 🏗️ Arquitetura
### Ecossistema
```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   MS-User    │         │ MS-Payments  │         │  MS-Wallet   │
│              │◀───────▶│              │◀───────▶│              │
│ • Cadastro   │  Events │ • PIX        │  HTTPS  │ • Saldo      │
│ • Perfil     │         │ • Depósito   │         │ • Débito     │
└──────────────┘         └──────────────┘         └──────────────┘
        │                         │                         │
        └─────────────────────────┼─────────────────────────┘
                                  ▼
                        ┌──────────────────┐
                        │   Apache Kafka   │
                        │  (Event Bus)     │
                        └──────────────────┘
                                  │
                                  ▼
                        ┌──────────────────┐
                        │   SQL Server     │
                        │  (Persistência)  │
                        └──────────────────┘
```

### Comunicação Entre Serviços

| Origem | Destino | Tipo | Protocolo | Uso |
|--------|---------|------|-----------|-----|
| Client | MS-Payments | Síncrono | HTTP/REST | Requisições API |
| MS-Payments | MS-Wallet | Síncrono | HTTPS + SSL | Débito/Crédito |
| MS-Payments | Kafka | Assíncrono | Event Streaming | Publicação de eventos |
| MS-User | Kafka | Assíncrono | Event Streaming | UserCreatedEvent |
| Kafka | MS-Wallet | Assíncrono | Event Streaming | DepositCreatedEvent |

---

### Estrutura de Pastas (DDD)
```
src/main/java/com/matheus/payments/
│
├── Controller/                          # API REST
│   └── TransactionController.java      # Endpoints HTTP
│
├── Application/                         # Lógica de Aplicação
│   ├── UseCases/                        # Orquestradores
│   │   ├── InstantPayment.java          # Fluxo completo PIX
│   │   └── CashDeposit.java             # Fluxo de depósito
│   │
│   ├── Services/                        # Serviços de Negócio
│   │   ├── TransactionService.java
│   │   ├── OutboxService.java           # Outbox Pattern
│   │   ├── PaymentProcessorService.java
│   │   ├── TransactionIdempotencyService.java
│   │   ├── DepositService.java
│   │   └── StatementService.java
│   │
│   ├── DTOs/                            # Data Transfer Objects
│   ├── Mappers/                         # DTO ↔ Entity
│   └── Audit/                           # Logs estruturados
│
├── Domain/                              # CORE - Regras de Negócio
│   ├── Models/                          # Entidades
│   │   ├── Transaction.java
│   │   ├── Deposit.java
│   │   ├── TransactionOutbox.java
│   │   └── TransactionIdempotency.java
│   │
│   ├── Events/                          # Eventos de Domínio
│   │   └── DepositCreatedEvent.java
│   │
│   ├── Repositories/                    # Interfaces (Ports)
│   │   ├── TransactionRepository.java
│   │   ├── DepositRepository.java
│   │   └── OutboxRepository.java
│   │
│   └── Exceptions/                      # Exceções de Negócio
│       ├── TransactionNotFound.java
│       └── DepositNotFound.java
│
└── Infra/                               # Detalhes Técnicos
    ├── Repository/                      # Adaptadores JPA
    │   ├── JpaImplements/               # Implementações
    │   └── JpaInterfaces/               # Spring Data JPA
    │
    ├── Kafka/                           # Mensageria
    │   ├── Configs/
    │   └── Listeners/
    │
    ├── Http/                            # Clientes HTTP
    │   └── WalletService.java           # Cliente HTTPS com SSL
    │
    ├── Schedulers/                      # Jobs
    │   └── OutboxScheduler.java         # Processa Outbox (10s)
    │
    ├── Audit/                           # Logs estruturados
    └── Exceptions/                      # Exceções técnicas
```

---

## 🎨 Padrões de Design

### 1. Transactional Outbox Pattern

Garante que eventos sejam publicados no Kafka mesmo em caso de falhas temporárias.

**Flow**: 
```
Transação DB → Outbox Table (same transaction) → Scheduler (10s) → Kafka → Update sent=true
```

**Benefício**: At-least-once delivery sem perda de eventos.

---

### 2. Idempotency Pattern

Tabela dedicada `transaction_idempotency` previne duplicação de pagamentos.

**Flow**: 
```
Check idempotency table → Se existe e sent=true → Reject (409) → Se não existe → Process → Insert
```

**Benefício**: Zero duplicação de transações financeiras.

---

### 3. Circuit Breaker + Retry

Resilience4j protege sistema de falhas em serviços externos (Kafka, Wallet Service, Database).

**Ordem correta das anotações**:
```java
@CircuitBreaker    ← Mais externo (protege tudo)
@Retry             ← Meio (recupera falhas temporárias)
@Transactional     ← Mais interno (gerencia DB)
```

**Configuração**:
- **Circuit Breaker**: 50% falhas em 10 calls → OPEN por 15s
- **Retry Database**: 3 tentativas, backoff exponencial (2s → 4s → 8s)
- **Retry HTTP**: 3 tentativas, backoff exponencial (2s → 4s → 8s)
- **Retry Kafka**: 3 tentativas, backoff exponencial (2s → 4s → 8s)

**Benefício**: Fail fast quando serviço está down, retry em falhas temporárias.

---

### 4. Repository Pattern

**Estrutura em 3 camadas**:
- **Domain**: Define interface (Port)
- **Infra**: Implementa interface (Adapter)
- **JPA**: Spring Data JPA

**Benefício**: Domain independente de tecnologia de persistência.

---

### 5. Event-Driven Architecture

Comunicação assíncrona via Kafka entre MS-User, MS-Payments e MS-Wallet.

**Eventos**: `UserCreatedEvent`, `DepositCreatedEvent`, `TransactionCompleted`

**Benefício**: Baixo acoplamento entre serviços.

---

### 6. Correlation ID Pattern

UUID único propagado em HTTP headers, logs (MDC) e mensagens Kafka.

**Benefício**: Rastreamento end-to-end para debugging e auditoria.

---
## 📡 Endpoints da API

### 1. Pagamento PIX

**Endpoint**: `POST /transaction/pix`

Processa pagamento instantâneo entre duas contas via chaves PIX.

**Request Body**:
```json
{
  "senderKey": "11111111111",
  "receiverKey": "22222222222",
  "amount": 150.50
}
```
**Response** (200 OK):
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "senderAccountId": "123e4567-e89b-12d3-a456-426614174000",
  "receiverAccountId": "987fcdeb-51a2-43f1-9876-543210fedcba",
  "isSent": true,
  "isSuccessful": true,
  "failedMessage": null,
  "timestamp": "2026-03-13T10:30:00"
}
```

**Validações**:
- `senderKey`: Obrigatório, CPF/CNPJ válido
- `receiverKey`: Obrigatório, CPF/CNPJ válido
- `amount`: Obrigatório, maior que zero

---

### 2. Depósito em Dinheiro

**Endpoint**: `POST /transaction/deposit`

Registra depósito em espécie para uma conta.

**Request Body**:
```json
{
  "receiverId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 500.00
}
```

**Response** (201 Created):
```json
{
  "depositId": "789abc12-3def-4567-8901-234567890abc",
  "status": "PENDING"
}
```

**Validações**:
- `receiverId`: Obrigatório, UUID válido
- `amount`: Obrigatório, maior que zero

---

### 3. Extrato de Conta

**Endpoint**: `GET /transaction/account-statement?account={accountId}`

Retorna histórico de transações onde a conta foi sender ou receiver.

**Query Params**:
- `account`: UUID da conta
**Response** (200 OK):
```json
[
  {
    "transactionId": "550e8400-e29b-41d4-a716-446655440000",
    "senderKey": "11111111111",
    "receiverKey": "22222222222",
    "senderAccountId": "123e4567-e89b-12d3-a456-426614174000",
    "receiverAccountId": "987fcdeb-51a2-43f1-9876-543210fedcba",
    "amount": 150.50,
    "status": "COMPLETED",
    "timestamp": "2026-03-13T10:30:00"
  },
  {
    "transactionId": "660f9511-f39c-52e5-b827-557766551111",
    "senderKey": "33333333333",
    "receiverKey": "11111111111",
    "senderAccountId": "456e7890-f12b-34d5-c678-901234567890",
    "receiverAccountId": "123e4567-e89b-12d3-a456-426614174000",
    "amount": 75.00,
    "status": "COMPLETED",
    "timestamp": "2026-03-13T09:15:00"
  }
]
```

---

## 💡 Exemplos Práticos

### Exemplo 1: Realizar Pagamento PIX

```bash
curl -X POST http://localhost:8080/transaction/pix \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -d '{
    "senderKey": "12345678901",
    "receiverKey": "98765432100",
    "amount": 250.00
  }'
```

**Response**:
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "senderAccountId": "123e4567-e89b-12d3-a456-426614174000",
  "receiverAccountId": "987fcdeb-51a2-43f1-9876-543210fedcba",
  "isSent": true,
  "isSuccessful": true,
  "failedMessage": null,
  "timestamp": "2026-03-13T10:30:00"
}
```

---

### Exemplo 2: Realizar Depósito

```bash
curl -X POST http://localhost:8080/transaction/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": "123e4567-e89b-12d3-a456-426614174000",
    "amount": 1000.00
  }'
```

**Response**:
```json
{
  "depositId": "789abc12-3def-4567-8901-234567890abc",
  "status": "PENDING"
}
```

---

### Exemplo 3: Consultar Extrato

```bash
curl -X GET "http://localhost:8080/transaction/account-statement?account=123e4567-e89b-12d3-a456-426614174000" \
  -H "Accept: application/json"
```

**Response**:
```json
[
  {
    "transactionId": "550e8400-e29b-41d4-a716-446655440000",
    "senderKey": "11111111111",
    "receiverKey": "22222222222",
    "amount": 150.50,
    "status": "COMPLETED",
    "timestamp": "2026-03-13T10:30:00"
  }
]
```

---

## 📦 Modelo de Entidades

### Diagrama de Entidades

```
┌────────────────────────┐
│     Transaction        │
├────────────────────────┤
│ + transactionId: UUID  │
│ + senderKey: String    │
│ + receiverKey: String  │
│ + senderAccountId: UUID│
│ + receiverAccountId: UUID│
│ + amount: BigDecimal   │
│ + status: String       │
│ + isSent: Boolean      │
│ + isSuccessful: Boolean│
│ + timestamp: LocalDateTime│
└────────────────────────┘
            │
            │ references
            ▼
┌────────────────────────┐        ┌────────────────────────┐
│       Deposit          │        │  TransactionOutbox     │
├────────────────────────┤        ├────────────────────────┤
│ + depositId: UUID      │        │ + id: Long             │
│ + receiverId: UUID     │───────▶│ + aggregateType: String│
│ + amount: BigDecimal   │        │ + aggregateId: String  │
│ + status: String       │        │ + eventType: String    │
│ + timestamp: LocalDateTime│     │ + payload: String      │
└────────────────────────┘        │ + sent: Boolean        │
                                  │ + createdAt: LocalDateTime│
                                  └────────────────────────┘
                                              ▲
                                              │
                                  ┌───────────┴────────────┐
                                  │ TransactionIdempotency │
                                  ├────────────────────────┤
                                  │ + idempotencyKey: String│
                                  │ + transactionId: UUID  │
                                  │ + sent: Boolean        │
                                  │ + createdAt: LocalDateTime│
                                  └────────────────────────┘
```

---

## 🛠️ Stack Tecnológico

| Categoria | Tecnologia | Versão | Propósito |
|-----------|------------|--------|-----------|
| **☕ Runtime** | Java | 21 | Linguagem base |
| **🍃 Framework** | Spring Boot | 3.5.7 | Framework principal |
| **💾 Persistência** | Spring Data JPA | 3.5.7 | ORM e persistência |
| **🗄️ Banco de Dados** | SQL Server | - | Armazenamento relacional |
| **📨 Mensageria** | Apache Kafka | - | Event streaming |
| **🔌 Client Kafka** | Spring Kafka | 3.5.7 | Integração Kafka |
| **🛡️ Resiliência** | Resilience4j | 2.3.0 | Circuit Breaker, Retry |
| **📝 Logs** | SLF4J + Logback | - | Sistema de logs |
| **📊 Logs Estruturados** | Logstash Encoder | - | JSON logging |
| **🗺️ Migrations** | Flyway | - | Versionamento DB |
| **🧪 Testes** | JUnit 5 + Mockito | - | Testes unitários |
| **🗃️ DB Testes** | H2 Database | - | In-memory database |
| **🔧 Build** | Maven | 3.9+ | Gerenciamento de build |
| **📈 Métricas** | Spring Actuator | 3.5.7 | Health checks e métricas |
| **🔐 Validação** | Bean Validation | - | Validação de entrada |

---
## 🛡️ Resiliência

### Configurações de Proteção

| Componente | Configuração | Aplicado em |
|------------|--------------|-------------|
| **⚡ Circuit Breaker** | 50% falhas/10 calls → OPEN 15s | Kafka, Wallet Service, Database |
| **🔄 Retry - Database** | 3x, 2s→4s→8s (exponential backoff) | Todas operações DB |
| **🔄 Retry - HTTP** | 3x, 2s→4s→8s (exponential backoff) | Chamadas Wallet Service |
| **🔄 Retry - Kafka** | 3x, 2s→4s→8s (exponential backoff) | Publicação de eventos |
| **⏰ Outbox Scheduler** | A cada 10 segundos | Reprocessamento de eventos |

### Ordem das Anotações

```java
@CircuitBreaker(name = "payments-cb", fallbackMethod = "fallback")
@Retry(name = "payments-retry")
@Transactional
public void processar() { }
```

**Lógica**: Circuit Breaker envolve Retry, que envolve Transaction.

---
## 📊 Observabilidade

### 📝 Logs Estruturados (JSON)

Todos os logs são gerados em formato JSON para fácil parsing e análise.

**Campos inclusos**:
```
timestamp, level, correlation_id, application_name, 
class_name, method_name, transaction_id, account_id, message
```

**Exemplo de log**:
```json
{
  "timestamp": "2026-03-13T10:30:00.123Z",
  "level": "INFO",
  "correlation_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "application_name": "MS-Payments",
  "class_name": "InstantPayment",
  "method_name": "execute",
  "transaction_id": "550e8400-e29b-41d4-a716-446655440000",
  "message": "PIX transaction completed successfully"
}
```

### 🔍 Rastreamento (Correlation ID)

Correlation ID propagado em:
- ✅ HTTP Headers (`X-Correlation-Id`)
- ✅ MDC (SLF4J) - Logs
- ✅ Kafka Headers
- ✅ Responses HTTP

**Fluxo**:
```
Request → Generate/Extract Correlation ID → MDC → Logs + HTTP + Kafka → Response
```

---
## 🧪 Testes

### Testes Unitários

O projeto possui testes unitários para garantir a qualidade do código e o comportamento esperado de cada componente.

**Componentes testados**:
- ✅ **Services** - Lógica de negócio
- ✅ **DTOs** - Validação de dados
- ✅ **Use Cases** - Fluxos de aplicação
- ✅ **Schedulers** - Jobs agendados

### Executar Testes

```bash
# Todos os testes
./mvnw test

# Apenas testes unitários
./mvnw test -Dtest="UnitTests.**"

# Teste específico
./mvnw test -Dtest="TransactionServiceTests"
```

### Estrutura de Testes

```
src/test/java/com/matheus/payments/UnitTests/
├── Services/
│   ├── TransactionServiceTests.java
│   ├── DepositServiceTests.java
│   ├── OutboxServiceTests.java
│   ├── PaymentProcessorServiceTests.java
│   └── TransactionIdempotencyServiceTests.java
├── DTOs/
│   ├── TransactionRequestTests.java
│   └── DepositRequestTests.java
├── UseCases/
│   ├── InstantPaymentTests.java
│   └── CashDepositTests.java
└── Schedulers/
    └── OutboxSchedulerTests.java
```

---
## 🚀 Como Executar

### Pré-requisitos

- ☕ **Java 21+**
- 🔧 **Maven 3.9+**
- 🗄️ **SQL Server**
- 📨 **Apache Kafka**

### 1️⃣ Clonar o Repositório

```bash
git clone https://github.com/matheusmaiagoulart/payment-microservices.git
cd MS-Payments
```

### 2️⃣ Configurar Banco de Dados

Crie um banco de dados SQL Server:
```sql
CREATE DATABASE [MS-Payments];
```

Configure as credenciais em `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=MS-Payments;encrypt=true;trustServerCertificate=true
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

### 3️⃣ Configurar Kafka

Certifique-se de que o Kafka está rodando em `localhost:9092`:
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=instant-payment-service-group
spring.kafka.consumer.auto-offset-reset=earliest
```

### 4️⃣ Executar a Aplicação

**Modo de desenvolvimento**:
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

**Ou build e execução**:
```bash
./mvnw clean package
java -jar target/InstantPaymentService-0.0.1-SNAPSHOT.jar
```

Aplicação estará disponível em `http://localhost:8080`

---

## 📚 Recursos Adicionais

### 📖 Documentação
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Transactional Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)

### 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">

**Made with ❤️ by [Matheus Maia Goulart](https://github.com/matheusmaiagoulart)**

⭐ **Se este projeto foi útil, considere dar uma estrela!** ⭐

</div>
