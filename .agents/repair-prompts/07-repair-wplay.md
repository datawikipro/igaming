# Задача для AI-агента: Ремонт и стабилизация igaming-source-wplay

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — восстановить работоспособность сервиса **`igaming-source-wplay`** (колумбийский букмекер Wplay) в Kubernetes неймспейсе `igaming-source` и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
Под `igaming-source-wplay` находится в статусе `0/1 Running` (readiness probe fails, частые рестарты).
Сервис построен на базе модуля `igaming-source-bet7k` (`Bet7kApplication`).
Причина сбоя:
1. Затянутая инициализация JPA/HikariCP и сканирования контекста Spring, из-за чего K8s Readiness/Startup проба убивает под до завершения старта.
2. Проверь настройки неблокирующего старта HikariCP и сетевой доступ к API `wplay.co`.
База данных: `igaming-source-wplay-db-0` (БД: `igaming_wplay`).

---

## 📋 Пошаговый план действий

### 1. Проверка логов и probe-таймаутов
1. Проверь логи пода `wplay`:
   ```powershell
   kubectl logs -n igaming-source -l app=igaming-source-wplay --tail=60 --request-timeout=10s
   ```
2. Проверь конфигурацию Readiness и Startup проб:
   ```powershell
   kubectl get deployment igaming-source-wplay -n igaming-source -o yaml --request-timeout=10s
   ```
   Убедись, что:
   - Включен неблокирующий старт HikariCP (Rule 4):
     `SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT: "0"`
     `SPRING_JPA_PROPERTIES_HIBERNATE_TEMP_USE_JDBC_METADATA_DEFAULTS: "false"`
   - `startupProbe.failureThreshold` установлен не менее 60 (давать до 3 минут на старт JVM при нагрузке на хост).

### 2. Проверка базы данных
Убедись, что таблица `match_cache` в `igaming_wplay` имеет правильные типы колонок (`start_time bigint`):
```powershell
kubectl --request-timeout=10s exec -n igaming-source igaming-source-wplay-db-0 -- psql -U postgres -d igaming_wplay -c "\d match_cache"
```

### 3. Сборка и деплой на Xeon (при правках)
Модуль: `igaming-source-bet7k`.
1. Синхронизируй изменения: `scp ... root@100.78.183.101:/root/igaming/...`
2. Собери OCI-образ через Jib на Xeon:
   ```powershell
   ssh -o StrictHostKeyChecking=no root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-bet7k/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-bet7k:latest -Djib.allowInsecureRegistries=true -DskipTests"
   ```
3. Примени образ и перезапусти под:
   ```powershell
   kubectl set image deployment/igaming-source-wplay -n igaming-source igaming-source-wplay=100.78.183.101:30500/igaming-source-bet7k:latest --request-timeout=10s
   kubectl rollout restart deployment/igaming-source-wplay -n igaming-source --request-timeout=10s
   ```

### 4. Верификация Definition of Done (DoD)
1. Под перешел в `Running 1/1` (0 рестартов).
2. Actuator Health возвращает `{"status":"UP"}`.
3. Линия наполняется матчами (критерий >= 500 активных событий):
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-wplay-db-0 -- psql -U postgres -d igaming_wplay -c "SELECT count(*) FROM match_cache;"
   ```
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
