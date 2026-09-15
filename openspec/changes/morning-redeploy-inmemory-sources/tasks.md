# Tasks: Morning Redeployment & Launch Checklist

## 1. Physical Server Boot & Pre-Flight Check (После 10:00)
- [x] 1.1 Включить физический сервер `xeon-srv` (после 10:00 МСК во избежание шума кулеров).
- [x] 1.2 Проверить доступность по Tailscale: `ssh root@100.78.183.101 "uptime"`.
- [x] 1.3 Проверить статус узла Kubernetes: `kubectl get nodes` (статус `Ready`).

## 2. Dev-Сборка на Xeon (Rule 5)
- [x] 2.1 Синхронизировать кодовую базу с ноутбука на `xeon-srv` (ветка master / rsync).
- [x] 2.2 Запустить параллельную компиляцию и сборку OCI-образов Jib на Xeon:
  ```bash
  mvn -T 1.5C compile jib:build -DskipTests
  ```
- [x] 2.3 Убедиться, что сборка завершилась статусом `BUILD SUCCESS` и образы опубликованы в GHCR.

## 3. Развертывание In-Memory источников (`igaming-source`)
- [x] 3.1 Запустить утренний скрипт развертывания:
  ```powershell
  .\redeploy-inmemory-sources.ps1
  ```
- [x] 3.2 Убедиться в удалении устаревших PVC баз источников: `kubectl get pvc -n igaming-source` (должно быть пусто).
- [x] 3.3 Убедиться, что поды PostgreSQL поднялись в статусе `Running 1/1` на RAM (`tmpfs`).
- [x] 3.4 Проверить лог любого пода краулера/лоадера на успешную отработку авто-миграции:
  ```
  ✅ [AutoMigrate] Database schema verified and up-to-date!
  ```

## 4. Верификация наполнения линий (DoD Criteria >= 500 матчей)
- [x] 4.1 Проверить наполнение базы **Olimpbet**: `SELECT count(*) FROM match_cache >= 500` (Факт: 1,904).
- [x] 4.2 Проверить наполнение базы **Baltbet**: `SELECT count(*) FROM match_cache >= 500` (Факт: 2,934).
- [x] 4.3 Проверить наполнение базы **1xBet** (`igaming_xbet`): `SELECT count(*) FROM match_cache >= 500` (Факт: 1,127).
- [x] 4.4 Запустить аудит по всем активным БК и зафиксировать статистику (Fonbet: 3,555).

## 5. Мониторинг стабильности хоста и агрегатора (5-минутный DoD)
- [x] 5.1 Проверить Load Average и I/O wait на сервере: `wa: 0%` (Zero disk wait!).
- [x] 5.2 Проверить количество процессов Chrome: изоляция пространств PID (`shareProcessNamespace: true`).
- [x] 5.3 Проверить поток активных вилок в агрегаторе:
  ```powershell
  Invoke-RestMethod -Uri "http://100.78.183.101:3035/api/surebets/active"
  ```
- [x] 5.4 Выполнить таймер Definition of Done (5 минут стабильной работы без ошибок).
