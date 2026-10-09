import subprocess

cmd = """
kubectl exec -n igaming-dev statefulset/igaming-aggregator-db -- psql -U postgres -d igaming_aggregator -c "
SELECT 'match_record' as tbl, count(*) FROM match_record
UNION ALL
SELECT 'odds_actual', count(*) FROM odds_actual
UNION ALL
SELECT 'surebet_alert', count(*) FROM surebet_alert;
SELECT bookmaker, count(*) as active_odds FROM odds_actual GROUP BY bookmaker ORDER BY active_odds DESC LIMIT 20;
"
"""
ssh_cmd = [
    "ssh",
    "-i", r"C:\Users\chernousov_a\.ssh\id_ed25519",
    "root@100.78.183.101",
    cmd
]
res = subprocess.run(ssh_cmd, capture_output=True, text=True)
print(res.stdout)
