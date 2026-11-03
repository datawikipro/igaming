-- ============================================================================
-- ClickHouse DDS: Tournament Scoring Baseline & League Categorizer
-- Computes regulation time (основное время без овертайма и штрафных) averages
-- ============================================================================

CREATE TABLE IF NOT EXISTS igaming_dds.tournament_scoring_baseline (
    sport LowCardinality(String),
    tournament_id UInt64,
    tournament_name String,
    matches_count UInt32,
    regulation_avg_home Float32,
    regulation_avg_away Float32,
    regulation_avg_total Float32,
    regulation_median_total Float32,
    cushion_low_total Float32,
    cushion_high_total Float32,
    handicap_cushion_delta Float32,
    scoring_category LowCardinality(String),
    updated_at DateTime DEFAULT now()
) ENGINE = ReplacingMergeTree(updated_at)
PRIMARY KEY (sport, tournament_id)
ORDER BY (sport, tournament_id);

-- 1. Basketball (Strictly regular 4 quarters, excluding overtime home_ot / away_ot)
INSERT INTO igaming_dds.tournament_scoring_baseline (
    sport, tournament_id, tournament_name, matches_count,
    regulation_avg_home, regulation_avg_away, regulation_avg_total,
    regulation_median_total, cushion_low_total, cushion_high_total,
    handicap_cushion_delta, scoring_category, updated_at
)
SELECT 
    'basketball' AS sport,
    b.tournament_id AS tournament_id,
    any(t.canonical_name) AS tournament_name,
    count() AS matches_count,
    round(avg(if(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4 > 0, toFloat64(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4), toFloat64(toInt32(b.home_score) - toInt32(b.home_ot)))), 2) AS reg_avg_home,
    round(avg(if(b.away_q1 + b.away_q2 + b.away_q3 + b.away_q4 > 0, toFloat64(b.away_q1 + b.away_q2 + b.away_q3 + b.away_q4), toFloat64(toInt32(b.away_score) - toInt32(b.away_ot)))), 2) AS reg_avg_away,
    round(avg(if(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4 > 0, toFloat64(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4 + b.away_q1 + b.away_q2 + b.away_q3 + b.away_q4), toFloat64(toInt32(b.home_score) - toInt32(b.home_ot) + toInt32(b.away_score) - toInt32(b.away_ot)))), 2) AS reg_avg_total,
    round(median(if(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4 > 0, toFloat64(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4 + b.away_q1 + b.away_q2 + b.away_q3 + b.away_q4), toFloat64(toInt32(b.home_score) - toInt32(b.home_ot) + toInt32(b.away_score) - toInt32(b.away_ot)))), 1) AS reg_median_total,
    round((reg_median_total - if(reg_median_total * 0.14 > 18.0, reg_median_total * 0.14, 18.0)) * 2) / 2 AS cushion_low_total,
    round((reg_median_total + if(reg_median_total * 0.15 > 20.0, reg_median_total * 0.15, 20.0)) * 2) / 2 AS cushion_high_total,
    round(median(if(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4 > 0, abs(toFloat64(b.home_q1 + b.home_q2 + b.home_q3 + b.home_q4) - toFloat64(b.away_q1 + b.away_q2 + b.away_q3 + b.away_q4)), abs(toFloat64(toInt32(b.home_score) - toInt32(b.home_ot)) - toFloat64(toInt32(b.away_score) - toInt32(b.away_ot))))) * 2) / 2 AS handicap_cushion_delta,
    multiIf(
        reg_median_total >= 205.0, 'ULTRA_PACE_CBA_NBA',
        reg_median_total >= 175.0, 'HIGH_PACE',
        reg_median_total >= 155.0, 'NORMAL_PACE_FIBA',
        'LOW_PACE_DEFENSIVE'
    ) AS scoring_category,
    now() AS updated_at
FROM igaming_dds.basketball_match_fact b
LEFT JOIN igaming_dds.dict_tournament t ON b.tournament_id = t.id
WHERE b.tournament_id IS NOT NULL AND b.tournament_id > 0 AND b.home_score > 0 AND b.away_score > 0
GROUP BY b.tournament_id
HAVING matches_count >= 3;

-- 2. Football (90 minutes regular score)
INSERT INTO igaming_dds.tournament_scoring_baseline (
    sport, tournament_id, tournament_name, matches_count,
    regulation_avg_home, regulation_avg_away, regulation_avg_total,
    regulation_median_total, cushion_low_total, cushion_high_total,
    handicap_cushion_delta, scoring_category, updated_at
)
SELECT 
    'football' AS sport,
    f.tournament_id AS tournament_id,
    any(t.canonical_name) AS tournament_name,
    count() AS matches_count,
    round(avg(toFloat64(f.home_score)), 2) AS reg_avg_home,
    round(avg(toFloat64(f.away_score)), 2) AS reg_avg_away,
    round(avg(toFloat64(f.home_score + f.away_score)), 2) AS reg_avg_total,
    round(median(toFloat64(f.home_score + f.away_score)), 1) AS reg_median_total,
    if(reg_median_total >= 3.5, 2.5, if(reg_median_total <= 2.0, 0.5, 1.5)) AS cushion_low_total,
    if(reg_median_total >= 3.5, 4.5, if(reg_median_total <= 2.0, 2.5, 3.5)) AS cushion_high_total,
    if(reg_avg_total >= 3.3, 2.0, 1.5) AS handicap_cushion_delta,
    multiIf(
        reg_avg_total >= 3.3, 'ULTRA_HIGH_YOUTH',
        reg_avg_total >= 2.85, 'HIGH_SCORING',
        reg_avg_total >= 2.3, 'NORMAL_BALANCED',
        'LOW_SCORING_DEFENSIVE'
    ) AS scoring_category,
    now() AS updated_at
FROM igaming_dds.football_match_fact f
LEFT JOIN igaming_dds.dict_tournament t ON f.tournament_id = t.id
WHERE f.tournament_id IS NOT NULL AND f.tournament_id > 0 AND f.home_score < 20 AND f.away_score < 20
GROUP BY f.tournament_id
HAVING matches_count >= 5;

-- 3. Ice Hockey (60 minutes regular score, excluding overtime and shootouts)
INSERT INTO igaming_dds.tournament_scoring_baseline (
    sport, tournament_id, tournament_name, matches_count,
    regulation_avg_home, regulation_avg_away, regulation_avg_total,
    regulation_median_total, cushion_low_total, cushion_high_total,
    handicap_cushion_delta, scoring_category, updated_at
)
SELECT 
    'ice-hockey' AS sport,
    h.tournament_id AS tournament_id,
    any(t.canonical_name) AS tournament_name,
    count() AS matches_count,
    round(avg(toFloat64(h.home_p1 + h.home_p2 + h.home_p3)), 2) AS reg_avg_home,
    round(avg(toFloat64(h.away_p1 + h.away_p2 + h.away_p3)), 2) AS reg_avg_away,
    round(avg(toFloat64(h.home_p1 + h.home_p2 + h.home_p3 + h.away_p1 + h.away_p2 + h.away_p3)), 2) AS reg_avg_total,
    round(median(toFloat64(h.home_p1 + h.home_p2 + h.home_p3 + h.away_p1 + h.away_p2 + h.away_p3)), 1) AS reg_median_total,
    if(reg_median_total >= 6.5, 5.5, 4.5) AS cushion_low_total,
    if(reg_median_total >= 6.5, 7.5, 6.5) AS cushion_high_total,
    1.5 AS handicap_cushion_delta,
    multiIf(
        reg_avg_total >= 6.2, 'HIGH_SCORING',
        reg_avg_total >= 5.0, 'NORMAL_BALANCED',
        'LOW_SCORING'
    ) AS scoring_category,
    now() AS updated_at
FROM igaming_dds.ice_hockey_match_fact h
LEFT JOIN igaming_dds.dict_tournament t ON h.tournament_id = t.id
WHERE h.tournament_id IS NOT NULL AND h.tournament_id > 0 AND h.home_score >= 0 AND h.away_score >= 0
GROUP BY h.tournament_id
HAVING matches_count >= 3;

-- ============================================================================
-- 4. Country & Demographic Scoring Baseline (Cross-League Domestic Anchors)
-- Used when a tournament has no seasonal statistics or is an international/friendly cross-match.
-- ============================================================================

CREATE TABLE IF NOT EXISTS igaming_dds.country_scoring_baseline (
    sport LowCardinality(String),
    country_id UInt32,
    country_name String,
    gender LowCardinality(String),
    age_category LowCardinality(String),
    matches_count UInt32,
    regulation_avg_total Float32,
    regulation_median_total Float32,
    cushion_low_total Float32,
    cushion_high_total Float32,
    scoring_category LowCardinality(String),
    updated_at DateTime DEFAULT now()
) ENGINE = ReplacingMergeTree(updated_at)
PRIMARY KEY (sport, country_id, gender, age_category)
ORDER BY (sport, country_id, gender, age_category);

INSERT INTO igaming_dds.country_scoring_baseline (
    sport, country_id, country_name, gender, age_category,
    matches_count, regulation_avg_total, regulation_median_total,
    cushion_low_total, cushion_high_total, scoring_category, updated_at
)
SELECT 
    'football' AS sport,
    toUInt32(t.country_id) AS country_id,
    any(c.canonical_name) AS country_name,
    t.gender AS gender,
    t.age_category AS age_category,
    count() AS matches_count,
    round(avg(f.home_score + f.away_score), 2) AS reg_avg_total,
    round(median(f.home_score + f.away_score), 1) AS reg_median_total,
    if(reg_median_total >= 3.5, 2.5, if(reg_median_total <= 2.0, 0.5, 1.5)) AS cushion_low_total,
    if(reg_median_total >= 3.5, 4.5, if(reg_median_total <= 2.0, 2.5, 3.5)) AS cushion_high_total,
    multiIf(
        reg_avg_total >= 3.3, 'ULTRA_HIGH_YOUTH',
        reg_avg_total >= 2.85, 'HIGH_SCORING',
        reg_avg_total >= 2.3, 'NORMAL_BALANCED',
        'LOW_SCORING_DEFENSIVE'
    ) AS scoring_category,
    now() AS updated_at
FROM igaming_dds.football_match_fact f
JOIN postgresql('igaming-aggregator-db.igaming-enrichment.svc.cluster.local:5432', 'igaming_mdm', 'mdm_tournament', 'postgres', 'postgres') t
    ON toInt64(f.tournament_id) = t.id
JOIN postgresql('igaming-aggregator-db.igaming-enrichment.svc.cluster.local:5432', 'igaming_mdm', 'mdm_country', 'postgres', 'postgres') c
    ON toInt32(t.country_id) = toInt32(c.id)
WHERE t.sport_id = 1 AND f.home_score < 20 AND f.away_score < 20 AND t.country_id > 0
GROUP BY t.country_id, t.gender, t.age_category
HAVING count() >= 30;

