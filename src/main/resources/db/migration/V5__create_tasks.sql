CREATE TABLE tasks (
id BIGSERIAL PRIMARY KEY,
title        VARCHAR(255) NOT NULL,
done         BOOLEAN NOT NULL DEFAULT false,
workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
author_id    BIGINT NOT NULL REFERENCES users(id),
created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tasks_workspace ON tasks(workspace_id);