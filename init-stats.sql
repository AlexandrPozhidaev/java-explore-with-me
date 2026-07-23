CREATE TABLE IF NOT EXISTS stats (
    id BIGSERIAL PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(1024) NOT NULL,
    ip VARCHAR(45) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_stats_app ON stats(app);
CREATE INDEX idx_stats_uri ON stats(uri);
CREATE INDEX idx_stats_timestamp ON stats(timestamp);
CREATE INDEX idx_stats_app_uri_ts ON stats(app, uri, timestamp);
