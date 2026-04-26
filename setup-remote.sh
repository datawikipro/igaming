#!/bin/bash
set -e
mkdir -p ~/igaming
cd ~/igaming
REPOS=(
  "igaming-aggregator"
  "igaming-bot"
  "igaming-dto"
  "igaming-portal"
  "igaming-source-baltbet"
  "igaming-source-betboom"
  "igaming-source-betcity"
  "igaming-source-betm"
  "igaming-source-bettery"
  "igaming-source-core"
  "igaming-source-fon-bet"
  "igaming-source-leon"
  "igaming-source-ligastavok"
  "igaming-source-marathonbet"
  "igaming-source-melbet"
  "igaming-source-olimpbet"
  "igaming-source-olimpbet-kz"
  "igaming-source-pari"
  "igaming-source-sportbet"
  "igaming-source-tennisi"
  "igaming-source-winline"
  "igaming-source-zenit"
)

for repo in "${REPOS[@]}"; do
  echo "Syncing $repo..."
  rm -rf "$repo"
  gh repo clone "datawikipro/$repo" "$repo"
done
echo "All repos synced."
