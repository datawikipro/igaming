# Implementation Tasks: [crawlers-playwright] Встроить установку Chromium Playwright в Docker-образы браузерных краулеров
- [x] 1. Изучить структуру модуля `igaming-source-winline` и исходные файлы API/парсеры букмекера
- [x] 2. Реализовать или обновить логику загрузки линий и маппинга исходов в таблицы match_factor и match_cache
- [x] 3. Проверить неблокирующий HikariCP, таймауты и кластерный прокси (ru-proxy 100.83.113.50:3128)
- [x] 4. Проверить успешность локальной компиляции модуля: `mvn -f igaming-source-winline/pom.xml test-compile -DskipTests`
- [x] 5. Проверить интеграцию и отсутствие регрессий
