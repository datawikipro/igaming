# Design: Audit and Scale Bookmakers Pipeline

## Architecture Overview
Пайплайн сбора котировок состоит из независимых модулей `igaming-source-<bookmaker>`, разделенных на:
1. **Crawler (`APP_ROLE=league-crawler`)**: сканирование линии (Live и Prematch), обнаружение матчей и лиг, сохранение сырых событий в базу источника (`match_cache`).
2. **Loader (`APP_ROLE=match-loader`)**: периодический опрос активных событий, получение глубокой росписи (маркеты, форы, тоталы), нормализация и отправка котировок в Kafka топик `odds.updates`.
3. **Database (`igaming-source-<bookmaker>-db`)**: изолированный PostgreSQL инстанс для локального кэша событий и несмапленных исходов (`unmapped_bet`).

## Verification & Subagent Strategy
Для каждого букмекера создается или запускается субпроцессная процедура проверки:
1. **Проверка логов**: `kubectl logs <pod> -n igaming-source --tail=100` с анализом `ERROR`, `WARN`, `Exception`, `Timeout`.
2. **Проверка базы**: проверка количества событий в `match_cache` и несмапленных исходов в `unmapped_bet`.
3. **Проверка отправки в Kafka**: проверка смещения и метрик в `odds_actual` в БД `igaming_aggregator`.
4. **Устранение ошибок**: при обнаружении падений Playwright или ошибок парсинга — правка мапперов, перезапуск сессий или тюнинг прокси.

## Scaling New Sources
- Развертывание манифестов из `igaming-k8s/` для `22bet`, `1xbit`, `888starz`, `betandyou`, `fonbet.kz`, `fonbet.by`, `melbet-com`.
- Контроль запуска подов с использованием лейбла `node-type=standard` на свободных SSD-нодах (`k8s-worker-2`, `k8s-worker-3`, `k8s-worker-1`).
