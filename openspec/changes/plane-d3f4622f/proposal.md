# Proposal: #81: [player-faces-tennis] [Теннис/Единоборства] Сбор портретов спортсменов (Player Face Avatars) и отображение в карточках одиночных матчей

## Context
Plane Task ID: `d3f4622f-8e23-48fd-a333-fc17f24e9e7d`

## Description

Для одиночных видов спорта (Теннис, Настольный теннис, MMA, Бокс, Бадминтон) в карточках матчей необходимо отображать портреты спортсменов (face avatars) вместо логотипов команд.

## Архитектурные решения

### Источник данных: SofaScore Player Photo API
SofaScore предоставляет два отдельных endpoint'а для изображений:
- **Команды**: `https://api.sofascore.app/api/v1/team/{teamId}/image`
- **Игроки**: `https://api.sofascore.app/api/v1/player/{playerId}/image`

В одиночных матчах SofaScore в JSON-ответе поле `homeTeam.type` / `awayTeam.type` равно `"player"`, а вложенный объект `homeTeam.player.id` содержит ID игрока.

### Backward-Compatible подход: metadata map
Вместо изменения схемы БД/DTO добавляем данные в существующее поле `metadata: Map<String, Object>` в `ReferenceFixtureDto`:
- `metadata["isIndividualSport"]` = `true` — маркер одиночного матча
- `metadata["team1FaceUrl"]` — URL портрета игрока 1
- `metadata["team2FaceUrl"]` — URL портрета игрока 2
- `metadata["team1PlayerId"]` — ID игрока 1 в SofaScore
- `metadata["team2PlayerId"]` — ID игрока 2 в SofaScore

Для `TeamProfileDto` (profile endpoint):
- `logoUrl` содержит face portrait URL для индивидуальных спортсменов
- `metadata["faceUrl"]` — явный ключ для потребителей
- `metadata["isPlayer"]` = `true` — маркер индивидуального спортсмена

### Определение одиночных видов спорта
Метод `isSinglePlayerSport(sportName)` — детектирует: TENNIS, TABLE_TENNIS, BADMINTON, SQUASH, MMA, BOXING, WRESTLING, DARTS, SNOOKER.
Дополнительно: если `homeTeam.type == "player"` в SofaScore JSON — автоматическое определение.

## Изменённые файлы

### `igaming-capture-sofascore`
- [`SofaScoreFixtureProvider.java`](../../igaming-capture-sofascore/src/main/java/pro/datawiki/igaming/capture/sofascore/provider/SofaScoreFixtureProvider.java)
  - `supportsSport()` — добавлены MMA, BOXING, BADMINTON, WRESTLING, DARTS
  - `fetchScheduledFixtures()` — определяет одиночные матчи, заполняет `metadata` face URL
  - `fetchTeamProfile()` — делегирует в `fetchPlayerProfile()` для индивидуальных спортов
  - `fetchPlayerProfile()` — новый метод: запрашивает `/api/v1/player/{id}` endpoint SofaScore
  - `isSinglePlayerSport()` — новый helper-метод для определения одиночных видов
  - `mapSportToSofa()` — добавлены mappings для TABLE_TENNIS, BADMINTON, MMA, BOXING
- [`FixtureSyncScheduler.java`](../../igaming-capture-sofascore/src/main/java/pro/datawiki/igaming/capture/sofascore/scheduler/FixtureSyncScheduler.java)
  - `SUPPORTED_SPORTS` — добавлены TABLE_TENNIS, BADMINTON, MMA, BOXING

## Потребители данных
- **igaming-aggregator** — получает `ReferenceFixtureDto` с `metadata`, сохраняет в `Team.additionalMetadata` / `Team.logoUrl`
- **igaming-portal API** — возвращает данные матча включая `logoUrl` из Team для фронтенда
- **smartbet.guru** — отображает face аватары в карточках одиночных матчей (Tennis/MMA)

## Статус
- [x] Реализация завершена
- [x] Компиляция: `mvn -B compile -DskipTests` → BUILD SUCCESS
- [ ] Деплой в K8s igaming-dev (требует Jib-сборки)
