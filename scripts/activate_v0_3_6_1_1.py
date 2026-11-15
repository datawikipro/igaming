import psycopg2

conn = psycopg2.connect(
    dbname='igaming_analytics',
    user='postgres',
    password='postgres',
    host='10.244.0.57'
)
cur = conn.cursor()

# Deactivate previous versions
cur.execute("UPDATE ai_model_version SET is_active = false WHERE is_active = true")

# Insert new active version v0.3.6.1.1
cur.execute("""
INSERT INTO ai_model_version (
    version_code, display_name, ideology_version, vector_version, loader_version,
    iteration_number, release_date, is_active, description, changelog
) VALUES (
    'v0.3.6.1.1',
    'Universal Multi-Tier Glicko-2 MMR Calibration (Ice Hockey & Basketball Wired)',
    '3', '6', '1', 1, NOW(), true,
    'Universal Multi-Tier Glicko-2 dynamic sports MMR integration for Ice Hockey (3,924 teams) and Basketball (11,706 teams) with full Python/Java tensor parity (Dims 14..19, 52..53). Eliminates the underdog +EV phantom edge trap in high-scoring sports.',
    '1. Universal Multi-Tier Glicko-2 ratings backfilled for Ice Hockey (3,924 teams) and Basketball (11,706 teams) in ClickHouse DDS and PostgreSQL.\n2. IceHockeyVectorExtractor and BasketballVectorExtractor wired to query sport_mmr_rating by effective ID.\n3. Complete tensor parity with Python prepare_multitask_matrix.py (Dims 14..19, 52..53).\n4. Eliminates flat-prior underdog +EV illusion in hockey and basketball.'
) ON CONFLICT (version_code) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    is_active = EXCLUDED.is_active,
    description = EXCLUDED.description,
    changelog = EXCLUDED.changelog;
""")

conn.commit()
print("Successfully activated v0.3.6.1.1 in ai_model_version!")
cur.close()
conn.close()
