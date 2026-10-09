# Задача для AI-агента: Ремонт и стабилизация igaming-source-svenskaspel

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — устранить сбой подключения к БД сервиса **`igaming-source-svenskaspel`** в Kubernetes неймспейсе `igaming-source` и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
Под `igaming-source-svenskaspel` находится в статусе `0/1 Running` (падают пробы готовности).
В логах зафиксирована критическая ошибка:
```
Caused by: java.net.ConnectException: Connection refused
    at java.base/java.net.SocksSocketImpl.connect(Unknown Source)
    at org.postgresql.core.PGStream.createSocket(PGStream.java:260)
    at org.postgresql.core.v3.ConnectionFactoryImpl.tryConnect(ConnectionFactoryImpl.java:140)
```
**Причина**: Java-процесс пытается подключиться к внутренней PostgreSQL БД (`igaming-source-svenskaspel-db.igaming-source.svc.cluster.local`) через **SOCKS-прокси** (`SocksSocketImpl`), который не может маршутизировать локальный K8s DNS трафик.
Сервис использует образ Unibet (`igaming-source-unibet`).

---

## 📋 Пошаговый план действий

### 1. Проверка сетевых параметров деплоймента
Проверь манифест деплоймента `igaming-source-svenskaspel`:
```powershell
kubectl get deployment igaming-source-svenskaspel -n igaming-source -o yaml --request-timeout=10s
```
Обрати внимание на переменные:
- `JAVA_TOOL_OPTIONS`: если там указан `-DsocksProxyHost` или `-Dhttp.proxyHost`, проверь наличие `-Dhttp.nonProxyHosts` и `-DsocksNonProxyHosts`:
  ```
  -Dhttp.nonProxyHosts="localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local" -DsocksNonProxyHosts="localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local"
  ```
- Либо отключи SOCKS-прокси для JVM, оставив только HTTP/HTTPS проксирование (`-Dhttp.proxyHost=100.83.113.50 -Dhttp.proxyPort=3128`).

### 2. Проверка кода VpnManagerService в igaming-source-unibet
1. Посмотри в коде `igaming-source-unibet` класс `VpnManagerService.java` или конфигурацию DataSource:
   Если там программно вызывается `System.setProperty("socksProxyHost", ...)`, убедись, что JDBC URL исключен из проксирования или настраивается `socksNonProxyHosts`.
2. Проверь базу данных `igaming-source-svenskaspel-db-0`:
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-svenskaspel-db-0 -- psql -U postgres -d igaming_svenskaspel -c "SELECT count(*) FROM match_cache;"
   ```

### 3. Сборка и деплой на Xeon
Модуль: `igaming-source-unibet`.
1. Синхронизируй изменения: `scp ... root@100.78.183.101:/root/igaming/...`
2. Собери образ на сервере Xeon:
   ```powershell
   ssh -o StrictHostKeyChecking=no root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-unibet/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-unibet:latest -Djib.allowInsecureRegistries=true -DskipTests"
   ```
3. Примени обновленный образ и перезапусти под:
   ```powershell
   kubectl set image deployment/igaming-source-svenskaspel -n igaming-source igaming-source-svenskaspel=100.78.183.101:30500/igaming-source-unibet:latest --request-timeout=10s
   kubectl rollout restart deployment/igaming-source-svenskaspel -n igaming-source --request-timeout=10s
   ```

### 4. Верификация Definition of Done (DoD)
1. Под перешел в `Running 1/1` (0 рестартов).
2. Actuator Health возвращает `{"status":"UP"}`.
3. Линия наполняется матчами (критерий >= 500):
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-svenskaspel-db-0 -- psql -U postgres -d igaming_svenskaspel -c "SELECT count(*) FROM match_cache;"
   ```
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
