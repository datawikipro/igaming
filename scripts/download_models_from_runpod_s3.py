#!/usr/bin/env python3
"""
SmartBet.guru / iGaming RunPod S3 Model Retrieval Engine
Downloads trained weights (*.pt) and accuracy leaderboards (*.json) from RunPod S3
directly into local cold storage (/data/cold-storage/sofascore/models/) and restarts inference.
"""

import os
import sys
import time
import subprocess

import boto3
from botocore.client import Config
from botocore.exceptions import ClientError

ENV_FILE = "/data/cold-storage/sofascore/runpod_s3.env"
MODELS_LOCAL_DIR = "/data/cold-storage/sofascore/models"

DEFAULT_CREDENTIALS = {
    "RUNPOD_S3_ACCESS_KEY": "user_3L9SrSmyMG2BtbknZ2Nm3NhYmyl",
    "RUNPOD_S3_SECRET_KEY": "rps_6W0I9IB12279WOQPS48PCURTTRG7O4Z4T9HHU03016sa6o",
    "RUNPOD_S3_ENDPOINT": "https://s3api-eu-ro-1.runpod.io/",
    "RUNPOD_S3_REGION": "EU-RO-1",
    "RUNPOD_VOLUME_ID": ""
}

def load_env():
    env = dict(DEFAULT_CREDENTIALS)
    if os.path.exists(ENV_FILE):
        with open(ENV_FILE, "r") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    env[k.strip()] = v.strip().strip('"').strip("'")
    for k in DEFAULT_CREDENTIALS:
        if k in os.environ:
            env[k] = os.environ[k]
    return env

def main():
    env = load_env()
    s3 = boto3.client(
        service_name="s3",
        endpoint_url=env["RUNPOD_S3_ENDPOINT"],
        region_name=env.get("RUNPOD_S3_REGION", "EU-RO-1"),
        aws_access_key_id=env["RUNPOD_S3_ACCESS_KEY"],
        aws_secret_access_key=env["RUNPOD_S3_SECRET_KEY"],
        config=Config(s3={"addressing_style": "path"})
    )

    bucket = env.get("RUNPOD_VOLUME_ID")
    if not bucket:
        res = s3.list_buckets()
        buckets = [b["Name"] for b in res.get("Buckets", [])]
        if not buckets:
            print("[Error] No active volume found!")
            sys.exit(1)
        bucket = buckets[0]

    os.makedirs(MODELS_LOCAL_DIR, exist_ok=True)
    print(f"\n=======================================================")
    print(f" [RUNPOD S3 MODEL SYNC] Downloading Trained Weights")
    print(f"   * Bucket: {bucket}")
    print(f"   * Local Dir: {MODELS_LOCAL_DIR}")
    print(f"=======================================================\n", flush=True)

    paginator = s3.get_paginator("list_objects_v2")
    downloaded = 0
    for page in paginator.paginate(Bucket=bucket, Prefix="models/"):
        for obj in page.get("Contents", []):
            key = obj["Key"]
            if key.endswith("/"):
                continue
            filename = os.path.basename(key)
            dest = os.path.join(MODELS_LOCAL_DIR, filename)
            print(f"Downloading {key} -> {dest} ({obj['Size'] / (1024**2):.1f} MB)...", flush=True)
            s3.download_file(bucket, key, dest)
            downloaded += 1

    print(f"\n[OK] Successfully downloaded {downloaded} model artifacts!")

    # Restart local ML inference service
    print("\nRestarting igaming-ml-inference service on port 8085...", flush=True)
    res = subprocess.run(["systemctl", "restart", "igaming-ml-inference"], capture_output=True, text=True)
    if res.returncode == 0:
        print("[OK] Service igaming-ml-inference restarted successfully with new V3 weights!")
    else:
        print(f"[Notice] systemctl restart: {res.stderr}")

if __name__ == "__main__":
    main()
