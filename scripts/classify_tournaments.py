"""
Tournament Classification and Coverage Tier Backfill.
Classifies all tournaments in mdm_tournament into:
- TIER_1_TACTICAL: Official lineups, player ratings, heatmaps, xG, institutional regulations (ML ELIGIBLE).
- TIER_2_BASIC: Official box scores, team stats, but no detailed player micro-stats.
- TIER_0_SCRATCH: Zero official reports, amateur, fast-cup, short-sport, cyber (IGNORED BY ML).
"""

import subprocess
import sys
import json

sys.stdout.reconfigure(encoding='utf-8')

def run_psql(db, sql):
    cmd = [
        'kubectl', 'exec', '-i', '-n', 'igaming-enrichment', 'igaming-aggregator-db-0',
        '--', 'psql', '-U', 'postgres', '-d', db
    ]
    res = subprocess.run(cmd, input=sql, text=True, capture_output=True, encoding='utf-8', errors='replace')
    if res.returncode != 0:
        print(f"Error executing on {db}: {res.stderr[:200]}")
    return res.stdout

def main():
    print("🚀 Starting tournament classification backfill...", flush=True)

    # Step 1: Classification updates
    print("1. Classifying tournaments into TIER_1_TACTICAL, TIER_2_BASIC, TIER_0_SCRATCH...", flush=True)
    tier1_sql = """
    -- Reset default to TIER_0_SCRATCH
    UPDATE mdm_tournament
    SET ml_eligible = false,
        has_official_reports = false,
        has_player_stats = false,
        coverage_tier = 'TIER_0_SCRATCH';

    -- Tier 1 Football (Major leagues & cups)
    UPDATE mdm_tournament tr
    SET ml_eligible = true,
        has_official_reports = true,
        has_player_stats = true,
        coverage_tier = 'TIER_1_TACTICAL'
    FROM mdm_sport s
    WHERE tr.sport_id = s.id
      AND s.slug = 'football'
      AND (
          tr.tier = 1
          OR tr.is_featured = true
          OR tr.popularity_rank <= 150
          OR tr.slug ~ '(premier-league|la-liga|serie-a|bundesliga|ligue-1|uefa-champions-league|uefa-europa-league|uefa-conference-league|premier-liga|eredivisie|primeira-liga|copa-libertadores|copa-sudamericana|super-lig|mls|pro-league|championship|brasileirao|primera-division|superliga|a-league)'
      )
      AND tr.canonical_name !~* '(short|3x3|4x4|5x5|6x6|7x7|8x8|battle|amateur|любител|reserve|u15|u16|u17|u18|regional|liga 4|liga 5|division 4|division 5)';

    -- Tier 1 Basketball (NBA, Euroleague, Eurocup, ACB, VTB, etc.)
    UPDATE mdm_tournament tr
    SET ml_eligible = true,
        has_official_reports = true,
        has_player_stats = true,
        coverage_tier = 'TIER_1_TACTICAL'
    FROM mdm_sport s
    WHERE tr.sport_id = s.id
      AND s.slug = 'basketball'
      AND (
          tr.tier = 1
          OR tr.is_featured = true
          OR tr.popularity_rank <= 100
          OR tr.slug ~ '(nba|euroleague|eurocup|liga-acb|vtb-united-league|bbl|lega-basket-serie-a|lnb-pro-a|fiba|cba|nbl)'
      )
      AND tr.canonical_name !~* '(short|3x3|4x4|battle|amateur|cyber)';

    -- Tier 1 Ice Hockey (NHL, KHL, SHL, Liiga, DEL, Extraliga)
    UPDATE mdm_tournament tr
    SET ml_eligible = true,
        has_official_reports = true,
        has_player_stats = true,
        coverage_tier = 'TIER_1_TACTICAL'
    FROM mdm_sport s
    WHERE tr.sport_id = s.id
      AND s.slug = 'ice-hockey'
      AND (
          tr.tier = 1
          OR tr.is_featured = true
          OR tr.popularity_rank <= 100
          OR tr.slug ~ '(nhl|khl|shl|liiga|del|extraliga|national-league|iihf)'
      )
      AND tr.canonical_name !~* '(short|3x3|battle|amateur|cyber)';

    -- Tier 1 Tennis (ATP, WTA, Grand Slam)
    UPDATE mdm_tournament tr
    SET ml_eligible = true,
        has_official_reports = true,
        has_player_stats = true,
        coverage_tier = 'TIER_1_TACTICAL'
    FROM mdm_sport s
    WHERE tr.sport_id = s.id
      AND s.slug = 'tennis'
      AND (
          tr.tier = 1
          OR tr.is_featured = true
          OR tr.slug ~ '(atp|wta|wimbledon|us-open|roland-garros|australian-open|davis-cup|billie-jean-king)'
      )
      AND tr.canonical_name !~* '(itf|daily|amateur|utr|exhibition)';

    -- Tier 2 Basic (Solid pro leagues with official box score, but secondary stats)
    UPDATE mdm_tournament tr
    SET has_official_reports = true,
        has_player_stats = false,
        coverage_tier = 'TIER_2_BASIC'
    FROM mdm_sport s
    WHERE tr.sport_id = s.id
      AND tr.coverage_tier = 'TIER_0_SCRATCH'
      AND (
          tr.tier = 2
          OR (tr.popularity_rank <= 500 AND tr.sofascore_id IS NOT NULL)
      )
      AND tr.canonical_name !~* '(short|3x3|4x4|battle|amateur|cyber|setka|liga pro|tt cup)';
    """
    out2 = run_psql('igaming_mdm', tier1_sql)
    print("Result:", out2.strip())

    # Step 3: Stats check
    stats_sql = """
    SELECT 
        coverage_tier,
        ml_eligible,
        has_official_reports,
        has_player_stats,
        count(*) as tournament_count
    FROM mdm_tournament
    GROUP BY coverage_tier, ml_eligible, has_official_reports, has_player_stats
    ORDER BY count(*) DESC;
    """
    print("\n3. Tournament classification summary in mdm_tournament:")
    out3 = run_psql('igaming_mdm', stats_sql)
    print(out3)

    # Step 4: Active matches check
    active_matches_sql = """
    SELECT 
        tr.coverage_tier,
        tr.ml_eligible,
        count(m.id) as active_matches
    FROM mdm_match m
    JOIN mdm_tournament tr ON m.tournament_id = tr.id
    WHERE (m.status IS NULL OR m.status != 'CANCELLED')
      AND m.start_time >= NOW() - INTERVAL '2 hours'
      AND m.start_time <= NOW() + INTERVAL '7 days'
    GROUP BY tr.coverage_tier, tr.ml_eligible
    ORDER BY count(m.id) DESC;
    """
    print("\n4. Active matches distribution by coverage tier:")
    out4 = run_psql('igaming_mdm', active_matches_sql)
    print(out4)

if __name__ == '__main__':
    main()
