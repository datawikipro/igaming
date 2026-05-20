-- ============================================================
-- Cleanup: Remove phantom surebets with implausible profit%
-- Root cause: crawler bugs producing extremely high odds
--             (e.g. 99, 999) that made profit_percent reach 70%+
-- Safe to run: marks them EXPIRED, does NOT delete.
-- ============================================================

-- 1. Preview what will be marked expired
SELECT id, match_description, profit_percent, margin, detected_at, status
FROM surebet_alert
WHERE profit_percent > 20.0
  AND status = 'ACTIVE'
ORDER BY profit_percent DESC;

-- 2. Mark all phantom surebets as EXPIRED (uncomment to apply)
-- UPDATE surebet_alert
-- SET status = 'EXPIRED',
--     expired_at = NOW()
-- WHERE profit_percent > 20.0
--   AND status = 'ACTIVE';

-- 3. Stats: how many phantom vs real active surebets exist right now
SELECT
    CASE WHEN profit_percent > 20.0 THEN 'PHANTOM (>20%)' ELSE 'REAL (<=20%)' END AS category,
    COUNT(*) AS count,
    ROUND(AVG(profit_percent)::numeric, 2) AS avg_profit_pct,
    ROUND(MAX(profit_percent)::numeric, 2) AS max_profit_pct
FROM surebet_alert
WHERE status = 'ACTIVE'
GROUP BY 1
ORDER BY 1;
