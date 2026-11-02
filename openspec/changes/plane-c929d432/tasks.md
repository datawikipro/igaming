# Implementation Tasks: [source-exchange] Интеграция биржевой ликвидности и объемов ставок (Betfair Exchange / Smart Money)
- [x] 1. Изучить структуру модуля `igaming-source-betfair` и исходные файлы API/парсеры букмекера
- [ ] 2. Реализовать или обновить логику загрузки линий и маппинга исходов в таблицы match_factor и match_cache
- [ ] 3. Проверить неблокирующий HikariCP, таймауты и кластерный прокси (ru-proxy 100.83.113.50:3128)
- [ ] 4. Проверить успешность локальной компиляции модуля: `mvn -f igaming-source-betfair/pom.xml test-compile -DskipTests`
- [ ] 5. Проверить интеграцию и отсутствие регрессий

