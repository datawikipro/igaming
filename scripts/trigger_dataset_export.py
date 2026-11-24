#!/usr/bin/env python3
"""
SmartBet.guru / iGaming Institutional Trigger: ML Dataset Matrix Export
Dispatches dataset matrix generation to the Pure Java MlDatasetExportService
running inside igaming-ai-analyst.
Outputs .npy binary features and targets for direct PyTorch GPU training.
"""

import os
import sys
import json
import argparse
import urllib.request
import urllib.parse
import urllib.error

DEFAULT_ANALYST_URL = os.environ.get(
    "ANALYST_URL",
    "http://igaming-ai-analyst.igaming-analytics.svc.cluster.local:8080"
)

def trigger_dataset_export(base_url: str, sport: str, limit: int = 0, output_dir: str = None, async_mode: bool = False):
    action = "export-async" if async_mode else "export"
    params = {"sport": sport, "limit": limit}
    if output_dir:
        params["outputDir"] = output_dir

    query_string = urllib.parse.urlencode(params)
    endpoint = f"{base_url.rstrip('/')}/api/v1/ai-analyst/dataset/{action}?{query_string}"
    print(f"[*] Calling Java ML Dataset Exporter: {endpoint}")

    req = urllib.request.Request(endpoint, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=600) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            print(f"[+] Dataset Export response (HTTP {resp.status}):")
            print(json.dumps(data, indent=2, ensure_ascii=False))
            return data
    except urllib.error.URLError as e:
        print(f"[-] Failed to trigger Java Dataset Export: {e}", file=sys.stderr)
        sys.exit(1)

def main():
    parser = argparse.ArgumentParser(description="Trigger Java ML Dataset Matrix Export (.npy generation)")
    parser.add_argument("--url", default=DEFAULT_ANALYST_URL, help="Base URL for igaming-ai-analyst service")
    parser.add_argument("--sport", default="basketball", help="Sport name/slug (e.g. basketball, football, tennis, ice-hockey, table-tennis)")
    parser.add_argument("--limit", type=int, default=0, help="Max match limit (0 = all)")
    parser.add_argument("--output-dir", default=None, help="Custom output directory for .npy matrices")
    parser.add_argument("--async", dest="async_mode", action="store_true", help="Run asynchronously in background")
    args = parser.parse_args()

    trigger_dataset_export(args.url, sport=args.sport, limit=args.limit, output_dir=args.output_dir, async_mode=args.async_mode)

if __name__ == "__main__":
    main()
