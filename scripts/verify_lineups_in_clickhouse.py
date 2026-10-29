import requests

url = "http://clickhouse.igaming-analytics.svc.cluster.local:8123/?database=igaming_dds&user=default&password=clickhouse"
r = requests.post(url, data="SELECT mdm_match_id, team_id, is_home, formation, is_confirmed, avg_starter_rating, avg_starter_height_cm, squad_market_value_eur, bench_attacking_depth_rating, substring(starters_json, 1, 100), substring(bench_json, 1, 100) FROM football_lineup_fact LIMIT 3 FORMAT Vertical")
print("Sample football_lineup_fact rows:\n", r.text)

r_cnt = requests.post(url, data="SELECT count(), countIf(avg_starter_rating > 0), countIf(avg_starter_height_cm > 0), countIf(bench_attacking_depth_rating > 0) FROM football_lineup_fact FORMAT TabSeparatedWithNames")
print("\nStats:\n", r_cnt.text)
