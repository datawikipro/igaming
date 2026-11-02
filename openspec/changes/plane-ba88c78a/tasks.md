# Implementation Tasks: [capture-injuries] Создать микросервис igaming-capture-injuries для сбора травм и дисквалификаций
- [ ] 1. Изучить структуру модуля `igaming-source-core` и исходные файлы API/парсеры букмекера
- [ ] 2. Реализовать или обновить логику загрузки линий и маппинга исходов в таблицы match_factor и match_cache
- [ ] 3. Проверить неблокирующий HikariCP, таймауты и кластерный прокси (ru-proxy 100.83.113.50:3128)
- [ ] 4. Проверить успешность локальной компиляции модуля: `mvn -f igaming-source-core/pom.xml test-compile -DskipTests`
- [ ] 5. Проверить интеграцию и отсутствие регрессий
