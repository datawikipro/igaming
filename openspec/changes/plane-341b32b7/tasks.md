# Implementation Tasks: #2392: [HIGH LAG] Критическое отставание линии Марафон (33.2 мин)
- [ ] 1. Изучить структуру модуля `igaming-source-marathonbet` и исходные файлы API/парсеры букмекера
- [ ] 2. Реализовать или обновить логику загрузки линий и маппинга исходов в таблицы match_factor и match_cache
- [ ] 3. Проверить неблокирующий HikariCP, таймауты и кластерный прокси (cluster-switch-proxy.proxy.svc.cluster.local:3128)
- [ ] 4. Проверить успешность локальной компиляции модуля: `mvn -f igaming-source-marathonbet/pom.xml test-compile -DskipTests`
- [ ] 5. Валидировать OpenSpec дельта-спецификацию: `openspec validate --specs`
