# Задача для AI-агента: Ремонт и стабилизация igaming-source-pinnacle

Ты — автономный AI-инженер проекта SmartBet.guru (igaming backend).
Твоя цель — восстановить работоспособность сервиса **`igaming-source-pinnacle`** (лоадер Pinnacle), собрать свежий образ, задеплоить в Kubernetes и подтвердить выполнение Definition of Done (DoD).

---

## 🎯 Проблема и контекст
Под `igaming-source-pinnacle-loader` в неймспейсе `igaming-source` падает при старте Spring Context:
```
org.springframework.beans.factory.UnsatisfiedDependencyException: 
Error creating bean with name 'genericMatchLoadScheduler': 
Parameter 0 of constructor in GenericMatchLoadScheduler required a bean of type 
'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' that could not be found.
```
**Причина**: В `igaming-source-core` компонент `GenericMatchLoadScheduler` жестко требовал бин `AbstractBaseBookmakerService`. У Pinnacle своя архитектура шедулера (`MatchFetchScheduler` / `MatchService`), поэтому базовый сервис не регистрируется.
Локально в файле `c:\Users\chernousov_a\IdeaProjects\igaming\igaming-source-core\src\main\java\pro\datawiki\igaming\source\core\scheduler\GenericMatchLoadScheduler.java` зависимость уже переведена на опциональный `ObjectProvider<AbstractBaseBookmakerService>`.

---

## 📋 Пошаговый план действий

### 1. Синхронизация исправлений на сервер Xeon
Синхронизируй исправленный файл `GenericMatchLoadScheduler.java` на хост `xeon-srv` (100.78.183.101):
```powershell
scp -o StrictHostKeyChecking=no c:\Users\chernousov_a\IdeaProjects\igaming\igaming-source-core\src\main\java\pro\datawiki\igaming\source\core\scheduler\GenericMatchLoadScheduler.java root@100.78.183.101:/root/igaming/igaming-source-core/src/main/java/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.java
```

### 2. Сборка core и модуля Pinnacle на Xeon (Jib)
Запусти установку `igaming-source-core` в локальный репозиторий Maven и сборку OCI-образа Pinnacle:
```powershell
ssh -o StrictHostKeyChecking=no root@100.78.183.101 "/usr/local/bin/mvn -f igaming-source-core/pom.xml clean install -DskipTests && /usr/local/bin/mvn -f igaming-source-pinnacle/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build -Djib.from.image=gcr.io/distroless/java21-debian12 -Djib.to.image=100.78.183.101:30500/igaming-source-pinnacle:latest -Djib.allowInsecureRegistries=true -DskipTests"
```
*Убедись, что сборка завершилась статусом `BUILD SUCCESS`.*

### 3. Перезапуск пода в Kubernetes
```powershell
kubectl rollout restart deployment/igaming-source-pinnacle-loader -n igaming-source --request-timeout=10s
```

### 4. Верификация Definition of Done (DoD)
1. Проверь статус пода:
   ```powershell
   kubectl get pods -n igaming-source -l app=igaming-source-pinnacle-loader --request-timeout=10s
   ```
   *Ожидается:* `Running 2/2` (контейнеры `igaming-source-pinnacle-loader` и `redis`), 0 рестартов.
2. Проверь Actuator Health:
   ```powershell
   kubectl exec -n igaming-source -c igaming-source-pinnacle-loader $(kubectl get pod -n igaming-source -l app=igaming-source-pinnacle-loader -o jsonpath='{.items[0].metadata.name}') -- wget -qO- http://localhost:3040/actuator/health/readiness
   ```
   *Ожидается:* `{"status":"UP"}`.
3. Проверь наполнение БД Pinnacle:
   ```powershell
   kubectl --request-timeout=10s exec -n igaming-source igaming-source-pinnacle-db-0 -- psql -U postgres -d igaming_pinnacle -c "SELECT count(*) FROM match_cache;"
   ```
4. **Выдержи 5-минутный чистый интервал работы пода без единой ошибки.**
5. Только после зеленого DoD закоммить и запушь изменения в `master`.
