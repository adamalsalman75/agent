CREATE TABLE IF NOT EXISTS tasks (
    id SERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    deadline TIMESTAMP,
    priority VARCHAR(20),
    constraints TEXT,
    parent_id BIGINT REFERENCES tasks(id),
    metadata JSONB
);