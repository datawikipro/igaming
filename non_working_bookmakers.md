# Неработающие букмекеры (Non-working Bookmakers)

В данном файле собран актуальный список неработающих букмекеров на основе анализа логов `igaming-aggregator-ingestion` и состояния базы данных. Всего в конфигурации Kubernetes (`igaming-k8s`) обнаружено **30 БК**, из которых **14 работают**, а **16 требуют исправления**.

Список разделен на **3 сбалансированные группы** для распределения по 3 агентам.

---

## 📊 Общая статистика

* **Всего БК в системе:** 30
* **🟢 Работающие БК (14):**
  * `baltbet` (baltbet.ru)
  * `betboom` (betboom.ru)
  * `betcity` (betcity.ru)
  * `betm` (betm)
  * `bettery` (bettery.ru)
  * `fon-bet-kz` (fonbet.kz)
  * `fon-bet-ru` (fon.bet)
  * `leon` (leon.ru)
  * `marathonbet` (marathonbet.ru)
  * `marathonbet-by` (marathonbet.by)
  * `marathonbet-com` (marathonbet.com)
  * `olimpbet` (olimpbet.ru)
  * `pari` (pari.ru)
  * `zenit` (zenit.win)
* **🔴 Неработающие БК (16):** См. группы ниже.

---

## 👥 Распределение задач по 3 агентам

### 🧩 Группа 1: Клоны BetB2B (Общий код)
> [!TIP]
> Все эти букмекеры работают на одной и той же кодовой базе `igaming-source-betb2b` (образ `ghcr.io/datawikipro/igaming-source-betb2b:latest`).
> Основная проблема — обход блокировок / ротация прокси или изменение путей API (например, `/service-api/LineFeed/...` возвращает 404/500 или редирект на `/ru/block`).
> **Решение для одного исправит сразу все 6 букмекеров!**

| ID БК | Название | Файл конфигурации | Домен |
| :--- | :--- | :--- | :--- |
| `1xbet` | 1xBet | [1xbet.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/1xbet.yaml) | `1xbet.com` |
| `888starz` | 888starz | [888starz.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/888starz.yaml) | `888starz.bet` |
| `betandyou` | Betandyou | [betandyou.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/betandyou.yaml) | `betandyou.com` |
| `fansport` | Fansport | [fansport.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/fansport.yaml) | `fan-sport.com` |
| `linebet` | Linebet | [linebet.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/linebet.yaml) | `linebet.com` |
| `megapari` | Megapari | [megapari.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/megapari.yaml) | `megapari.com` |

---

### 🌐 Группа 2: Зеркала, Региональные клоны и Простые API
> [!IMPORTANT]
> В эту группу входят зеркала и региональные версии уже работающих контор (Fonbet, Betcity, Olimpbet), Melbet (работающий на BetB2B для .ru зоны) и простой региональный парсер Sportbet.

| ID БК | Название | Файл конфигурации | Домен | Примечание |
| :--- | :--- | :--- | :--- | :--- |
| `melbet` | Melbet.ru | [melbet.ru.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/melbet.ru.yaml) | `melbet.ru` | Использует кодовую базу BetB2B, но для зоны РФ |
| `betcity-com` | Betcity.com | [betcity.com.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/betcity.com.yaml) | `betcity.com` | Зеркало Betcity.ru |
| `fon-bet-by` | Fonbet.by | [fonbet.by.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/fonbet.by.yaml) | `fonbet.by` | Зеркало Fonbet.ru |
| `olimpbet-kz` | Olimpbet.kz | [olimpbet.kz.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/olimpbet.kz.yaml) | `olimpbet.kz` | Зеркало Olimpbet.ru |
| `sportbet` | Sportbet.ru | [sportbet.ru.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/sportbet.ru.yaml) | `sportbet.ru` | Отдельный краулер |

---

### 🛡️ Группа 3: Уникальные и Крупные Standalone букмекеры
> [!WARNING]
> Эти букмекеры используют уникальный код парсинга (`igaming-source-{bk-name}`). У них сложные защиты от парсинга (Cloudflare, Akamai) или специфические XHR/WebSocket сессии.
> Потребуется индивидуальная отладка каждого модуля.

| ID БК | Название | Файл конфигурации | Домен | Особенности |
| :--- | :--- | :--- | :--- | :--- |
| `winline` | Winline.ru | [winline.ru.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/winline.ru.yaml) | `winline.ru` | База данных содержит старые коэффициенты, сейчас заблокирован/лежит |
| `tennisi` | Tennisi | [tennisi.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/tennisi.yaml) | `tennisi.com` | Нестандартный API |
| `ligastavok` | Liga Stavok | [ligastavok.ru.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/ligastavok.ru.yaml) | `ligastavok.ru` | Защита Cloudflare / сложные сессии |
| `pinnacle` | Pinnacle | [pinnacle.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/pinnacle.yaml) | `pinnacle.com` | Требуются API-ключи / VPN |
| `sbobet` | SBOBET | [sbobet.yaml](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-k8s/sbobet.yaml) | `sbobet.com` | Азиатский гигант, жесткая защита Cloudflare |

---

## 🛠️ Как использовать

Файл сохранен в формате JSON по пути: [non_working_bookmakers.json](file:///c:/Users/chernousov_a/IdeaProjects/igaming/non_working_bookmakers.json). Вы можете скормить этот JSON вашим агентам или раздать им markdown-задачи на основе таблицы выше.
