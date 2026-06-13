-- 1. Deduplicate league_alias
WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY source_id, raw_name, sport_id ORDER BY id DESC) as rn
    FROM league_alias
)
DELETE FROM league_alias WHERE id IN (SELECT id FROM duplicates WHERE rn > 1);

ALTER TABLE league_alias DROP CONSTRAINT IF EXISTS uk5lbpqpvmhm593451dy2hj6wvp;
ALTER TABLE league_alias DROP CONSTRAINT IF EXISTS league_alias_source_id_raw_name_sport_id_key;
ALTER TABLE league_alias ADD CONSTRAINT league_alias_source_id_raw_name_sport_id_key UNIQUE (source_id, raw_name, sport_id);


-- 2. Deduplicate team_alias
WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY source_id, raw_name, sport_id ORDER BY id DESC) as rn
    FROM team_alias
)
DELETE FROM team_alias WHERE id IN (SELECT id FROM duplicates WHERE rn > 1);

ALTER TABLE team_alias DROP CONSTRAINT IF EXISTS uk9021dipq7uh5kq1pgj3l33o9e;
ALTER TABLE team_alias DROP CONSTRAINT IF EXISTS team_alias_source_id_raw_name_sport_id_key;
ALTER TABLE team_alias ADD CONSTRAINT team_alias_source_id_raw_name_sport_id_key UNIQUE (source_id, raw_name, sport_id);


-- 3. Deduplicate market_alias
WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY source_id, raw_name, sport_id ORDER BY id DESC) as rn
    FROM market_alias
)
DELETE FROM market_alias WHERE id IN (SELECT id FROM duplicates WHERE rn > 1);

ALTER TABLE market_alias DROP CONSTRAINT IF EXISTS uknykac1c5ip23hbkf5i5levgpj;
ALTER TABLE market_alias DROP CONSTRAINT IF EXISTS market_alias_source_id_raw_name_sport_id_key;
ALTER TABLE market_alias ADD CONSTRAINT market_alias_source_id_raw_name_sport_id_key UNIQUE (source_id, raw_name, sport_id);


-- 4. Deduplicate odds_type_alias
WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY source_id, raw_name, sport_id ORDER BY id DESC) as rn
    FROM odds_type_alias
)
DELETE FROM odds_type_alias WHERE id IN (SELECT id FROM duplicates WHERE rn > 1);

ALTER TABLE odds_type_alias DROP CONSTRAINT IF EXISTS ukl7xwmi741fi8dyi6a0chy9okj;
ALTER TABLE odds_type_alias DROP CONSTRAINT IF EXISTS odds_type_alias_source_id_raw_name_sport_id_key;
ALTER TABLE odds_type_alias ADD CONSTRAINT odds_type_alias_source_id_raw_name_sport_id_key UNIQUE (source_id, raw_name, sport_id);


-- 5. Deduplicate sport_alias
WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY source_id, raw_name ORDER BY id DESC) as rn
    FROM sport_alias
)
DELETE FROM sport_alias WHERE id IN (SELECT id FROM duplicates WHERE rn > 1);

ALTER TABLE sport_alias DROP CONSTRAINT IF EXISTS ukapo5o76i1x3leo0rmk7uomjkb;
ALTER TABLE sport_alias DROP CONSTRAINT IF EXISTS sport_alias_source_id_raw_name_key;
ALTER TABLE sport_alias ADD CONSTRAINT sport_alias_source_id_raw_name_key UNIQUE (source_id, raw_name);


-- 6. Deduplicate outright_selection_alias
WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY source_id, raw_name, sport_id ORDER BY id DESC) as rn
    FROM outright_selection_alias
)
DELETE FROM outright_selection_alias WHERE id IN (SELECT id FROM duplicates WHERE rn > 1);

ALTER TABLE outright_selection_alias DROP CONSTRAINT IF EXISTS ukgm8ksmq472jo5k3wo3hmwn1g8;
ALTER TABLE outright_selection_alias DROP CONSTRAINT IF EXISTS outright_selection_alias_source_id_raw_name_sport_id_key;
ALTER TABLE outright_selection_alias ADD CONSTRAINT outright_selection_alias_source_id_raw_name_sport_id_key UNIQUE (source_id, raw_name, sport_id);
