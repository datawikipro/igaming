CREATE TABLE IF NOT EXISTS public.match_enrichment (
    match_id BIGINT PRIMARY KEY,
    motivation_desc TEXT,
    motivation_score1 INTEGER,
    motivation_score2 INTEGER,
    match_importance INTEGER,
    popularity_trap TEXT,
    lineups_json TEXT,
    h2h_json TEXT,
    storyline_json TEXT,
    enrichment_captured_at TIMESTAMP WITHOUT TIME ZONE,
    closing_narrative TEXT,
    missing_count_team1 INTEGER DEFAULT 0,
    missing_count_team2 INTEGER DEFAULT 0,
    rotation_count_team1 INTEGER DEFAULT 0,
    rotation_count_team2 INTEGER DEFAULT 0,
    avg_rating_team1 DOUBLE PRECISION,
    avg_rating_team2 DOUBLE PRECISION,
    is_lineup_confirmed BOOLEAN DEFAULT FALSE,
    has_broadcast BOOLEAN DEFAULT FALSE,
    broadcasts_json TEXT,
    bookmaker_urls TEXT,
    format_info TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION fn_match_record_update()
RETURNS trigger AS $$
BEGIN
    INSERT INTO match_enrichment (
        match_id,
        motivation_desc,
        motivation_score1,
        motivation_score2,
        match_importance,
        popularity_trap,
        lineups_json,
        h2h_json,
        storyline_json,
        enrichment_captured_at,
        closing_narrative,
        missing_count_team1,
        missing_count_team2,
        rotation_count_team1,
        rotation_count_team2,
        avg_rating_team1,
        avg_rating_team2,
        is_lineup_confirmed,
        has_broadcast,
        broadcasts_json,
        bookmaker_urls,
        format_info,
        updated_at
    ) VALUES (
        NEW.id,
        NEW.motivation_desc,
        NEW.motivation_score1,
        NEW.motivation_score2,
        NEW.match_importance,
        NEW.popularity_trap,
        NEW.lineups_json,
        NEW.h2h_json,
        NEW.storyline_json,
        COALESCE(NEW.enrichment_captured_at, NOW()),
        NEW.closing_narrative,
        COALESCE(NEW.missing_count_team1, 0),
        COALESCE(NEW.missing_count_team2, 0),
        COALESCE(NEW.rotation_count_team1, 0),
        COALESCE(NEW.rotation_count_team2, 0),
        NEW.avg_rating_team1,
        NEW.avg_rating_team2,
        COALESCE(NEW.is_lineup_confirmed, false),
        COALESCE(NEW.has_broadcast, false),
        NEW.broadcasts_json,
        NEW.bookmaker_urls,
        NEW.format_info,
        NOW()
    )
    ON CONFLICT (match_id) DO UPDATE SET
        motivation_desc = COALESCE(EXCLUDED.motivation_desc, match_enrichment.motivation_desc),
        motivation_score1 = COALESCE(EXCLUDED.motivation_score1, match_enrichment.motivation_score1),
        motivation_score2 = COALESCE(EXCLUDED.motivation_score2, match_enrichment.motivation_score2),
        match_importance = COALESCE(EXCLUDED.match_importance, match_enrichment.match_importance),
        popularity_trap = COALESCE(EXCLUDED.popularity_trap, match_enrichment.popularity_trap),
        lineups_json = COALESCE(EXCLUDED.lineups_json, match_enrichment.lineups_json),
        h2h_json = COALESCE(EXCLUDED.h2h_json, match_enrichment.h2h_json),
        storyline_json = COALESCE(EXCLUDED.storyline_json, match_enrichment.storyline_json),
        enrichment_captured_at = COALESCE(EXCLUDED.enrichment_captured_at, match_enrichment.enrichment_captured_at),
        closing_narrative = COALESCE(EXCLUDED.closing_narrative, match_enrichment.closing_narrative),
        missing_count_team1 = COALESCE(EXCLUDED.missing_count_team1, match_enrichment.missing_count_team1),
        missing_count_team2 = COALESCE(EXCLUDED.missing_count_team2, match_enrichment.missing_count_team2),
        rotation_count_team1 = COALESCE(EXCLUDED.rotation_count_team1, match_enrichment.rotation_count_team1),
        rotation_count_team2 = COALESCE(EXCLUDED.rotation_count_team2, match_enrichment.rotation_count_team2),
        avg_rating_team1 = COALESCE(EXCLUDED.avg_rating_team1, match_enrichment.avg_rating_team1),
        avg_rating_team2 = COALESCE(EXCLUDED.avg_rating_team2, match_enrichment.avg_rating_team2),
        is_lineup_confirmed = COALESCE(EXCLUDED.is_lineup_confirmed, match_enrichment.is_lineup_confirmed),
        has_broadcast = COALESCE(EXCLUDED.has_broadcast, match_enrichment.has_broadcast),
        broadcasts_json = COALESCE(EXCLUDED.broadcasts_json, match_enrichment.broadcasts_json),
        bookmaker_urls = COALESCE(EXCLUDED.bookmaker_urls, match_enrichment.bookmaker_urls),
        format_info = COALESCE(EXCLUDED.format_info, match_enrichment.format_info),
        updated_at = NOW();

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP VIEW IF EXISTS public.match_record CASCADE;

CREATE VIEW public.match_record AS
SELECT 
    m.id,
    m.home_team_id AS team1_id,
    m.away_team_id AS team2_id,
    m.tournament_id AS league_id,
    s.domain_key AS sport_name,
    CASE
        WHEN m.start_time IS NOT NULL THEN (EXTRACT(epoch FROM m.start_time) * 1000::numeric)::bigint
        ELSE NULL::bigint
    END AS start_time,
    m.status::text = 'LIVE'::text AS is_live,
    COALESCE(e.has_broadcast, false) AS has_broadcast,
    1 AS source_count,
    NULL::character varying(10) AS match_result,
    e.format_info AS format_info,
    e.bookmaker_urls AS bookmaker_urls,
    COALESCE(m.source_origin, 'SOFASCORE')::character varying(50) AS reference_provider,
    m.sofascore_id::character varying(100) AS reference_fixture_id,
    m.sofascore_id IS NOT NULL AS reference_verified,
    m.sofascore_id IS NOT NULL AS is_reference_verified,
    m.created_at,
    m.updated_at,
    e.motivation_desc,
    e.motivation_score1,
    e.motivation_score2,
    e.match_importance,
    false AS is_exported,
    e.popularity_trap,
    NULL::character varying(255) AS created_by_source_id,
    NULL::character varying(255) AS last_updated_by_source_id,
    m.score1::character varying(20) AS current_score1,
    m.score2::character varying(20) AS current_score2,
    NULL::character varying(30) AS current_minute,
    m.score1::character varying(20) AS final_score1,
    m.score2::character varying(20) AS final_score2,
    NULL::timestamp without time zone AS result_captured_at,
    e.lineups_json,
    e.h2h_json,
    e.storyline_json,
    e.enrichment_captured_at,
    e.closing_narrative,
    COALESCE(e.missing_count_team1, 0) AS missing_count_team1,
    COALESCE(e.missing_count_team2, 0) AS missing_count_team2,
    COALESCE(e.rotation_count_team1, 0) AS rotation_count_team1,
    COALESCE(e.rotation_count_team2, 0) AS rotation_count_team2,
    e.avg_rating_team1,
    e.avg_rating_team2,
    COALESCE(e.is_lineup_confirmed, false) AS is_lineup_confirmed,
    e.broadcasts_json
FROM mdm_match m
LEFT JOIN mdm_sport s ON m.sport_id = s.id
LEFT JOIN match_enrichment e ON e.match_id = m.id;

CREATE TRIGGER trg_match_record_upd
    INSTEAD OF UPDATE ON public.match_record
    FOR EACH ROW EXECUTE FUNCTION fn_match_record_update();

CREATE RULE match_record_ins AS
    ON INSERT TO public.match_record DO INSTEAD NOTHING;

CREATE RULE match_record_del AS
    ON DELETE TO public.match_record DO INSTEAD NOTHING;
