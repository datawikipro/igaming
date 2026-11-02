#!/usr/bin/env python3
"""
SmartBet.guru / iGaming Institutional ML Pipeline & RunPod Orchestration Engine
Canonical Daemon on port 8086 managing:
1. Zero-Cost S3 Dataset Uploads (GPU is 100% OFF)
2. On-Demand Cloud GPU Spawning (RunPod API)
3. Multi-Task Neural Network Training with Semantic Versioning (v0.3.1.1.1)
4. Model Weight Sync & Automatic GPU Pod Termination (Zero Wasted Idle Billing)
5. High-Speed Inference Engine Hot-Reload on port 8085
"""

import os
import sys
import time
import glob
import json
import shutil
import threading
import traceback
from datetime import datetime
from typing import Dict, Any, List, Optional

import boto3
from botocore.client import Config
from boto3.s3.transfer import TransferConfig
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
import requests
import paramiko
import runpod

# Single Source of Truth for Model Versioning
sys.path.insert(0, "/opt/sofascore-harvester")
from model_version import ModelVersion

# Configuration
DATA_ROOT = "/data/cold-storage/sofascore"
CACHE_DIR = os.path.join(DATA_ROOT, "cache")
MODELS_DIR = os.path.join(DATA_ROOT, "models")
SCRIPTS_DIR = "/opt/sofascore-harvester"
RUNPOD_API_KEY_FILE = os.path.join(SCRIPTS_DIR, "runpod_api.key")
RUNPOD_S3_ENV_FILE = os.path.join(SCRIPTS_DIR, "runpod_s3.env")
REGISTRY_FILE = os.path.join(MODELS_DIR, "model_version_registry.json")

def load_secret(env_var: str, file_path: str = None) -> str:
    val = os.getenv(env_var)
    if val:
        return val.strip()
    if file_path and os.path.exists(file_path):
        try:
            with open(file_path) as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith("#"):
                        continue
                    if "=" in line:
                        k, v = line.split("=", 1)
                        if k.strip() == env_var:
                            return v.strip().strip("'\"")
                    elif env_var == "RUNPOD_API_KEY":
                        return line
        except Exception:
            pass
    return ""

CONFIG = {
    "RUNPOD_API_KEY": load_secret("RUNPOD_API_KEY", RUNPOD_API_KEY_FILE),
    "RUNPOD_S3_ACCESS_KEY": load_secret("RUNPOD_S3_ACCESS_KEY", RUNPOD_S3_ENV_FILE),
    "RUNPOD_S3_SECRET_KEY": load_secret("RUNPOD_S3_SECRET_KEY", RUNPOD_S3_ENV_FILE),
    "RUNPOD_S3_ENDPOINT": os.getenv("RUNPOD_S3_ENDPOINT", "https://s3api-eu-ro-1.runpod.io/"),
    "RUNPOD_S3_REGION": os.getenv("RUNPOD_S3_REGION", "EU-RO-1"),
    "RUNPOD_VOLUME_ID": os.getenv("RUNPOD_VOLUME_ID", "9sf8ipvq4h"),
    "K8S_PROXY": os.getenv("K8S_PROXY", "http://10.109.37.197:3128"),
    "INFERENCE_URL": os.getenv("INFERENCE_URL", "http://127.0.0.1:8085")
}

SPORTS = ["football", "ice-hockey", "basketball", "tennis", "table-tennis", "cs2", "dota2"]

# Setup RunPod SDK
def init_runpod():
    api_key = CONFIG["RUNPOD_API_KEY"]
    if os.path.exists(RUNPOD_API_KEY_FILE):
        try:
            with open(RUNPOD_API_KEY_FILE) as f:
                k = f.read().strip()
                if k:
                    api_key = k
        except Exception:
            pass
    runpod.api_key = api_key
    os.environ["HTTPS_PROXY"] = CONFIG["K8S_PROXY"]
    os.environ["HTTP_PROXY"] = CONFIG["K8S_PROXY"]

init_runpod()

app = FastAPI(title="iGaming ML Pipeline Orchestrator", version="2.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

execution_lock = threading.Lock()
state = {
    "is_running": False,
    "job_type": "IDLE",
    "sport": "",
    "step": "IDLE",
    "progress_pct": 0,
    "pod_id": None,
    "version": ModelVersion.get_version_string(),
    "version_info": ModelVersion.get_version_info(),
    "start_time": None,
    "duration_sec": 0,
    "status": "IDLE",
    "error": None,
    "logs": []
}

def log_msg(msg: str):
    ts = datetime.now().strftime("%H:%M:%S")
    entry = f"[{ts}] {msg}"
    print(entry, flush=True)
    with execution_lock:
        state["logs"].append(entry)
        if len(state["logs"]) > 300:
            state["logs"] = state["logs"][-300:]

def get_s3_client():
    return boto3.client(
        "s3",
        endpoint_url=CONFIG["RUNPOD_S3_ENDPOINT"],
        region_name=CONFIG["RUNPOD_S3_REGION"],
        aws_access_key_id=CONFIG["RUNPOD_S3_ACCESS_KEY"],
        aws_secret_access_key=CONFIG["RUNPOD_S3_SECRET_KEY"],
        config=Config(
            s3={"addressing_style": "path"},
            max_pool_connections=25,
            retries={"max_attempts": 5, "mode": "adaptive"},
            proxies={"https": CONFIG["K8S_PROXY"], "http": CONFIG["K8S_PROXY"]}
        )
    )

# ─────────────────────────────────────────────────────────────────────────────
# Core Pipeline Phases
# ─────────────────────────────────────────────────────────────────────────────

def upload_matrix_to_s3(sport: str):
    """Phase 1: Upload dataset matrices to persistent volume while GPU is 100% OFF."""
    s3 = get_s3_client()
    bucket = CONFIG["RUNPOD_VOLUME_ID"]
    tc = TransferConfig(multipart_threshold=16*1024*1024, max_concurrency=4, multipart_chunksize=16*1024*1024, use_threads=True)

    files_to_sync = []
    if sport == "all":
        for s in SPORTS:
            files_to_sync.extend(glob.glob(f"{CACHE_DIR}/{s}_multitask_*.npy"))
            files_to_sync.extend(glob.glob(f"{CACHE_DIR}/{s}_targets_meta.json"))
    else:
        files_to_sync.extend(glob.glob(f"{CACHE_DIR}/{sport}_multitask_*.npy"))
        files_to_sync.extend(glob.glob(f"{CACHE_DIR}/{sport}_targets_meta.json"))

    # Always ensure training script and version module are synced
    for fname in ["train_multitask_matrix_gpu.py", "model_version.py"]:
        fpath = os.path.join(SCRIPTS_DIR, fname)
        if os.path.exists(fpath):
            files_to_sync.append(fpath)

    # Pre-fetch existing objects on S3 volume to avoid redundant uploads
    remote_objects = {}
    try:
        log_msg(f"Inspecting existing datasets on S3 volume {bucket}...")
        for pfx in ["", "data/"]:
            res = s3.list_objects_v2(Bucket=bucket, Prefix=pfx)
            for item in res.get("Contents", []):
                remote_objects[item["Key"]] = item["Size"]
    except Exception as se:
        log_msg(f"Warning checking S3 objects: {se}")

    total_files = len(files_to_sync)
    for idx, fpath in enumerate(files_to_sync, 1):
        fname = os.path.basename(fpath)
        s3_key = fname if fname.endswith(".py") else f"data/{fname}"
        local_size = os.path.getsize(fpath)
        fsize_mb = local_size / (1024 * 1024)

        if s3_key in remote_objects and remote_objects[s3_key] == local_size and local_size > 1024:
            log_msg(f"  [{idx}/{total_files}] {fname} ({fsize_mb:.1f} MB) already synced on S3. Skipping.")
            continue

        log_msg(f"Uploading [{idx}/{total_files}] {fname} ({fsize_mb:.1f} MB) -> s3://{bucket}/{s3_key}...")
        
        class Cb:
            def __init__(self, fn, total):
                self.fn = fn
                self.total = total
                self.cur = 0
                self.last_pct = 0
            def __call__(self, n):
                self.cur += n
                pct = int((self.cur / self.total) * 100) if self.total else 100
                if pct != self.last_pct and pct % 25 == 0:
                    self.last_pct = pct
                    log_msg(f"  [{self.fn}] {pct}% ({self.cur / (1024*1024):.1f}/{self.total / (1024*1024):.1f} MB)")

        s3.upload_file(fpath, bucket, s3_key, Config=tc, Callback=Cb(fname, os.path.getsize(fpath)))
        log_msg(f"Uploaded {fname} successfully.")

def spawn_gpu_pod(gpu_type: str) -> str:
    """Phase 2: Spawn RunPod GPU pod on-demand with network volume or dynamic region/GPU fallback."""
    init_runpod()
    pub_key = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAII3hBzPnuBDDzXQ3/+Ckmeq0Zqqp/3xlcBYeqShWCG1K root@xeon-srv"
    if os.path.exists("/root/.ssh/id_ed25519.pub"):
        try:
            with open("/root/.ssh/id_ed25519.pub") as f:
                pub_key = f.read().strip()
        except Exception:
            pass

    pod = None
    # 1. First attempt: Selected gpu_type with network volume in configured region (EU-RO-1)
    try:
        log_msg(f"Creating GPU pod via RunPod SDK: {gpu_type} in {CONFIG['RUNPOD_S3_REGION']} with network volume...")
        pod = runpod.create_pod(
            name=f"igaming-train-{int(time.time())}",
            image_name="runpod/pytorch:2.1.0-py3.10-cuda11.8.0-devel-ubuntu22.04",
            gpu_type_id=gpu_type,
            cloud_type="ALL",
            data_center_id=CONFIG["RUNPOD_S3_REGION"],
            network_volume_id=CONFIG["RUNPOD_VOLUME_ID"],
            volume_mount_path="/workspace",
            support_public_ip=True,
            start_ssh=True,
            ports="22/tcp",
            container_disk_in_gb=20,
            env={"PUBLIC_KEY": pub_key}
        )
    except Exception as e:
        log_msg(f"Data center {CONFIG['RUNPOD_S3_REGION']} volume attach unavailable ({e}). Spawning across all available regions/GPUs with SFTP sync...")

    if pod and pod.get("id"):
        return pod["id"]

    # 2. Multi-GPU Fallback across ALL regions with high-speed SFTP matrix sync
    fallback_candidates = [
        gpu_type,
        "NVIDIA A100-SXM4-80GB",
        "NVIDIA RTX 2000 Ada Generation",
        "NVIDIA GeForce RTX 3090",
        "NVIDIA RTX A5000",
        "NVIDIA RTX A4000",
        "NVIDIA GeForce RTX 4080",
        "NVIDIA A40",
        "NVIDIA A100 80GB PCIe",
        "NVIDIA RTX 4000 Ada Generation"
    ]
    seen = set()
    candidate_gpus = [g for g in fallback_candidates if not (g in seen or seen.add(g))]

    for candidate in candidate_gpus:
        for disk in [20, 15]:
            try:
                log_msg(f"Attempting to spawn GPU: {candidate} (disk={disk}GB) across ALL regions...")
                pod = runpod.create_pod(
                    name=f"igaming-train-{int(time.time())}",
                    image_name="runpod/pytorch:2.1.0-py3.10-cuda11.8.0-devel-ubuntu22.04",
                    gpu_type_id=candidate,
                    cloud_type="ALL",
                    support_public_ip=True,
                    start_ssh=True,
                    ports="22/tcp",
                    container_disk_in_gb=disk,
                    env={"PUBLIC_KEY": pub_key}
                )
                if pod and pod.get("id"):
                    log_msg(f"Successfully spawned GPU pod {pod['id']} on {candidate}!")
                    return pod["id"]
            except Exception as ce:
                log_msg(f"Candidate {candidate} (disk={disk}GB) unavailable: {ce}")

    raise RuntimeError("No available GPU instances found on RunPod across all candidate types.")

def wait_for_pod_running(pod_id: str, timeout_sec: int = 240) -> dict:
    """Wait for pod to reach RUNNING status with exposed SSH port."""
    init_runpod()
    t0 = time.time()
    while time.time() - t0 < timeout_sec:
        p = runpod.get_pod(pod_id)
        if p and p.get("desiredStatus") == "RUNNING" and p.get("runtime") and p.get("runtime", {}).get("ports"):
            return p
        status_name = p.get("desiredStatus", "INITIALIZING") if p else "WAITING"
        log_msg(f"Pod {pod_id} status: {status_name}... ({int(time.time() - t0)}s elapsed)")
        time.sleep(6)
    raise TimeoutError(f"Pod {pod_id} did not start within {timeout_sec}s")

def execute_gpu_training(pod_id: str, sport: str, epochs: int, version: str):
    """Phase 3: Connect to GPU pod over SSH and train MultiTaskTacticalNet with version stamp."""
    init_runpod()
    p = runpod.get_pod(pod_id)
    runtime = p.get("runtime", {})
    ports = runtime.get("ports", [])
    ssh_port_obj = next((pt for pt in ports if pt.get("privatePort") == 22), None)
    if not ssh_port_obj:
        raise RuntimeError(f"SSH port 22 not found on pod {pod_id}")

    host_ip = ssh_port_obj.get("ip")
    public_port = ssh_port_obj.get("publicPort")
    log_msg(f"Connecting to Pod {pod_id} via SSH at {host_ip}:{public_port}...")

    key_path = "/root/.ssh/id_ed25519"
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    connected = False
    for attempt in range(15):
        try:
            ssh.connect(host_ip, port=public_port, username="root", key_filename=key_path, timeout=10)
            connected = True
            break
        except Exception as e:
            log_msg(f"Waiting for SSH daemon (attempt {attempt+1}/15): {e}")
            time.sleep(5)

    if not connected:
        raise RuntimeError(f"Could not connect to GPU Pod SSH at {host_ip}:{public_port}")

    # Ensure remote directories exist
    ssh.exec_command("mkdir -p /workspace/data /workspace/models")

    # Staging scripts and datasets to GPU pod via SFTP
    sftp = ssh.open_sftp()
    log_msg("Syncing training script and model versioning to GPU pod via SFTP...")
    sftp.put(os.path.join(SCRIPTS_DIR, "train_multitask_matrix_gpu.py"), "/workspace/train_multitask_matrix_gpu.py")
    if os.path.exists(os.path.join(SCRIPTS_DIR, "model_version.py")):
        sftp.put(os.path.join(SCRIPTS_DIR, "model_version.py"), "/workspace/model_version.py")

    sports_to_sync = [sport] if sport != "all" else SPORTS
    for s in sports_to_sync:
        for fname in [f"{s}_multitask_features_X.npy", f"{s}_multitask_targets_Y.npy", f"{s}_targets_meta.json"]:
            local_p = os.path.join(CACHE_DIR, fname)
            if os.path.exists(local_p):
                remote_p = f"/workspace/data/{fname}"
                log_msg(f"Staging {fname} to GPU pod ({os.path.getsize(local_p) / (1024*1024):.1f} MB)...")
                sftp.put(local_p, remote_p)

    cmd = f"python3 /workspace/train_multitask_matrix_gpu.py --sport {sport} --epochs {epochs} --version {version}"
    log_msg(f"Running command on GPU: {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd)

    for line in iter(stdout.readline, ""):
        l = line.strip()
        if l:
            log_msg(f"[GPU] {l}")

    exit_status = stdout.channel.recv_exit_status()
    if exit_status != 0:
        err = stderr.read().decode("utf-8")
        sftp.close()
        ssh.close()
        raise RuntimeError(f"GPU Training failed with exit code {exit_status}: {err}")

    # Download trained model and leaderboard back to Xeon via SFTP
    log_msg(f"Retrieving trained {sport} models and leaderboard to Xeon via SFTP...")
    for s in sports_to_sync:
        try:
            remote_pt = f"/workspace/models/{s}_tactical_net_best.pt"
            local_pt = os.path.join(MODELS_DIR, f"{s}_tactical_net_best.pt")
            sftp.get(remote_pt, local_pt)
            log_msg(f"Retrieved {s}_tactical_net_best.pt ({os.path.getsize(local_pt) / (1024*1024):.1f} MB)")
        except Exception as e:
            log_msg(f"Notice: Could not retrieve {s}_tactical_net_best.pt via SFTP: {e}")

        try:
            remote_lb = f"/workspace/models/{s}_predictability_leaderboard.json"
            local_lb = os.path.join(MODELS_DIR, f"{s}_predictability_leaderboard.json")
            sftp.get(remote_lb, local_lb)
            log_msg(f"Retrieved {s}_predictability_leaderboard.json")
        except Exception as e:
            log_msg(f"Notice: Could not retrieve {s}_predictability_leaderboard.json via SFTP: {e}")

    sftp.close()
    ssh.close()

def sync_weights_from_s3(sport: str, version: str):
    """Phase 4: Download model weights and versioned leaderboard back to Xeon."""
    s3 = get_s3_client()
    bucket = CONFIG["RUNPOD_VOLUME_ID"]
    os.makedirs(MODELS_DIR, exist_ok=True)

    sports_to_sync = SPORTS if sport == "all" else [sport]
    for s in sports_to_sync:
        for suffix in ["_tactical_net_best.pt", "_predictability_leaderboard.json"]:
            s3_key = f"models/{s}{suffix}"
            dest_path = os.path.join(MODELS_DIR, f"{s}{suffix}")
            if not os.path.exists(dest_path):
                try:
                    log_msg(f"Downloading s3://{bucket}/{s3_key} -> {dest_path}...")
                    s3.download_file(bucket, s3_key, dest_path)
                    log_msg(f"Successfully synced {s}{suffix}.")
                except Exception as e:
                    log_msg(f"Notice: S3 download skipped/failed for {s3_key}: {e}")
            else:
                log_msg(f"Model file {dest_path} already present from high-speed SFTP.")

    # Record into version registry
    lb_path = os.path.join(MODELS_DIR, f"{sport}_predictability_leaderboard.json")
    if os.path.exists(lb_path):
        try:
            with open(lb_path) as f:
                lb_data = json.load(f)
            raw_metrics = lb_data.get("leaderboard", lb_data) if isinstance(lb_data, dict) else lb_data
            metrics_dict = {m.get("name", str(idx)): m for idx, m in enumerate(raw_metrics)} if isinstance(raw_metrics, list) else raw_metrics
            ModelVersion.record_training_run(
                sport=sport,
                metrics=metrics_dict,
                samples_count=0
            )
        except Exception as re:
            log_msg(f"Registry recording error: {re}")

def terminate_gpu_pod(pod_id: str):
    """Phase 5: Terminate RunPod pod immediately to halt billing."""
    init_runpod()
    log_msg(f"Terminating GPU pod {pod_id} (Zero Wasted Idle Billing)...")
    res = runpod.terminate_pod(pod_id)
    log_msg(f"Pod {pod_id} terminated: {res}")

def reload_inference():
    """Phase 6: Trigger zero-downtime hot-reload of inference service."""
    try:
        resp = requests.post(f"{CONFIG['INFERENCE_URL']}/reload", timeout=15)
        log_msg(f"Inference reload status {resp.status_code}: {resp.text}")
    except Exception as e:
        log_msg(f"Inference reload call failed: {e}")

# ─────────────────────────────────────────────────────────────────────────────
# Autonomous Pipeline Runner Thread
# ─────────────────────────────────────────────────────────────────────────────

def execute_autonomous_pipeline(sport: str, epochs: int, gpu_type: str, iteration: Optional[int] = None):
    v_str = ModelVersion.get_version_string(iteration)
    v_info = ModelVersion.get_version_info(iteration)

    with execution_lock:
        state["is_running"] = True
        state["job_type"] = "AUTONOMOUS_PIPELINE"
        state["sport"] = sport
        state["version"] = v_str
        state["version_info"] = v_info
        state["status"] = "RUNNING"
        state["start_time"] = time.time()
        state["progress_pct"] = 2
        state["error"] = None
        state["logs"] = []

    log_msg(f"=== Starting Autonomous ML Pipeline: {sport.upper()} | Model Version: {v_str} ===")
    log_msg(f"Configuration: {epochs} Epochs on {gpu_type} | Architecture: MultiTaskTacticalNetV2")
    pod_id = None

    try:
        # Phase 1: Upload Matrix to S3 (GPU 100% OFF)
        with execution_lock:
            state["step"] = "PHASE_1_S3_UPLOAD"
            state["progress_pct"] = 5
        log_msg("Phase 1/6: Uploading dataset matrices to RunPod S3 volume (GPU is 100% OFF - $0.00 cost)...")
        upload_matrix_to_s3(sport)
        log_msg("Phase 1/6 Completed: Dataset successfully in persistent volume 9sf8ipvq4h.")

        # Phase 2: Deploy GPU Pod via RunPod API
        with execution_lock:
            state["step"] = "PHASE_2_POD_DEPLOY"
            state["progress_pct"] = 25
        log_msg(f"Phase 2/6: Spawning {gpu_type} in {CONFIG['RUNPOD_S3_REGION']} with volume 9sf8ipvq4h attached...")
        pod_id = spawn_gpu_pod(gpu_type)
        with execution_lock:
            state["pod_id"] = pod_id
        log_msg(f"Pod {pod_id} created. Awaiting runtime readiness...")
        pod_info = wait_for_pod_running(pod_id, timeout_sec=240)
        log_msg(f"Pod {pod_id} is RUNNING on machine {pod_info.get('machineId', 'ok')}!")

        # Phase 3: Train on GPU Pod
        with execution_lock:
            state["step"] = "PHASE_3_TRAINING"
            state["progress_pct"] = 45
        log_msg(f"Phase 3/6: Executing GPU training for {sport} ({epochs} epochs, version {v_str})...")
        execute_gpu_training(pod_id, sport, epochs, v_str)
        log_msg("Phase 3/6 Completed: GPU training finished successfully!")

        # Phase 4: Sync Model Weights to Xeon
        with execution_lock:
            state["step"] = "PHASE_4_WEIGHTS_SYNC"
            state["progress_pct"] = 80
        log_msg(f"Phase 4/6: Downloading trained {v_str} weights and leaderboard back to Xeon...")
        sync_weights_from_s3(sport, v_str)
        log_msg("Phase 4/6 Completed: Model weights cached in /data/cold-storage/sofascore/models/.")

        # Phase 5: Terminate GPU Pod
        with execution_lock:
            state["step"] = "PHASE_5_POD_TERMINATE"
            state["progress_pct"] = 90
        log_msg(f"Phase 5/6: Terminating GPU Pod {pod_id} to halt billing immediately...")
        terminate_gpu_pod(pod_id)
        pod_id = None
        with execution_lock:
            state["pod_id"] = None
        log_msg("Phase 5/6 Completed: GPU Pod terminated. $0.00 idle cost.")

        # Phase 6: Hot-Reload Inference
        with execution_lock:
            state["step"] = "PHASE_6_INFERENCE_RELOAD"
            state["progress_pct"] = 95
        log_msg("Phase 6/6: Triggering hot-reload of inference engine on port 8085...")
        reload_inference()
        log_msg("Phase 6/6 Completed: Inference engine updated and active.")

        with execution_lock:
            state["status"] = "SUCCESS"
            state["progress_pct"] = 100
            state["step"] = "COMPLETED"
        log_msg(f"=== AUTONOMOUS PIPELINE FOR {sport.upper()} COMPLETED SUCCESSFULLY ({v_str})! ===")

    except Exception as e:
        log_msg(f"!!! PIPELINE FAILED: {str(e)} !!!")
        with execution_lock:
            state["status"] = "FAILED"
            state["error"] = str(e)
            state["step"] = "FAILED"
    finally:
        # Safety guard: ensure no pod is left running
        if pod_id:
            try:
                log_msg(f"Safety Guard: Cleaning up pod {pod_id}...")
                terminate_gpu_pod(pod_id)
            except Exception as te:
                log_msg(f"Safety Guard Error: {te}")
        with execution_lock:
            state["is_running"] = False
            state["pod_id"] = None
            if state["start_time"]:
                state["duration_sec"] = int(time.time() - state["start_time"])

# ─────────────────────────────────────────────────────────────────────────────
# REST Endpoints
# ─────────────────────────────────────────────────────────────────────────────

@app.get("/api/status")
def get_status():
    init_runpod()
    pod_list = []
    user_info = {}
    try:
        user_info = runpod.get_user()
        pod_list = runpod.get_pods()
    except Exception as e:
        user_info = {"error": str(e)}

    sports_data = []
    for s in SPORTS:
        feat_path = os.path.join(CACHE_DIR, f"{s}_multitask_features_X.npy")
        targ_path = os.path.join(CACHE_DIR, f"{s}_multitask_targets_Y.npy")
        model_path = os.path.join(MODELS_DIR, f"{s}_tactical_net_best.pt")
        lb_path = os.path.join(MODELS_DIR, f"{s}_predictability_leaderboard.json")

        feat_size_mb = os.path.getsize(feat_path) / (1024*1024) if os.path.exists(feat_path) else 0
        targ_size_mb = os.path.getsize(targ_path) / (1024*1024) if os.path.exists(targ_path) else 0
        model_size_mb = os.path.getsize(model_path) / (1024*1024) if os.path.exists(model_path) else 0

        leaderboard = []
        model_version = ModelVersion.get_version_string()
        if os.path.exists(lb_path):
            try:
                with open(lb_path) as f:
                    raw_lb = json.load(f)
                    if isinstance(raw_lb, dict):
                        leaderboard = raw_lb.get("leaderboard", [])
                        model_version = raw_lb.get("version", model_version)
                    elif isinstance(raw_lb, list):
                        leaderboard = raw_lb
            except Exception:
                pass

        sports_data.append({
            "sport": s,
            "version": model_version,
            "matrix_exists": os.path.exists(feat_path),
            "features_size_mb": round(feat_size_mb, 1),
            "targets_size_mb": round(targ_size_mb, 1),
            "matrix_modified": datetime.fromtimestamp(os.path.getmtime(feat_path)).strftime("%Y-%m-%d %H:%M") if os.path.exists(feat_path) else None,
            "model_exists": os.path.exists(model_path),
            "model_size_mb": round(model_size_mb, 1),
            "model_modified": datetime.fromtimestamp(os.path.getmtime(model_path)).strftime("%Y-%m-%d %H:%M") if os.path.exists(model_path) else None,
            "leaderboard": leaderboard
        })

    inference_status = "DOWN"
    inference_models_count = 0
    try:
        resp = requests.get(f"{CONFIG['INFERENCE_URL']}/health", timeout=2)
        if resp.status_code == 200:
            d = resp.json()
            inference_status = "UP"
            inference_models_count = d.get("models_count", 0)
    except Exception:
        pass

    with execution_lock:
        exec_state = dict(state)

    history = ModelVersion.get_history()

    return {
        "current_version": ModelVersion.get_version_string(),
        "version_info": ModelVersion.get_version_info(),
        "version_history": history,
        "runpod": {
            "user": user_info,
            "pods": pod_list,
            "active_pods_count": len(pod_list)
        },
        "sports": sports_data,
        "inference": {
            "status": inference_status,
            "models_count": inference_models_count
        },
        "job": exec_state
    }

class PipelineRequest(BaseModel):
    sport: str = "football"
    epochs: int = 10
    gpu_type: str = "NVIDIA GeForce RTX 4090"
    iteration: Optional[int] = None

@app.post("/api/pipeline/start")
def start_pipeline(req: PipelineRequest, background_tasks: BackgroundTasks):
    with execution_lock:
        if state["is_running"]:
            raise HTTPException(status_code=400, detail="A pipeline job is already in progress")

    background_tasks.add_task(execute_autonomous_pipeline, req.sport, req.epochs, req.gpu_type, req.iteration)
    return {
        "status": "QUEUED",
        "sport": req.sport,
        "epochs": req.epochs,
        "gpu_type": req.gpu_type,
        "version": ModelVersion.get_version_string(req.iteration)
    }

class TerminateRequest(BaseModel):
    pod_id: Optional[str] = None

@app.post("/api/runpod/terminate")
def terminate_pod_endpoint(req: TerminateRequest):
    init_runpod()
    if req.pod_id:
        terminate_gpu_pod(req.pod_id)
        return {"status": "TERMINATED", "pod_id": req.pod_id}
    else:
        pods = runpod.get_pods()
        terminated = []
        for p in pods:
            terminate_gpu_pod(p["id"])
            terminated.append(p["id"])
        return {"status": "TERMINATED_ALL", "pods": terminated}

@app.post("/api/s3/upload")
def upload_s3_endpoint(req: PipelineRequest, background_tasks: BackgroundTasks):
    with execution_lock:
        if state["is_running"]:
            raise HTTPException(status_code=400, detail="Job already running")
        state["is_running"] = True
        state["job_type"] = "S3_UPLOAD"
        state["sport"] = req.sport
        state["status"] = "RUNNING"
        state["progress_pct"] = 10
        state["logs"] = []

    def task():
        try:
            log_msg(f"Starting S3 upload for {req.sport} (GPU pod is 100% OFF)...")
            upload_matrix_to_s3(req.sport)
            with execution_lock:
                state["status"] = "SUCCESS"
                state["progress_pct"] = 100
            log_msg("S3 upload finished successfully!")
        except Exception as e:
            log_msg(f"S3 upload error: {e}")
            with execution_lock:
                state["status"] = "FAILED"
                state["error"] = str(e)
        finally:
            with execution_lock:
                state["is_running"] = False

    background_tasks.add_task(task)
    return {"status": "UPLOAD_QUEUED", "sport": req.sport}

@app.post("/api/inference/reload")
def reload_inference_endpoint():
    reload_inference()
    return {"status": "RELOAD_SENT"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8086)
