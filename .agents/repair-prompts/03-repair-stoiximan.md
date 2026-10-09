# Задача для AI-агента: Ремонт и стабилизация igaming-source-stoiximan

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — восстановить работоспособность и сбор линии букмекера **`igaming-source-stoiximan`** в Kubernetes неймспейсе `igaming-source` и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
Под `igaming-source-stoiximan` находится в цикле `CrashLoopBackOff` / не готов.
В логах зафиксировано:
1. `VpnManagerService : Proxy system properties cleared — using direct connection.`
   **Нарушение Rule 6**: Stoiximan — европейский/оффшорный букмекер (Греция / Kaizen Gaming). При сбросе прокси на прямой домашний IP РФ соединение блокируется или падает по таймауту.
2. `Launching browser with profile: BASIC and proxy: http://100.83.113.50:3128 (engine: chromium)` — Playwright в базовом профиле зависает или не может инициализироваться при перегрузке рендереров.

---

## 📋 Пошаговый план действий

### 1. Проверка конфигурации деплоймента
Проверь env-переменные деплоймента `igaming-source-stoiximan`:
```powershell
kubectl get deployment igaming-source-stoiximan -n igaming-source -o yaml --request-timeout=10s
```
Убедись, что заданы параметры:
- `APP_VPN_FORCE: "true"` (запрет отключения прокси)
- `APP_IS_OFFSHORE: "true"`
- `HTTP_PROXY: "http://100.83.113.50:3128"`
- `HTTPS_PROXY: "http://100.83.113.50:3128"`
- `JAVA_TOOL_OPTIONS`: `-Dhttp.proxyHost=100.83.113.50 -Dhttp.proxyPort=3128 -Dhttps.proxyHost=100.83.113.50 -Dhttps.proxyPort=3128 -Dhttp.nonProxyHosts="localhost|127.*|10.*|172.*|*.cluster.local|*.svc.cluster.local"`

### 2. Проверка кода маппера и сервиса
Модуль сервиса: `igaming-source-888sport` (или выделенный модуль `stoiximan`, если существует).
1. Изучи логи пода для выявления точной причины падения:
   ```powershell
   kubectl logs -n igaming-source -l app=igaming-source-stoiximan --tail=100 --request-timeout=10s
   ```
2. Если в `VpnManagerService` вызывается автоматическое отключение прокси при доступности хоста, убедись, что для оффшорных БК прокси остается активным.
3. Проверь базу данных `igaming-source-stoiximan-db-0`:
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-stoiximan-db-0 -- psql -U postgres -d igaming_stoiximan -c "SELECT count(*) FROM match_cache;"
   ```

### 3. Сборка и деплой на Xeon (при необходимости правки кода)
1. Синхронизируй изменения на Xeon: `scp ... root@100.78.183.101:/root/igaming/...`
2. Собери OCI-образ через Jib на сервере:
   ```powershell
   ssh -o StrictHostKeyChecking=no root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-888sport/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-888sport:latest -Djib.allowInsecureRegistries=true -DskipTests"
   ```
3. Перезапусти деплоймент:
   ```powershell
   kubectl rollout restart deployment/igaming-source-stoiximan -n igaming-source --request-timeout=10s
   ```

### 4. Верификация Definition of Done (DoD)
1. Проверь статус пода: `Running 1/1`, 0 рестартов.
2. Проверь Actuator Health: HTTP 200 `UP`.
3. Убедись в сборе линии: количество матчей в таблице `match_cache` должно расти и достигнуть >= 500.
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
