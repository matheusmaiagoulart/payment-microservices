CREATE TABLE transactions (
    transaction_id UNIQUEIDENTIFIER PRIMARY KEY,
    sender_key VARCHAR(255) NOT NULL,
    receiver_key VARCHAR(255) NOT NULL,
    sender_account_id UNIQUEIDENTIFIER,
    receiver_account_id UNIQUEIDENTIFIER,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp DATETIME2 NOT NULL
);

CREATE INDEX idx_transactions_sender_account_id ON transactions(sender_account_id);
CREATE INDEX idx_transactions_receiver_account_id ON transactions(receiver_account_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);
CREATE INDEX idx_transactions_sender_key ON transactions(sender_key);
CREATE INDEX idx_transactions_receiver_key ON transactions(receiver_key);
