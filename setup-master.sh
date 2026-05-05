#!/bin/bash
set -e

# Cloudflared - install via direct URL with x86_64 naming
echo "Downloading cloudflared..."
curl -L -o /tmp/cloudflared.rpm "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-x86_64.rpm"
file /tmp/cloudflared.rpm
rpm -ivh /tmp/cloudflared.rpm
cloudflared --version
echo "CLOUDFLARED_DONE"
