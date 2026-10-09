# Задача для AI-агента: Ремонт и стабилизация Digitain (crawler & loader)

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — восстановить работоспособность связки краулера и лоадера платформы Digitain:
- `igaming-source-digitain-crawler`
- `igaming-source-digitain-loader`
в Kubernetes неймспейсе `igaming-source` и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
1. `igaming-source-digitain-crawler` находится в статусе `1/2 CrashLoopBackOff` (один из контейнеров падает).
2. `igaming-source-digitain-loader` находится в статусе `1/2 CreateContainerError / Error`.
3. При этом сиблинги на движке Digitain (`totogaming`, `vivarobet`) успешно работают (`1/1 Running`).
4. База данных: `igaming-source-digitain-db-0` (БД: `igaming_digitain`).

---

## 📋 Пошаговый план действий

### 1. Анализ контейнеров и логов
1. Определи, какой именно контейнер падает в краулере и лоадере (app или redis):
   ```powershell
   kubectl get pods -n igaming-source | grep digitain
   kubectl describe pod -n igaming-source -l app=igaming-source-digitain-crawler --request-timeout=10s
   kubectl describe pod -n igaming-source -l app=igaming-source-digitain-loader --request-timeout=10s
   ```
2. Посмотри логи упавших контейнеров:
   ```powershell
   kubectl logs -n igaming-source -l app=igaming-source-digitain-crawler -c igaming-source-digitain-crawler --tail=50 --request-timeout=10s
   kubectl logs -n igaming-source -l app=igaming-source-digitain-loader -c igaming-source-digitain-loader --tail=50 --request-timeout=10s
   ```

### 2. Устранение выявленных дефектов
1. **Проверка портов и Readiness/Startup Probes**:
   Убедись, что порты в манифесте деплоймента (`SERVER_PORT`, probes) совпадают с реальным портом приложения Spring Boot (обычно 8080 или 3043).
2. **Проверка базы данных**:
   Убедись, что колонка `start_time` в `igaming_digitain` имеет тип `bigint`:
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-digitain-db-0 -- psql -U postgres -d igaming_digitain -c "\d match_cache"
   ```
3. **Сверка с работающим кодом сиблинга (`totogaming` / `vivarobet`)**:
   Сравни манифесты и переменные окружения с работающим `igaming-source-totogaming`:
   ```powershell
   kubectl get deployment igaming-source-totogaming -n igaming-source -o yaml --request-timeout=10s
   ```

### 3. Сборка и деплой на Xeon (при необходимости)
Модуль сервиса: `igaming-source-digitain`.
1. Синхронизируй изменения: `scp ... root@100.78.183.101:/root/igaming/...`
2. Собери OCI-образ через Jib на Xeon:
   ```powershell
   ssh -o StrictHostKeyChecking=no root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-digitain/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-digitain:latest -Djib.allowInsecureRegistries=true -DskipTests"
   ```
3. Перезапусти деплойменты:
   ```powershell
   kubectl rollout restart deployment/igaming-source-digitain-crawler deployment/igaming-source-digitain-loader -n igaming-source --request-timeout=10s
   ```

### 4. Верификация Definition of Done (DoD)
1. Оба пода (`crawler` и `loader`) перешли в статус `Running 2/2` (0 рестартов).
2. Actuator Health возвращает `{"status":"UP"}`.
3. В базе `igaming_digitain` наполняется линия (критерий >= 500 матчей):
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-digitain-db-0 -- psql -U postgres -d igaming_digitain -c "SELECT count(*) FROM match_cache;"
   ```
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
