#!/usr/bin/env python3
"""
SmartBet.guru / iGaming Institutional RunPod Orchestrator
Full Lifecycle: Auto-Provision GPU -> Transfer Matrices -> Train Multi-Task Net -> Fetch Models -> Terminate Pod
Enforces Rule 12: Zero Wasted Idle Billing.
"""

import os
import sys
import time
import json
import socket
import subprocess
import requests
from typing import Tuple

os.environ['HTTPS_PROXY'] = 'http://10.110.39.160:3128'
os.environ['HTTP_PROXY'] = 'http://10.110.39.160:3128'

import runpod

RUNPOD_API_KEY_PATH = "/opt/sofascore-harvester/runpod_api.key"
if os.path.exists(RUNPOD_API_KEY_PATH):
    runpod.api_key = open(RUNPOD_API_KEY_PATH).read().strip()
else:
    runpod.api_key = "rpa_I056FSRX1F7HL2XQ85ZYPAU7MLKJXGGOYXIFUMOD3p46w0"

SSH_PUBKEY = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAII3hBzPnuBDDzXQ3/+Ckmeq0Zqqp/3xlcBYeqShWCG1K root@xeon-srv"
MODEL_VERSION = "v0.5.0.1.1"

def wait_for_ssh(host: str, port: int, timeout: int = 180) -> bool:
    print(f"Waiting for SSH on {host}:{port}...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            with socket.create_connection((host, port), timeout=3):
                print(f" SSH port {port} is open and accepting connections!")
                time.sleep(2)
                return True
        except (socket.timeout, ConnectionRefusedError, OSError):
            time.sleep(3)
    return False

def get_or_create_pod() -> Tuple[str, str, int]:
    print("\n=== 1. Checking Active Pods ===")
    pods = runpod.get_pods()
    for p in pods:
        if p.get("desiredStatus") == "RUNNING":
            pod_id = p.get("id")
            print(f"Found active pod: {pod_id} ({p.get('name')})")
            runtime = p.get("runtime") or {}
            ports = runtime.get("ports") or []
            for port_info in ports:
                if port_info.get("privatePort") == 22:
                    return pod_id, port_info.get("ip"), port_info.get("publicPort")

    print("\n=== 2. Deploying On-Demand GPU Pod on RunPod ===")
    preferred_gpus = [
        "NVIDIA RTX 4000 Ada Generation",
        "NVIDIA GeForce RTX 4090",
        "NVIDIA RTX PRO 4500 Blackwell",
        "NVIDIA GeForce RTX 3090",
        "NVIDIA L40S",
        "NVIDIA RTX 5090"
    ]

    pod = None
    for gpu_candidate in preferred_gpus:
        print(f"Attempting to deploy on GPU: {gpu_candidate}...")
        try:
            pod = runpod.create_pod(
                name="igaming-tactical-trainer",
                image_name="runpod/pytorch:2.1.0-py3.10-cuda11.8.0-devel-ubuntu22.04",
                gpu_type_id=gpu_candidate,
                cloud_type="ALL",
                support_public_ip=True,
                start_ssh=True,
                gpu_count=1,
                volume_in_gb=40,
                container_disk_in_gb=30,
                volume_mount_path="/workspace",
                ports="22/tcp",
                env={"PUBLIC_KEY": SSH_PUBKEY}
            )
            if pod and pod.get("id"):
                print(f" Successfully reserved GPU: {gpu_candidate} (Pod ID: {pod.get('id')})")
                break
        except Exception as e:
            print(f"   [Unavailable] {gpu_candidate}: {e}")

    if not pod or not pod.get("id"):
        raise RuntimeError("Failed to provision any of the preferred GPUs on RunPod.")

    pod_id = pod.get("id")
    print(f" Pod creation initiated: ID={pod_id}. Waiting for instance boot and IP assignment...")

    # Wait for pod runtime
    host = None
    port = None
    for attempt in range(60):
        time.sleep(5)
        p_info = runpod.get_pod(pod_id)
        status = p_info.get("desiredStatus")
        runtime = p_info.get("runtime") or {}
        ports = runtime.get("ports") or []
        for port_info in ports:
            if port_info.get("privatePort") == 22:
                host = port_info.get("ip")
                port = port_info.get("publicPort")
                break
        if host and port and status == "RUNNING":
            print(f" Pod {pod_id} is RUNNING at {host}:{port}!")
            break
        print(f"   [{attempt+1}/60] Status: {status}, Host: {host}, Port: {port}...")

    if not host or not port:
        raise RuntimeError(f"Pod {pod_id} failed to allocate public SSH port within timeout.")

    if not wait_for_ssh(host, port):
        raise RuntimeError(f"Could not connect to SSH on {host}:{port}")

    return pod_id, host, port

def train_and_collect(pod_id: str, host: str, port: int):
    ssh_opts = "-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
    ssh_cmd = f"ssh {ssh_opts} -p {port} root@{host}"
    scp_cmd = f"scp {ssh_opts} -P {port}"

    print(f"\n=== 3. Initializing Workspaces on Pod {pod_id} ===")
    subprocess.check_call(f'{ssh_cmd} "mkdir -p /workspace/data /workspace/models"', shell=True)

    print("\n=== 4. Transferring Training Script and Multi-Task Matrices ===")
    subprocess.check_call(f'{scp_cmd} /opt/sofascore-harvester/train_multitask_matrix_gpu.py root@{host}:/workspace/', shell=True)

    for sport in ["tennis", "table-tennis"]:
        print(f"Transferring {sport} matrices...")
        subprocess.check_call(f'{scp_cmd} /data/cold-storage/sofascore/cache/{sport}_multitask_features_X.npy root@{host}:/workspace/data/', shell=True)
        subprocess.check_call(f'{scp_cmd} /data/cold-storage/sofascore/cache/{sport}_multitask_targets_Y.npy root@{host}:/workspace/data/', shell=True)
        subprocess.check_call(f'{scp_cmd} /data/cold-storage/sofascore/cache/{sport}_targets_meta.json root@{host}:/workspace/data/', shell=True)

    print("\n=== 5. Launching Multi-Task GPU Training on RunPod ===")
    try:
        train_exec = (
            f'{ssh_cmd} "python3 -u /workspace/train_multitask_matrix_gpu.py '
            f'--sport tennis,table-tennis --epochs 12 --batch-size 8192 --version {MODEL_VERSION} '
            f'| tee /workspace/training.log"'
        )
        subprocess.check_call(train_exec, shell=True)

        print("\n=== 6. Copying Trained Models and Leaderboards Back to xeon-srv ===")
        os.makedirs("/data/cold-storage/sofascore/models", exist_ok=True)
        subprocess.check_call(f'{scp_cmd} root@{host}:/workspace/models/* /data/cold-storage/sofascore/models/', shell=True)
    finally:
        print("\n=== 7. Terminating RunPod GPU Pod (Zero Wasted Idle Billing) ===")
        for attempt in range(3):
            try:
                runpod.terminate_pod(pod_id)
                print(f" Pod {pod_id} successfully TERMINATED!")
                break
            except Exception as e:
                print(f"Warning on terminate_pod attempt {attempt+1}: {e}")
                time.sleep(3)

    print("\n=== 8. Restarting ML Inference Engine on xeon-srv ===")
    subprocess.run(["systemctl", "restart", "igaming-ml-inference"])
    time.sleep(3)
    subprocess.run(["systemctl", "status", "igaming-ml-inference", "--no-pager"])

    print(f"\n Multi-Task Neural Net Training for Tennis & Basketball ({MODEL_VERSION}) Completed Successfully!")

def main():
    pod_id = None
    try:
        pod_id, host, port = get_or_create_pod()
        train_and_collect(pod_id, host, port)
    except Exception as e:
        print(f"\n Error in RunPod pipeline: {e}")
        if pod_id:
            print(f"Cleaning up pod {pod_id}...")
            try:
                runpod.terminate_pod(pod_id)
            except Exception:
                pass
        sys.exit(1)

if __name__ == "__main__":
    main()
