#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_test = """
echo "=== Testing K8s connectivity on xeon-srv ==="
DB_IP=$(kubectl get svc -n plane plane-db-rw -o jsonpath='{.spec.clusterIP}')
echo "plane-db-rw ClusterIP: $DB_IP"
PGPASSWORD='plane_db_secure_password_2026' psql -h "$DB_IP" -U plane -d plane -c "SELECT count(*) FROM issues;"

echo "Testing plane-ai-executor NodePort (30885):"
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:30885/health || echo "Failed"

echo "Testing accounts-service ClusterIP:"
ACC_IP=$(kubectl get svc -n accounts accounts-service -o jsonpath='{.spec.clusterIP}')
curl -s -o /dev/null -w "%{http_code}\n" "http://$ACC_IP:8091/actuator/health" || echo "Failed"
"""

cmd = ["ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", SSH_HOST, remote_test]
res = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
