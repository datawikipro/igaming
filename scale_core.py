import subprocess
import time

targets = [
    "igaming-source-fon-bet-ru-refresher",
    "igaming-source-winline-refresher",
    "igaming-source-fon-bet-ru-crawler",
    "igaming-source-fon-bet-ru-loader",
    "igaming-source-winline-crawler",
    "igaming-source-winline-loader",
    "igaming-source-betcity-crawler",
    "igaming-source-betcity-loader",
    "igaming-source-pari-crawler",
    "igaming-source-pari-loader",
    "igaming-source-leon-crawler",
    "igaming-source-leon-loader",
    "igaming-source-baltbet-crawler",
    "igaming-source-baltbet-loader",
    "igaming-source-betboom-crawler",
    "igaming-source-betboom-loader",
    "igaming-source-olimpbet-crawler",
    "igaming-source-olimpbet-loader",
    "igaming-source-tennisi-crawler",
    "igaming-source-tennisi-loader",
    "igaming-source-zenit-crawler",
    "igaming-source-zenit-loader",
    "igaming-source-pinnacle-crawler",
    "igaming-source-pinnacle-loader",
    "igaming-source-marathonbet-com-loader"
]

print(f"Scaling {len(targets)} heavy & refresher deployments to 1...")
for name in targets:
    subprocess.call(["kubectl", "scale", f"deployment/{name}", "--replicas=1", "-n", "igaming-source"])
    time.sleep(1)

print("✅ Scaled heavy & refresher deployments to 1!")
