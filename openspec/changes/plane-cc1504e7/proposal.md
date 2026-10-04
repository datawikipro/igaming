# Proposal: [SUPER-ARB] Аномальная вилка 21.8% с участием FanSport

## Context
Plane Task ID: `cc1504e7-941e-4c8f-8174-5ac18c9659d9`

## Description
Анализ и расследование причин возникновения алерта об аномальной вилке 21.8% с участием букмекера FanSport (`igaming-source-fansport`).

### Результаты анализа:
1. **Маппинг рынков (BetB2B Family Mapper)**:
   - FanSport обслуживается модулем `igaming-source-betb2b` с компонентной архитектурой маппинга (`XbetFamilyMapper`).
   - Стратегии маппинга (`XbetMainResultStrategy`, `XbetHandicapStrategy`, `XbetTotalStrategy`, `XbetHalvesStrategy`, `XbetHalfTimeFullTimeStrategy`, `XbetTeamToScoreStrategy`, `XbetBinaryMarketStrategy`, `XbetStatsStrategy`) строго изолируют исходы:
     - Основные рынки матча (`StatType.MATCH`, `BetScope.FULL_MATCH`) не смешиваются со статистикой (угловые `1707..1712`, желтые карточки `1738..1743` имеют `StatType.CORNERS` и `StatType.YELLOW_CARDS`).
     - Исходы первого/второго тайма (`15..17`, `41..48`, `61..78`) строго типизированы с `BetScope.HALF_1` и `HALF_2`.
     - Все 8 unit-тестов в `XbetFamilyMapperTest` выполняются успешно.

2. **Фильтрация аномалий в Aggregator**:
   - В ядре агрегатора `igaming-aggregator-surebet` правила валидации (`LivePrematchSeparationRule`, `CloneSyndicateRule`, `DoubleChanceDominanceRule`) своевременно перехватывают и бракуют недопустимые комбинации (включая расхождения Live vs Prematch и внутрисиндикатные связки между BetB2B клонами).

3. **Состояние сервисов FanSport в Kubernetes**:
   - `igaming-source-fansport-crawler` и `igaming-source-fansport-loader` работают в namespace `igaming-source` без перезапусков (uptime > 36 часов).
   - Actuator-пробы `/actuator/health/readiness` и `/actuator/health/liveness` отдают `{"status":"UP"}`.
   - База данных `igaming_fansport` содержит **1 304 активных матча** в `match_cache` (требование Threshold >= 500 выполнено с запасом).
   - В `odds_actual` агрегатора зарегистрировано **15 358 актуальных котировок** FanSport.
