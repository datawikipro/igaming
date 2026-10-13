# Единая Инфраструктура Экосистемы (Infrastructure Single Source of Truth)
## SmartBet.guru, S3 Storage & CareerLab

Этот репозиторий (`datawikipro/infrastructure-openspec`) является **единым центральным источником правды** об аппаратной, сетевой и кластерной инфраструктуре для всех проектов экосистемы:
- **iGaming** (`smartbet.guru`, краулеры, агрегатор, SMM)
- **S3 Storage** (распределенное S3-совместимое объектное хранилище)
- **CareerLab** (карьерная лаборатория)

> **СТРОЖАЙШЕЕ ПРАВИЛО ДЛЯ ВСЕХ AI-АССИСТЕНТОВ**:
> При решении задач в любых проектах (iGaming, S3, CareerLab) конфигурация серверов, IP-адресов, прокси и маршрутов берется **СТРОГО** из этого репозитория.
> **КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО** опираться на воспоминания о старых серверах (`outline-vpn-eu-nl`, `34.158.66.184`, старые IP Нидерландов) — эти узлы давно выведены из эксплуатации!

---

## 🧭 Краткая Карта Узлов и Сетей

| Узел / Сервис | Tailscale IP / K8s DNS | Внешний IP / Порт | Назначение |
| :--- | :--- | :--- | :--- |
| **`xeon-srv`** | `100.78.183.101` | Bare-metal узел (СПб) | K3s кластер, PostgreSQL, Redroid, Git-зеркало (`/data/git-mirror/`). **Запрет ночного ребута 22:00-10:00!** |
| **`ru-proxy`** | `100.83.113.50` | `188.242.33.93` (домашний СПб) | Прямой резидентный выход РФ (ЦУПИС/ЕРАИ, ВКонтакте, доступ извне с российским IP). |
| **`de-proxy-1`** | `100.111.55.54` | `85.192.24.152` (AEZA Франкфурт) | Свободный интернет без РКН: Tinyproxy `:3128`, туннели `:3130` (US), `:3131` (FI). |
| **`cluster-switch-proxy`** | `cluster-switch-proxy.proxy:3128` | Tinyproxy `:3128` | **Nintendo Switch** и личные смартфоны (переключение апстрима: DE обход РКН / RU / Orion). |
| **`purevpn-*` Pool** | `purevpn-<loc>.proxy:3128` | 8 статических нод | Долгоживущие сессии 1:1 для SMM-аккаунтов и антифрода (квота 8/10). |
| **`orion-work-gateway`** | `orion-work-gateway.proxy` | NodePort 31328/31081/31053 | Шлюз проекта «Орион» (`orion-exit-node` `100.108.115.63`). |
| **`xiaomi-12t-pro`** | `100.114.62.28` | SIM `+79137671550` | Физический SIM-шлюз WhatsApp и SMS для приёма 2FA OTP кодов Instagram. |

---

## 📚 Архитектурные Документы:
- 🌐 [Сетевая карта и прокси-шлюзы](file:///c:/Users/chernousov_a/IdeaProjects/infrastructure-openspec/docs/architecture/network-and-proxy-mesh.md)
- 🎮 [Nintendo Switch, персональные смартфоны и обход РКН](file:///c:/Users/chernousov_a/IdeaProjects/infrastructure-openspec/docs/architecture/nintendo-switch-and-personal-devices.md)
- 📱 [SMM-персоны, циркадный ритм и SIM-шлюз Xiaomi 12T Pro](file:///c:/Users/chernousov_a/IdeaProjects/infrastructure-openspec/docs/architecture/smm-personas-and-sim-gateway.md)
- 💻 [Enterprise-регламент синхронной документации и 2 ноутбука](file:///c:/Users/chernousov_a/IdeaProjects/infrastructure-openspec/docs/workflows/enterprise-doc-sync-and-multi-laptop.md)
