# Audit & Resolution Report: [SUPER-ARB] Аномальная вилка 29.2% с участием BetM

**Task ID**: `c318ae03-5710-4bbb-8e8d-d44a660fbcf2`  
**Дата верификации**: 2026-10-06  
**Статус**: Выполнено / Verified  

---

## 1. Верификация источника данных BetM
- **Kubernetes Pods** (namespace `igaming-source`):
  - `igaming-source-betm-crawler-78db656858-p2shr`: `Running 2/2` (uptime > 3h59m)
  - `igaming-source-betm-loader-7d8d49bb6b-4j7g8`: `Running 2/2` (uptime > 3h58m)
  - `igaming-source-betm-db-0`: `Running 1/1` (uptime > 3d)
- **Критерий наполнения линии** (Threshold $\ge 500$ матчей):
  - База данных `igaming_betm`, таблица `match_cache`: **4 303 активных матча** (критерий полностью выполнен).
- **Сетевая конфигурация**:
  - Используются исключительно K8s DNS Service Names (`igaming-source-betm-db`, без хардкода IP-адресов).
  - Неблокирующий старт HikariCP и корректная персистентность факторов в локальном Redis контейнере.

---

## 2. Анализ аномального арбитража 29.2% и валидация маппинга
- **Политика No Yield Cap и пороги телеметрии**:
  - В соответствии с архитектурной политикой No Yield Cap, реальные рыночные вилки высокой доходности (29.2%) не отсекаются и публикуются со статусом `ACTIVE`.
  - Пороги телеметрии аномалий (`EXTREME_SUREBET`):
    - Live: $> 30.0\%$
    - Prematch: $> 50.0\%$
  - При превышении порога арбитраж регистрируется в сервисе `OddsAnomalyService` (`odds_anomaly`) со статусом `EXTREME_SUREBET` (`Severity.WARNING`) и запускает приоритетный рефреш линии через `OddsRefreshService`.
- **Изоляция клонов платформы Betcity (`CloneSyndicateRule`)**:
  - Букмекер `betm` и его псевдоним `betm-ru` зарегистрированы в правиле `CloneSyndicateRule` как часть синдиката `BETCITY` (наряду с `betcity`, `betcity-ru`, `betcity-com`, `betcity-by`).
  - Ложные связки между клонами внутри одной платформы отклоняются правилом `CloneSyndicateRule` с причиной `ALL_LEGS_SAME_SYNDICATE_CLONE`.
- **Маппинг исходов**:
  - В модуле `igaming-source-betcity` метод `resolveScope` расширен для корректной видимости и верификации скоупов периодов/сетов/карт.
  - Исключены конфликты дублирования коэффициентов при разборе основных и расширенных блоков линий.

---

## 3. Отчет о тестировании

### Модуль `aggregator-surebet`:
- **Команда**: `mvn -f aggregator-surebet/pom.xml test`
- **Результат**: `BUILD SUCCESS`, `Tests run: 27, Failures: 0, Errors: 0, Skipped: 0`
- **Проверенные тест-кейсы**:
  - `shouldAcceptBetmWithIndependentBookmakerAtHighYield`: вилка 29.2% с участием BetM и Pinnacle публикуется в активные сигналы (No Yield Cap).
  - `shouldRecordExtremeSurebetAnomalyTelemetryForBetm`: экстремальная доходность (>50% прематч) инициирует запись `EXTREME_SUREBET` в `OddsAnomalyService`.
  - `shouldRejectBetmAndBetcityCloneSurebet`: отсечение ложного арбитража между BetM и Betcity через `CloneSyndicateRule`.
  - Тесты базовой линии Pinnacle, структурной целостности, тоталов, фор, двойных шансов и коридоров.

### Модуль `igaming-source-betcity`:
- **Команда**: `mvn -f igaming-source-betcity/pom.xml test`
- **Результат**: `BUILD SUCCESS`, `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`
- **Проверенные тест-кейсы**:
  - `BetcityParsingTest`: 9 тестов парсинга событий, исходов и интервальных тоталов.
  - `BetcityMappersTest`: 9 тестов мапперов (футбол, хоккей, теннис, киберспорт, граничные случаи коллизий).

### Валидация спецификаций OpenSpec:
- **Команда**: `python3 scripts/validate_openspec_specs.py`
- **Результат**:
  - 12 канонических спецификаций в `openspec/specs/` проверены и валидны.
  - Спецификация активного изменения `plane-c318ae03` проверена и валидна.
  - Ошибок валидации: 0.
