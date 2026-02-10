DROP type if exists webhook_event_status;
CREATE TYPE webhook_event_status AS ENUM ('RECEIVED', 'PROCESSING', 'PROCESSED_SUCCESS', 'PROCESSED_FAILURE');

DROP type if exists webhook_event_type;
CREATE TYPE webhook_event_type AS ENUM ('PUSH', 'PULL_REQUEST', 'POLL_COMMIT', 'POLL_PULL_REQUEST', 'PING');

DROP table if EXISTS webhook_event;
CREATE TABLE webhook_event
(
    id                     SERIAL PRIMARY KEY,
    repository_full_name   TEXT                 NOT NULL, -- e.g., <organisation>/<someRepository>
    webhook_id             TEXT                 NOT NULL, -- X-GitHub-Delivery
    webhook_event_type     webhook_event_type   NOT NULL,
    display_name           TEXT                 NOT NULL,
    sender_name            TEXT                 NOT NULL,
    event_url              TEXT                 NOT NULL,
    event_url_display_text TEXT                 NOT NULL,
    extra_detail           TEXT                 NOT NULL,
    payload                JSONB                NOT NULL, -- raw JSON payload
    webhook_event_status   webhook_event_status NOT NULL,
    error_message          TEXT,
    received_at            TIMESTAMP            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at           TIMESTAMP
);


DROP Table if Exists github_polled_event;
CREATE TABLE github_polled_event
(
    id                     SERIAL PRIMARY KEY,
    repository_full_name   TEXT                 NOT NULL, -- e.g., <organisation>/<someRepository>
    source_id              TEXT                 NOT NULL, -- commit sha, pull_request id, issue id etc
    webhook_event_type     webhook_event_type   NOT NULL,
    display_name           TEXT                 NOT NULL,
    sender_name            TEXT                 NOT NULL,
    event_url              TEXT                 NOT NULL,
    event_url_display_text TEXT                 NOT NULL,
    extra_detail           TEXT                 NOT NULL,
    payload                JSONB                NOT NULL, -- raw JSON payload
    webhook_event_status   webhook_event_status NOT NULL DEFAULT 'RECEIVED',
    error_message          TEXT,
    fetched_at             TIMESTAMP            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at           TIMESTAMP
);

Drop Table if exists webhook_event_delivery_log;
CREATE table webhook_event_delivery_log
(
    id                       serial PRIMARY KEY,
    webhook_event_id         serial               not null, -- ID of the corresponding webhook_event
    delivery_destination     TEXT                 NOT NULL, -- MS Teams, etc.
    delivery_destination_url TEXT                 NOT NULL, -- MS Teams, etc.
    webhook_event_status     webhook_event_status NOT NULL,
    delivered_at             TIMESTAMP
);

Drop Table if exists github_polled_event_delivery_log;
CREATE table github_polled_event_delivery_log
(
    id                       serial PRIMARY KEY,
    github_polled_event_id   serial               not null, -- ID of the corresponding github_polled_event
    delivery_destination     TEXT                 NOT NULL, -- MS Teams, etc.
    delivery_destination_url TEXT                 NOT NULL, -- MS Teams, etc.
    webhook_event_status     webhook_event_status NOT NULL,
    delivered_at             TIMESTAMP
);
