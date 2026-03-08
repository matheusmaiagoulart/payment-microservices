CREATE TABLE transaction_idempotency (
    transaction_id UNIQUEIDENTIFIER PRIMARY KEY,
    correlation_id UNIQUEIDENTIFIER,
    payload TEXT NOT NULL,
    sent BIT NOT NULL DEFAULT 0,
    failed BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL,
    failure_reason VARCHAR(500),
    failure_at DATETIME2
);

CREATE INDEX idx_transaction_idempotency_sent ON transaction_outbox(sent);
CREATE INDEX idx_transaction_idempotency_failed ON transaction_outbox(failed);
CREATE INDEX idx_transaction_idempotency_created_at ON transaction_outbox(created_at DESC);
CREATE INDEX idx_transaction_idempotency_transaction_id ON transaction_outbox(transaction_id);