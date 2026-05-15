#!/bin/bash
set -e
exec > /var/log/startup-script.log 2>&1
sleep 10
curl -fsSL https://tailscale.com/install.sh | sh
tailscale up --authkey="tskey-auth-kZotQnypWz11CNTRL-tZfx6Wiowm2GwL6euwP5n2Ti4487uRHab" --ssh
TAILSCALE_IP=$(tailscale ip -4)
curl -sfL https://get.k3s.io | \
  KUBECONFIG_MODE="644" \
  K3S_URL="https://master-vm:6443" \
  K3S_TOKEN="K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739" \
  INSTALL_K3S_EXEC="agent --node-ip=$TAILSCALE_IP --node-external-ip=$TAILSCALE_IP --node-label=node-type=standard --node-label=provisioning=standard --node-label=role=worker --node-label=provider=gcp" \
  sh -
