# Implementation Tasks: #82: [marathonbet-by] Подключить Playwright volume mount и восстановить сбор котировок
- [x] 1. Подключить Playwright volume mount, переменные окружения и Actuator-пробы в K8s манифесте (igaming-k8s/marathonbet.by.yaml)
- [ ] 2. Применить обновленный манифест в K8s и запустить 5-минутный soak-тест с верификацией сбора котировок
- [ ] 3. Проверить наполнение линии в БД (критерий match_cache >= 500) и валидировать OpenSpec спецификации
