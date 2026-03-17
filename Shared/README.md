# 📦 Shared

> Biblioteca compartilhada para os microserviços do sistema de pagamentos

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)

---

## 📋 Sobre o Projeto

O **Shared** é uma biblioteca Java que centraliza componentes comuns utilizados pelos microserviços (MS-User, MS-Wallet, MS-Payments). Seu objetivo é evitar duplicação de código e garantir consistência entre os serviços.

---

## 📁 Estrutura

```
src/main/java/org/shared/
│
├── Domain/                     # Enums de domínio
│   ├── accountType.java        # Tipos de conta (CHECKING, MERCHANT)
│   └── keyType.java            # Tipos de chave PIX (CPF, PHONE, EMAIL)
│
├── DTOs/                       # Data Transfer Objects compartilhados
│   ├── TransactionDTO.java     # DTO para transações entre serviços
│   └── PaymentProcessorResponse.java  # Resposta do processamento de pagamentos
│
└── Logs/                       # Utilitários de logging
    └── LogBuilder.java         # Builder para logs estruturados com Logstash
```

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Java | 21 | Linguagem de programação |
| Logstash Logback Encoder | 7.4 | Encoder para logs estruturados JSON |

---

## 📦 Como Usar

### Instalação Local

```bash
cd Shared
mvn clean install
```

### Dependência nos Microserviços

Adicione ao `pom.xml` do microserviço:

```xml
<dependency>
    <groupId>com.matheus.shared</groupId>
    <artifactId>Shared</artifactId>
    <version>1.1.8</version>
</dependency>
```

---

## 📂 Componentes

### Domain

Enums compartilhados utilizados para tipagem consistente em todo o sistema:

- **`accountType`**: Define os tipos de conta (CHECKING, MERCHANT)
- **`keyType`**: Define os tipos de chave PIX (CPF, PHONE, EMAIL)

### DTOs

Classes de transferência de dados para comunicação entre serviços:

- **`TransactionDTO`**: Representa uma transação com dados do remetente, destinatário, valor e status
- **`PaymentProcessorResponse`**: Resposta padronizada do processamento de pagamentos com factory methods

### Logs

Utilitários para logging estruturado:

- **`LogBuilder`**: Cria logs estruturados com campos padronizados para rastreabilidade (correlation_id, service_name, etc.)
