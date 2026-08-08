# 📋 [IGAMING-SRC-BETB2B] Fail-Fast для BetB2B Family (1xbet, 22bet, melbet, etc.)

## 📌 Метаданные задачи (Plane)
- **ID**: `IGAMING-SRC-BETB2B`
- **Эпик**: `[EPIC-CRAWLERS]`
- **Модуль**: `igaming-source-betb2b`
- **Манифесты K8s**: `1xbet.yaml`, `1xbit.yaml`, `22bet.yaml`, `melbet.ru.yaml`, `melbet-com.yaml`, `megapari.yaml`, `betandyou.yaml`, `fansport.yaml`, `linebet.yaml`, `spinbetter.yaml`, `betlabel.yaml`
- **Приоритет**: `Urgent`
- **Теги**: `betb2b`, `1xbet`, `22bet`, `kafka-timeout`, `406-error`, `k8s-probes`, `robot-task`
- **Статус**: `Backlog`

---

## 🎯 Проблема и симптомы
1. В логах краулера:
   - `406 NotAcceptable: Error occurred during request execution`
   - `No route to host` / `Connect timed out`
2. В логах лоадера:
   - `org.apache.kafka.common.errors.TimeoutException: Expiring 18 record(s) for odds.updates-7: 120000 ms has passed since batch creation`
3. Поды продолжают висеть в `2/2 Running` без рестартов, засоряя Kafka продюсер просроченными батчами.

---

## 🤖 Пошаговая инструкция для робота

### Шаг 1: Обработка 406 и Network Timeout в `AbstractBetB2BFamilyApiClient`
1. При получении JSON с `"status":406` или `type: "feed/NotAcceptableException"`:
   - Регистрировать сбой:
     ```java
     emergencyIncidentTracker.recordFailure(
         EmergencySubsystem.PROXY_NETWORK, 
         "BetB2B API returned 406 NotAcceptable / Host Unreachable", 
         null
     );
     ```
2. При сбое отправки в Kafka (`LoggingProducerListener` / `TimeoutException`):
   - Регистрировать `EmergencySubsystem.KAFKA`.

### Шаг 2: Внедрение K8s Probes во все 11 YAML-манифестов
Добавить в деплойменты краулера и лоадера:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 3033
  initialDelaySeconds: 45
  periodSeconds: 15
  failureThreshold: 3
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 3033
  initialDelaySeconds: 20
  periodSeconds: 10
  failureThreshold: 2
```

### Шаг 3: Верификация
```bash
mvn -pl igaming-source-betb2b test-compile
```

---

## ✅ Критерии приемки
- Модуль `igaming-source-betb2b` собирается без ошибок.
- При истечении 120с Kafka таймаута или 406 ошибках pod деградирует liveness и падает в K8s CrashLoopBackOff.
- В 11 YAML-файлах добавлены `livenessProbe` и `readinessProbe`.
