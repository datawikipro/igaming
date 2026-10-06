# Implementation Tasks: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

- [x] 1. Синхронизировать исполняемые скрипты агента публикации в smm-agent/ и манифест igaming-k8s/smm-bot-telegram.yaml
- [ ] 2. Добавить юнит-тесты в smm-agent/tests/test_telegram_poster.py и верифицировать healthcheck эндпоинты
- [ ] 3. Обновить ConfigMap в Kubernetes кластере, перезапустить под, подтвердить статус Running 1/1 и выдержать 5-минутный Soak Test
