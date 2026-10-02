# Proposal: [player-faces-tennis] [Теннис/Единоборства] Сбор портретов спортсменов (Player Face Avatars) и отображение в карточках одиночных матчей

## Context
Plane Task ID: `d3f4622f-8e23-48fd-a333-fc17f24e9e7d`

## Description

В одиночных видах спорта (теннис, MMA, бокс, бадминтон, сквош и пр.) участники матча — это **индивидуальные игроки**, а не команды. API SofaScore возвращает для таких событий `homeTeam`/`awayTeam` — узлы, которые фактически описывают отдельного игрока (ID, имя, страна). Для игрока существует отдельный endpoint с портретным фото:

```
https://api.sofascore.app/api/v1/player/{playerId}/image
```

**Что реализовано:**

### 1. `IndividualSportDetector` (util)
Утилитарный класс, содержащий список «индивидуальных» видов спорта (Tennis, MMA, Boxing, Badminton, Squash, Padel, Snooker, Darts, Golf, Billiards, Sumo, Table Tennis) и фабричные методы:
- `isIndividualSport(sportName)` — определяет, является ли спорт одиночным
- `buildPlayerAvatarUrl(playerId)` — строит URL портрета игрока
- `buildTeamLogoUrl(teamId)` — строит URL логотипа команды (для командных спортов)

### 2. `SofaScoreFixtureProvider` (расширен)
При формировании `ReferenceFixtureDto` для одиночных видов спорта:
- `team1LogoUrl` / `team2LogoUrl` теперь содержат **URL портрета игрока** (`/player/{id}/image`) вместо логотипа команды
- В поле `metadata` добавляются дополнительные ключи:
  - `match_type` = `"INDIVIDUAL"` или `"TEAM"`
  - `player1_face_url`, `player2_face_url` — URL портретных фото
  - `player1_sofa_id`, `player2_sofa_id` — внутренние ID SofaScore для last-mile запросов

### 3. `PlayerAvatarFetchService` (новый сервис)
Spring `@Service` для получения расширенного профиля игрока из SofaScore `/api/v1/player/{playerId}`:
- Возвращает `PlayerFaceProfile` (record): id, fullName, shortName, country, countryCode, faceAvatarUrl, sport
- Кеширует результаты в `ConcurrentHashMap` (in-memory, TTL не ограничен, удерживается на весь жизненный цикл JVM)
- Используется планировщиком прогрева и может быть вызван любым downstream компонентом

### 4. `PlayerAvatarWarmupScheduler` (новый планировщик)
`@Scheduled` компонент, запускающийся через 80 с после старта и каждые 4 часа:
- Получает фикстуры на 3 дня вперёд для индивидуальных видов спорта
- Проходит по каждому матчу и вызывает `PlayerAvatarFetchService.fetchPlayerFace()` для обоих игроков
- Подогревает кеш заблаговременно, чтобы запросы frontend/portal были мгновенными

## Технические решения
- **Без изменений в `igaming-dto`**: поля `team1LogoUrl` / `team2LogoUrl` уже существуют в `ReferenceFixtureDto` и подходят для хранения URL портрета; расширенные данные передаются через поле `metadata: Map<String, Object>`
- **Без Redis**: нагрузка (сотни игроков) укладывается в in-memory кеш 512 Mi пода; при необходимости кеш можно вынести в Redis без изменения интерфейсов
- **SofaScore-only**: ESPN не поддерживает теннис/MMA, поэтому `EspnFixtureProvider` изменений не требует
