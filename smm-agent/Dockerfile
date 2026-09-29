# ==============================================================================
# SmartBet.guru — Stealth noVNC Browser Agent Base Image
# Task #76 [stealth-novnc] & Task #54 [smm-runner]
# ==============================================================================

FROM python:3.11-slim

ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONUNBUFFERED=1
ENV DISPLAY=:99

# Install system dependencies: Xvfb, x11vnc, websockify, noVNC, ffmpeg, openbox
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11vnc \
    openbox \
    novnc \
    websockify \
    curl \
    git \
    wget \
    tar \
    bzip2 \
    ffmpeg \
    libgl1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libxtst6 \
    libdbus-glib-1-2 \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Setup symlink for noVNC web assets if needed
RUN ln -s /usr/share/novnc/vnc.html /usr/share/novnc/index.html || true

# Set working directory
WORKDIR /app

# Install Python packages: Playwright, Camoufox, OpenCV, Redis, requests
RUN pip install --no-cache-dir \
    playwright==1.45.0 \
    camoufox==0.4.10 \
    opencv-python-headless==4.10.0.84 \
    numpy==1.26.4 \
    redis==5.0.7 \
    requests==2.32.3 \
    pyotp==2.9.0

# Install Playwright browser dependencies (Firefox / Gecko engine)
RUN playwright install-deps firefox && \
    playwright install firefox

# Copy entrypoint and python scripts
COPY entrypoint-stealth-novnc.sh /app/entrypoint-stealth-novnc.sh
RUN chmod +x /app/entrypoint-stealth-novnc.sh

COPY captcha_solver.py /app/captcha_solver.py

# Expose noVNC WebSocket port
EXPOSE 6080

ENTRYPOINT ["/app/entrypoint-stealth-novnc.sh"]
CMD ["python", "/app/captcha_solver.py"]
