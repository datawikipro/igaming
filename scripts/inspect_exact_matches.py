import subprocess

def run_sql(sql):
    cmd = [
        "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
        "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
    ]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.stdout

mids = [95517, 422, 103985, 97093, 103453, 33244, 41315]

for mid in mids:
    sql = f"""
    SELECT m.id, m.sport_name, m.start_time, m.updated_at,
           t1.name as team1, t2.name as team2
    FROM match_record m
    LEFT JOIN team t1 ON m.team1_id = t1.id
    LEFT JOIN team t2 ON m.team2_id = t2.id
    WHERE m.id = {mid};
    """
    print(f"================ MATCH {mid} ================")
    print(run_sql(sql))

    odds_sql = f"""
    SELECT oa.bookmaker, ot.code, oa.value, oa.param, oa.updated_at, oa.external_event_id
    FROM odds_actual oa
    JOIN odds_type ot ON oa.odds_type_id = ot.id
    WHERE oa.match_id = {mid}
      AND (ot.code LIKE '%TOTAL%' OR ot.code LIKE '%HANDICAP%' OR ot.code LIKE '%WIN%' OR ot.code LIKE '%DC%')
    ORDER BY ot.code, oa.param, oa.bookmaker;
    """
    print(run_sql(odds_sql))
