import subprocess
import time
import re

def get_offsets():
    cmd = "kubectl exec -n igaming-master kafka-0 -- /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group aggregator-ingestion-v2-updates"
    for attempt in range(4):
        try:
            res = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=60)
            lines = res.stdout.strip().split("\n")
            curr_map = {}
            end_map = {}
            for l in lines:
                parts = l.split()
                if len(parts) >= 6 and parts[0] == "aggregator-ingestion-v2-updates":
                    try:
                        p = int(parts[2])
                        c = int(parts[3])
                        e = int(parts[4])
                        curr_map[p] = c
                        end_map[p] = e
                    except Exception:
                        pass
            if len(curr_map) == 8:
                return sum(curr_map.values()), sum(end_map.values()), curr_map, end_map
        except Exception as e:
            pass
        time.sleep(3)
    return None, None, {}, {}

print(">>> Snapshot 1 (T0): Getting starting offsets...", flush=True)
c0, e0, cmap0, emap0 = get_offsets()
if c0 is None:
    print("Failed to get T0 snapshot")
    sys.exit(1)

print(f"T0: Current={c0:,} | Log-End={e0:,} | Lag={e0-c0:,}", flush=True)

duration = 60
print(f">>> Sleeping {duration} seconds...", flush=True)
time.sleep(duration)

print(">>> Snapshot 2 (T1): Getting ending offsets...", flush=True)
c1, e1, cmap1, emap1 = get_offsets()
if c1 is None:
    print("Failed to get T1 snapshot")
    sys.exit(1)

print(f"T1: Current={c1:,} | Log-End={e1:,} | Lag={e1-c1:,}", flush=True)

consumed = c1 - c0
incoming = e1 - e0
delta_lag = (e1 - c1) - (e0 - c0)

print("\n" + "="*55)
print("=== РЕЗУЛЬТАТ ЗАМЕРА ЗА 1 МИНУТУ (odds.updates) ===")
print("="*55)
print(f"📥 Приходит в Kafka:     {incoming:,} сообщений/мин (~{incoming/60:.1f} msg/sec)")
print(f"📤 Вычитывается из Kafka: {consumed:,} сообщений/мин (~{consumed/60:.1f} msg/sec)")
print(f"⚖️  Изменение очереди:     {'+' if delta_lag>0 else ''}{delta_lag:,} сообщений/мин")
print("="*55)
for p in range(8):
    inc_p = emap1[p] - emap0[p]
    con_p = cmap1[p] - cmap0[p]
    lag_p = emap1[p] - cmap1[p]
    print(f"  Партиция {p}: пришло +{inc_p:,} | вычитано +{con_p:,} | лаг: {lag_p:,}")
