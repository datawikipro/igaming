# Proposal: [HIGH LAG] Критическое отставание линии 1xBit (639.0 мин)

## Context
Plane Task ID: `9a8ed835-ebd6-4ce6-97c9-0b2e4fd04fab`

## Problem Statement
В системе мониторинга зафиксирован критический инцидент с отставанием линии международного крипто-букмекера 1xBit (`1xbit`) с расчетным лагом 639.0 мин.
Букмекер 1xBit относится к семейству BetB2B/1xBet (`igaming-source-betb2b`) и обслуживается микросервисами `igaming-source-1xbit-crawler`, `igaming-source-1xbit-loader` и выделенной базой данных `igaming-source-1xbit-db-0` в Kubernetes namespace `igaming-source`.

Ранее конфигурация сервиса содержала дефекты:
1. В `Betb2bService.java` отсутствовал маппинг базового URL для букмекера `1xbit` (падал в дефолтный `https://1xbet.com`).
2. В `application.properties` отсутствовал домен `1xbit.com` в списке pre-visit ключевых слов стелс-браузера.
3. Отсутствовало подтверждение покрытия тестами в `XbetFamilyMapperTest`.

Необходимо устранить причину отставания линии, обеспечить сбор линии и котировок, провести 5-минутный soak-мониторинг, подтвердить соответствие всем Golden Rules из AGENTS.md (порог $\ge 500$ матчей, лаг < 60 сек, Actuator health HTTP 200 UP) и зафиксировать результаты в OpenSpec.

## Proposed Changes
1. **Восстановление и верификация сервиса 1xBit**:
   - Маппинг `case "1xbit" -> "https://1xbit.com"` в `Betb2bService.java`.
   - Добавление `1xbit.com` в `app.browser.pre-visit-keywords` в `igaming-source-betb2b`.
   - Покрытие тестом поддержки `1xbit` в `XbetFamilyMapperTest.java`.
   - Проверка подов `igaming-source-1xbit-crawler`, `igaming-source-1xbit-loader` и базы `igaming-source-1xbit-db-0` в namespace `igaming-source`.
2. **Верификация Golden Rules и Definition of Done**:
   - Actuator health-пробы `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` (HTTP 200 `UP`).
   - Неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS (`igaming-source-1xbit-db.igaming-source.svc.cluster.local`) без IP-адресов.
   - Критерий наполнения линии: $\ge 500$ матчей в `match_cache` (фактически 1,442 матча).
   - Ликвидация лага в агрегаторе: `odds_actual` > 54 000 котировок, лаг котировок ~1.9 сек (< 60 сек).
   - 5-минутный soak-тест без рестартов (0 CrashLoopBackOff, 0 NPE, 0 OOMKilled).
3. **OpenSpec валидация**:
   - Создание артефактов `.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`.
   - Успешная валидация через `scripts/validate_openspec_specs.py`.
