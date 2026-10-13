# Implementation Tasks: #2355: [STALE] Букмекер Sbobet перестал присылать данные (лаг 924.1 мин)
- [ ] 1. Изучить структуру модуля `igaming-source-sbobet` и исходные файлы API/парсеры букмекера
- [ ] 2. Реализовать или обновить логику загрузки линий и маппинга исходов в таблицы match_factor и match_cache
- [ ] 3. Проверить неблокирующий HikariCP, таймауты и кластерный прокси (cluster-switch-proxy.proxy.svc.cluster.local:3128)
- [ ] 4. Проверить успешность локальной компиляции модуля: `mvn -f igaming-source-sbobet/pom.xml test-compile -DskipTests`
- [ ] 5. Валидировать OpenSpec дельта-спецификацию: `openspec validate --specs`
