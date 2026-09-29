# Proposal: [ligastavok] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `dec18472-bbc7-474d-8f1a-a49590bbe481`
Bookmaker: Liga Stavok (`igaming-source-ligastavok`)

## Description
Завершение интеграции, верификации и развертывания модуля `igaming-source-ligastavok` с ООП-мапперами исходов, тоталов, фор, киберспорта и статистики:
1. Сборка OCI-образа через Maven Jib (`100.78.183.101:30500/igaming-source-ligastavok:latest`).
2. Проверка тестового пода в K8s с 5-минутным мониторингом логов.
3. Мердж Pull Request #5 в master.
4. Развертывание в продуктивном окружении (namespace `igaming-source`).
5. 5-минутный мониторинг и валидация линии матчей.
