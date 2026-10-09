import subprocess
import time

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def get_stats():
    cmd = (
        "kubectl exec -n igaming-master kafka-0 -- sh -c "
        "\"KAFKA_OPTS='-Djdk.net.hosts.file=/tmp/myhosts' /opt/kafka/bin/kafka-consumer-groups.sh "
        "--bootstrap-server localhost:9092 --describe --group aggregator-ingestion-v2-updates\""
    )
    res = subprocess.run([
        'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, timeout=60)
    
    parts = {}
    for line in res.stdout.strip().split("\n"):
        cols = line.split()
        if len(cols) >= 6 and cols[0] == 'aggregator-ingestion-v2-updates':
            try:
                p_id = int(cols[2])
                curr_off = int(cols[3])
                end_off = int(cols[4])
                lag = int(cols[5])
                parts[p_id] = {'current': curr_off, 'end': end_off, 'lag': lag}
            except ValueError:
                pass
    if not parts:
        print("DEBUG STDOUT:\n", res.stdout)
        print("DEBUG STDERR:\n", res.stderr)
    return parts

print("=== MEASURING KAFKA ODDS.UPDATES THROUGHPUT & DRAIN ===")
t1 = time.time()
s1 = get_stats()
if not s1:
    print("Failed to get initial stats")
    exit(1)

total_curr1 = sum(v['current'] for v in s1.values())
total_end1 = sum(v['end'] for v in s1.values())
total_lag1 = sum(v['lag'] for v in s1.values())

print(f"Initial: Read={total_curr1:,}, Produced={total_end1:,}, Lag={total_lag1:,} across {len(s1)} partitions")
for p in sorted(s1.keys()):
    print(f"  P{p}: current={s1[p]['current']:,}, end={s1[p]['end']:,}, lag={s1[p]['lag']:,}")

INTERVAL = 40
print(f"\nWaiting {INTERVAL} seconds to sample throughput...")
time.sleep(INTERVAL)

t2 = time.time()
s2 = get_stats()
total_curr2 = sum(v['current'] for v in s2.values())
total_end2 = sum(v['end'] for v in s2.values())
total_lag2 = sum(v['lag'] for v in s2.values())
dt = t2 - t1

consumed_rate = (total_curr2 - total_curr1) / dt * 60
produced_rate = (total_end2 - total_end1) / dt * 60
net_drain_rate = (total_lag1 - total_lag2) / dt * 60

print(f"\nAfter {dt:.1f}s:")
print(f"Total Read:     {total_curr2:,} (+{total_curr2 - total_curr1:,}) -> Rate: {consumed_rate:,.0f} msg/min")
print(f"Total Produced: {total_end2:,} (+{total_end2 - total_end1:,}) -> Rate: {produced_rate:,.0f} msg/min")
print(f"Total Lag:      {total_lag2:,} ({total_lag2 - total_lag1:+,}) -> Net Drain Rate: {net_drain_rate:,.0f} msg/min")

print("\nPartition Breakdown:")
for p in sorted(s2.keys()):
    c_diff = s2[p]['current'] - s1.get(p, {}).get('current', 0)
    e_diff = s2[p]['end'] - s1.get(p, {}).get('end', 0)
    l_diff = s2[p]['lag'] - s1.get(p, {}).get('lag', 0)
    rate_p = c_diff / dt * 60
    print(f"  P{p}: read +{c_diff:,} ({rate_p:,.0f}/min), prod +{e_diff:,}, lag={s2[p]['lag']:,} ({l_diff:+,})")
