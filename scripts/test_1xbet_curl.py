import subprocess

proxies = [
    "http://service-proxy-backend.service-proxy.svc.cluster.local:8080",
    "http://laptop-proxy-ru.proxy.svc.cluster.local:3128",
    "http://proxy-vpn-pool-openvpn.proxy.svc.cluster.local:3128"
]

for p in proxies:
    print(f"Testing proxy: {p}")
    cmd = [
        "kubectl", "exec", "-n", "igaming-source", "igaming-source-1xbet-crawler-998cfb64f-wrqhv",
        "-c", "igaming-source-1xbet-crawler", "--",
        "curl", "-s", "--max-time", "10", "-x", p,
        "https://1xbet.com/LineFeed/Get1x2_VZip?mode=4&virtualSports=true&lng=ru&partner=321&count=10"
    ]
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
        print("  Code:", res.returncode, "Len:", len(res.stdout), "Preview:", res.stdout[:150])
    except Exception as e:
        print("  Error:", e)
