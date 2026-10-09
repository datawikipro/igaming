# Задача для AI-агента: Ремонт и стабилизация igaming-source-betfair

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — восстановить работоспособность сервиса **`igaming-source-betfair`** в Kubernetes неймспейсе `igaming-source` и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
Под `igaming-source-betfair` застрял в статусе `Init:CreateContainerError`:
```
Warning  Failed  kubelet  Error: failed to reserve container name "wait-for-db_igaming-source-betfair-...": name is reserved for "..."
```
**Причина**: В containerd на хосте `xeon-srv` произошел lockup пайпов при создании init-контейнера `wait-for-db` из-за высокой нагрузки. Ранее в базе данных `igaming_betfair` колонка `start_time` была мигрирована в `bigint`.

---

## 📋 Пошаговый план действий

### 1. Разблокировка containerd и пересоздание пода
1. Удали зависший pod с флагом force:
   ```powershell
   kubectl delete pod -n igaming-source -l app=igaming-source-betfair --force --grace-period=0 --request-timeout=10s
   ```
2. Разблокируй пайпы containerd на ноде `xeon-srv`:
   ```powershell
   ssh -o StrictHostKeyChecking=no root@100.78.183.101 "python3 -c 'import os, stat; [os.close(os.open(os.path.join(r, f), os.O_NONBLOCK | os.O_RDWR)) for r, d, fs in os.walk(\"/run/containerd\") for f in fs if stat.S_ISFIFO(os.stat(os.path.join(r, f)).st_mode)]'"
   ```

### 2. Проверка состояния базы данных
Убедись, что база `igaming-source-betfair-db-0` работает и колонка `start_time` имеет тип `bigint`:
```powershell
kubectl --request-timeout=10s exec -n igaming-source igaming-source-betfair-db-0 -- psql -U postgres -d igaming_betfair -c "\d match_cache"
```
*(Если колонка start_time осталась timestamp without time zone, выполни:*
`ALTER TABLE match_cache ALTER COLUMN start_time TYPE bigint USING (EXTRACT(EPOCH FROM start_time) * 1000)::bigint;`*)*

### 3. Анализ логов запуска Java
После старта нового пода проверь логи основного контейнера:
```powershell
kubectl logs -n igaming-source -l app=igaming-source-betfair -c igaming-source-betfair --tail=100 -f --request-timeout=10s
```
- Если Java успешно запустилась — переходи к шагу 4.
- Если возникла ошибка сборки/маппера:
  - Внеси исправления в `igaming-source-betfair` в локальном репозитории.
  - Синхронизируй файл на Xeon: `scp ... root@100.78.183.101:/root/igaming/...`
  - Пересобери OCI-образ на Xeon:
    `ssh root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-betfair/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-betfair:latest -Djib.allowInsecureRegistries=true -DskipTests"`
  - Обнови деплоймент:
    `kubectl set image deployment/igaming-source-betfair -n igaming-source igaming-source-betfair=100.78.183.101:30500/igaming-source-betfair:latest`

### 4. Верификация Definition of Done (DoD)
1. Проверь статус пода: `kubectl get pods -n igaming-source -l app=igaming-source-betfair` (должен быть `Running 1/1`, 0 рестартов).
2. Проверь Actuator:
   ```powershell
   kubectl exec -n igaming-source $(kubectl get pod -n igaming-source -l app=igaming-source-betfair -o jsonpath='{.items[0].metadata.name}') -- wget -qO- http://localhost:8080/actuator/health/readiness
   ```
3. Проверь наполнение БД (критерий >= 500 матчей):
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-betfair-db-0 -- psql -U postgres -d igaming_betfair -c "SELECT count(*) FROM match_cache;"
   ```
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
