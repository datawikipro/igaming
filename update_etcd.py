with open("/etc/kubernetes/manifests/etcd.yaml", "r") as f:
    content = f.read()

if "--heartbeat-interval" not in content:
    content = content.replace(
        "- --snapshot-count=10000",
        "- --snapshot-count=10000\n    - --heartbeat-interval=500\n    - --election-timeout=2500"
    )
    with open("/etc/kubernetes/manifests/etcd.yaml", "w") as f:
        f.write(content)
    print("Successfully added --heartbeat-interval=500 and --election-timeout=2500 to /etc/kubernetes/manifests/etcd.yaml!")
else:
    print("Heartbeat and election timeouts already present in etcd.yaml")
