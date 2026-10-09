import subprocess

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

dbs = [
    ("10bet", "igaming-source-10bet-db-0", "igaming_10bet"),
    ("apuestatotal", "igaming-source-apuestatotal-db-0", "igaming_apuestatotal"),
    ("bet7k", "igaming-source-bet7k-db-0", "igaming_bet7k"),
    ("betesporte", "igaming-source-betesporte-db-0", "igaming_betesporte"),
    ("betnacional", "igaming-source-betnacional-db-0", "igaming_betnacional"),
    ("betrivers", "igaming-source-betrivers-db-0", "igaming_betrivers"),
    ("betway", "igaming-source-betway-db-0", "igaming_betway"),
    ("esportesdasorte", "igaming-source-esportesdasorte-db-0", "igaming_esportesdasorte"),
    ("paf", "igaming-source-paf-db-0", "igaming_paf"),
    ("unibet", "igaming-source-unibet-db-0", "igaming_unibet"),
    ("vaidebet", "igaming-source-vaidebet-db-0", "igaming_vaidebet")
]

print("=== CHECKING MATCH COUNTS & FACTORS IN BATCH 2 DATABASES ===")
for bm, pod, db in dbs:
    query = "SELECT count(*) FROM match_cache; SELECT count(*) FROM match_factor;"
    remote_cmd = f"kubectl exec -n igaming-source {pod} -- psql -U postgres -d {db} -t -c '{query}'"
    res = subprocess.run([
        'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', XEON_HOST, remote_cmd
    ], capture_output=True, text=True)
    lines = [l.strip() for l in res.stdout.strip().splitlines() if l.strip()]
    if len(lines) >= 2:
        print(f"{bm:18}: matches = {lines[0]:>6}, factors = {lines[1]:>8}")
    elif len(lines) == 1:
        print(f"{bm:18}: matches = {lines[0]:>6}")
    else:
        print(f"{bm:18}: {res.stderr.strip()[:60]}")
