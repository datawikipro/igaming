#!/bin/bash
# ==============================================================================
# SmartBet.guru — Stealth noVNC Container Entrypoint
# Task #76 [stealth-novnc]
# ==============================================================================
set -e

DISPLAY_NUM=99
SCREEN_RES="1280x800x24"
VNC_PORT=5900
WEBSOCKIFY_PORT=6080

echo "=== [SmartBet Stealth Runner] Starting Container Services ==="
echo "Display: :${DISPLAY_NUM} (${SCREEN_RES})"
echo "noVNC Port: ${WEBSOCKIFY_PORT}"

# 1. Clean up stale lock files
rm -f /tmp/.X${DISPLAY_NUM}-lock /tmp/.X11-unix/X${DISPLAY_NUM} || true

# 2. Start Xvfb virtual framebuffer display
echo "-> Starting Xvfb on :${DISPLAY_NUM}..."
Xvfb :${DISPLAY_NUM} -screen 0 ${SCREEN_RES} -ac +extension GLX +render -noreset &
XVFB_PID=$!
sleep 1

# 3. Start Openbox lightweight window manager (for clean window framing)
if command -v openbox >/dev/null 2>&1; then
    DISPLAY=:${DISPLAY_NUM} openbox &
fi

# 4. Start x11vnc attached to Xvfb
echo "-> Starting x11vnc on port ${VNC_PORT}..."
x11vnc -display :${DISPLAY_NUM} \
       -forever \
       -shared \
       -rfbport ${VNC_PORT} \
       -nopw \
       -bg \
       -quiet \
       -xkb

# 5. Start websockify for HTML5 / WebSocket noVNC access
echo "-> Starting websockify on port ${WEBSOCKIFY_PORT}..."
NOVNC_DIR="/usr/share/novnc"
if [ ! -d "$NOVNC_DIR" ]; then
    NOVNC_DIR="/opt/novnc"
fi

websockify --web "$NOVNC_DIR" ${WEBSOCKIFY_PORT} localhost:${VNC_PORT} &
WEBSOCKIFY_PID=$!

export DISPLAY=:${DISPLAY_NUM}
echo "=== [SmartBet Stealth Runner] Virtual Display & noVNC Ready! ==="
echo "noVNC URL: http://localhost:${WEBSOCKIFY_PORT}/vnc.html"

# 6. Execute main command passed to container (e.g. python agent script)
if [ "$#" -gt 0 ]; then
    echo "-> Executing agent command: $@"
    exec "$@"
else
    echo "-> No command specified; keeping container alive with bash..."
    tail -f /dev/null
fi
