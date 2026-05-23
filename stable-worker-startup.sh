#!/bin/bash
# Stable Worker VM startup script - joins K3s cluster automatically as STABLE node
# Use this for non-preemptible (on-demand) GCP VMs that host critical services
# (aggregator, aggregator-db, portal-postgres, etc.)
set -e
exec > /var/log/startup-script.log 2>&1

# Wait for network
sleep 10

# 1. Install Tailscale and join VPN (required to reach k3s master at master-vm)
curl -fsSL https://tailscale.com/install.sh | sh
tailscale up --authkey="tskey-auth-kZotQnypWz11CNTRL-tZfx6Wiowm2GwL6euwP5n2Ti4487uRHab" --ssh
TAILSCALE_IP=$(tailscale ip -4)
echo "Tailscale IP: $TAILSCALE_IP"

# 2. Install K3s agent using Tailscale IP so the master can reach back
curl -sfL https://get.k3s.io | \
  K3S_URL="https://master-vm:6443" \
  K3S_TOKEN="K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739" \
  INSTALL_K3S_EXEC="agent --node-ip=$TAILSCALE_IP --node-external-ip=$TAILSCALE_IP --node-label=node-type=stable --node-label=role=core" \
  sh -

echo "K3s agent installed and joined cluster as STABLE"
