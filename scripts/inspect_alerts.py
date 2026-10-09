import psycopg2

conn = psycopg2.connect(dbname="igaming", user="postgres", password="postgres", host="igaming-aggregator-db-0.igaming-aggregator-db.igaming-dev.svc.cluster.local")
cur = conn.cursor()

alert_ids = [39326, 39364, 39365, 39402, 39390, 39395, 39385, 39343, 39391]

for aid in alert_ids:
    cur.execute("""
        SELECT a.id, a.profit_percent, m.id, m.name, m.sport, m.team1, m.team2
        FROM surebet_alert a
        JOIN match m ON a.match_id = m.id
        WHERE a.id = %s
    """, (aid,))
    row = cur.fetchone()
    if row:
        print(f"=== ALERT {row[0]}: {row[1]}% | match {row[2]} {row[3]} ({row[4]}) ===")
        cur.execute("""
            SELECT oa.bookmaker, ot.code, oa.value, oa.param, oa.updated_at, oa.external_event_id
            FROM odds_actual oa
            JOIN odds_type ot ON oa.odds_type_id = ot.id
            WHERE oa.match_id = %s
            ORDER BY oa.bookmaker, ot.code, oa.param
        """, (row[2],))
        odds = cur.fetchall()
        for o in odds:
            print(f"  {o[0]}: {o[1]} val={o[2]} param={o[3]} updated={o[4]} extId={o[5]}")
