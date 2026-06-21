# Правила для AI-ассистентов — Проект igaming (бэкенд-монорепо)

Этот документ содержит обязательные правила и контекст для AI-ассистентов, работающих с проектом **SmartBet.guru**.

---

## 📌 Общий контекст проекта

**SmartBet.guru** — спортивно-аналитическая SaaS-платформа для поиска **вилок (Surebets)**, **ставок с перевесом (+EV / ValueBets)** и **коридоров (Middles)** в реальном времени.

- **Бизнес-модель**: подписка Free / Premium + реферальная программа + API-поток.
- **Основная документация**: `/docs/GUIDE.md` — читать ОБЯЗАТЕЛЬНО перед любыми изменениями.
- **Архитектура**: `/docs/архитектура.md`

---

## 🗂️ Структура рабочих директорий

| Директория (macOS) | Назначение |
|---|---|
| `/Users/aleksei.chernousov/IdeaProjects/igaming` | **Этот репозиторий** — бэкенд-монорепо (Java/Spring + K8s + скрипты) |
| `/Users/aleksei.chernousov/WebstormProjects/igaming` | Фронтенд (Next.js `smartbet.guru`, Chrome Extension, Admin UI) |
| `/Users/aleksei.chernousov/Documents/igaming` | Рабочие скрипты, kubectl-команды, сессии |

---

## ⚙️ Ключевые компоненты (этот репозиторий)

| Модуль | Стек | Описание |
|---|---|---|
| `aggregator-api` | Spring Boot | REST API агрегатора (матчи, коэффициенты) |
| `aggregator-domain` | Java | Доменные сущности (Match, Odds, Surebet) |
| `aggregator-ingestion` | Spring Kafka | Приём данных от краулеров через Kafka |
| `aggregator-normalizer` | Spring Boot | Нормализация названий команд/событий |
| `aggregator-surebet` | Spring Boot | Детектор вилок в памяти + запись в PostgreSQL |
| `aggregator-odds-sync` | Spring Boot | Синхронизация коэффициентов между БК |
| `aggregator-enrichment` | Spring Boot | Обогащение данных (статистика, лиги) |
| `igaming-portal` | Spring Boot + JWT | API-шлюз: авторизация, тарифы, кэш, прокси |
| `igaming-bot` | Spring Boot + TelegramBots | Telegram-бот: сигналы вилок по подпискам |
| `igaming-auth-microservice` | Spring Security | JWT-авторизация (auth-base) |
| `igaming-source-core` | Java + Playwright | Базовая библиотека краулеров |
| `igaming-source-*` | Java/Node.js/Python | Краулеры/лоадеры каждого букмекера |
| `igaming-capture-liveresult` | Java | Сбор live-результатов матчей |
| `igaming-capture-sofascore` | Java | Интеграция SofaScore API |
| `service-proxy-backend` | Node.js | VPN/прокси-пул для краулеров |
| `igaming-k8s` | YAML | K8s-манифесты для всех сервисов |
| `igaming-llm-admin` / `igaming-llm-worker` | Python | LLM-инфраструктура |
| `igaming-analytics-service` | Java | Аналитический сервис |
| `igaming-infra-operator` | Go/Java | Kubernetes-оператор |

---

## 🔧 Правила разработки

### CI/CD и деплой
- **GitHub Actions НЕ используется** для деплоя (несмотря на наличие `.github/workflows`).
- **Деплой выполняется локально** через PowerShell-скрипты:
  - `.\restart-ci.ps1` — сборка всех модулей
  - `.\restart-ci.ps1 -Only {имя_модуля}` — сборка конкретного модуля (например `tennisi`, `fon-bet-ru`)
- **КРИТИЧЕСКИ ВАЖНО**: Сборки выполняются **строго по очереди** на удалённой машине `100.86.137.112`. Параллельный запуск `restart-ci.ps1` **ЗАПРЕЩЁН** — скрипты перезаписывают файлы друг друга (`git reset --hard`).

### Kubernetes — правила расстановки нод

> ⚠️ Неправильный `nodeSelector` = Pending-поды. Всегда проверять!

| Тип пода | nodeSelector |
|---|---|
| `*-db` (PostgreSQL StatefulSet) | `required: node-type=master` |
| `*-crawler`, `*-loader` | `preferred: standard`, `allowed: standard+spot` |
| Доп. реплики лоадеров | `preferred: spot` |
| `igaming-aggregator`, `igaming-portal` | `required: standard` |
| Остальные сервисы | `allowed: standard+spot` |

### Docker-образы
Публикуются в GitHub Container Registry:
```
ghcr.io/datawikipro/igaming-source-{bookmaker}:latest
ghcr.io/datawikipro/igaming-aggregator:latest
ghcr.io/datawikipro/igaming-portal:latest
```
Секрет для pull: `regcred`.

---

## 🌐 Браузерные профили краулеров (Stealth Profiles)

Краулеры `igaming-source-*` используют три режима. **Использовать минимально возможный!**

| Режим | Переменная `APP_BROWSER_STEALTH_PROFILE` | Когда использовать |
|---|---|---|
| `BASIC` (default) | *(не задавать)* | Простые API: `baltbet`, `leon`, `pinnacle`, `sbobet` |
| `HEADLESS_STEALTH` | `HEADLESS_STEALTH` | Средняя защита: `winline`, `marathonbet`, `betcity`, `olimpbet`, `zenit`, `fon-bet-ru` |
| `XVFB_HEADED` | `XVFB_HEADED` | Жёсткая защита: семейство Fonbet (зарубежные), семейство BetB2B/1xbet, `ligastavok`, `tennisi`, `sportbet` |

⚠️ **ЗАПРЕТ**: нельзя глобально включать `HEADLESS_STEALTH`/`XVFB_HEADED` для всех краулеров — это перегрузит кластер.

---

## 📋 Активные букмекеры (igaming-source-*)

1xbet, 888sport, baltbet, betb2b, betboom, betcity, betfair, betmgm, betsson, bwin, caesars, dafabet, digitain, draftkings, fanduel, fon-bet-ru, leon, ligastavok, marathonbet, olimpbet, pinnacle, sbobet, sportbet, tennisi, unibet, winline, zenit

---

## 🚫 Автоматизация социальных сетей

При автоматизации (Threads, Instagram и т.п.) **НЕ пытаться войти в аккаунт** — сессия уже настроена.

- **Chrome Profile**: `/Users/aleksei.chernousov/Documents/igaming/sessions/chrome`
- **Credentials**: `/Users/aleksei.chernousov/Documents/igaming/passwords.txt`

Запуск Chrome:
```bash
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
  --remote-debugging-port=9222 \
  --headless=new \
  --user-data-dir="/Users/aleksei.chernousov/Documents/igaming/sessions/chrome" \
  --disable-gpu
```
Прочитать WebSocket URL из `http://127.0.0.1:9222/json/version` и записать в `/Users/aleksei.chernousov/Library/Application Support/Google/Chrome/DevToolsActivePort` перед вызовом browser-субагента.

---

## ⚡ Частые проблемы

- **Поды Pending**: проверь `nodeSelector` / `affinity` — всегда есть `standard` в `required` или `allowed`.
- **CrashLoopBackOff**: смотри `kubectl logs {pod} -n igaming-dev --previous`.
- **Spot-ноды удалены GCP**: временно навесить `node-type=spot` на standard-ноды.
- **OOMKilled**: увеличить `resources.limits.memory` в K8s-манифесте.
