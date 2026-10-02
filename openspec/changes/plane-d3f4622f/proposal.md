# Proposal: #81: [player-faces-tennis] [Теннис/Единоборства] Сбор портретов спортсменов (Player Face Avatars) и отображение в карточках одиночных матчей

## Context
Plane Task ID: `d3f4622f-8e23-48fd-a333-fc17f24e9e7d`

## Description

В SofaScore API для одиночных видов спорта (теннис, MMA, бокс, настольный теннис, бадминтон, сквош, дартс)
поля `homeTeam`/`awayTeam` фактически представляют **отдельных игроков**, а не команды.
Каждый игрок имеет числовой `id`, который одновременно используется как `team_id` и `player_id`.

SofaScore предоставляет портретные фото через отдельный endpoint:
`https://api.sofascore.app/api/v1/player/{playerId}/image`

### Что реализовано

1. **`ReferenceFixtureDto`** расширен двумя новыми полями:
   - `team1PlayerFaceUrl` — URL портрета игрока 1 (участника 1)
   - `team2PlayerFaceUrl` — URL портрета игрока 2 (участника 2)
   - Для командных видов спорта поля остаются `null`
   - Локальный файл DTO в модуле `igaming-capture-sofascore` переопределяет одноимённый класс из артефакта `igaming-dto`

2. **`SofaScoreFixtureProvider`** обновлён:
   - Добавлена константа `SOFASCORE_PLAYER_URL`
   - Добавлен приватный метод `isSinglePlayerSport(String sportName)` — детектирует одиночные виды: TENNIS, MMA, BOXING, TABLE_TENNIS, BADMINTON, SQUASH, DARTS
   - При сборке `ReferenceFixtureDto` для одиночных видов дополнительно формируются `team1PlayerFaceUrl` и `team2PlayerFaceUrl` по схеме `https://api.sofascore.app/api/v1/player/{id}/image`, где `{id}` берётся из `homeTeam.id` / `awayTeam.id`
   - Добавлен debug-лог с именами игроков

### Технические решения

- Логика определения одиночного вида спорта (`isSinglePlayerSport`) инкапсулирована в `SofaScoreFixtureProvider`, так как только этот провайдер поддерживает данный подход (в ESPN API другая модель)
- Интерфейс `MatchFixtureProvider` не изменён — расширение сделано только в SofaScore-реализации
- Изменения в `pom.xml` не требуются — пакет `pro.datawiki.igaming.dto` уже присутствует в classpath модуля

### Целевые виды спорта

| Вид спорта    | SofaScore slug | Поддержка |
|---------------|----------------|-----------|
| Tennis        | tennis         | ✅        |
| MMA           | mma            | ✅        |
| Boxing        | boxing         | ✅        |
| Table Tennis  | table-tennis   | ✅        |
| Badminton     | badminton      | ✅        |
| Squash        | squash         | ✅        |
| Darts         | darts          | ✅        |
