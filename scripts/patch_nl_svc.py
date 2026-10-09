import subprocess

script = """
cat << 'EOF' | kubectl apply -f -
apiVersion: v1
kind: Service
metadata:
  name: purevpn-nl-direct
  namespace: proxy
  labels:
    app: purevpn-nl-direct
spec:
  type: NodePort
  ports:
  - name: http-proxy
    port: 3128
    targetPort: 3128
    nodePort: 31280
  selector:
    app: purevpn-nl-direct
EOF
"""
res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", script],
                     capture_output=True, text=True)
print(res.stdout)
print(res.stderr)
