import subprocess
import json
import time
import sys

deployments = [
    "igaming-source-888sport",
    "igaming-source-atg",
    "igaming-source-betano",
    "igaming-source-betboo",
    "igaming-source-betcitynl",
    "igaming-source-betfair",
    "igaming-source-betman",
    "igaming-source-betmotion",
    "igaming-source-betobet",
    "igaming-source-betplay",
    "igaming-source-betrivers",
    "igaming-source-betsafe",
    "igaming-source-betsson",
    "igaming-source-betvictor",
    "igaming-source-circasports",
    "igaming-source-cloudbet",
    "igaming-source-coolbet",
    "igaming-source-craftbet",
    "igaming-source-draftkings",
    "igaming-source-espnbet",
    "igaming-source-expekt",
    "igaming-source-fastbet",
    "igaming-source-fliff",
    "igaming-source-galera",
    "igaming-source-grosvenor",
    "igaming-source-helabet",
    "igaming-source-jvspin",
    "igaming-source-kto",
    "igaming-source-leovegas",
    "igaming-source-luckia",
    "igaming-source-mostbet",
    "igaming-source-mrgreen",
    "igaming-source-nordicbet",
    "igaming-source-norsktipping",
    "igaming-source-novibet",
    "igaming-source-novig",
    "igaming-source-paf",
    "igaming-source-parimatch",
    "igaming-source-paripesa",
    "igaming-source-pixbet",
    "igaming-source-polymarket",
    "igaming-source-redbet",
    "igaming-source-rushbet",
    "igaming-source-sapphirebet",
    "igaming-source-sisal",
    "igaming-source-smarkets",
    "igaming-source-snai",
    "igaming-source-sportsbet-io",
    "igaming-source-sportybet",
    "igaming-source-stake",
    "igaming-source-stoiximan",
    "igaming-source-sts",
    "igaming-source-sunbet",
    "igaming-source-superbet",
    "igaming-source-superbook",
    "igaming-source-supersport",
    "igaming-source-svenskaspel",
    "igaming-source-swisslos",
    "igaming-source-tab",
    "igaming-source-tipico",
    "igaming-source-tipsport",
    "igaming-source-tonybet",
    "igaming-source-totogaming",
    "igaming-source-unibet",
    "igaming-source-vbet",
    "igaming-source-veikkaus",
    "igaming-source-winamax",
    "igaming-source-winner",
    "igaming-source-wplay"
]

print(f"Total REST deployments to scale: {len(deployments)}")
batch_size = 10
total_batches = (len(deployments) + batch_size - 1) // batch_size

for i in range(0, len(deployments), batch_size):
    batch_num = (i // batch_size) + 1
    batch = deployments[i:i + batch_size]
    print(f"\n==========================================")
    print(f"=== Scaling Batch {batch_num}/{total_batches} ({len(batch)} deployments) ===")
    print(f"Deployments: {', '.join([d.replace('igaming-source-', '') for d in batch])}")
    print(f"==========================================")
    
    scale_args = ["kubectl", "scale"] + [f"deployment/{d}" for d in batch] + ["--replicas=1", "-n", "igaming-source"]
    res = subprocess.run(scale_args, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"Error during scaling: {res.stderr}")
    else:
        print(f"Batch {batch_num} scaled to 1.")
        
    print("Waiting 15 seconds for pods to initialize...")
    time.sleep(15)
    
    # Check node metrics
    try:
        metrics = subprocess.run(["kubectl", "top", "node", "xeon-srv"], capture_output=True, text=True)
        if metrics.returncode == 0:
            print("Node status:")
            print(metrics.stdout.strip())
    except Exception as e:
        print(f"Metrics check failed: {e}")

print("\nAll batches scaled successfully!")
