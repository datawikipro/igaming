# 📋 [IGAMING-CORE-001] Внедрение EmergencyIncidentTracker, Actuator & LivenessState.BROKEN

## 📌 Метаданные задачи (Plane)
- **ID**: `IGAMING-CORE-001`
- **Эпик**: `[EPIC-CORE]`
- **Модуль**: `igaming-source-core` (также `igaming-dto`)
- **Приоритет**: `Urgent`
- **Теги**: `core`, `fail-fast`, `actuator`, `k8s-probes`, `robot-task`
- **Статус**: `In Progress`

---

## 🎯 Цель задачи
Обеспечить единый стандарт мониторинга сбоев для всех 27 краулеров/лоадеров и общих библиотек. При возникновении 5 подряд сбоев подсистемы (БД, Kafka, Прокси, Парсер) переводить Spring Actuator Liveness в состояние `BROKEN` и выводить заметный дамп аварии `🔥🔥🔥 [FATAL INCIDENT] 🔥🔥🔥`.

---

## 🛠️ Затронутые файлы
- [EmergencySubsystem.java](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-source-core/src/main/java/pro/datawiki/igaming/source/core/health/EmergencySubsystem.java)
- [EmergencyIncidentTracker.java](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-source-core/src/main/java/pro/datawiki/igaming/source/core/health/EmergencyIncidentTracker.java)
- [FatalIncidentBanner.java](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-source-core/src/main/java/pro/datawiki/igaming/source/core/health/FatalIncidentBanner.java)
- [BookmakerLivenessHealthIndicator.java](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-source-core/src/main/java/pro/datawiki/igaming/source/core/health/BookmakerLivenessHealthIndicator.java)
- [AbstractApiErrorTracker.java](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-source-core/src/main/java/pro/datawiki/igaming/source/core/service/AbstractApiErrorTracker.java)

---

## 🤖 Пошаговая инструкция для робота
1. Убедиться, что `spring-boot-starter-actuator` присутствует в `pom.xml`.
2. Проверить интеграцию `EmergencyIncidentTracker` в базовых классах `AbstractApiErrorTracker` и `AbstractBaseBookmakerService`.
3. Убедиться, что при 100% сбоях API вызывается `recordFailure(EmergencySubsystem.PROXY_NETWORK, reason, cause)`.
4. Запустить проверку сборки:
   ```bash
   mvn -pl igaming-source-core test-compile
   ```

---

## ✅ Критерии приемки
- Модуль компилируется без ошибок.
- Классы лежат в пакете `pro.datawiki.igaming.source.core.health`.
- При вызове `recordFailure` счетчик увеличивается, а при достижении порога публикуется событие `LivenessState.BROKEN`.
