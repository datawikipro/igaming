# Implementation Tasks: [draftkings] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

- [ ] 1. ООП-рефакторинг мапперов (Киберспорт, Статистика Угловые/ЖК, роспись исходов) + Unit-тесты
  - [ ] Создание объектно-ориентированной архитектуры мапперов на базе AbstractBetTypeMapper (Result, Total, Handicap, Stats, Esports, Props)
  - [ ] Поддержка статистических маркеров (CORNERS, YELLOW_CARDS, CARDS, OFFSIDES, FOULS, SHOTS_ON_TARGET)
  - [ ] Поддержка киберспорта (MAPS, ROUNDS, KILLS, FIRST_BLOOD, TOWERS, ROSHAN)
  - [ ] Поддержка расширенной росписи (BTTS, Double Chance, Draw No Bet, Team Totals, Scopes: Halves/Periods/Quarters/Innings/Sets/Maps)
  - [ ] Покрытие модульными тестами в DraftKingsOddsMapperTest
  - [ ] Обновление pom.xml (Actuator, Jib OCI) и application.properties (неблокирующий HikariCP)
- [ ] 2. Jib сборка (igaming-source-draftkings) с .m2 кешем
- [ ] 3. Развертывание тестового пода в K8s (igaming-dev)
- [ ] 4. 5-минутный soak-тест тестового пода и анализ логов
- [ ] 5. Мердж PR в master
- [ ] 6. Деплой в прод (production rollout в namespace igaming-source)
- [ ] 7. 5-минутный мониторинг прода и верификация наполнения линии
- [ ] 8. Итоговый рапорт
