# Handover State: #243ca9d1
- **Migrated From**: plane-ai-worker-5 (datawiki.pro@gmail.com)
- **Timestamp**: 2026-10-02T15:21:39.754415
- **Target Branch**: feature/plane-243ca9d1
- **Reason**: Gemini Quota Depletion on worker pod plane-ai-worker-5
- **Remaining Tasks**:
# Implementation Tasks: [team-national-detect] Автоматическое распознавание национальных сборных, связывание с ISO-кодами стран и бекфилл БД
- [ ] 1. Разработать сервис распознавания национальных сборных и словарь ISO-маппинга стран
  - [ ] 1.1 Создать словарь NationalTeamDictionary — паттерны имён (ru/en/local) → ISO 3166-1 alpha-2
  - [ ] 1.2 Реализовать NationalTeamDetectorService с методом detect(teamName) → Optional<CountryMatch>
  - [ ] 1.3 Покрыть главные сборные (футбол, баскетбол, хоккей, теннис) — минимум 50 стран
- [ ] 2. Реализовать бекфилл-компонент и REST API для обогащения БД
  - [ ] 2.1 Реализовать NationalTeamBackfillService — пакетное чтение team записей без is_national_team, применение NationalTeamDetectorService, запись country_code/flag_url/is_national_team/team_type
  - [ ] 2.2 Добавить REST эндпоинт POST /api/enrichment/national-teams/backfill (запуск + dry-run режим)
  - [ ] 2.3 Добавить плановый @Scheduled запуск бекфилла для новых записей (раз в 6 часов)
  - [ ] 2.4 Написать юнит-тесты для NationalTeamDetectorService и NationalTeamBackfillService
- [ ] 3. Валидировать OpenSpec и проверить сборку/тесты
  - [ ] 3.1 Запустить сборку aggregator-enrichment и прогон тестов
  - [ ] 3.2 Обновить delta-спеку aggregator-core с требованием National Team Enrichment
  - [ ] 3.3 Зафиксировать изменения в git


## Instructions for incoming worker:
1. Pull branch `feature/plane-243ca9d1`.
2. Read `/workspace/repo/openspec/changes/plane-243ca9d1/tasks.md`.
3. Continue from the next unchecked item (`- [ ]`).
4. Validate changes and submit final PR.
