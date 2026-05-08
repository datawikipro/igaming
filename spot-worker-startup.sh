#!/bin/bash
# Spot Worker VM startup script - joins K3s cluster automatically as SPOT node
set -e
exec > /var/log/startup-script.log 2>&1

# Wait for network
sleep 10

# 1. Install Tailscale and join VPN (required to reach k3s master at 10.226.0.2)
curl -fsSL https://tailscale.com/install.sh | sh
tailscale up --authkey="tskey-auth-kGSVQGHSbc11CNTRL-6uiQBZMqfqMNy5XcNwXdqM69pD18cdih" --ssh
TAILSCALE_IP=$(tailscale ip -4)
echo "Tailscale IP: $TAILSCALE_IP"

# 2. Install K3s agent using Tailscale IP so the master can reach back
curl -sfL https://get.k3s.io | \
  K3S_URL="https://10.226.0.2:6443" \
  K3S_TOKEN="K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739" \
  INSTALL_K3S_EXEC="agent --node-ip=$TAILSCALE_IP --node-external-ip=$TAILSCALE_IP --node-label=node-type=spot --node-label=role=loader" \
  sh -

echo "K3s agent installed and joined cluster as SPOT"
