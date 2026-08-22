# 📋 [IGAMING-FIX-AGGREGATOR-SUREBET] Ремонт и Fail-Fast детектора вилок aggregator-surebet

## 📌 Метаданные задачи (Plane)
- **ID**: `IGAMING-FIX-AGGREGATOR-SUREBET`
- **Эпик**: `[EPIC-AGGREGATOR]`
- **Модуль**: `aggregator-surebet`
- **Приоритет**: `Urgent`
- **Теги**: `repair`, `surebet`, `fail-fast`, `hikari`, `actuator`, `k8s-probes`
- **Статус**: `Backlog`

---

## 🎯 Symptom & Cause
Под `igaming-aggregator-surebet` находится в `CrashLoopBackOff`. Причина: блокировка потока при попытке подключения Hikari к БД.

---

## 🛠️ Затронутые файлы
- [application.properties](file:///C:/Users/chernousov_a/IdeaProjects/igaming/aggregator-surebet/src/main/resources/application.properties)

---

## 🤖 Пошаговая инструкция
1. Добавить `spring.datasource.hikari.initialization-fail-timeout=0`.
2. Добавить `management.health.livenessstate.enabled=true` и `readinessstate.enabled=true`.
3. Скомпилировать и проверить: `mvn -pl aggregator-surebet test-compile -DskipTests`.
