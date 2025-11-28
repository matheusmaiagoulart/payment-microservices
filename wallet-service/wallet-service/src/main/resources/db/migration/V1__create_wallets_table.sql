CREATE TABLE wallets
(
    account_id   uniqueidentifier NOT NULL,
    balance      decimal(18, 2),
    account_type smallint,
    is_active    bit,
    created_at   datetime,
    CONSTRAINT pk_wallets PRIMARY KEY (account_id)
);