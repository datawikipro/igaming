# Proposal: #10: [fon-bet-ru] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `9b798427-c85a-4d3f-ae65-4d639779cb8e`
Bookmaker: Fonbet RU (`igaming-source-fon-bet-ru`)

## Description
Комплексный ООП-рефакторинг маппинга котировок модуля `igaming-source-fon-bet-ru`:
1. Внедрение специализированных ООП-мапперов `FonbetEsportsMapper` (киберспорт: CS2, Dota 2, LoL, Valorant и др. с поддержкой карт, раундов, убийств, спецсобытий) и `FonbetStatsMapper` (футбольная и хоккейная статистика: угловые, желтые карточки, фолы, офсайды, удары в створ).
2. Четкое разграничение ответственности и предотвращение коллизий в `BetTypeResolverService` между общими рынками (`FonbetCommon*Mapper`), киберспортом и статистикой.
3. Локальная верификация unit-тестами `FonbetMapperTest` и сборка OCI-образа через Maven Jib (`100.78.183.101:30500/igaming-source-fon-bet-ru:latest`).
4. Развертывание и 5-минутный soak-тест тестового пода в K8s (namespace `igaming-dev`).
5. Слияние изменений ветки `feature/plane-9b798427` в `master`.
6. Деплой в продуктивный контур (namespace `igaming-source`) и 5-минутный мониторинг наполнения линии (порог >= 500 матчей).
