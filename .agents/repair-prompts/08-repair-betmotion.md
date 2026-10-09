# Задача для AI-агента: Ремонт и стабилизация igaming-source-betmotion

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — восстановить работоспособность сервиса **`igaming-source-betmotion`** в Kubernetes неймспейсе `igaming-source` и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
Под `igaming-source-betmotion` находится в статусе `0/1 Running` (падают проверки готовности / застрял под).
В манифесте:
1. `image: ghcr.io/datawikipro/igaming-source-bet7k:latest` с `imagePullPolicy: Always`.
   **Нарушение Rule 5**: Запрещено использовать `ghcr.io` (блокировки РКН/ТСПУ и ошибки 403 Forbidden). Образ должен браться из локального реестра `100.78.183.101:30500/igaming-source-bet7k:latest`.
2. База данных: `igaming-source-betmotion-db-0` (БД: `igaming_betmotion`).
3. Платформа: Altenar (`APP_ALTENAR_BRAND: "betmotion"`).

---

## 📋 Пошаговый план действий

### 1. Перевод на локальный Container Registry
Обнови образ деплоймента `igaming-source-betmotion` на локальный реестр:
```powershell
kubectl set image deployment/igaming-source-betmotion -n igaming-source igaming-source-betmotion=100.78.183.101:30500/igaming-source-bet7k:latest --request-timeout=10s
```
*(Если в локальном реестре образ `igaming-source-bet7k:latest` отсутствует или устарел — собери его на Xeon через Jib, см. Шаг 3).*

### 2. Проверка базы данных
Убедись, что база `igaming-source-betmotion-db-0` готова и `start_time` имеет тип `bigint`:
```powershell
kubectl --request-timeout=10s exec -n igaming-source igaming-source-betmotion-db-0 -- psql -U postgres -d igaming_betmotion -c "\d match_cache"
```
*(Если колонка start_time в match_cache имеет тип timestamp, смени на bigint:*
`ALTER TABLE match_cache ALTER COLUMN start_time TYPE bigint USING (EXTRACT(EPOCH FROM start_time) * 1000)::bigint;`*)*

### 3. Сборка и деплой на Xeon (при необходимости)
Модуль: `igaming-source-bet7k`.
1. Синхронизируй изменения: `scp ... root@100.78.183.101:/root/igaming/...`
2. Собери OCI-образ через Jib на сервере Xeon:
   ```powershell
   ssh -o StrictHostKeyChecking=no root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-bet7k/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-bet7k:latest -Djib.allowInsecureRegistries=true -DskipTests"
   ```
3. Перезапусти деплоймент:
   ```powershell
   kubectl rollout restart deployment/igaming-source-betmotion -n igaming-source --request-timeout=10s
   ```

### 4. Верификация Definition of Done (DoD)
1. Под перешел в `Running 1/1` (0 рестартов).
2. Actuator Health возвращает `{"status":"UP"}`:
   ```powershell
   kubectl exec -n igaming-source $(kubectl get pod -n igaming-source -l app=igaming-source-betmotion -o jsonpath='{.items[0].metadata.name}') -- wget -qO- http://localhost:8080/actuator/health/readiness
   ```
3. В базе `igaming_betmotion` наполняется линия (критерий >= 500 активных матчей):
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-betmotion-db-0 -- psql -U postgres -d igaming_betmotion -c "SELECT count(*) FROM match_cache;"
   ```
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
