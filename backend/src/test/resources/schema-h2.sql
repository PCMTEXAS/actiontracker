-- H2-compatible schema for tests
-- Replaces TIMESTAMPTZ with TIMESTAMP WITH TIME ZONE

CREATE TABLE IF NOT EXISTS app_users (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    picture_url VARCHAR(500),
    role VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'MEMBER')),
    daily_digest_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    assignee_email VARCHAR(255) NOT NULL,
    assignee_name VARCHAR(255) NOT NULL,
    assigned_by_email VARCHAR(255) NOT NULL,
    assigned_by_name VARCHAR(255) NOT NULL,
    due_date DATE,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'WAITING_ON', 'BLOCKED', 'COMPLETE')),
    source VARCHAR(500),
    source_url VARCHAR(1000),
    project_tag VARCHAR(255),
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence VARCHAR(20) CHECK (recurrence IN ('WEEKLY', 'MONTHLY')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS task_activity (
    id UUID NOT NULL,
    task_id UUID NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    actor_name VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL CHECK (event_type IN ('TASK_CREATED', 'STATUS_CHANGED', 'REASSIGNED', 'DUE_DATE_UPDATED', 'COMMENT_ADDED', 'TASK_COMPLETED')),
    detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS task_comments (
    id UUID NOT NULL,
    task_id UUID NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    author_name VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS comment_mentions (
    comment_id UUID NOT NULL,
    mentioned_email VARCHAR(255)
);
