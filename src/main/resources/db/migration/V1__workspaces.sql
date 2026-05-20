CREATE TABLE workspaces (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID NOT NULL,
    group_id    UUID NOT NULL,
    name        TEXT NOT NULL,
    slug        TEXT NOT NULL,
    status      TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'archived')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT workspaces_org_slug_uk UNIQUE (org_id, slug),
    CONSTRAINT workspaces_group_slug_uk UNIQUE (group_id, slug)
);

CREATE INDEX workspaces_org_id_idx ON workspaces (org_id);
CREATE INDEX workspaces_group_id_idx ON workspaces (group_id);
