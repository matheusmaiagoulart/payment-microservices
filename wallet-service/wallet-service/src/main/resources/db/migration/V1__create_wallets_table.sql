CREATE TABLE wallets
(
    account_id   uniqueidentifier NOT NULL,
    user_id      uniqueidentifier NOT NULL,
    balance      decimal(18, 0),
    account_type smallint,
    is_active    bit,
    created_at   datetime,
    CONSTRAINT pk_wallets PRIMARY KEY (account_id),
    CONSTRAINT UQ_wallets_user_id UNIQUE (user_id)
);