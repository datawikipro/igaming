# Сетевая Архитектура и Топология Прокси-Шлюзов (Network & Proxy Mesh)
## SmartBet.guru / iGaming Infrastructure

---

## 1. Обзор Сетевой Архитектуры

Инфраструктура iGaming объединяет bare-metal вычислительные мощности, европейские шлюзы для свободного доступа в сеть, резидентные домашние IP РФ для работы с лицензированными букмекерами ЕРАИ/ЦУПИС и защищенную mesh-сеть **Tailscale** (подсеть `100.64.0.0/10`).

```mermaid
flowchart TB
    subgraph Clients["Клиенты и Устройства"]
        Laptop1["Ноутбук 1 (Dev)"]
        Laptop2["Ноутбук 2 (Dev)"]
        Switch["Nintendo Switch"]
        Phone["Смартфон / Xiaomi 12T Pro<br>100.114.62.28"]
    end

    subgraph TailscaleMesh["Tailscale Mesh Network"]
        TS_Xeon["xeon-srv (Bare-metal Master)<br>100.78.183.101"]
        TS_Ru["ru-proxy (Home Gateway, SPb)<br>100.83.113.50"]
        TS_DE["de-proxy-1 (AEZA Frankfurt)<br>100.111.55.54 (ext: 85.192.24.152)"]
        TS_Orion["orion-exit-node<br>100.108.115.63"]
        TS_Mullvad["Mullvad VPN Nodes<br>HK, SG, DE, SE, US"]
    end

    subgraph XeonCluster["Kubernetes (K3s) Cluster on xeon-srv"]
        SwitchProxy["cluster-switch-proxy (Tinyproxy :3128)<br>10.244.3.222 / Service"]
        DE_Svc["de-proxy.proxy.svc<br>:3128"]
        FI_Svc["fi-proxy.proxy.svc<br>:3128"]
        US_Svc["us-proxy.proxy.svc<br>:3128"]
        PureVPN["PureVPN 8-Node Pool<br>(NL, UK, DE, US, HK, SG, BR)"]
        OrionGW["orion-work-gateway<br>:3128 / :1080 / :53"]
        Pods["Поды: Краулеры, SMM, Воркеры"]
        GitMirror["Git Mirror: /data/git-mirror/"]
    end

    subgraph Egress["Внешний Выход в Интернет"]
        EU_Net["Свободный Интернет (Европа/Мир)<br>Без блокировок РКН / ТСПУ"]
        RU_Net["Резидентный РФ Интернет (СПб)<br>188.242.33.93 (ЦУПИС / ЕРАИ)"]
        OrionNet["Внутренняя сеть Orion"]
    end

    Switch -->|WiFi Proxy :3128| SwitchProxy
    Phone -->|WiFi Proxy :3128| SwitchProxy
    Laptop1 --> TS_Xeon
    Laptop2 --> TS_Xeon

    SwitchProxy -->|Режим DE (Default)| TS_DE
    SwitchProxy -->|Режим RU| TS_Ru
    SwitchProxy -->|Режим Orion| OrionGW

    TS_DE --> EU_Net
    TS_Ru --> RU_Net
    OrionGW --> OrionNet

    Pods --> DE_Svc --> TS_DE
    Pods --> PureVPN
    Pods --> FI_Svc
    Pods --> US_Svc
```

---

## 2. Инвентаризация Узлов и Серверов

### 2.1. Физический Bare-Metal Сервер: `xeon-srv`
- **Tailscale IP**: `100.78.183.101`
- **Аппаратные ресурсы**: 128 GB RAM, многоядерный Intel Xeon, NVMe дисковый массив.
- **Роль**: Главный узел кластера Kubernetes (K3s), хранилище баз данных PostgreSQL (`synchronous_commit = off`), среда выполнения фоновых воркеров Plane, Docker-пул виртуальных телефонов Redroid, локальное зеркало Git (`/data/git-mirror/`).
- **Строгий регламент**: Запрет перезагрузки в ночное время (22:00 — 10:00) во избежание шума охлаждения в жилом помещении.

### 2.2. Российский Домашний Шлюз: `ru-proxy`
- **Tailscale IP**: `100.83.113.50`
- **Внешний IP**: `188.242.33.93` (домашний статический провайдер в Санкт-Петербурге).
- **Роль**: Прямой резидентный выход (`direct`) для российских букмекеров (Фонбет, Винлайн, Пари, Леон, Бетсити и др.), отечественных социальных сетей (ВКонтакте) и удаленного доступа к российским сервисам при нахождении за пределами РФ.
- **Кластерный сервис**: `cluster-ai-proxy` (`100.83.113.50:3128`).

### 2.3. Европейский Выходной Сервер: `de-proxy-1`
- **Внешний IP**: `85.192.24.152` (AEZA, дата-центр Frankfurt am Main, Германия).
- **Tailscale IP**: `100.111.55.54`
- **Службы**:
  - `tinyproxy` на порту `3128`: прямой нефильтрованный выход в европейский интернет в обход ТСПУ и блокировок РКН.
  - Порт `3130`: локальный форвардинг туннеля в США (`us-proxy`).
  - Порт `3131`: локальный форвардинг туннеля в Финляндию (`fi-proxy`).
- **Назначение**: Оффшорные и европейские букмекеры (Pinnacle, Bet365, Sbobet), LLM API (OpenAI, Google Gemini, Anthropic), Telegram Bot API.

### 2.4. Пул Прокси PureVPN (Dedicated Static Pool)
- **Namespace в K8s**: `proxy`
- **Доступные ноды**: `purevpn-nl`, `purevpn-uk`, `purevpn-de`, `purevpn-us-ca`, `purevpn-hk`, `purevpn-sg`, `purevpn-br`, `purevpn-jp`.
- **Лимит безопасности**: Максимум 8 активных туннелей на аккаунт (из 10 доступных квот).
- **Назначение**: Долгоживущие статические сессии для антифрод-защищенных сервисов (Meta, Instagram, Threads, Reddit).

### 2.5. Шлюз Проекта «Орион»: `orion-work-gateway`
- **Tailscale IP узла**: `100.108.115.63` (`orion-exit-node`).
- **Сервис в K8s**: `orion-work-gateway.proxy.svc.cluster.local`:
  - Порт `3128` (HTTP Connect Proxy).
  - Порт `1080` (SOCKS5 Proxy).
  - Порт `53` (Внутренний DNS Orion).
- **Назначение**: Изолированная маршрутизация запросов к экосистеме Orion.

---

## 3. Матрица Маршрутизации в Kubernetes

Для подов внутри Kubernetes действуют правила прямого объявления исходящей сети:

| Сервис в K8s DNS | Назначение и Протокол | Целевой Upstream |
| :--- | :--- | :--- |
| `http://de-proxy.proxy.svc.cluster.local:3128` | HTTP/HTTPS прокси | `100.111.55.54:3128` (Германия) |
| `http://us-proxy.proxy.svc.cluster.local:3128` | HTTP/HTTPS прокси | `100.111.55.54:3130` (США) |
| `http://fi-proxy.proxy.svc.cluster.local:3128` | HTTP/HTTPS прокси | `100.111.55.54:3131` (Финляндия) |
| `http://hk-proxy.proxy.svc.cluster.local:3128` | HTTP/HTTPS прокси | `100.109.193.55:3128` (Гонконг) |
| `http://purevpn-nl.proxy.svc.cluster.local:3128` | Статический прокси Нидерланды | PureVPN WireGuard Pod |
| `http://purevpn-uk.proxy.svc.cluster.local:3128` | Статический прокси Великобритания | PureVPN WireGuard Pod |
| `http://cluster-switch-proxy.proxy.svc.cluster.local:3128` | Переключаемый шлюз | Nintendo Switch & Смартфоны |
| `direct` (`NO_PROXY=*`) | Без прокси (чистый IP СПб) | Домашний IP `188.242.33.93` |
