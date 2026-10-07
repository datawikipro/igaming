# Proposal: [HIGH LAG] Критическое отставание линии 1xBit (32.7 мин)

## Context
Plane Task ID: `613b7f54-2bc4-40fc-8246-b1aa62778405`

## Problem Statement
В системе мониторинга зафиксирован критический инцидент с отставанием линии международного крипто-букмекера 1xBit (`1xbit`) с расчетным лагом 32.7 мин.
Букмекер 1xBit относится к семейству BetB2B/1xBet (`igaming-source-betb2b`) и обслуживается микросервисами `igaming-source-1xbit-crawler`, `igaming-source-1xbit-loader` и выделенной базой данных `igaming-source-1xbit-db-0` в Kubernetes namespace `igaming-source`.

Ранее в манифесте `igaming-k8s/1xbit.yaml` контейнер краулера `igaming-source-1xbit-crawler` не содержал стандартных Actuator health-проб (`startupProbe`, `livenessProbe`, `readinessProbe`), что нарушало Golden Rule 1 из `AGENTS.md` и могло приводить к задержке детекции зависания браузерных сессий или HTTP-клиента.

Необходимо восстановить и подтвердить надежность потока котировок, добавить недостающие health-пробы в K8s манифест, провести аудит базы источника и агрегатора, обеспечить соблюдение всех Golden Rules из `AGENTS.md` (порог $\ge 500$ матчей, лаг < 60 сек, Actuator health HTTP 200 UP) и зафиксировать результаты в OpenSpec.

## Proposed Changes
1. **Восстановление и верификация сервиса 1xBit**:
   - Маппинг `case "1xbit" -> "https://1xbit.com"` в `Betb2bService.java` и домен `1xbit.com` в `app.browser.pre-visit-keywords`.
   - Добавление health-проб (`startupProbe`, `livenessProbe`, `readinessProbe`) для контейнера `igaming-source-1xbit-crawler` в `igaming-k8s/1xbit.yaml`.
   - Прохождение полного набора тестов в модуле `igaming-source-betb2b` (включая `XbetFamilyMapperTest` и `Betb2bLoadIntegrationTest`).
2. **Верификация Golden Rules и Definition of Done**:
   - Actuator health-пробы `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` (HTTP 200 `UP`) для краулера и лоадера.
   - Неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS (`igaming-source-1xbit-db.igaming-source.svc.cluster.local`) без IP-адресов.
   - Критерий наполнения линии: $\ge 500$ матчей в `match_cache` (фактически 1,571 матч, лаг < 1 сек).
   - Ликвидация лага в агрегаторе: `odds_actual` свыше 80 000 котировок, лаг котировок 0.8 сек (< 60 сек), `bet_source` активен с heartbeat < 5 сек.
3. **5-минутный soak-мониторинг и OpenSpec валидация**:
   - Проведение 5-минутного soak-теста без ошибок и рестартов.
   - Валидация спецификаций через `python3 scripts/validate_openspec_specs.py`.
