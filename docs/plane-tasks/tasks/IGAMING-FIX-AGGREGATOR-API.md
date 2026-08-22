# 📋 [IGAMING-FIX-AGGREGATOR-API] Ремонт и Fail-Fast агрегатора aggregator-api

## 📌 Метаданные задачи (Plane)
- **ID**: `IGAMING-FIX-AGGREGATOR-API`
- **Эпик**: `[EPIC-AGGREGATOR]`
- **Модуль**: `aggregator-api`
- **Приоритет**: `Urgent`
- **Теги**: `repair`, `aggregator-api`, `fail-fast`, `hikari`, `actuator`, `k8s-probes`
- **Статус**: `Backlog`

---

## 🎯 Сymptom & Cause
При запуске под `igaming-aggregator-api` падает с ошибкой `UnknownHostException: igaming-aggregator-db.igaming-master.svc.cluster.local`, блокируя главный поток из-за таймаута инициализации Hikari.

---

## 🛠️ Затронутые файлы
- [application.properties](file:///C:/Users/chernousov_a/IdeaProjects/igaming/aggregator-api/src/main/resources/application.properties)
- [EmergencyIncidentTracker.java](file:///C:/Users/chernousov_a/IdeaProjects/igaming/aggregator-domain/src/main/java/pro/datawiki/igaming/aggregator/domain/health/EmergencyIncidentTracker.java)

---

## 🤖 Пошаговая инструкция
1. Задать неблокирующий старт Hikari: `spring.datasource.hikari.initialization-fail-timeout=0`.
2. Включить Actuator liveness & readiness пробы (`management.health.livenessstate.enabled=true`).
3. Добавить Liveness & Readiness probes в K8s деплоймент.
4. Выполнить компиляцию и верификацию: `mvn -pl aggregator-api test-compile -DskipTests`.
