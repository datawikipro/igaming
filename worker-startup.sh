#!/bin/bash
# Worker VM startup script - joins K3s cluster automatically

# Wait for network
sleep 10

# Install K3s agent
curl -sfL https://get.k3s.io | \
  K3S_URL="https://10.226.0.2:6443" \
  K3S_TOKEN="K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739" \
  INSTALL_K3S_EXEC="agent --node-label=role=stable --node-label=node-type=worker" \
  sh -

echo "K3s agent installed and joined cluster" >> /var/log/startup-script.log
