import subprocess
import sys

modules = [
    ('igaming-dto', 'feat(taxonomy): expand betting scopes, stats, BTTS, HT/FT, team to score, winning margin'),
    ('igaming-source-core', 'feat(taxonomy): add multi-sport stat and scope matchers, support new bet scopes and stats'),
    ('aggregator-surebet', 'feat(surebet): implement surebet evaluation rules for HT/FT, Team to Score, and Winning Margin'),
    ('igaming-source-winline', 'feat(winline): add WinlineFootballSpecialMapper for BTTS, HT/FT, Team to Score, fix HikariCP config'),
    ('igaming-source-fon-bet-ru', 'feat(fonbet): map special markets in FonbetCommonSpecialMapper and FonbetOddsService'),
    ('igaming-source-betcity', 'feat(betcity): add BetcityFootballSpecialMapper and BetcityTennisSpecialMapper'),
    ('igaming-source-marathonbet', 'feat(marathonbet): add MarathonFootballSpecialMapper for HT/FT, Team to Score, BTTS'),
    ('igaming-source-baltbet', 'feat(baltbet): map HT/FT and special factors in BaltbetCommonMapper'),
    ('igaming-source-olimpbet', 'feat(olimpbet): map HT/FT and multi-sport markets in OlimpbetCommonResultMapper'),
]

for repo, msg in modules:
    print(f"=== Committing {repo} ===")
    subprocess.run(['git', '-C', repo, 'add', '.'], check=False)
    diff = subprocess.run(['git', '-C', repo, 'diff', '--cached', '--quiet'], check=False)
    if diff.returncode != 0:
        c_res = subprocess.run(['git', '-C', repo, 'commit', '-m', msg], capture_output=True, text=True)
        print(c_res.stdout.strip())
        p_res = subprocess.run(['git', '-C', repo, 'push', 'origin', 'HEAD'], capture_output=True, text=True, timeout=60)
        print(p_res.stdout.strip())
        if p_res.stderr:
            print("STDERR:", p_res.stderr.strip()[:200])
    else:
        print("Nothing to commit in", repo)

print("=== Done all module commits & pushes ===")
