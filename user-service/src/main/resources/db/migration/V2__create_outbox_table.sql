CREATE TABLE outbox (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    user_id UNIQUEIDENTIFIER,
    event_type VARCHAR(255),
    payload NVARCHAR(MAX),
    is_sent BIT NOT NULL,
    is_failed BIT NOT NULL,
    failure_reason VARCHAR(255),
    created_at DATETIME2 NOT NULL,
    updated_at DATETIME2 NOT NULL
);