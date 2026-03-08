CREATE TABLE deposits (
    deposit_id UNIQUEIDENTIFIER PRIMARY KEY,
    sender_id UNIQUEIDENTIFIER NOT NULL,
    receiver_id UNIQUEIDENTIFIER NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payed_at DATETIME2 NOT NULL,
    confirmed_at DATETIME2
);

CREATE INDEX idx_deposits_sender_id ON deposits(sender_id);
CREATE INDEX idx_deposits_receiver_id ON deposits(receiver_id);
CREATE INDEX idx_deposits_status ON deposits(status);
CREATE INDEX idx_deposits_payed_at ON deposits(payed_at);
