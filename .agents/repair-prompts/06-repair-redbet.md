# Задача для AI-агента: Ремонт и стабилизация igaming-source-redbet

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — восстановить работоспособность сервиса **`igaming-source-redbet`** в Kubernetes неймспейсе `igaming-source` и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
Под `igaming-source-redbet` падает или находится в `0/1 Running` (readiness probe connection refused).
Как и у родственного сервиса Svenskaspel, процесс использует образ `igaming-source-unibet`, в котором соединение к локальной PostgreSQL базе перехватывается SOCKS-прокси:
```
Caused by: java.net.ConnectException: Connection refused
    at java.base/java.net.SocksSocketImpl.connect(Unknown Source)
    at org.postgresql.core.PGStream.createSocket(PGStream.java:260)
```
База данных сервиса: `igaming-source-redbet-db-0` (БД: `igaming_redbet`).

---

## 📋 Пошаговый план действий

### 1. Проверка сетевых параметров деплоймента
Проверь манифест деплоймента `igaming-source-redbet`:
```powershell
kubectl get deployment igaming-source-redbet -n igaming-source -o yaml --request-timeout=10s
```
Проверь и исправь переменные прокси:
- В `JAVA_TOOL_OPTIONS` обязательно должны быть исключены внутренние K8s хосты:
  `-Dhttp.nonProxyHosts="localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local"`
  `-DsocksNonProxyHosts="localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local"`
- Проверь `NO_PROXY`: `localhost,127.0.0.1,.svc,.cluster.local,igaming-source-redbet-db`.

### 2. Проверка базы данных
Убедись, что база `igaming-source-redbet-db-0` доступна и готова принимать подключения:
```powershell
kubectl --request-timeout=10s exec -n igaming-source igaming-source-redbet-db-0 -- psql -U postgres -d igaming_redbet -c "SELECT count(*) FROM match_cache;"
```
*(Если колонка start_time в match_cache имеет тип timestamp, смени на bigint:*
`ALTER TABLE match_cache ALTER COLUMN start_time TYPE bigint USING (EXTRACT(EPOCH FROM start_time) * 1000)::bigint;`*)*

### 3. Сборка и деплой на Xeon (при необходимости)
Если уже собран исправленный образ `100.78.183.101:30500/igaming-source-unibet:latest`:
```powershell
kubectl set image deployment/igaming-source-redbet -n igaming-source igaming-source-redbet=100.78.183.101:30500/igaming-source-unibet:latest --request-timeout=10s
kubectl rollout restart deployment/igaming-source-redbet -n igaming-source --request-timeout=10s
```
Если требуются правки кода в модуле `igaming-source-unibet`:
1. Синхронизируй файлы: `scp ... root@100.78.183.101:/root/igaming/...`
2. Собери образ на Xeon:
   ```powershell
   ssh -o StrictHostKeyChecking=no root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-unibet/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-unibet:latest -Djib.allowInsecureRegistries=true -DskipTests"
   ```
3. Выполни rollout restart.

### 4. Верификация Definition of Done (DoD)
1. Под перешел в `Running 1/1` (0 рестартов).
2. Actuator Health возвращает `{"status":"UP"}`.
3. Линия наполняется матчами (критерий >= 500 активных событий):
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-redbet-db-0 -- psql -U postgres -d igaming_redbet -c "SELECT count(*) FROM match_cache;"
   ```
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
