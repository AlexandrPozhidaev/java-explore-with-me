CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    created         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    annotation          TEXT,
    description         TEXT,
    event_date          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    category_id         BIGINT NOT NULL REFERENCES categories(id),
    initiator_id        BIGINT NOT NULL REFERENCES users(id),
    paid                BOOLEAN DEFAULT false,
    participant_limit   INTEGER,
    pinned              BOOLEAN DEFAULT false,
    is_request_moderation BOOLEAN DEFAULT false,
    state               VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT chk_state CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED'))
);

CREATE INDEX IF NOT EXISTS idx_events_category ON events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_initiator ON events(initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_state ON events(state);
CREATE INDEX IF NOT EXISTS idx_events_pinned ON events(pinned);

CREATE TABLE IF NOT EXISTS participation_requests (
    id                  BIGSERIAL PRIMARY KEY,
    requester_id        BIGINT NOT NULL REFERENCES users(id),
    event_id            BIGINT NOT NULL REFERENCES events(id),
    comment             VARCHAR(255),
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created             TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_req_status CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_requests_event ON participation_requests(event_id);
CREATE INDEX IF NOT EXISTS idx_requests_user ON participation_requests(requester_id);

CREATE TABLE IF NOT EXISTS compilations (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL UNIQUE,
    description     VARCHAR(512),
    pinned          BOOLEAN DEFAULT false
);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id  BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
    event_id        BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);