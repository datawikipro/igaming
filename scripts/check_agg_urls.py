import os
import glob
import re

k8s_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "igaming-k8s")
for f in sorted(glob.glob(os.path.join(k8s_dir, "*.yaml"))):
    with open(f, "r", encoding="utf-8") as fp:
        content = fp.read()
    agg_urls = re.findall(r"name:\s*APP_AGGREGATOR_URL\s*\n\s*value:\s*([^\n]+)", content)
    for u in agg_urls:
        u_clean = u.strip().strip("'\"")
        if u_clean != "http://igaming-aggregator.igaming-master.svc.cluster.local":
            print(f"{os.path.basename(f)}: {u_clean}")
