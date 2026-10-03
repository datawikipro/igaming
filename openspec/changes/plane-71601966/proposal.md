# Proposal: [melbet-com] Обновить зеркало BetB2B и запустить сбор линии

## Context
Plane Task ID: `71601966-6782-4079-9d04-5670184ca85a`
Bookmaker: Melbet International (`melbet-com`, платформа BetB2B)
Namespace: `igaming-source`
Database: `igaming_melbet_com` (`igaming-source-melbet-com-db`)

## Problem Statement
Микросервис краулера и лоадера международной версии Melbet (`igaming-source-melbet-com-crawler` и `igaming-source-melbet-com-loader`) находился в состоянии деградации с 0 собранных матчей в таблице `match_cache` (`SELECT count(*) FROM match_cache = 0`).
При анализе логов выявлены две ключевые проблемы:
1. **Сетевой таймаут и блокировка прямого домена**:
   В качестве `APP_TARGET_HOST` был указан заблокированный в РФ домен `melbet.com`. Проверка доступности в `VpnManagerService.isDirectlyReachable("melbet.com")` завершалась неудачей (таймаут SSL handshake).
2. **Маршрутизация на несуществующий прокси (UnknownHostException)**:
   При сбое прямой проверки `VpnManagerService` пытался запросить прокси из пула, но из-за отсутствия конфигурации в сервисе прокси `BrowserProxyManager.getCurrentProxyUri()` возвращал устаревший адрес по умолчанию `http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128`. Так как данный DNS-хост отсутствует в кластере, все сетевые HTTP-запросы `RestTemplate` и `Playwright` завершались ошибкой `I/O error on GET request: proxy-vpn-pool.service-proxy.svc.cluster.local`.
3. **Отсутствие K8s Health Probes в деплойменте краулера**:
   В деплойменте `igaming-source-melbet-com-crawler` отсутствовали проверки `livenessProbe` и `readinessProbe`, нарушая единый стандарт надежности BetB2B-источников.

## Proposed Changes
1. **Актуализация зеркала BetB2B и хоста доступности**:
   - Установить `APP_TARGET_HOST=1x-bet.com` (работающее зеркало BetB2B API для семейства 1x/Melbet).
   - Установить параметры прямого подключения `APP_VPN_ENABLED="false"` и `APP_PROXY_DIRECT="true"`, гарантируя обход устаревшего неактивного адреса `proxy-vpn-pool` по аналогии с другими стабильно работающими источниками (`melbet.ru`, `baltbet`, `pari`, `tennisi`).
2. **Внедрение K8s Probes**:
   - Добавить `startupProbe`, `livenessProbe` и `readinessProbe` для контейнера `igaming-source-melbet-com-crawler` на порту `3058` (`/actuator/health/liveness`, `/actuator/health/readiness`).
3. **Запуск и верификация сбора линии**:
   - Применить обновленный манифест `igaming-k8s/melbet-com.yaml` в K8s.
   - Обеспечить наполнение таблицы `match_cache` до уровня $\ge 500$ активных спортивных событий (Threshold DoD).
   - Выдержать 5-минутный Soak Test в соответствии с требованиями AGENTS.md.

## Impact & Capabilities
- Восстановление потока котировок Melbet International (`melbet-com`) в агрегатор `igaming-aggregator`.
- Достижение целевого показателя $>1\,000$ матчей в `igaming_melbet_com`.
- Повышение отказоустойчивости подов за счет пробирования Actuator в K8s.
