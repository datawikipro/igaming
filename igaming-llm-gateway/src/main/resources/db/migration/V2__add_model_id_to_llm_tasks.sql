-- V2__add_model_id_to_llm_tasks.sql
-- Добавляем логическую ссылку на llm_models.id из igaming-llm-admin.
-- FK constraint не создаём: llm_models живёт в отдельной БД (cross-service).

ALTER TABLE llm_tasks
    ADD COLUMN IF NOT EXISTS model_id BIGINT;

-- Индекс для group-by по model_id в метриках очереди
CREATE INDEX IF NOT EXISTS idx_llm_tasks_model_id
    ON llm_tasks (model_id);

-- Best-effort: заполнить model_id для уже существующих задач,
-- если в этой же БД есть таблица llm_models (иначе просто пропустить)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'llm_models'
    ) THEN
        UPDATE llm_tasks t
        SET model_id = m.id
        FROM llm_models m
        WHERE m.model_id = t.model_name
          AND t.model_id IS NULL;
    END IF;
END
$$;
