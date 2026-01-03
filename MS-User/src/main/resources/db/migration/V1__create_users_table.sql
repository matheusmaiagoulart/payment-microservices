CREATE TABLE users (
    id UNIQUEIDENTIFIER NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(255) NOT NULL,
    cpf NVARCHAR(11) NOT NULL,
    phone_number NVARCHAR(11) NOT NULL,
    account_type NVARCHAR(50) NOT NULL,
    birth_date DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    active BIT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_cpf UNIQUE (cpf),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
);