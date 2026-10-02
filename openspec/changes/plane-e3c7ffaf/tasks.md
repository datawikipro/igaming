# Implementation Tasks: #46: [aggregator] Метрики и дашборд доходности вилок после расширения росписи маркетов
- [x] 1. Изучить кодовую базу, спроектировать метрики и подготовить OpenSpec артефакты
  - [x] 1.1 Анализ жизненного цикла вилок в SurebetRuleEvaluator, SurebetAlertManager и SurebetDetectorService
  - [x] 1.2 Определение структуры метрик Prometheus (surebet_generated_total, surebet_active_gauge, surebet_yield_distribution, surebet_cross_bookmaker_matrix)
  - [x] 1.3 Подготовка OpenSpec артефактов (proposal.md, design.md, delta spec.md, tasks.md) и валидация через validate_openspec_specs.py
- [x] 2. Реализовать сбор метрик Prometheus и внутренний REST дашборд доходности
  - [x] 2.1 Подключение spring-boot-starter-web и micrometer-registry-prometheus в pom.xml, включение веб-слоя и Prometheus эндпоинта
  - [x] 2.2 Разработка SurebetMetricsService с поддержкой всех целевых метрик (счетчики, gauge, гистограмма доходности, матрица пар букмекеров)
  - [x] 2.3 Интеграция SurebetMetricsService в SurebetRuleEvaluator, SurebetAlertManager и SurebetDetectorService
  - [x] 2.4 Разработка SurebetDashboardController и DTO для внутреннего дашборда доходности вилок (/api/v1/surebets/dashboard)
  - [x] 2.5 Разработка комплексных модульных тестов SurebetMetricsServiceTest и SurebetDashboardControllerTest
- [x] 3. Сборка OCI образа, деплой в K8s, верификация DoD и 5-минутный тест стабильности
  - [x] 3.1 Сборка Maven Jib и пуш OCI-образа в локальный registry
  - [x] 3.2 Обновление K8s манифеста Deployment/Service для igaming-aggregator-surebet с пробросом портов и проб
  - [x] 3.3 Верификация /actuator/prometheus, /actuator/health/readiness и дашборда в K8s
  - [x] 3.4 5-минутный тест стабильности (schedule DoD soak test)
  - [x] 3.5 Валидация OpenSpec и оформление коммитов

