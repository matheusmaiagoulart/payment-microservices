CREATE TABLE wallet_ledger
(
    id                     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    transaction_id         UNIQUEIDENTIFIER NOT NULL,
    wallet_id              UNIQUEIDENTIFIER NOT NULL,
    counterparty_wallet_id UNIQUEIDENTIFIER NOT NULL,
    amount                 DECIMAL(18, 2)   NOT NULL,
    entry_type             VARCHAR(10)      NOT NULL,
    timestamp              DATETIME         NOT NULL
);

CREATE INDEX idx_wallet_ledger_wallet_id ON wallet_ledger (wallet_id);
CREATE INDEX idx_wallet_ledger_counterparty_id ON wallet_ledger (counterparty_wallet_id);
CREATE INDEX idx_wallet_ledger_transaction_id ON wallet_ledger (transaction_id);
CREATE INDEX idx_wallet_ledger_timestamp ON wallet_ledger (timestamp);