#!/usr/bin/env python3
"""
Load and Stress Test Suite for smm-bot-telegram
Tests actuator probes, status API, webhook comment ingestion, and signal posting under concurrent load.
"""

import concurrent.futures
import json
import time
import os
import urllib.error
import urllib.request
from typing import Dict, List, Tuple

BASE_URL = os.environ.get("SMM_BASE_URL", "http://10.100.76.51:8080")


SAMPLE_UPDATES = [
    {
        "update_id": 10001,
        "message": {
            "message_id": 101,
            "chat": {"id": -1003960368887, "type": "supergroup"},
            "from": {"id": 1234567, "username": "pro_bettor"},
            "text": "How do you calculate 80% freebet cash with matched betting?",
            "reply_to_message": {
                "message_id": 888,
                "text": "IDEAL FOR FREEBET (80% GUARANTEED CASH) UEFA Champions League Arsenal vs Real Madrid",
            },
        },
    },
    {
        "update_id": 10002,
        "message": {
            "message_id": 102,
            "chat": {"id": -1002244889900, "type": "supergroup"},
            "from": {"id": 2345678, "username": "ivan_invest"},
            "text": "Как работает перекрытие фрибета в калькуляторе на сайте?",
            "reply_to_message": {
                "message_id": 889,
                "text": "ФРИБЕТ = 80% ГАРАНТИРОВАННЫХ ДЕНЕГ Лига Чемпионов УЕФА",
            },
        },
    },
    {
        "update_id": 10003,
        "message": {
            "message_id": 103,
            "chat": {"id": -1004346736376, "type": "supergroup"},
            "from": {"id": 3456789, "username": "carlos_val"},
            "text": "¿Cómo se calculan los beneficios del arbitraje deportivo?",
        },
    },
    {
        "update_id": 10004,
        "message": {
            "message_id": 104,
            "chat": {"id": -1004371643544, "type": "supergroup"},
            "from": {"id": 4567890, "username": "jean_paris"},
            "text": "Est-ce que le bot envoie des alertes surebet pour le foot français?",
        },
    },
]


import http.client


def send_request(method: str, path: str, payload: dict = None, max_retries: int = 3) -> Tuple[int, float, str]:
    t0 = time.perf_counter()
    last_err = ""
    for attempt in range(max_retries):
        try:
            conn = http.client.HTTPConnection("127.0.0.1", 18080, timeout=10)
            headers = {"Content-Type": "application/json"} if payload else {}
            data = json.dumps(payload) if payload else None
            conn.request(method, path, body=data, headers=headers)
            resp = conn.getresponse()
            elapsed = time.perf_counter() - t0
            body = resp.read().decode("utf-8")
            status = resp.status
            conn.close()
            return status, elapsed, body
        except Exception as e:
            last_err = str(e)
            time.sleep(0.05 * (attempt + 1))
    elapsed = time.perf_counter() - t0
    return 0, elapsed, last_err



def run_benchmark(name: str, tasks: List[Tuple[str, str, dict]], concurrency: int = 10) -> Dict:
    print(f"\n========================================================")
    print(f"Running: {name} (Total: {len(tasks)}, Concurrency: {concurrency})")
    print(f"========================================================")

    latencies = []
    status_codes = {}
    errors = 0

    t_start = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(send_request, m, p, d) for (m, p, d) in tasks]
        for f in concurrent.futures.as_completed(futures):
            code, lat, resp = f.result()
            latencies.append(lat)
            status_codes[code] = status_codes.get(code, 0) + 1
            if code not in (200, 201):
                errors += 1

    total_time = time.perf_counter() - t_start
    latencies.sort()
    p50 = latencies[len(latencies) // 2] * 1000
    p95 = latencies[int(len(latencies) * 0.95)] * 1000
    p99 = latencies[int(len(latencies) * 0.99)] * 1000
    avg_lat = (sum(latencies) / len(latencies)) * 1000
    rps = len(tasks) / total_time if total_time > 0 else 0

    print(f"Results for {name}:")
    print(f"  Total Requests:  {len(tasks)}")
    print(f"  Duration:        {total_time:.3f} s")
    print(f"  Throughput:      {rps:.1f} req/s")
    print(f"  Status Codes:    {status_codes}")
    print(f"  Errors:          {errors}")
    print(f"  Latency (avg):   {avg_lat:.2f} ms")
    print(f"  Latency (p50):   {p50:.2f} ms")
    print(f"  Latency (p95):   {p95:.2f} ms")
    print(f"  Latency (p99):   {p99:.2f} ms")

    return {
        "name": name,
        "total": len(tasks),
        "duration_s": total_time,
        "rps": rps,
        "status_codes": status_codes,
        "errors": errors,
        "p50_ms": p50,
        "p95_ms": p95,
        "p99_ms": p99,
    }


def main():
    print("Testing connectivity to smm-bot-telegram...")
    code, lat, body = send_request("GET", "/actuator/health")
    if code != 200:
        print(f"FAILED initial health check: HTTP {code}")
        exit(1)
    print(f"SUCCESS: Health check HTTP 200 OK ({lat*1000:.1f}ms): {body}")

    results = []

    # 1. Probes & Actuators under high concurrency (60 requests, 10 workers)
    probe_tasks = []
    for _ in range(20):
        probe_tasks.append(("GET", "/healthz", None))
        probe_tasks.append(("GET", "/actuator/health/readiness", None))
        probe_tasks.append(("GET", "/actuator/health/liveness", None))
    results.append(run_benchmark("Actuator & Health Probes", probe_tasks, concurrency=10))

    # 2. Status API (30 requests, 5 workers)
    status_tasks = [("GET", "/api/v1/telegram/status", None) for _ in range(30)]
    results.append(run_benchmark("Telegram Fleet Status API", status_tasks, concurrency=5))

    # 3. Webhook Comment & Patron CRM Ingestion (40 requests, 5 workers)
    webhook_tasks = []
    for i in range(40):
        sample = dict(SAMPLE_UPDATES[i % len(SAMPLE_UPDATES)])
        sample["update_id"] = 20000 + i
        msg = dict(sample["message"])
        msg["message_id"] = 5000 + i
        sample["message"] = msg
        webhook_tasks.append(("POST", "/api/v1/telegram/webhook", sample))
    results.append(run_benchmark("Webhook & AI Prompter Ingestion", webhook_tasks, concurrency=5))

    # Summary
    print("\n========================================================")
    print("LOAD TEST SUMMARY")
    print("========================================================")
    all_ok = True
    for r in results:
        status_str = "PASS" if r["errors"] == 0 else "FAIL"
        if r["errors"] > 0:
            all_ok = False
        print(f"[{status_str}] {r['name']}: {r['total']} reqs, {r['rps']:.1f} rps, p95={r['p95_ms']:.1f}ms, errors={r['errors']}")

    if all_ok:
        print("\nAll load tests PASSED with 0 errors!")
    else:
        print("\nSome tests had errors!")
        exit(1)


if __name__ == "__main__":
    main()
