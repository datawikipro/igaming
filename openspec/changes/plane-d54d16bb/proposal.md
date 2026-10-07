# Proposal: [STALE] Букмекер Linebet перестал присылать данные (лаг 461.6 мин)

## Context
Plane Task ID: `d54d16bb-0000-0000-0000-000000000000`

## Problem Statement
В системе мониторинга зафиксирован инцидент с прекращением поступления данных от букмекера Linebet (`linebet`) с расчетным лагом 461.6 мин (порог: 15.0 мин).
Необходимо провести комплексный аудит источника `igaming-source-linebet`, верифицировать работоспособность краулера, лоадера и базы данных в кластере Kubernetes (`igaming-source`), проверить актуальность URL-адресов API семейства BetB2B, маршрутизацию через кластерный HTTP-прокси `100.83.113.50:3128`, передачу heartbeats в ядро `igaming-aggregator`, наполнение линии ($\ge 500$ матчей в `match_cache`), доставку котировок в ядро агрегации и подтвердить полное устранение лага.

## Proposed Changes
1. **Анализ и устранение причин отставания линии**:
   - Настройка корректных BetB2B эндпоинтов (`https://1x-bet.com/LiveFeed/Get1xMatchByLeague`, `https://1x-bet.com/LineFeed/Get1xMatchByLeague`) для обхода блокировок `linebet.com`.
   - Настройка параметров проксирования (`100.83.113.50:3128`) с обязательным указанием `-Dhttp.nonProxyHosts="localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local"` и `-DsocksNonProxyHosts` для исключения перехвата внутренних K8s-запросов к `igaming-aggregator`.
   - Актуализация K8s Service DNS для `APP_AGGREGATOR_URL` (`http://igaming-aggregator.igaming-dev.svc.cluster.local:8080`) и `APP_PROXY_BACKEND_URL` (`http://service-proxy-backend.proxy.svc.cluster.local`).
   - Настройка неблокирующих параметров HikariCP/JPA (`initialization-fail-timeout=0`, `connection-timeout=5000`, `validation-timeout=3000`, `ddl-auto=update`).
2. **Верификация работоспособности и наполнения линии**:
   - Проверка подов `igaming-source-linebet-crawler`, `igaming-source-linebet-loader`, `igaming-source-linebet-db-0` в namespace `igaming-source`.
   - Проверка наполнения `match_cache` ($\ge 500$ матчей, факт > 1500 матчей).
   - Проверка статуса источника в `igaming_aggregator` (`bet_source.is_active=true`, `last_seen` актуален) и доставки котировок в `odds_actual` (> 84 000 котировок, лаг < 10 сек).
3. **5-минутный Soak-тест**:
   - Мониторинг стабильной работы подов без ошибок в течение 5 минут.
4. **Фиксация в OpenSpec**:
   - Создание и валидация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
