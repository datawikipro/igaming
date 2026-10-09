import subprocess
import time

def get_p0():
    cmd = [
        "kubectl", "exec", "-n", "igaming-master", "kafka-0", "--",
        "sh", "-c", "KAFKA_OPTS='-Djdk.net.hosts.file=/tmp/myhosts' /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group aggregator-ingestion-v2-updates"
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
    for l in res.stdout.strip().split("\n"):
        p = l.split()
        if len(p) >= 6 and p[0] == "aggregator-ingestion-v2-updates" and p[2] == "0":
            return int(p[3]), int(p[4])
    return None, None

c0, e0 = get_p0()
print(f"T0 (0s):   Current={c0:,} | End={e0:,} | Lag={e0-c0:,}", flush=True)

interval = 30
time.sleep(interval)

c1, e1 = get_p0()
print(f"T1 ({interval}s):  Current={c1:,} | End={e1:,} | Lag={e1-c1:,}", flush=True)

inc_30s = e1 - e0
con_30s = c1 - c0

inc_min = inc_30s * (60 // interval)
con_min = con_30s * (60 // interval)
lag_diff_min = inc_min - con_min

print("\n" + "="*50)
print(f"📊 ТОЧНЫЕ ЗАМЕРЫ (топик odds.updates):")
print(f"📥 Приходит в Kafka:     {inc_min:,} сообщений/мин (~{inc_min/60:.1f} msg/sec)")
print(f"📤 Вычитывается из Kafka: {con_min:,} сообщений/мин (~{con_min/60:.1f} msg/sec)")
print(f"⚖️  Изменение очереди:     {'+' if lag_diff_min > 0 else ''}{lag_diff_min:,} сообщ/мин")
print("="*50)
