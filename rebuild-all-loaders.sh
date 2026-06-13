#!/usr/bin/env bash
# rebuild-all-loaders.sh

MODULES=("zenit" "betcity" "olimpbet" "marathonbet" "fon-bet-ru" "leon" "tennisi" "winline" "sportbet" "betb2b" "ligastavok" "betboom" "baltbet" "pinnacle" "sbobet" "unibet")

for m in "${MODULES[@]}"; do
    echo ""
    echo -e "\033[35m========================================\033[0m"
    echo -e "\033[35m=== Rebuilding and Restarting $m ===\033[0m"
    echo -e "\033[35m========================================\033[0m"
    echo ""
    ./restart-ci.sh --only "$m"
done
