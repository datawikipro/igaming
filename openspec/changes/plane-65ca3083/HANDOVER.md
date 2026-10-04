# Handover State: #65ca3083
- **Migrated From**: plane-ai-worker-13 (aleksei.a.chernousov@gmail.com)
- **Timestamp**: 2026-10-04T15:29:22.630555
- **Target Branch**: feature/plane-65ca3083
- **Reason**: Gemini Quota Depletion on worker pod plane-ai-worker-13
- **Remaining Tasks**:
# Implementation Tasks: #23: [betway] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов
- [x] 1. Изучить текущий модуль и подготовить структуру классов
- [x] 2. Реализовать основную бизнес-логику и маппинги данных
  - [x] 2.1 Маппинг киберспортивных дисциплин (CS2, Dota 2, LoL, Valorant) и роспись по картам/раундам
    - [x] 2.1.1 Поддержка скоупов карт (Map 1..7) и игр (Game 1..7) в AbstractBetwayMarketHandler
    - [x] 2.1.2 Определение киберспортивных дисциплин через sport/league name в BetwayOddsMapper
    - [x] 2.1.3 Обработка тоталов и фор убийств (StatType.KILLS), раундов (StatType.ROUNDS), карт (StatType.MAPS) и First Blood в EsportsMarketHandler
    - [x] 2.1.4 Модульные тесты для CS2, Dota 2, LoL, Valorant в BetwayOddsMapperTest
  - [x] 2.2 Маппинг статистики (угловые удары и желтые карточки)
    - [x] 2.2.1 Реализация угловых ударов (CornersMarketHandler): тоталы (матч/команды), 1X2, форы, Двойной шанс, DNB, Чет/Нечет, First/Last Corner, 1-й/2-й таймы
    - [x] 2.2.2 Реализация карточек (CardsMarketHandler): тоталы (матч/команды), 1X2, форы, Двойной шанс, DNB, Чет/Нечет, Red Card (Yes/No), First/Last Card, 1-й/2-й таймы
    - [x] 2.2.3 Изоляция статистических маркетов в общих обработчиках (DoubleChance, DrawNoBet, BTTS)
    - [x] 2.2.4 Комплексные модульные тесты для угловых и карточек в BetwayOddsMapperTest
  - [x] 2.3 Маппинг расширенной росписи (DNB, BTTS, точный счет, тайм/матч, двойной шанс)
    - [x] 2.3.1 Draw No Bet (DNB): поддержка основного времени и таймов (1st/2nd Half) с маппингом в HandicapBet(0.0)
    - [x] 2.3.2 Both Teams To Score (BTTS): поддержка общего матча, таймов и Both Halves BTTS (BinaryMarketBet)
    - [x] 2.3.3 Точный счет (Correct Score): поддержка матча и таймов с обработкой "Any Other Score" (CorrectScoreBet)
    - [x] 2.3.4 Тайм/Матч (HT/FT): поддержка всех 9 комбинаций исходов и названий команд (HalfTimeFullTimeBet)
    - [x] 2.3.5 Двойной шанс (Double Chance): поддержка 1X, 12, X2 для матча и таймов с форматированием групп (MatchResultBet)
    - [x] 2.3.6 Модульные тесты расширенной росписи в BetwayOddsMapperTest
- [x] 3. Валидировать OpenSpec и проверить сборку/тесты
  - [x] 3.1 Запуск полного набора unit-тестов BetwayOddsMapperTest
  - [x] 3.2 Проверка компиляции и сборки JAR пакета модуля
- [ ] 4. Jib-сборка образа контейнера и подготовка манифестов K8s
  - [ ] 4.1 Проверка готовности Jib-конфигурации в pom.xml модуля igaming-source-betway
  - [ ] 4.2 Аудит K8s манифеста igaming-k8s/betway.yaml на соответствие Golden Rules (DNS-имена, Actuator health-пробы, nodeAffinity)
  - [ ] 4.3 Сборка Jib-образа ghcr.io/datawikipro/igaming-source-betway:latest
  - [ ] 4.4 Деплой и верификация в K8s dev (5-минутный таймер soak-тестирования, проверка логов на отсутствие Exception)
  - [ ] 4.5 Контроль порога наполнения линии (Threshold >= 500 матчей)



## Instructions for incoming worker:
1. Pull branch `feature/plane-65ca3083`.
2. Read `/workspace/repo/openspec/changes/plane-65ca3083/tasks.md`.
3. Continue from the next unchecked item (`- [ ]`).
4. Validate changes and submit final PR.
