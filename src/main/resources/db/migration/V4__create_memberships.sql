CREATE TABLE memberships (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL REFERENCES users(id),
workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
role VARCHAR(10) NOT NULL,
UNIQUE(user_id,workspace_id)
);