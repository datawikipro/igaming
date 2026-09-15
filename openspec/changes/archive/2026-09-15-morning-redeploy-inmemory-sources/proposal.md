# Proposal: Утренний перезапуск и запуск In-Memory источников SmartBet.guru

## Summary
Перевод всех 52 источников (`igaming-source`) на изолированные In-Memory базы данных PostgreSQL (RAM-диск `tmpfs`) с отключением сброса на диск (`fsync=off`), автоматическая миграция схемы БД на уровне Java (`DatabaseMigrationRunner` в `igaming-source-core`) и пошаговый регламент запуска системы утром 15.09.2026 после включения сервера `xeon-srv`.

## Why
1. **Авария 14.09.2026 и падение API-сервера:**
   - Headless Chrome краулеры (`caesars`, `fanduel`, `betmgm`, `betmgm-firefox`) накопили свыше 400 зомби-процессов `[chrome] <defunct>` из-за отсутствия init-процесса (PID 1) в контейнерах.
   - 52 экземпляра PostgreSQL непрерывно сбрасывали временные котировки на физический диск (`fsync`), вызвав шторм дискового ввода-вывода (`Dirty: 365 MB / Writeback`).
   - Совокупно это привело к Load Average >88 и зависанию containerd.
2. **Системный дефект схемы (Schema Drift):**
   - У большинства «нулевых» БК краулеры падали из-за отсутствия колонок `id` в `match_factor` и `bookmaker` в `unmapped_sport`.
   - Ручное исправление Olimpbet и Baltbet подтвердило гипотезу: оба букмекера мгновенно наполнили линию до **1 919** и **2 589** матчей соответственно.
3. **Решение:**
   - Внедрить автоматическую миграцию на уровне Java (`DatabaseMigrationRunner`) в общем модуле `igaming-source-core`.
   - Перевести все базы источников на чистый `tmpfs` (`emptyDir.medium: Memory`) со сбросом I/O в 0.
   - Добавить `shareProcessNamespace: true` для гарантированной утилизации процессов Chrome.

## What Changes
- Сборка обновленных OCI-образов на Linux-сервере `xeon-srv` через Maven Jib (`mvn -T 1.5C compile jib:build -DskipTests`).
- Очистка старых дисков PVC в `igaming-source`.
- Применение обновленных 109 K8s-манифестов (`igaming-k8s/`).
- Развертывание и верификация наполнения линий (критерий DoD: $\ge 500$ матчей на букмекера).
- Верификация генерации вилок в `igaming-dev`.
