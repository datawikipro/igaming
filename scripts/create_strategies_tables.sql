-- Migration: Multi-Strategy Ladder and Voting Engine
-- 1. Table for Strategy Templates (7 default ladder strategies + user custom templates)
CREATE TABLE IF NOT EXISTS ai_strategy_template (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(20) NOT NULL DEFAULT '🛡️',
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE,
    min_odd NUMERIC(5,2) NOT NULL DEFAULT 1.30,
    max_odd NUMERIC(5,2) NULL,
    min_probability_pct NUMERIC(5,2) NOT NULL DEFAULT 88.0,
    ladder_step_rule VARCHAR(50) NOT NULL DEFAULT 'SAFEST_ELIGIBLE',
    pure_win_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    target_risk_tier VARCHAR(20) NOT NULL DEFAULT 'LOW',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2. Table for User Active Strategy Setting (fallback to default 'nerves_shield')
CREATE TABLE IF NOT EXISTS ai_user_strategy_setting (
    user_id BIGINT PRIMARY KEY,
    active_strategy_slug VARCHAR(50) NOT NULL REFERENCES ai_strategy_template(slug) ON UPDATE CASCADE,
    custom_overrides_json JSONB NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 3. Table for Strategy Votes per Recommendation (Audit and Multi-Strategy PnL)
CREATE TABLE IF NOT EXISTS ai_strategy_vote (
    id BIGSERIAL PRIMARY KEY,
    recommendation_id BIGINT NOT NULL REFERENCES match_ai_recommendation(id) ON DELETE CASCADE,
    match_id BIGINT NOT NULL,
    strategy_slug VARCHAR(50) NOT NULL,
    strategy_name VARCHAR(100) NOT NULL,
    market VARCHAR(50) NOT NULL,
    market_label VARCHAR(100),
    recommended_odd NUMERIC(6,3),
    fair_probability NUMERIC(5,2),
    expected_value_pct NUMERIC(6,2),
    vote_decision VARCHAR(20) NOT NULL DEFAULT 'PICK', -- PICK, PASS, NO_ELIGIBLE_MARKET
    stake_rub NUMERIC(10,2) NOT NULL DEFAULT 250.00,
    result_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, WON, LOST, VOID, SKIPPED
    final_score VARCHAR(50),
    actual_pnl NUMERIC(10,2) DEFAULT 0.00,
    roi_pct NUMERIC(6,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    settled_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_strategy_vote_rec ON ai_strategy_vote(recommendation_id);
CREATE INDEX IF NOT EXISTS idx_strategy_vote_strat ON ai_strategy_vote(strategy_slug);
CREATE INDEX IF NOT EXISTS idx_strategy_vote_status ON ai_strategy_vote(result_status);

-- 4. Insert / Upsert the 7 Default Ladder Strategies
INSERT INTO ai_strategy_template 
(slug, name, icon, description, is_system_default, min_odd, max_odd, min_probability_pct, ladder_step_rule, pure_win_allowed, target_risk_tier, sort_order)
VALUES
(
    'ultra_sniper',
    'Ультра-Снайпер',
    '🎯',
    'Редчайшие железобетонные матчи (1-3 в месяц). Только максимальная надежность (95%+), безопасные ступени лесенки (1X, ТМ 3.5, Фора +1.5). Кайф без риска.',
    FALSE,
    1.30,
    1.50,
    95.0,
    'SAFEST_ELIGIBLE',
    FALSE,
    'ULTRA_LOW',
    1
),
(
    'nerves_shield',
    'Защита нервов',
    '🛡️',
    'Системный стандарт SmartBet по умолчанию. Минимизация ошибок, отсечение микрокэфов < 1.30, автоматическая защита ничьей (1X вместо рискованной П1). Таргетный винрейт 90%+.',
    TRUE,
    1.30,
    1.65,
    88.0,
    'SAFEST_ELIGIBLE',
    FALSE,
    'LOW',
    2
),
(
    'iron_bank',
    'Железный банк',
    '🏰',
    'Консервативный рост капитала. Форы (0) / Draw No Bet при рисках ничьей, умеренные тоталы ТМ 3.0 / ТБ 1.5, форы по сетам. Минимальная просадка.',
    FALSE,
    1.35,
    1.78,
    82.0,
    'SAFEST_ELIGIBLE',
    FALSE,
    'LOW_MEDIUM',
    3
),
(
    'balanced_growth',
    'Сбалансированный',
    '⚖️',
    'Оптимум по критерию Келли. Баланс между коэффициентом и проходимостью. Чистые победы при явном перевесе, ТБ/ТМ 2.5, форы (0). Диапазон 1.50–1.95.',
    FALSE,
    1.50,
    1.95,
    72.0,
    'BALANCED',
    TRUE,
    'MEDIUM',
    4
),
(
    'tactical_value',
    'Тактический Value',
    '📈',
    'Умеренно-агрессивный поиск перевеса над линией букмекера. Чистые победы на равных котировках, тоталы с повышенной доходностью. Диапазон 1.70–2.25.',
    FALSE,
    1.70,
    2.25,
    63.0,
    'MAX_ODD_ELIGIBLE',
    TRUE,
    'MEDIUM_HIGH',
    5
),
(
    'high_odds',
    'Высокий кэф',
    '🚀',
    'Для игроков, готовых к дисперсии ради крупных выигрышей. Плюсовые форы андердогов, победы гостей, ничьи. Коэффициенты 2.05–2.85.',
    FALSE,
    2.05,
    2.85,
    55.0,
    'MAX_ODD_ELIGIBLE',
    TRUE,
    'HIGH',
    6
),
(
    'jackpot_outlier',
    'Джекпот / Аномалия',
    '💣',
    'Максимальный риск и выстрелы в яблочко. Аномальные перевесы модели против ошибочной линии рынка, чистые ничьи, крупные тоталы. Коэффициенты 2.80 - 4.00.',
    FALSE,
    2.80,
    4.00,
    45.0,
    'MAX_ODD_ELIGIBLE',
    TRUE,
    'ULTRA_HIGH',
    7
)
ON CONFLICT (slug) DO UPDATE SET
    name = EXCLUDED.name,
    icon = EXCLUDED.icon,
    description = EXCLUDED.description,
    is_system_default = EXCLUDED.is_system_default,
    min_odd = EXCLUDED.min_odd,
    max_odd = EXCLUDED.max_odd,
    min_probability_pct = EXCLUDED.min_probability_pct,
    ladder_step_rule = EXCLUDED.ladder_step_rule,
    pure_win_allowed = EXCLUDED.pure_win_allowed,
    target_risk_tier = EXCLUDED.target_risk_tier,
    sort_order = EXCLUDED.sort_order;
