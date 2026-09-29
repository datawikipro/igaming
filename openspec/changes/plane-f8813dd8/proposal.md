# Proposal: [stoiximan] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `f8813dd8-fcf5-4424-bd34-f6b96e4922ca`
Bookmaker: Stoiximan (`igaming-source-stoiximan`)

## Description
Завершение интеграции, верификации и развертывания модуля `igaming-source-stoiximan` с ООП-мапперами исходов, тоталов, фор, киберспорта и статистики:
1. Сборка OCI-образа через Maven Jib (`100.78.183.101:30500/igaming-source-stoiximan:latest`).
2. Проверка тестового пода в K8s с 5-минутным мониторингом логов.
3. Мердж Pull Request #13 в master.
4. Развертывание в продуктивном окружении (namespace `igaming-source`).
5. Устранение OOMKilled-инцидента: оптимизация параметров памяти (`1024Mi`) и тайм-аутов Actuator проб (`10s`) в K8s манифесте `igaming-k8s/stoiximan.yaml`.
6. 5-минутный мониторинг и валидация линии матчей (достигнуто >1000 активных матчей).
