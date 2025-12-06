CREATE TABLE wallet_keys
(
    id UNIQUEIDENTIFIER PRIMARY KEY,
    key_value VARCHAR(255) NOT NULL UNIQUE,
    type SMALLINT NOT NULL,
    account_id UNIQUEIDENTIFIER NOT NULL
);

create index idx_wallet_keys_account_id on wallet_keys(account_id);