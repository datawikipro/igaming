import subprocess

pod_name = "igaming-source-olimpbet-kz-crawler-df4dcfd64-6mh8f"
namespace = "igaming-dev"

for i in range(11):
    url = f"https://api4.olimpbet.kz/v4/{i}/live/popular/sports-with-competitions-with-events"
    cmd = [
        "kubectl", "exec", pod_name, "-n", namespace, "-c", "igaming-source-olimpbet-kz-crawler",
        "--", "curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", url
    ]
    try:
        res = subprocess.run(cmd, capture_output=True, text=True)
        code = res.stdout.strip()
        print(f"Direct v4/{i} HTTP {code}")
    except Exception as e:
        print(f"Error {i}: {e}")
