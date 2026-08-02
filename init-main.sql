CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    created         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    active          BOOLEAN NOT NULL DEFAULT TRUE
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
    created_on          TIMESTAMP,
    published_on        TIMESTAMP,
    location_lat        DOUBLE PRECISION,
    location_lon        DOUBLE PRECISION

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

    CONSTRAINT chk_req_status CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELED'))
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

CREATE TABLE IF NOT EXISTS comments (
    id                  BIGSERIAL PRIMARY KEY,
    text                TEXT NOT NULL,
    event_id            BIGINT NOT NULL,
    author_id           BIGINT NOT NULL,
    created             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated             TIMESTAMP WITHOUT TIME ZONE,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    moderator_comment   TEXT,
    moderated_at        TIMESTAMP WITHOUT TIME ZONE,
    moderator_id        BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_comments_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_moderator FOREIGN KEY (moderator_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_comment_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_comments_event_id ON comments(event_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);
CREATE INDEX IF NOT EXISTS idx_comments_status ON comments(status);
CREATE INDEX IF NOT EXISTS idx_comments_created ON comments(created);
CREATE INDEX IF NOT EXISTS idx_comments_event_status ON comments(event_id, status);
CREATE INDEX IF NOT EXISTS idx_comments_event_status_deleted ON comments(event_id, status, is_deleted);