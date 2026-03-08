alter table transaction_outbox
    add correlation_id UNIQUEIDENTIFIER;