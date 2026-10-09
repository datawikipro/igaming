import os

def generate_manifest(ovpn_path, name_prefix, service_dns_name, port_out=3128):
    with open(ovpn_path, 'r', encoding='utf-8', errors='ignore') as f:
        ovpn_content = f.read()

    processed_lines = []
    for line in ovpn_content.splitlines():
        line = line.strip()
        if not line:
            continue
        if 'data-ciphers' in line or 'route-method' in line or 'route 0.0.0.0' in line:
            continue
        if line.startswith('auth-user-pass'):
            processed_lines.append('auth-user-pass /etc/openvpn/auth.txt')
            continue
        if line.startswith('compress'):
            processed_lines.append('compress stub-v2')
            continue
        processed_lines.append(line)

    processed_lines.append('redirect-gateway def1')
    processed_lines.append('http-proxy 172.16.0.1 3128')
    processed_lines.append('script-security 2')
    processed_lines.append('up /etc/openvpn/up.sh')
    processed_lines.append('connect-retry-max 5')
    processed_lines.append('connect-timeout 15')

    final_ovpn = '\n'.join(processed_lines)
    indented_ovpn = '\n'.join('    ' + l for l in final_ovpn.splitlines())

    manifest = f'''apiVersion: v1
kind: ConfigMap
metadata:
  name: {name_prefix}-config
  namespace: proxy
data:
  auth.txt: |
    purevpn0s14035525
    Tz9N0O6InBwGK@
  up.sh: |
    #!/bin/sh
    GW=$(ip route show table main | awk '/default/ {{print $3; exit}}')
    ip route add 10.96.0.0/12 via $GW dev eth0 2>/dev/null || true
    ip route add 10.244.0.0/16 via $GW dev eth0 2>/dev/null || true
    ip route add 100.64.0.0/10 via $GW dev eth0 2>/dev/null || true
    ip route add 172.16.0.0/12 via $GW dev eth0 2>/dev/null || true
    ip route add 192.168.0.0/16 via $GW dev eth0 2>/dev/null || true
    iptables -t nat -A POSTROUTING -o tun0 -j MASQUERADE 2>/dev/null || true
    iptables -t mangle -A FORWARD -p tcp --tcp-flags SYN,RST SYN -j TCPMSS --clamp-mss-to-pmtu 2>/dev/null || true
    umount /etc/resolv.conf 2>/dev/null || true
    echo "nameserver 172.16.0.1" > /etc/resolv.conf
    echo "nameserver 1.1.1.1" >> /etc/resolv.conf
    echo "nameserver 8.8.8.8" >> /etc/resolv.conf
    exit 0

  tinyproxy.conf: |
    User nobody
    Group nogroup
    Port {port_out}
    Timeout 300
    MaxClients 1000
    Allow 127.0.0.1
    Allow 10.0.0.0/8
    Allow 172.16.0.0/12
    Allow 192.168.0.0/16
    Allow 100.64.0.0/10
    ViaProxyName "tinyproxy-{name_prefix}"
    ConnectPort 443
    ConnectPort 80
    ConnectPort 563
  client.ovpn: |
{indented_ovpn}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {name_prefix}
  namespace: proxy
  labels:
    app: {name_prefix}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: {name_prefix}
  template:
    metadata:
      labels:
        app: {name_prefix}
    spec:
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: kubernetes.io/hostname
                operator: In
                values:
                - xeon-srv
                - k8s-master-1
      containers:
      - name: openvpn
        image: dperson/openvpn-client:latest
        imagePullPolicy: IfNotPresent
        command: ["/bin/sh", "-c"]
        args:
        - |
          mkdir -p /etc/openvpn
          cp /config/client.ovpn /etc/openvpn/client.ovpn
          cp /config/auth.txt /etc/openvpn/auth.txt
          cp /config/up.sh /etc/openvpn/up.sh
          chmod 600 /etc/openvpn/auth.txt
          chmod +x /etc/openvpn/up.sh
          umount /etc/resolv.conf 2>/dev/null || true
          echo "nameserver 172.16.0.1" > /etc/resolv.conf
          exec openvpn --config /etc/openvpn/client.ovpn
        securityContext:
          privileged: true
          capabilities:
            add: ["NET_ADMIN", "MKNOD"]
        volumeMounts:
        - name: cfg
          mountPath: /config
        resources:
          requests:
            cpu: 10m
            memory: 32Mi
          limits:
            cpu: 500m
            memory: 256Mi
      - name: tinyproxy
        image: vimagick/tinyproxy:latest
        imagePullPolicy: IfNotPresent
        command: ["/bin/sh", "-c"]
        args:
        - |
          mkdir -p /etc/tinyproxy
          cp /config/tinyproxy.conf /etc/tinyproxy/tinyproxy.conf
          umount /etc/resolv.conf 2>/dev/null || true
          echo "nameserver 172.16.0.1" > /etc/resolv.conf
          exec tinyproxy -d -c /etc/tinyproxy/tinyproxy.conf
        ports:
        - name: http-proxy
          containerPort: {port_out}
        volumeMounts:
        - name: cfg
          mountPath: /config
        resources:
          requests:
            cpu: 5m
            memory: 16Mi
          limits:
            cpu: 200m
            memory: 128Mi
      - name: curl-tester
        image: curlimages/curl:latest
        imagePullPolicy: IfNotPresent
        command: ["sleep", "infinity"]
      volumes:
      - name: cfg
        configMap:
          name: {name_prefix}-config
          defaultMode: 0755
---
apiVersion: v1
kind: Service
metadata:
  name: {service_dns_name}
  namespace: proxy
  labels:
    app: {name_prefix}
spec:
  type: ClusterIP
  ports:
  - name: http-proxy
    port: {port_out}
    targetPort: {port_out}
  selector:
    app: {name_prefix}
'''
    return manifest

uk_manifest = generate_manifest('scripts/purevpn_uk_141.ovpn', 'purevpn-uk-direct', 'purevpn-uk-direct')
with open('scripts/purevpn-uk-direct.yaml', 'w', encoding='utf-8') as f:
    f.write(uk_manifest)
print("Generated scripts/purevpn-uk-direct.yaml")

sg_manifest = generate_manifest('scripts/purevpn_sg_133.ovpn', 'purevpn-sg-direct', 'purevpn-sg-direct')
with open('scripts/purevpn-sg-direct.yaml', 'w', encoding='utf-8') as f:
    f.write(sg_manifest)
print("Generated scripts/purevpn-sg-direct.yaml")
