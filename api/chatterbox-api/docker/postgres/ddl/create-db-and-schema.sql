DROP table if EXISTS webhook_received;

CREATE TABLE webhook_received
(
    id                   SERIAL PRIMARY KEY,
    webhook_id           TEXT UNIQUE, -- 'GitHub delivery ID',
    repository_full_name TEXT,        -- 'e.g., <organisation>/<someRepository>',
    event_type           TEXT,        -- 'e.g., push, pull_request',
    payload              JSONB,       -- 'raw JSON payload'
    processed_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
