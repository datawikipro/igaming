#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generates and deploys the Plane DAG Autonomous Orchestrator to Kubernetes.
Creates ConfigMap with dependency_graph.yaml & dag_autonomous_orchestrator.py,
then creates Deployment plane-dag-orchestrator in namespace plane.
"""

import os
import sys
import subprocess

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

with open("docs/plane-tasks/dependency_graph.yaml", "r", encoding="utf-8") as f:
    dag_yaml_content = f.read()

with open("scripts/dag_autonomous_orchestrator.py", "r", encoding="utf-8") as f:
    orchestrator_py_content = f.read()

# Helper to indent content for ConfigMap data block
def indent(text, spaces=4):
    prefix = " " * spaces
    return "\n".join(prefix + line if line.strip() else "" for line in text.splitlines())

manifest = f"""apiVersion: v1
kind: ConfigMap
metadata:
  name: plane-dag-orchestrator-config
  namespace: plane
data:
  dependency_graph.yaml: |
{indent(dag_yaml_content, 4)}
  dag_autonomous_orchestrator.py: |
{indent(orchestrator_py_content, 4)}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: plane-dag-orchestrator
  namespace: plane
  labels:
    app: plane-dag-orchestrator
spec:
  replicas: 1
  selector:
    matchLabels:
      app: plane-dag-orchestrator
  template:
    metadata:
      labels:
        app: plane-dag-orchestrator
    spec:
      containers:
        - name: orchestrator
          image: python:3.11-slim
          imagePullPolicy: IfNotPresent
          command:
            - /bin/sh
            - -c
            - |
              echo "Installing runtime dependencies..."
              pip install --no-cache-dir psycopg2-binary pyyaml --prefer-offline 2>/dev/null || pip install --no-cache-dir psycopg2-binary pyyaml
              echo "Starting Autonomous DAG Orchestrator..."
              exec python3 -u /app/dag_autonomous_orchestrator.py --config /app/dependency_graph.yaml --interval 30
          env:
            - name: PROJECT_ID
              value: "2df124d7-25b0-4145-a63c-aafcb0fe0041"
            - name: WORKSPACE_SLUG
              value: "dataplatform"
            - name: PLANE_DB_HOST
              value: "plane-db-rw.plane.svc.cluster.local"
            - name: PLANE_DB_PORT
              value: "5432"
            - name: PLANE_DB_USER
              value: "plane"
            - name: PLANE_DB_PASSWORD
              value: "plane_db_secure_password_2026"
            - name: PLANE_DB_NAME
              value: "plane"
            - name: PLANE_EXECUTOR_URL
              value: "http://plane-ai-executor.plane.svc.cluster.local:8080/webhook/plane"
            - name: TOTAL_WORKERS
              value: "17"
            - name: DIRECT_PG_ACCESS
              value: "1"
            - name: GITHUB_TOKEN
              valueFrom:
                secretKeyRef:
                  name: git-credentials
                  key: GITHUB_TOKEN
                  optional: true

          resources:
            requests:
              cpu: 50m
              memory: 128Mi
            limits:
              cpu: 500m
              memory: 512Mi
          volumeMounts:
            - name: config-volume
              mountPath: /app
      volumes:
        - name: config-volume
          configMap:
            name: plane-dag-orchestrator-config
"""

manifest_path = "igaming-k8s/plane-dag-orchestrator.yaml"
os.makedirs("igaming-k8s", exist_ok=True)
with open(manifest_path, "w", encoding="utf-8") as f:
    f.write(manifest)
print(f"Generated {manifest_path} ({len(manifest)} bytes).")

print("Applying to Kubernetes namespace plane via SSH...")
cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl apply -f -"
]

res = subprocess.run(cmd, input=manifest, capture_output=True, text=True, timeout=40, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)

if res.returncode == 0:
    print("Deployment applied successfully! Restarting rollout...")
    rollout_cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        "kubectl rollout restart deployment/plane-dag-orchestrator -n plane"
    ]
    subprocess.run(rollout_cmd, capture_output=True, text=True, timeout=20)
    print("Rollout restarted.")
