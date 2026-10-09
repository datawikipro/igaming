-- Migration: Isolate synthetic matchday and round statistical markets into SYNTHETIC_STATISTICS
BEGIN;

-- 1. Ensure SYNTHETIC_STATISTICS sport exists
INSERT INTO sport (id, display_name, priority, is_temporary, is_three_way_possible)
VALUES ('SYNTHETIC_STATISTICS', 'Статистика тура / Спецставки', -100, false, true)
ON CONFLICT (id) DO NOTHING;

-- 2. Update existing synthetic matches in match_record
UPDATE match_record
SET sport_name = 'SYNTHETIC_STATISTICS'
WHERE id IN (
    SELECT m.id FROM match_record m
    JOIN team t1 ON m.team1_id = t1.id
    JOIN team t2 ON m.team2_id = t2.id
    WHERE (
        (LOWER(t1.default_display_name) LIKE '%хозяев%' AND LOWER(t2.default_display_name) LIKE '%гост%')
        OR (LOWER(t1.default_display_name) LIKE '%гост%' AND LOWER(t2.default_display_name) LIKE '%хозяев%')
        OR (LOWER(t1.default_display_name) LIKE '%home%' AND LOWER(t2.default_display_name) LIKE '%away%')
        OR (LOWER(t1.default_display_name) LIKE '%away%' AND LOWER(t2.default_display_name) LIKE '%home%')
        OR LOWER(t1.default_display_name) LIKE '%(голы)%'
        OR LOWER(t1.default_display_name) LIKE '%(раны)%'
        OR LOWER(t1.default_display_name) LIKE '%(угл)%'
        OR LOWER(t1.default_display_name) LIKE '%(жк)%'
        OR LOWER(t2.default_display_name) LIKE '%(голы)%'
        OR LOWER(t2.default_display_name) LIKE '%(раны)%'
        OR LOWER(t2.default_display_name) LIKE '%(угл)%'
        OR LOWER(t2.default_display_name) LIKE '%(жк)%'
    )
    AND NOT (
        -- Exclude real tennis players (e.g. Simone Agostini)
        LOWER(t2.default_display_name) LIKE '%агостини%'
    )
);

-- 3. Delete any lingering alerts on synthetic matches
DELETE FROM surebet_outcome_legs WHERE alert_id IN (
    SELECT id FROM surebet_alert WHERE match_id IN (
        SELECT id FROM match_record WHERE sport_name = 'SYNTHETIC_STATISTICS'
    )
);
DELETE FROM surebet_alert WHERE match_id IN (
    SELECT id FROM match_record WHERE sport_name = 'SYNTHETIC_STATISTICS'
);
DELETE FROM valuebet_alert WHERE match_id IN (
    SELECT id FROM match_record WHERE sport_name = 'SYNTHETIC_STATISTICS'
);
DELETE FROM middle_alert WHERE match_id IN (
    SELECT id FROM match_record WHERE sport_name = 'SYNTHETIC_STATISTICS'
);

COMMIT;
