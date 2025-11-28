CREATE TABLE wallet_keys
(
    id UNIQUEIDENTIFIER PRIMARY KEY,
    key_value VARCHAR(255) NOT NULL UNIQUE,
    type SMALLINT NOT NULL,
    wallet_id UNIQUEIDENTIFIER NOT NULL
);

create index idx_wallet_keys_wallet_id on wallet_keys(wallet_id);