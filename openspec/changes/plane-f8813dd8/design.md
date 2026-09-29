# Design: [stoiximan] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration
Модуль `igaming-source-stoiximan` построен на Spring Boot 3.4 и базовых компонентах `igaming-source-core`.
Архитектура сервиса:
- Интеграция с Kambi API через `StoiximanApiClient` (listView и betOffer эндпоинты через прокси `proxy-us.service-proxy.svc.cluster.local:31292`).
- Периодический опрос линий 16 видов спорта (`StoiximanDiscoveryService` с защитой от избыточных обновлений и оптимистических блокировок).
- ООП-маппинг исходов, тоталов, фор, киберспорта и статистических рынков через `StoiximanOddsMapper`.
- Отправка Heartbeat-сигналов в `igaming-aggregator` каждые 60 секунд.

## Verification & Deployment Strategy
- Сборка OCI-образа через Maven Jib: `100.78.183.101:30500/igaming-source-stoiximan:latest`.
- Прогон тестового пода в K8s с неблокирующим HikariCP и Actuator health probes.
- 5-минутный мониторинг тестового пода (отсутствие сбоев и ошибок).
- Слияние Pull Request #13 в master.
- Развертывание в продуктивном окружении (namespace `igaming-source`).
- Тюнинг ресурсов памяти: лимит поднят с `512Mi` до `1024Mi`, тайм-ауты проб увеличены до 10s.
- 5-минутный аудит продуктивных подов и проверка наполнения линии матчей (зафиксировано 1047 активных матчей).
