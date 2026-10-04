# Architecture Design: [SUPER-ARB] Аномальная вилка 30.4% с участием Fonbet (RU)

## 1. Problem Analysis & Root Cause

### 1.1 Anomaly Description
В процессе кросс-букмекерского сопоставления котировок ядром арбитража (`igaming-aggregator-surebet`) была зафиксирована аномальная арбитражная ситуация (супер-вилка с расчетной доходностью **30.4%**) с участием букмекера **Fonbet (RU)** (`fon-bet-ru`).

В нормальных рыночных условиях доходность арбитражных ситуаций в прематче редко превышает 3–7%, а в лайве — 8–15%. Доходность уровня 30.4% практически всегда является следствием одной из двух причин:
1. **Инверсия исходов (Inverted Outcomes / Swapped Over-Under)**: некорректная классификация ID факторов букмекера в маппере (например, исход Over интерпретируется как Under или наоборот), в результате чего высокий коэффициент на маловероятный исход ошибочно сопоставляется с противоположным исходом другого букмекера.
2. **Коллизия вторичных линий и дубликатов факторов (Duplicate / Secondary Lines Collision)**: Fonbet транслирует как основные маркеты тоталов (факторы 930/931), так и расширенные/дополнительные маркеты (1727/1728, 1730/1731, 1736/1737, 1739/1791), часть из которых при определенных типах спорта или событий инвертирована или содержит параметры сдвига.

### 1.2 Telemetry & Validator Evidence
В логах контейнера `igaming-source-fon-bet-ru-loader` валидатор целостности линий `OddsIntegrityValidator` фиксирует:
```
[ODDS ANOMALY] Inverted total progression in fon-bet-ru on match 'Динамо-Нева (ж) vs Бирюса (ж)' (event 68655712): Over 3.5 @ 1.35 (factor=1730) is lower than Over 1.5 @ 2.55 (factor=930)
```
Это прямо указывает на инверсию прогрессии: Over 3.5 с коэффициентом 1.35 ниже, чем Over 1.5 с коэффициентом 2.55, что математически невозможно при корректном маппинге исходов. 

## 2. Component Architecture & Class Structure

### 2.1 Module Structure (`igaming-source-fon-bet-ru`)
```
pro.datawiki.igaming.source.core.engine.fonbet
├── dto
│   ├── FonbetCustomFactor.java         # Сырой фактор (f: ID фактора, v: котировка, p: параметр)
│   ├── FonbetEvent.java                # Событие линии
│   └── FonbetPayloadDto.java           # DTO сериализации для кэша и шины
├── mapper
│   ├── FonbetCommonTotalMapper.java    # RuleBasedBetTypeMapper для тоталов (OVER_IDS, UNDER_IDS, TEAM1/2)
│   ├── FonbetCommonHandicapMapper.java # RuleBasedBetTypeMapper для фор
│   ├── FonbetCommonResultMapper.java   # RuleBasedBetTypeMapper для 1X2 и двойного шанса
│   └── FonbetCommonSpecialMapper.java  # RuleBasedBetTypeMapper для BTTS, HT/FT, чет/нечет
└── service
    ├── FonbetMatchMapper.java          # Сборка FonbetPayloadDto из сырых данных API
    ├── FonbetOddsService.java          # Таблица наименований факторов FACTOR_NAMES
    ├── AbstractFonbetFamilyApiClient.java # REST/HTTP клиент с пулом сессий
    └── FonbetAggregatorService.java    # Разрешение BetType через betTypeResolver, фильтрация дубликатов, аудит OddsIntegrityValidator
```

### 2.2 Integration with Aggregator & Core Validation
- `OddsIntegrityValidator` (`igaming-source-core`): выполняет проверку отрицательной маржи и монотонности прогрессий тоталов перед отправкой в Kafka/Aggregator API.
- `OddsAnomalyService` & `SurebetValidator` (`igaming-aggregator-surebet`): осуществляет валидацию `InvertedOutcomeRule` и регистрирует структурные дефекты котировок согласно спецификации `aggregator-core`.

## 3. Action Plan & Architectural Decisions

### 3.1 Class & Mapping Adjustments
1. **Аудит наборов факторов в `FonbetCommonTotalMapper`**:
   - Верифицировать четные/нечетные идентификаторы дополнительных тоталов Fonbet (1727..1739, 1791..1797).
   - Проверить соответствие factor=1730 и factor=1731 (Over vs Under) для исключения инверсии.
   - Обеспечить фильтрацию вторичных факторов-дубликатов через `KNOWN_DUPLICATE_FACTOR_IDS` в `FonbetAggregatorService`.
2. **Расширение тестов в `FonbetMapperTest`**:
   - Добавить явные тестовые кейсы на проверку монотонности прогрессий для факторов 1727..1739.
   - Проверить корректность Direction (OVER / UNDER) и BetSubject (MATCH / TEAM1 / TEAM2).
3. **OpenSpec & DoD верификация**:
   - Валидация спецификаций скриптом `validate_openspec_specs.py`.
   - Прогон unit-тестов модуля `igaming-source-fon-bet-ru` через `mvn test`.
