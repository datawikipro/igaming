# Proposal: [fon-bet-by] Исправить имя БД PostgreSQL и восстановить инжест (target >= 500 matches)

## Context
Plane Task ID: `b169e481-0702-41e2-b5cc-6cbdc8b8235f`

## Description

### Проблема
Сервис `igaming-source-fon-bet-by` имел 0 матчей в БД, несмотря на статус `Running 2/2`.

**Корневая причина**: `APP_VPN_FORCE=true` в обоих деплоях (crawler + loader) требовал получения прокси из пула `service-proxy-backend`, который возвращал HTTP 503 (все ноды `purevpn-*2-auto-tcp-qr` в статусе DEAD). Краулер зависал в цикле ожидания прокси и не производил запросы.

Кластерный HTTP-прокси `100.83.113.50:3128` при этом работал корректно для `fonbet.by` (трафик идёт через sing-box → прямое соединение для BY доменов).

**Дополнительно**: Fallback-зеркала были указаны как `line06.fonbet.by`, `line1.fonbet.by` и т.д., которые больше не работают. Актуальные API endpoints для fonbet.by: `line11.by0e87-resources.by`, `line12.by0e87-resources.by` (из `/urls.json`).

### Исправления в `igaming-k8s/fonbet.by.yaml`
- `APP_VPN_FORCE: 'false'` (crawler + loader) — использует кластерный прокси без ожидания пула
- `APP_FONBET_FALLBACK_MIRRORS` — обновлено на актуальные BY API домены

### Результат
- `match_cache` count: **1720 матчей** ≥ 500 ✅
- Все поды: `Running 2/2` ✅
