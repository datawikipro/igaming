#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

service_unit = """[Unit]
Description=Plane Tasks Autonomous DAG Orchestrator
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/root/plane-dag-orchestrator
Environment="PYTHONUNBUFFERED=1"
Environment="HOME=/root"
Environment="KUBECONFIG=/root/.kube/config"
Environment="PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
Environment="PROJECT_ID=2df124d7-25b0-4145-a63c-aafcb0fe0041"
Environment="WORKSPACE_SLUG=dataplatform"
Environment="PLANE_DB_HOST=10.105.178.50"
Environment="PLANE_DB_PORT=5432"
Environment="PLANE_DB_USER=plane"
Environment="PLANE_DB_PASSWORD=plane_db_secure_password_2026"
Environment="PLANE_DB_NAME=plane"
Environment="PLANE_EXECUTOR_URL=http://127.0.0.1:30885/webhook/plane"
Environment="TOTAL_WORKERS=20"
Environment="DIRECT_PG_ACCESS=1"
ExecStart=/usr/bin/python3 -u /root/plane-dag-orchestrator/dag_autonomous_orchestrator.py --config /root/plane-dag-orchestrator/dependency_graph.yaml --interval 30
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
"""

remote_script = f"""
cat <<'EOF' > /etc/systemd/system/plane-dag-orchestrator.service
{service_unit}
EOF

systemctl daemon-reload
systemctl enable plane-dag-orchestrator
systemctl restart plane-dag-orchestrator
sleep 3
systemctl status plane-dag-orchestrator --no-pager | head -n 25
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "tr -d '\\r' | bash -s"
]

res = subprocess.run(cmd, input=remote_script, capture_output=True, text=True, timeout=30)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
