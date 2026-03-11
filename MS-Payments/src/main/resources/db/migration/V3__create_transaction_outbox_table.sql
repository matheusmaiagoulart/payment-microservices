CREATE TABLE transaction_outbox (
    transaction_id VARCHAR(255) PRIMARY KEY,
    topic VARCHAR(255),
    payload TEXT NOT NULL,
    correlation_id UNIQUEIDENTIFIER,
    sent BIT NOT NULL DEFAULT 0,
    failed BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL,
    failure_reason VARCHAR(500),
    failure_at DATETIME2
);

CREATE INDEX idx_outbox_sent ON transaction_outbox(sent);
CREATE INDEX idx_outbox_failed ON transaction_outbox(failed);
CREATE INDEX idx_outbox_created_at ON transaction_outbox(created_at DESC);
