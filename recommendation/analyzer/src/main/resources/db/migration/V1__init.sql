CREATE TABLE user_actions (
    id BIGSERIAL PRIMARY KEY,                                
    user_id INTEGER NOT NULL,                               
    event_id INTEGER NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT check_action_type CHECK (action_type IN ('VIEW', 'REGISTER', 'LIKE'))
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_user_actions_user_id ON user_actions (user_id);
CREATE INDEX idx_user_actions_event_id ON user_actions (event_id);
CREATE INDEX idx_user_actions_timestamp ON user_actions (timestamp DESC);
CREATE INDEX idx_user_actions_action_type ON user_actions (action_type);
CREATE INDEX idx_user_actions_composite ON user_actions (user_id, event_id, action_type);

-- Добавление комментариев к таблице и полям для документации
COMMENT ON TABLE user_actions IS 'Сохраняет информацию о действиях пользователей по отношению к событиям';
COMMENT ON COLUMN user_actions.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN user_actions.user_id IS 'Идентификатор пользователя';
COMMENT ON COLUMN user_actions.event_id IS 'Идентификатор события';
COMMENT ON COLUMN user_actions.action_type IS 'Тип действия: VIEW (просмотр), REGISTER (регистрация), LIKE (лайк)';
COMMENT ON COLUMN user_actions.timestamp IS 'Время совершения действия';
COMMENT ON COLUMN user_actions.created_at IS 'Время создания записи в базе данных';

-- Создание таблицы схожести событий с оптимизациями
CREATE TABLE event_similarities (
    id BIGSERIAL PRIMARY KEY,
    event_a INTEGER NOT NULL,
    event_b INTEGER NOT NULL,
    score REAL NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT check_different_events CHECK (event_a != event_b),
    CONSTRAINT check_score_range CHECK (score BETWEEN 0 AND 1),
    CONSTRAINT unique_event_pair UNIQUE (event_a, event_b)
);


-- Индексы для оптимизации запросов
CREATE INDEX idx_event_similarities_event_a ON event_similarities (event_a);
CREATE INDEX idx_event_similarities_event_b ON event_similarities (event_b);
CREATE INDEX idx_event_similarities_score ON event_similarities (score DESC); -- Оптимизация для поиска высоких оценок
CREATE INDEX idx_event_similarities_timestamp ON event_similarities (timestamp DESC);
CREATE INDEX idx_event_sim_a_score ON event_similarities (event_a, score DESC); -- Композитный индекс для ускорения поиска похожих событий

-- Добавление комментариев к таблице и полям для документации
COMMENT ON TABLE event_similarities IS 'Сохраняет информацию о схожести между парами событий';
COMMENT ON COLUMN event_similarities.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN event_similarities.event_a IS 'Идентификатор первого события в паре';
COMMENT ON COLUMN event_similarities.event_b IS 'Идентификатор второго события в паре';
COMMENT ON COLUMN event_similarities.score IS 'Оценка схожести от 0 до 1, где 1 - максимальная схожесть';
COMMENT ON COLUMN event_similarities.timestamp IS 'Время вычисления схожести';
COMMENT ON COLUMN event_similarities.updated_at IS 'Время последнего обновления записи';

-- Триггер для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp
BEFORE UPDATE ON event_similarities
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- Опционально: создание материализованного представления для частых запросов
-- без автоматического обновления (для ручного обновления через приложение)
CREATE MATERIALIZED VIEW mv_event_similarity_summary AS
SELECT 
    event_a,
    COUNT(*) as similar_count,
    AVG(score) as avg_similarity_score,
    MAX(score) as max_similarity_score
FROM 
    event_similarities
GROUP BY 
    event_a;

CREATE INDEX idx_mv_event_similarity_summary ON mv_event_similarity_summary (event_a);