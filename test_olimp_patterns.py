import subprocess

pod_name = "igaming-source-olimpbet-kz-crawler-df4dcfd64-6mh8f"
namespace = "igaming-dev"

regions = [0, 3]
patterns = [
    "live/sports-with-competitions-with-events",
    "live/popular/sports-with-competitions-with-events",
    "line/sports-with-competitions-with-events",
    "line/top/sports-with-competitions-with-events",
    "sports-with-competitions-with-events",
    "live/events",
    "line/events",
    "events"
]

for r in regions:
    for pat in patterns:
        url = f"https://api4.olimpbet.kz/api/v4/{r}/{pat}"
        cmd = [
            "kubectl", "exec", pod_name, "-n", namespace, "-c", "igaming-source-olimpbet-kz-crawler",
            "--", "curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", url
        ]
        try:
            res = subprocess.run(cmd, capture_output=True, text=True)
            code = res.stdout.strip()
            print(f"Region {r}, pattern '{pat}': HTTP {code}")
        except Exception as e:
            print(f"Error Region {r}, pattern '{pat}': {e}")

# Also try without region prefix
for pat in patterns:
    url = f"https://api4.olimpbet.kz/api/v4/{pat}"
    cmd = [
        "kubectl", "exec", pod_name, "-n", namespace, "-c", "igaming-source-olimpbet-kz-crawler",
        "--", "curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", url
    ]
    try:
        res = subprocess.run(cmd, capture_output=True, text=True)
        code = res.stdout.strip()
        print(f"No Region, pattern '{pat}': HTTP {code}")
    except Exception as e:
        print(f"Error No Region, pattern '{pat}': {e}")
