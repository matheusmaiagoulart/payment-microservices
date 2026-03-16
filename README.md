# Payment System - Sistema de Pagamentos Distribuído

Sistema de pagamentos distribuído baseado em microserviços, implementando uma arquitetura orientada a eventos (Event-Driven Architecture) com comunicações síncronas e assíncronas entre os serviços.

## 📋 Visão Geral

Este projeto demonstra a implementação de um ecossistema de pagamentos utilizando:
- **Arquitetura de Microserviços**: Serviços independentes e escaláveis
- **Clean Architecture**: Separação clara de responsabilidades em camadas
- **Event-Driven Architecture**: Comunicação baseada em eventos via Apache Kafka
- **Outbox Pattern**: Garantia de consistência eventual entre serviços
- **Comunicação Híbrida**: REST APIs (síncrona) e mensageria (assíncrona)
- **Observabilidade**: Logs estruturados com Correlation ID para rastreamento distribuído

## 🏛️ Padrões Arquiteturais

### Clean Architecture

Todos os microserviços seguem os princípios da **Clean Architecture**, organizados em camadas bem definidas:

```
├── Application/       # Casos de uso, DTOs, serviços de aplicação
├── Domain/            # Entidades, eventos de domínio, regras de negócio
├── Infra/             # Implementações de repositórios, integrações externas
├── Controller/        # APIs REST, entrada de dados
└── Utils/             # Utilitários e helpers
```

Esta estrutura garante:
- **Independência de frameworks**: O domínio não depende de bibliotecas externas
- **Testabilidade**: Facilita testes unitários
- **Manutenibilidade**: Alterações em uma camada não afetam as demais

### Event-Driven Architecture

O sistema implementa **Event-Driven Architecture** em dois níveis:

**Nível Interno (Domain Events)**:
- Eventos de domínio são disparados dentro de cada microserviço
- Permitem desacoplamento entre componentes internos
- Facilitam a implementação de side-effects de forma organizada

**Nível de Serviços (Integration Events)**:
- Comunicação assíncrona entre microserviços via **Apache Kafka**
- Eventos como `UserCreatedEvent`, `DepositCreatedEvent` propagam informações entre serviços
- Garante baixo acoplamento e alta escalabilidade

### Outbox Pattern

Para garantir **consistência eventual** entre o banco de dados e o envio de eventos Kafka, o sistema implementa o **Outbox Pattern**:

1. A transação de negócio e o evento são salvos **atomicamente** no banco de dados
2. Um processo em background (scheduler) lê os eventos pendentes da tabela `outbox`
3. Os eventos são publicados no Kafka e marcados como enviados

Isso evita inconsistências onde a transação é commitada mas o evento falha ao ser publicado (ou vice-versa).

## 🏗️ Arquitetura

### Microserviços e Use Cases

O sistema é composto pelos seguintes microserviços:

| Serviço | Descrição | Documentação |
|---------|-----------|--------------|
| **MS-Payments** | Serviço principal de processamento de pagamentos | [README](MS-Payments/README.md) |
| **MS-Wallet** | Gerenciamento de carteiras digitais | [README](MS-Wallet/README.md) |
| **MS-User** | Gerenciamento de usuários | [README](MS-User/README.md) |
| **Shared** | Biblioteca compartilhada com modelos e utilitários comuns | - |

#### MS-Payments (Processamento de Pagamentos)
- **CashDeposit**: Processamento de depósitos em dinheiro
- **InstantPayment**: Transferências instantâneas (PIX)
- Gerencia idempotência de transações (`TransactionIdempotency`)
- Implementa Outbox Pattern para consistência (`TransactionOutbox`)
- Publica eventos: `DepositCreatedEvent`

#### MS-Wallet (Carteiras Digitais)
- **CreateWallet**: Criação de novas carteiras
- **Deposit**: Processamento de depósitos na carteira
- **InstantPayment**: Execução de pagamentos instantâneos
- Gerencia saldos e ledger de transações (`WalletLedger`)
- Suporte a chaves PIX (`PixKey`)
- Controle de depósitos e transações processadas

#### MS-User (Gestão de Usuários)
- **CreateUser**: Registro de novos usuários
- **ActivateUserAccount**: Ativação de contas
- Publica eventos: `UserCreatedEvent`

### Infraestrutura

- **Apache Kafka + Zookeeper**: Mensageria para comunicação assíncrona entre serviços
- **SQL Server**: Banco de dados relacional (um database por microserviço)
- **Docker**: Containerização da infraestrutura

## 🚀 Como Rodar a Aplicação

### Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- SQL Server

### Configuração do Banco de Dados

Cada microserviço utiliza seu próprio banco de dados no **SQL Server**. Antes de rodar a aplicação, crie os seguintes databases:

| Microserviço | Database |
|--------------|----------|
| MS-Payments | `MS-Payments` |
| MS-Wallet | `MS-Wallet` |
| MS-User | `MS-User` |

As configurações de conexão estão nos arquivos `application.properties` de cada serviço. Ajuste conforme necessário:

```properties
spring.datasource.url=jdbc:sqlserver://SEU_SERVIDOR:1433;databaseName=MS-Payments;encrypt=true;trustServerCertificate=true
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

> **Nota**: Os serviços **MS-Wallet** e **MS-User** utilizam **Flyway** para gerenciamento de migrations, que serão executadas automaticamente ao iniciar a aplicação.

### Passo 1: Compilar o Módulo Shared

**IMPORTANTE**: O módulo `Shared` contém dependências utilizadas por todos os microserviços. Sempre que houver alterações neste módulo, você deve recompilá-lo e instalá-lo no repositório Maven local antes de rodar os serviços.

```bash
cd Shared
mvn clean install
```

### Passo 2: Subir a Infraestrutura

Na raiz do projeto, suba os containers Docker (Kafka, Zookeeper e serviços auxiliares):

```bash
docker-compose up -d
```

### Passo 3: Rodar os Microserviços

Compile e execute cada microserviço individualmente:

```bash
# MS-Payments
cd MS-Payments
mvn spring-boot:run

# MS-Wallet
cd MS-Wallet
mvn spring-boot:run

# MS-User
cd MS-User
mvn spring-boot:run
```

## 🔍 Observabilidade

### Logs com Correlation ID

Todos os microserviços implementam **Correlation ID** para rastreamento distribuído de requisições. Isso permite acompanhar uma transação completa através de todos os serviços envolvidos, facilitando debugging e análise de problemas.

- Cada requisição recebe um ID único que é propagado entre os serviços
- Os logs são estruturados e incluem o Correlation ID em todas as operações
- Eventos Kafka carregam o Correlation ID para manter a rastreabilidade

### Rastreamento de Eventos

A comunicação entre microserviços via Kafka é totalmente rastreável, permitindo visualizar o fluxo completo de eventos desde a origem até o processamento final.

## 🛠️ Tecnologias Utilizadas

- **Spring Boot**: Framework para desenvolvimento dos microserviços
- **Apache Kafka**: Plataforma de streaming de eventos
- **SQL Server**: Banco de dados relacional
- **Flyway**: Gerenciamento de migrations de banco de dados
- **Resilience4j**: Circuit Breaker e Retry para resiliência
- **Docker**: Containerização da infraestrutura

## 📁 Estrutura do Projeto

```
PaymentSystem/
├── MS-Payments/         # Microserviço de pagamentos
├── MS-Wallet/           # Microserviço de carteiras
├── MS-User/             # Microserviço de usuários
├── Shared/              # Biblioteca compartilhada
├── docker-compose.yml   # Orquestração da infraestrutura
└── README.md            # Este arquivo
```

## 🎯 Funcionalidades Principais

- ✅ Processamento de pagamentos em tempo real
- ✅ Gerenciamento de carteiras digitais
- ✅ Comunicação síncrona via REST APIs
- ✅ Comunicação assíncrona via eventos Kafka
- ✅ Consistência eventual com Outbox Pattern
- ✅ Resiliência com Circuit Breaker e Retry
- ✅ Idempotência de transações
- ✅ Rastreabilidade com Correlation ID

## 📖 Documentação Detalhada

Para informações detalhadas sobre cada microserviço, incluindo endpoints, eventos publicados/consumidos e regras de negócio, consulte os READMEs individuais:

- [MS-Payments - Serviço de Pagamentos](MS-Payments/README.md)
- [MS-Wallet - Serviço de Carteiras](MS-Wallet/README.md)
- [MS-User - Serviço de Usuários](MS-User/README.md)

---

**Desenvolvido por [Matheus Maia Goulart](https://www.linkedin.com/in/matheusmaiagoulart/) com foco em arquitetura de microserviços, Clean Architecture e boas práticas de engenharia de software.**
