-- V1__create_llm_tasks.sql
-- Task queue and cache for igaming-llm-gateway

CREATE TABLE IF NOT EXISTS llm_tasks (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_type  VARCHAR(64)  NOT NULL,           -- e.g. "deepseek", "gemini"
    model_name     VARCHAR(128) NOT NULL,           -- e.g. "deepseek-chat"
    prompt_hash    VARCHAR(64)  NOT NULL,           -- SHA-256(provider+model+prompt)
    prompt         TEXT         NOT NULL,
    system_prompt  TEXT,
    temperature    DOUBLE PRECISION,
    max_tokens     INTEGER,
    user_id        VARCHAR(128),
    metadata       JSONB,
    permanent      BOOLEAN      NOT NULL DEFAULT false,  -- вечный кеш vs временный
    ttl_hours      INTEGER      NOT NULL DEFAULT 24,     -- TTL в часах (ignored if permanent)
    status         VARCHAR(32)  NOT NULL DEFAULT 'PENDING', -- PENDING | PROCESSING | COMPLETED | FAILED
    result_text    TEXT,
    tokens_used    INTEGER,
    finish_reason  VARCHAR(64),
    error_message  TEXT,
    cached         BOOLEAN      NOT NULL DEFAULT false,  -- true если вернули из кеша
    worker_id      VARCHAR(256),                        -- pod name взявшего задачу воркера
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    expires_at     TIMESTAMP                            -- NULL if permanent, else created_at + ttl_hours
);

-- Index for cache lookup: find completed non-expired tasks by hash
CREATE INDEX IF NOT EXISTS idx_llm_tasks_cache
    ON llm_tasks (prompt_hash, status, permanent, expires_at);

-- Index for worker claim: find pending tasks by provider/model
CREATE INDEX IF NOT EXISTS idx_llm_tasks_pending
    ON llm_tasks (provider_type, model_name, status, created_at)
    WHERE status = 'PENDING';

-- Index for polling by taskId (already covered by PK, just document intent)
-- Trigger to auto-set updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER llm_tasks_updated_at
    BEFORE UPDATE ON llm_tasks
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
